package pl.edu.agh.tinsnake;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Map implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int maxZoom;
	private BoundingBox boundingBox;
	private List<GPSPoint> points;
	
	public int getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}

	public List<GPSPoint> getPoints() {
		return points;
	}

	public void setPoints(List<GPSPoint> points) {
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	public Map(String name, BoundingBox boundingBox, int maxZoom) {
		this.name = name;
		points = new ArrayList<GPSPoint>();
		this.boundingBox = boundingBox;
		this.maxZoom = maxZoom;
	}
}
