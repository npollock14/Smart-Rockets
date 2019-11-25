import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SmartRocketsMain extends JPanel
		implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;

	int screenWidth = 1000;
	int screenHeight = 1000;
	boolean[] keys = new boolean[300];
	boolean[] keysToggled = new boolean[300];
	boolean[] mouse = new boolean[200];
	int frame = 0;
	Rocket r = new Rocket(new Point(300, 300), new Point(0, 0), new NeuralNetwork(4, 4, 4));
	Rect obs = new Rect(600, 100, 200, 300);
	ArrayList<Rect> obstacles = new ArrayList<Rect>();
	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.drawString("" + frame, 20, 20);
		g.setColor(new Color(30, 30, 30));
		g.fillRect(0, 0, screenWidth, screenHeight);
		g.setColor(Color.red);
		for (Rect r : obstacles) {
			r.draw(g);
		}

		r.show(g);
	}

	public void update() throws InterruptedException {
		r.update(obstacles);
		frame++;
	}

	private void init() {
		obstacles.add(obs);
	}

	// ==================code above ===========================

	@Override
	public void actionPerformed(ActionEvent arg0) {

		try {
			update();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repaint();
	}

	public static void main(String[] arg) {
		@SuppressWarnings("unused")
		SmartRocketsMain d = new SmartRocketsMain();
	}

	public SmartRocketsMain() {
		JFrame f = new JFrame();
		f.setTitle("Smart Rockets");
		f.setSize(screenWidth, screenHeight);
		f.setBackground(Color.BLACK);
		f.setResizable(false);
		f.addKeyListener(this);
		f.addMouseMotionListener(this);
		f.addMouseWheelListener(this);
		f.addMouseListener(this);

		f.add(this);

		init();

		t = new Timer(15, this);
		t.start();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

	Timer t;

	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;

	}

	@Override
	public void keyReleased(KeyEvent e) {

		keys[e.getKeyCode()] = false;

		if (keysToggled[e.getKeyCode()]) {
			keysToggled[e.getKeyCode()] = false;
		} else {
			keysToggled[e.getKeyCode()] = true;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouse[e.getButton()] = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

}

class Sensor {
	Point pos,close;
	double thetaOff;
	double theta = 0.0;
	double len;
	double data;

	public Sensor(Point pos, double thetaOff, double len) {
		super();
		this.pos = pos;
		this.len = len;
		this.thetaOff = thetaOff;
		this.data = len;
	}

	public void draw(Graphics g) {
		g.setColor(Color.yellow);
		g.drawLine((int) pos.x, (int) pos.y, (int) (pos.x + len * Math.cos(theta)),
				(int) (pos.y + len * -Math.sin(theta)));
		close.fillCircle(10, g);
	}

	public void update(Point pos, double theta, ArrayList<Rect> obstacles) {
		this.pos = pos;
		this.theta = theta + thetaOff;
		double m = Math.tan(theta);
		close = new Point((pos.x + len * Math.cos(theta)),(pos.y + len * -Math.sin(theta)));
		for(Rect r : obstacles) {
		//y-y0 = m(x-x0)
			double yInt = m*(r.pos.x - pos.x) + pos.y;
			if(yInt > r.pos.y && yInt < r.pos.y + r.h) {
				if(pos.distanceTo(new Point(r.pos.x,yInt)) < pos.distanceTo(close)) {
					close = new Point(r.pos.x,yInt);
				}
			}
			yInt = m*(r.pos.x + r.w - pos.x) + pos.y;
			if(yInt > r.pos.y && yInt < r.pos.y + r.h) {
				if(pos.distanceTo(new Point(r.pos.x + r.w,yInt)) < pos.distanceTo(close)) {
					close = new Point(r.pos.x + r.w,yInt);
				}
			}
			//((y-y0)/m) + x0 = x
			//double xInt = 
			
			
		}
		
	}

}
class Line{
	double m;
	Point pos;
	public Line(double m, Point pos) {
		super();
		this.m = m;
		this.pos = pos;
	}
	public Point getIntersection(Line l2) {
		double x = (m*pos.x - pos.y + l2.pos.y - l2.m*l2.pos.x)/(m - l2.m);
		double y = m*(x - pos.x) + pos.y;
		
		return new Point(x,y);
	}
	
}

class Rocket {
	Point pos, target;
	Sensor[] sensors = new Sensor[8];
	double w = 50.0, h = 200.0;
	Vec2 vel = new Vec2(0, 0);
	double alpha = -0.0001;
	double omega = Math.toRadians(1.0);
	double theta = Math.toRadians(0);
	double a = 0.01;
	double friction = 0.00;
	double maxAlpha = 0.001;
	double maxA = 0.05;
	int lifeSpan = 150;
	int age = 0;
	boolean dead;
	double fitness = 0;
	boolean achieved;
	NeuralNetwork brain;
	Point ul, ur, bl, br;
	double d = Math.sqrt(Math.pow(w / 2, 2) + Math.pow(h / 2, 2));

	public Rocket(Point pos, Point target, NeuralNetwork brain) {
		super();
		this.pos = pos;
		this.target = target;
		this.brain = brain;
		for (int i = 0; i < sensors.length; i++) {
			sensors[i] = new Sensor(pos, i * Math.toRadians(360 / sensors.length), 500);
		}
		updateCorners();

	}

	public void show(Graphics g) {

		g.setColor(Color.white);
		g.fillOval((int) pos.x - 5, (int) pos.y - 5, 10, 10);

		for (Sensor s : sensors) {
			s.draw(g);
		}

		g.setColor(Color.white);
		ul.drawLine(ur, g);
		ul.drawLine(bl, g);
		ur.drawLine(br, g);
		bl.drawLine(br, g);

		// g.drawString(Math.toDegrees(theta) + " deg", (int) (pos.x + w), (int) (pos.y
		// - h));

	}

	public void updateCorners() {
		double va = 2 * Math.atan2(w / 2, h / 2);
		double ha = (Math.PI * 2 - va * 2) / 2;

		ul = new Point(pos.x + d * Math.cos(theta - ha - Math.toRadians(45) + va),
				pos.y - d * Math.sin(theta - ha - Math.toRadians(45) + va));
		ur = new Point(pos.x + d * Math.cos(theta - ha - Math.toRadians(45) + va + ha),
				pos.y - d * Math.sin(theta - ha - Math.toRadians(45) + va + ha));
		br = new Point(pos.x + d * Math.cos(theta - ha - Math.toRadians(45) + va + ha + va),
				pos.y - d * Math.sin(theta - ha - Math.toRadians(45) + va + ha + va));
		bl = new Point(pos.x + d * Math.cos(va + ha + va + ha + theta - ha - Math.toRadians(45)),
				pos.y - d * Math.sin(ha + va + ha + va + theta - ha - Math.toRadians(45)));
	}

	public void update(ArrayList<Rect> obstacles) {
		omega += alpha;
		theta += omega;

		vel.x += a * Math.cos(theta);
		vel.y -= a * Math.sin(theta);

		omega *= 1 - friction;
		vel.x *= 1 - friction;
		vel.y *= 1 - friction;

		pos.add(vel);

		for (Sensor s : sensors) {
			s.update(pos, theta, obstacles);
		}
		updateCorners();
	}
}

class Point {
	double x, y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void fillCircle(int d, Graphics g) {
		g.fillOval((int) (x - d / 2), (int) (y - d / 2), d, d);
		
	}

	public double distanceTo(Point p2) {
		return Math.sqrt((this.x - p2.x) * (this.x - p2.x) + (this.y - p2.y) * (this.y - p2.y));
	}

	public void drawLine(Point p2, Graphics g) {
		g.drawLine((int) x, (int) y, (int) p2.x, (int) p2.y);
	}

	public void drawCircle(int d, Graphics g) {
		g.drawOval((int) (x - d / 2), (int) (y - d / 2), d, d);
	}

	public double angleTo(Point p2) {
		try {
			return Math.atan2(this.y - p2.y, this.x - p2.x);
		} catch (Exception e) {

		}
		return 0;
	}

	public boolean inside(Rect r) {
		return (x > r.pos.x && x < r.pos.x + r.w && y > r.pos.y && y < r.pos.y + r.h);
	}

	public void add(Vec2 v) {
		x += v.x;
		y += v.y;
	}
}

class Vec2 {
	double x, y;

	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2() {
		this.x = Math.random() * 2 - 1;
		this.y = Math.random() * 2 - 1;
		double mag = this.getMagnitude();
		this.x /= mag;
		this.y /= mag;

	}

	public double getMagnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public double getAngle() {
		try {
			return Math.atan2(this.y, this.x);
		} catch (Exception e) {

		}
		return 0;
	}

}

class Rect {
	Point pos;
	double h, w;

	public Rect(double x, double y, double w, double h) {

		this.pos = new Point(x, y);
		this.h = h;
		this.w = w;

	}

	public void draw(Graphics g) {
		g.drawRect((int) pos.x, (int) pos.y, (int) w, (int) h);
	}

	public boolean intersects(Rect r) {
		return (pos.inside(r) || new Point(pos.x + w, pos.y).inside(r) || new Point(pos.x + w, pos.y + h).inside(r)
				|| new Point(pos.x, pos.y + h).inside(r));
	}
}
