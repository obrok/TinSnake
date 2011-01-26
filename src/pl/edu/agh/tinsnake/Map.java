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
	private List<GPSPoint> locationHistory;
	private List<MapSize> mapSizes;
	
	public int getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}

	public List<GPSPoint> getPoints() {
		return points;
	}
	
	public List<GPSPoint> getLocationHistory() {
		return locationHistory;
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
		locationHistory = new ArrayList<GPSPoint>();
		mapSizes = new ArrayList<MapSize>();
		this.boundingBox = boundingBox;
		this.maxZoom = maxZoom;
	}

	public void addCurrentLocation(GPSPoint gpsPoint) {
		locationHistory.add(gpsPoint);
	}

	public void setMapSize(int width, int height) {
		mapSizes.add(new MapSize(width, height));
	}
	
	public MapSize getMapSize(int zoom){
		return mapSizes.get(zoom - 1);
	}
	
	public class MapSize implements Serializable {
		private static final long serialVersionUID = 1L;

		public MapSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		private int width;
		private int height;
		
		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
	}
}
