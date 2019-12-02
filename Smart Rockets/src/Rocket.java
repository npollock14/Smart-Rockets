import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Rocket {
	Point pos, target;
	Sensor[] sensors = new Sensor[0];

	int extraInputs = 9;

	double maxOmega = .2;

	double w = 10.0, h = 40.0;
	Vec2 vel = new Vec2(0, 0);
	double alpha = 0.00000;
	double omega = Math.toRadians(0.0);
	double theta = Math.toRadians(0);
	double a = 0.000;
	double friction = 0.000;
	double maxAlpha = 0.003;// * 100;
	double maxA = .05;// * 200;
	int age = 0;
	static int maxAge = 400;
	boolean dead;
	double fitness = 0;
	double distanceTraveled;
	boolean achieved;
	NeuralNetwork brain;
	Point ul, ur, bl, br;
	double d = Math.sqrt(Math.pow(w / 2, 2) + Math.pow(h / 2, 2));
	double farD = Math.sqrt(Math.pow(SmartRocketsMain.screenHeight, 2) + Math.pow(SmartRocketsMain.screenWidth, 2));
	double closeEnc = farD;

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
		this.brain = new NeuralNetwork(sensors.length + extraInputs, 64,16, 2);
		updateCorners();

	}

	public void show(Graphics g) {
		// if (!dead || achieved) {

		g.setColor(Color.white);

		// for (Sensor s : sensors) {
		// s.draw(g);
		// }

		g.setColor(Color.white);
		ul.drawLine(ur, g);
		ul.drawLine(bl, g);
		ur.drawLine(br, g);
		bl.drawLine(br, g);
		ul.drawLine(pos, g);
		bl.drawLine(pos, g);
	}
	// g.drawString(Math.toDegrees(theta) + " deg", (int) (pos.x + w), (int) (pos.y
	// - h));

	// }

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
		double targetDist = pos.distanceTo(target);
		
		if (!dead && targetDist < SmartRocketsMain.targetDiam / 2) {
			achieved = true;
			dead = true;
		}
		 for(Rect r : obstacles) {
		 if(pos.inside(r)) {
		 dead = true;
		 }
		 }
		if (!dead && (age == maxAge || achieved)) {
			dead = true;
			fitness = (1 / (Math.pow(((closeEnc)), 2)) / (age / 2));
			// System.out.println(fitness);

		}
		if (!dead) {
			if (age % 5 == 0) {
				double[][] input = new double[sensors.length + extraInputs][1];
				for (int i = 0; i < input.length - extraInputs; i++) {
					input[i][0] = 1 - sensors[i].data;
				}

				// input[sensors.length + 2][0] = omega / maxOmega;
				// give delta theta
				input[sensors.length + 0][0] = Math.cos(theta);/// 2 + .5; delta x next frame
				input[sensors.length + 1][0] = Math.cos(pos.angleTo(target));/// 2 + .5;
				input[sensors.length + 2][0] = Math.sin(theta);
				input[sensors.length + 3][0] = Math.sin(pos.angleTo(target));
				input[sensors.length + 4][0] = Math.cos(theta) / (pos.x - target.x);
				input[sensors.length + 5][0] = Math.sin(theta) / (pos.y - target.y);
				// input[sensors.length + 4][0] = sameSign(Math.cos(theta), pos.x - target.x) ?
				// 1 : 0;
				// input[sensors.length + 5][0] = sameSign(Math.sin(theta), pos.y - target.y) ?
				// 1 : 0;
				// input[sensors.length + 3][0] = ((pos.y - target.y) /
				// (SmartRocketsMain.screenHeight));
				// input[sensors.length + 6][0] = ((target.x) - SmartRocketsMain.screenWidth /
				// 2)
				// / (SmartRocketsMain.screenWidth / 2);
				// input[sensors.length + 7][0] = ((target.y) - SmartRocketsMain.screenHeight /
				// 2)
				// / (SmartRocketsMain.screenHeight / 2);
				// input[sensors.length + 4][0] = target.x / SmartRocketsMain.screenWidth;
				// input[sensors.length + 5][0] = target.y / SmartRocketsMain.screenHeight;
				input[sensors.length + 6][0] = vel.x;
				input[sensors.length + 7][0] = vel.y;
				input[sensors.length + 8][0] = omega / maxOmega / 2 + maxOmega / 2;

				// new Matrix(input).show();

				// input[sensors.length + 8][0] = (2*Math.PI) / (theta % (2 * Math.PI));

				Matrix out = brain.feedFoward(new Matrix(input));
				alpha = (out.data[0][0] * 2 - 1) * maxAlpha;
				a = out.data[1][0] * maxA;
				// System.out.println(out.data[0][0] + " \n " + out.data[1][0]);
			}
			// out.show();
			// System.out.println();

			omega += alpha;
			theta += omega;

			vel.x += a * Math.cos(theta);
			vel.y -= a * Math.sin(theta);
			distanceTraveled += vel.getMagnitude();
			pos.add(vel);

			omega *= 1 - friction;
			vel.x *= 1 - friction;
			vel.y *= 1 - friction;
			
			if(targetDist < closeEnc) {
				closeEnc = targetDist;
			}

			for (Sensor s : sensors) {
				s.update(pos, theta, obstacles);
			}

			updateCorners();
			age++;

		}
	}

	public boolean sameSign(double d1, double d2) {
		return ((d1 > 0 && d2 > 0) || (d1 < 0 && d2 < 0));
	}
}
