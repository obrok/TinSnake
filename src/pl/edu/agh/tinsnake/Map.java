package pl.edu.agh.tinsnake;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Class Map representing the downloaded map.
 */
public class Map implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	private String name;

	/** The max zoom. */
	private int maxZoom;

	/** The bounding box. */
	private BoundingBox boundingBox;

	/** The points on the map. */
	private List<MapPoint> points;

	/** The location history. */
	private List<GPSPoint> locationHistory;

	/** The map sizes. */
	private List<MapSize> mapSizes;

	/**
	 * Gets the max zoom.
	 * 
	 * @return the max zoom
	 */
	public int getMaxZoom() {
		return maxZoom;
	}

	/**
	 * Gets the points located on the map (excluding location history).
	 * 
	 * @return the points
	 */
	public List<MapPoint> getPoints() {
		return points;
	}

	/**
	 * Gets the location history.
	 * 
	 * @return the location history
	 */
	public List<GPSPoint> getLocationHistory() {
		Comparator<GPSPoint> c = new Comparator<GPSPoint>() {

			@Override
			public int compare(GPSPoint object1, GPSPoint object2) {
				return (int)(object1.getDate() - object2.getDate());
			}
		};
		Collections.sort(locationHistory, c );
		return locationHistory;
	}

	/**
	 * Sets the points located on the map.
	 * 
	 * @param points
	 *            the new points
	 */
	public void setPoints(List<MapPoint> points) {
		this.points = points;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the bounding box of the map.
	 * 
	 * @return the bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Instantiates a new map.
	 * 
	 * @param name
	 *            the name
	 * @param boundingBox
	 *            the bounding box
	 * @param maxZoom
	 *            the max zoom
	 */
	public Map(String name, BoundingBox boundingBox, int maxZoom) {
		this.name = name;
		points = new ArrayList<MapPoint>();
		locationHistory = new ArrayList<GPSPoint>();
		mapSizes = new ArrayList<MapSize>();
		this.boundingBox = boundingBox;
		this.maxZoom = maxZoom;
	}

	/**
	 * Adds the current location to the location history.
	 * 
	 * @param gpsPoint
	 *            the gps point
	 */
	public void addCurrentLocation(GPSPoint gpsPoint) {
		locationHistory.add(gpsPoint);
	}

	/**
	 * Called after creating the map - stores the map sizes for the given zoom
	 * level (used for displaying lines on the map). Implemented on list because
	 * of the bug with serialization of the HashMap in Android (has to be called
	 * in order of increasing zoom).
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void setMapSize(int zoom, int width, int height) {
		if (zoom > maxZoom) {
			maxZoom = zoom;
		}

		mapSizes.add(new MapSize(width, height));
	}

	/**
	 * Gets the map size on the given zoom level.
	 * 
	 * @param zoom
	 *            the zoom
	 * @return the map size
	 */
	public MapSize getMapSize(int zoom) {
		return mapSizes.get(zoom - 1);
	}

	/**
	 * The Class MapSize.
	 */
	public class MapSize implements Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new map size.
		 * 
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 */
		public MapSize(int width, int height) {
			this.width = width;
			this.height = height;
		}

		/** The width. */
		private int width;

		/** The height. */
		private int height;

		/**
		 * Gets the width.
		 * 
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * Gets the height.
		 * 
		 * @return the height
		 */
		public int getHeight() {
			return height;
		}
	}

	public MapPoint getCurrentLocation() {
		if (locationHistory.size() > 0) {
			return locationHistory.get(locationHistory.size() - 1);
		}
		return null;
	}

	public void clearLocationHistory() {
		locationHistory = new ArrayList<GPSPoint>();
	}
}
