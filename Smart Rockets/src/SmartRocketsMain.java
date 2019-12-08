import java.awt.Canvas;
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
 //hrwijdwjd
	static long generation = 0;
	static int screenWidth = 1920;
	static int screenHeight = 1080;
	boolean[] keys = new boolean[300];
	boolean[] keysToggled = new boolean[300];
	boolean[] mouse = new boolean[200];
	boolean allDead;
	int numRockets = 1000;
	long frame = 0;
	int skip = 0;
	Point currentTarget = new Point(screenWidth/2 + Math.random() * screenWidth/2, Math.random() * screenHeight);
	static int targetDiam = 20;
	// Rect obs = new Rect(600, 100, 200, 300);
	ArrayList<Rect> obstacles = new ArrayList<Rect>();
	ArrayList<Rocket> rockets = new ArrayList<Rocket>();
	ArrayList<Rocket> genePool = new ArrayList<Rocket>();
	ArrayList<Double> averages = new ArrayList<Double>();
	Graph avgs;
	double avgAcc = 0.0;
	double sum = 0;
	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(30, 30, 30));
		g.fillRect(0, 0, screenWidth, screenHeight);
		

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
		g.setColor(new Color(30,30,30));
		g.fillRect(0, 0, 150, 100);
		g.setColor(Color.WHITE);
		g.drawString("" + frame, 20, 20);
		g.drawString("Generation: " + generation, 20, 60);
		g.drawString("Avg r: " + (float)avgAcc*100, 20, 90);
		avgs.draw(g);

	}

	public void update() throws InterruptedException {
		skip = keys[32] || keysToggled[88] ? Rocket.maxAge + 1 : 1;
		
		for (int f = 0; f < skip; f++) {
			if(keysToggled[84]) {
			currentTarget = getMousePos();
			}
			allDead = true;
			for (Rocket r : rockets) {
				if(keys[90]) {
					System.out.println("DEBUG");
				}
				
				r.target = currentTarget;
				r.update(obstacles);
				if (!r.dead) {
					allDead = false;
				}
			}
			if (allDead) {
				//make all fitnesses between 0 and 1
				normalizeFitnesses();
				
				//add to gene pool based on fitness
				int achieved = 0;
				for (Rocket r : rockets) { 
					if(r.achieved) achieved++;
					for (int i = 0; i < getChildren(r.fitness); i++) {
						genePool.add(r);
					}
				}
				double sum = 0;
				for(Rocket r : rockets) {
					sum += 1/ (r.pos.distanceTo(currentTarget) - targetDiam/2 + 2);
				}
				//averages.add((double)achieved / (double) numRockets);
				averages.add(100*sum / (double) numRockets);
				sum = 0;
				for(int i = 0; i < (averages.size() >= 10 ? 10 : averages.size()); i++) {
					sum += averages.get(averages.size() - i - 1);
				}
				avgAcc = (float)(sum / (double)((averages.size() >= 10 ? 10 : averages.size())));
				avgs.data = averages;
				System.out.println(averages.get(averages.size()-1));
				//kill all of current generation
				rockets.clear();
				
				//generate a new random target
				if(generation % 1 == 0) currentTarget = keysToggled[84] ? getMousePos() : new Point(screenWidth/2 + Math.random() * screenWidth/2, Math.random() * screenHeight); // new Point(screenWidth/2, screenHeight/2);//
				//currentTarget = new Point(screenWidth, 100);

				//create new population
				for (int i = 0; i < numRockets; i++) {
					NeuralNetwork newBrain = new NeuralNetwork(
							genePool.get((int) (Math.random() * genePool.size())).brain);
					newBrain.mutate(.015f); //controls mutation rate
					
					rockets.add(new Rocket(new Point(20,screenHeight/2 - 30), currentTarget, newBrain));
				}
				genePool.clear();
				generation++;
				frame = 0;

			}

			frame++;
		}
	}

	private Point getMousePos() {
		Point p;
try {
	p = new Point(getMousePosition().x, getMousePosition().y);
	
}catch (Exception e){
	p = new Point(0,0);
}
return p;
	}

	private int getChildren(double fitness) {
	//	System.out.println((int) (100*Math.pow(fitness, 1)));
		return (int) (100*Math.pow(fitness, 1));
	}

	private void normalizeFitnesses() {
		double highFitness = rockets.get(0).fitness;
		for (Rocket r : rockets) {
			if (r.fitness > highFitness) {
				highFitness = r.fitness;
			}
		}
		for (Rocket r : rockets) {
			r.fitness /= highFitness;
		}		
	}

	private void init() {
		for (int i = 0; i < numRockets; i++) {
			rockets.add(new Rocket(new Point(20,screenHeight/2 - 30), currentTarget));
		}
		
		avgs = new Graph(new Point(0,screenHeight - 240),400,200,averages, 8, false, 0.0,1.0);
		
//		obstacles.add(new Rect(0,-90,screenWidth, 100));
//		obstacles.add(new Rect(-90,0,100, screenHeight));
//		obstacles.add(new Rect(0,screenHeight - 40,screenWidth, 100));
//		obstacles.add(new Rect(screenWidth - 10,0,100, screenHeight));
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


