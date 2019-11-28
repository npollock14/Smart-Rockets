import java.awt.Graphics;

public class Point {
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

