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
	Population p = new Population(200, new Point(500, 50));
	int frame = 0;

	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		p.show(g);
		g.drawString("" + frame, 20, 20);
	}

	public void update() throws InterruptedException {
		p.update();
		frame++;
	}

	private void init() {
		p.obstacles.add(new Rect(300,400, 400, 50));
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

class Population {
	int generation = 0;
	Rocket[] rockets;
	ArrayList<Rocket> breedingPool = new ArrayList<Rocket>();
	ArrayList<Rect> obstacles = new ArrayList<Rect>();
	int age = 0;
	int lifeSpan;
	Point target;
	int targetDiameter = 50;

	public Population(int size, Point target) {
		this.target = target;
		rockets = new Rocket[size];
		for (int i = 0; i < rockets.length; i++) {
			rockets[i] = new Rocket(new Point(500, 950), target);
		}
		lifeSpan = rockets[0].lifeSpan;
	}

	public void update() {
		if (age < lifeSpan) {
			for (Rocket r : rockets) {
				r.update();
				for(Rect b : obstacles) {
					if(r.pos.inside(b)) r.dead = true;
				}
			}
		} else {
			evaluate();
			selection();
//			for (int i = 0; i < rockets.length; i++) {
//				rockets[i] = new Rocket(new Point(500, 950), target);
//			}
			age = 0;
		}
		age++;
	}

	public void show(Graphics g) {
		for (Rocket r : rockets) {
			r.show(g);
		}
		g.fillOval((int)(target.x - targetDiameter/2), (int) (target.y - targetDiameter/2), targetDiameter, targetDiameter);
		for(Rect r : obstacles) {
			g.drawRect((int)r.pos.x, (int)r.pos.y, (int)r.w, (int)r.h);
		}
	
	}

	private void evaluate() {
		double sum = 0;
		for (Rocket r : rockets) {
			
			r.fitness = 1 / Math.pow(r.pos.distanceTo(target),1);
			sum += r.fitness;
			if(r.pos.distanceTo(target) < 50) r.fitness = 1;

		}
		System.out.println(sum/200);
		double maxFit = rockets[0].fitness;
		for (Rocket r : rockets) {
			r.fitness /= maxFit;
		}
	}

	private void selection() {
		for (Rocket r : rockets) {
			for (int i = 0; i < r.fitness * 10 * (r.achieved ? 100 : 1) * (r.dead ? 0.1 : 1); i++) { ///(r.age/r.lifeSpan) : 1)
				breedingPool.add(r);
			}
		}
		Rocket[] newRs = new Rocket[rockets.length];
		for (int i = 0; i < rockets.length; i++) {
			Genes a = breedingPool.get((int) (Math.random() * breedingPool.size())).g;
			Genes b = breedingPool.get((int) (Math.random() * breedingPool.size())).g;
			newRs[i] = new Rocket(new Point(500,950), target);
			newRs[i].g = a.cross(b);
		}
		this.rockets = newRs;
	}
}

class Rocket {
	Point pos;
	Genes g;
	int w = 10, h = 10;
	Vec2 vel = new Vec2(0, 0);
	int lifeSpan = 150;
	int age = 0;
	boolean dead;
	Point target;
	double fitness = 0;
	boolean achieved;

	public Rocket(Point pos, Point target) {
		super();
		this.pos = pos;
		this.target = target;
		this.g = new Genes(lifeSpan);
	}

	public void show(Graphics g) {
		g.drawOval((int) (pos.x + w / 2), (int) (pos.y + h / 2), w, h);
	}

	public void update() {
		if (age < lifeSpan && !dead && !achieved) {
			vel.x += g.dna[age].x;
			vel.y += g.dna[age].y;
			pos.x += vel.x;
			pos.y += vel.y;
			age++;
		} else {
			dead = true;
		}
		if(pos.distanceTo(target) < 50) {
			achieved = true;
		}
		if(pos.x > 1000 || pos.x < 0 || pos.y > 1000 || pos.y > 1000) {
			dead = true;
		}
	}
}

class Genes {
	int len;
	Vec2[] dna;

	public Genes(int len) {
		this.len = len;
		this.dna = new Vec2[len];
		for (int i = 0; i < len; i++) {
			dna[i] = new Vec2();
		}
	}

	public Genes cross(Genes b) {
		Genes res = new Genes(len);
		int partition = (int) (Math.random() * len);
		for (int i = 0; i < len; i++) {
			res.dna[i] = i > partition ? this.dna[i] : b.dna[i];
			if(Math.random() * 101 > 99) res.dna[i] = new Vec2();
		}

		return res;
	}
}

class Point {
	double x, y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distanceTo(Point p2) {
		return Math.sqrt((this.x - p2.x) * (this.x - p2.x) + (this.y - p2.y) * (this.y - p2.y));
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

	public boolean intersects(Rect r) {
		return (pos.inside(r) || new Point(pos.x + w, pos.y).inside(r) || new Point(pos.x + w, pos.y + h).inside(r)
				|| new Point(pos.x, pos.y + h).inside(r));
	}
}