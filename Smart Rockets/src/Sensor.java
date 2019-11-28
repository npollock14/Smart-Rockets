import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Sensor {
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