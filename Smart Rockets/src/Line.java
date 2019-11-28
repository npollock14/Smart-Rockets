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

