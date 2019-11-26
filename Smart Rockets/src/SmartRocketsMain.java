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

	static int generation = 0;
	static int screenWidth = 1200;
	static int screenHeight = 650;
	boolean[] keys = new boolean[300];
	boolean[] keysToggled = new boolean[300];
	boolean[] mouse = new boolean[200];
	boolean allDead;
	int numRockets = 100;
	int frame = 0;
	int skip = 0;
	Point currentTarget;
	static int targetDiam = 50;
	// Rect obs = new Rect(600, 100, 200, 300);
	ArrayList<Rect> obstacles = new ArrayList<Rect>();
	ArrayList<Rocket> rockets = new ArrayList<Rocket>();
	ArrayList<Rocket> genePool = new ArrayList<Rocket>();
	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(30, 30, 30));
		g.fillRect(0, 0, screenWidth, screenHeight);
		g.setColor(Color.WHITE);
		g.drawString("" + frame, 20, 20);
		g.drawString("Generation: " + generation, 20, 60);

		g.setColor(Color.red);
		for (Rect r : obstacles) {
			r.draw(g);
		}
		for (Rocket r : rockets) {
			r.show(g);
		}
		g.setColor(Color.green);
		g.fillOval((int) (currentTarget.x - targetDiam / 2), (int) (currentTarget.y - targetDiam / 2), targetDiam,
				targetDiam);

	}

	public void update() throws InterruptedException {
		skip = keys[32] || keysToggled[88] ? Rocket.maxAge : 1;
		for (int f = 0; f < skip; f++) {
			allDead = true;
			for (Rocket r : rockets) {
				r.update(obstacles);
				if (!r.dead) {
					allDead = false;
				}
			}
			if (allDead) {
				double highFitness = rockets.get(0).fitness;
				for (Rocket r : rockets) {
					if (r.fitness > highFitness) {
						highFitness = r.fitness;
					}
				}
				System.out.println(highFitness);
				for (Rocket r : rockets) {
					r.fitness /= highFitness;
				}
				for (Rocket r : rockets) {
					for (int i = 0; i < 10*Math.pow(r.fitness, 5); i++) {
						genePool.add(r);
					}
				}
				rockets.clear();
				currentTarget = new Point(Math.random() * screenWidth, Math.random() * screenHeight);
				for (int i = 0; i < numRockets; i++) {
					NeuralNetwork newBrain = new NeuralNetwork(
							genePool.get((int) (Math.random() * genePool.size())).brain);
					newBrain.mutate(.1f);
					rockets.add(new Rocket(new Point(screenWidth/2,screenHeight/2), currentTarget, newBrain));
				}
				genePool.clear();
				generation++;

			}

			frame++;
		}
	}

	private void init() {
		currentTarget = new Point(Math.random() * screenWidth, Math.random() * screenHeight);
		for (int i = 0; i < numRockets; i++) {
			rockets.add(new Rocket(new Point(500, 500), currentTarget));
		}
		// obstacles.add(obs);
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

		t = new Timer(10, this);
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
	Point pos, close;
	double thetaOff;
	double theta = 0.0;
	double len;
	float data;

	public Sensor(Point pos, double thetaOff, double len) {
		super();
		this.pos = pos;
		this.len = len;
		this.thetaOff = thetaOff;
		this.data = (float) len;
		close = new Point((pos.x + len * Math.cos(theta)), (pos.y + len * -Math.sin(theta)));
	}

	public void draw(Graphics g) {
		g.setColor(Color.yellow);
		g.drawLine((int) pos.x, (int) pos.y, (int) (pos.x + len * Math.cos(theta)),
				(int) (pos.y + len * -Math.sin(theta)));
		close.fillCircle(10, g);
		g.drawString(data + "", (int) close.x, (int) close.y);
	}

	public void update(Point pos, double theta, ArrayList<Rect> obstacles) {
		this.pos = pos;
		this.theta = theta + thetaOff;
		double m = -Math.tan(this.theta);
		close = new Point((pos.x + len * Math.cos(this.theta)), (pos.y + len * -Math.sin(this.theta)));
		for (Rect r : obstacles) {
			// y-y0 = m(x-x0)
			double yInt = m * (r.pos.x - pos.x) + pos.y;
			if (yInt > r.pos.y && yInt < r.pos.y + r.h) {
				if (pos.distanceTo(new Point(r.pos.x, yInt)) < pos.distanceTo(close)) {
					close = new Point(r.pos.x, yInt);
				}
			}
			yInt = m * (r.pos.x + r.w - pos.x) + pos.y;
			if (yInt > r.pos.y && yInt < r.pos.y + r.h) {
				if (pos.distanceTo(new Point(r.pos.x + r.w, yInt)) < pos.distanceTo(close)) {
					close = new Point(r.pos.x + r.w, yInt);
				}
			}
			// ((y-y0)/m) + x0 = x
			double xInt = ((r.pos.y - pos.y) / m) + pos.x;
			if (xInt > r.pos.x && xInt < r.pos.x + r.w) {
				if (pos.distanceTo(new Point(xInt, r.pos.y)) < pos.distanceTo(close)) {
					close = new Point(xInt, r.pos.y);
				}
			}
			xInt = ((r.pos.y + r.h - pos.y) / m) + pos.x;
			if (xInt > r.pos.x && xInt < r.pos.x + r.w) {
				if (pos.distanceTo(new Point(xInt, r.pos.y + r.h)) < pos.distanceTo(close)) {
					close = new Point(xInt, r.pos.y + r.h);
				}
			}

		}
		data = (float) (pos.distanceTo(close) / len);

	}

}

class Line {
	double m;
	Point pos;

	public Line(double m, Point pos) {
		super();
		this.m = m;
		this.pos = pos;
	}

	public Point getIntersection(Line l2) {
		double x = (m * pos.x - pos.y + l2.pos.y - l2.m * l2.pos.x) / (m - l2.m);
		double y = m * (x - pos.x) + pos.y;

		return new Point(x, y);
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
