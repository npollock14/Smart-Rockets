import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Rocket {
	Point pos, target;
	Sensor[] sensors = new Sensor[0];
	double w = 10.0, h = 40.0;
	Vec2 vel = new Vec2(0, 0);
	double alpha = 0.00000;
	double omega = Math.toRadians(0.0);
	double theta = Math.toRadians(90);
	double a = 0.000;
	double friction = 0.05;
	double maxAlpha = 0.01;
	double maxA = 0.05;
	int age = 0;
	static int maxAge = 800;
	boolean dead;
	double fitness = 0;
	boolean achieved;
	NeuralNetwork brain;
	Point ul, ur, bl, br;
	double d = Math.sqrt(Math.pow(w / 2, 2) + Math.pow(h / 2, 2));
	double farD = Math.sqrt(Math.pow(SmartRocketsMain.screenHeight,2) + Math.pow(SmartRocketsMain.screenWidth, 2));

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

	public Rocket(Point pos, Point target) {
		super();
		this.pos = pos;
		this.target = target;
		for (int i = 0; i < sensors.length; i++) {
			sensors[i] = new Sensor(pos, i * Math.toRadians(360 / sensors.length), 500);
		}
		this.brain = new NeuralNetwork(sensors.length + 8, 2);
		updateCorners();

	}

	public void show(Graphics g) {

		g.setColor(Color.white);

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

	public void update(ArrayList<Rect> obstacles) { // sensors, velocityLin, velocityAng, pos, theta
		if(!dead && pos.distanceTo(target) < SmartRocketsMain.targetDiam/2) {
			achieved = true;
		}
		if (!dead && (pos.x > SmartRocketsMain.screenWidth || pos.y > SmartRocketsMain.screenHeight || pos.x < 0
				|| pos.y < 0 || age == maxAge || achieved)) {
			dead = true;
			fitness = Math.pow((1 / pos.distanceTo(target)), 2);

		}
		if (!dead) {
			double[][] input = new double[sensors.length + 8][1];
			for (int i = 0; i < input.length - 8; i++) {
				input[i][0] = 1 - sensors[i].data;
			}
			input[sensors.length][0] = vel.x;
			input[sensors.length + 1][0] = vel.y;
			input[sensors.length + 2][0] = target.x/SmartRocketsMain.screenWidth;
			input[sensors.length + 3][0] = target.y/SmartRocketsMain.screenHeight;
			input[sensors.length + 4][0] = pos.x/SmartRocketsMain.screenWidth;
			input[sensors.length + 5][0] = pos.y/SmartRocketsMain.screenHeight;
			input[sensors.length + 6][0] = omega;
			input[sensors.length + 7][0] = (2*Math.PI) / (theta % (2 * Math.PI));
			
			Matrix out = brain.feedFoward(new Matrix(input));

			alpha = (out.data[0][0] * 2 - 1) * maxAlpha;
			a = (out.data[1][0] * 2 - 1) * maxA;

			// System.out.println(out.data[0][0] + " \n " + out.data[1][0]);

//		out.show();
//		System.out.println();

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
			age++;
		}
	}
}
