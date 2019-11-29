import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Graph {
	Point pos;
	int w, h;
	ArrayList<Double> data = new ArrayList<Double>();
	double pointSize;
	boolean connectPoints;
	double min,max;
	public Graph(Point pos, int w, int h, ArrayList<Double> data, double pointSize, boolean connectPoints, double min, double max) {
		super();
		this.pos = pos;
		this.w = w;
		this.h = h;
		this.data = data;
		this.pointSize = pointSize;
		this.connectPoints = connectPoints;
		this.min = min;
		this.max = max;
	}
	public void draw(Graphics g) {
	
		g.setColor(Color.black);
		g.fillRect((int)pos.x, (int)pos.y, w, h);
		g.setColor(Color.green);
		g.drawRect((int)pos.x, (int)pos.y, w, h);
		
		g.setColor(Color.white);
		double spacing = 1;
		if(data.size() <= w) {
			spacing = (double)w/(double)data.size();
		}
		double range = max-min;
		for(int i = 0; i < data.size(); i++) {
			g.fillOval((int)(pos.x + (i*spacing - pointSize/2)), (int)((pos.y + h) - ((h * ((data.get(i).doubleValue() + min)/range) - pointSize/2))), (int)pointSize, (int)pointSize);
		}
	}
	
	
}
