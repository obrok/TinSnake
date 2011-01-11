package pl.edu.agh.tinsnake;

import java.io.Serializable;

/***
 * Represents the coordinates of a squarish slice of the map
 * 
 * @author obrok
 * 
 */
public class EarthCoordinates implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/***
	 * Creates a slice approximately centered on lat and lng
	 * 
	 * @param lat
	 * @param lng
	 */
	private double lat, lng;
	private int size;
	private int zoom;

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lng;
	}

	public int getSize() {
		return size;
	}

	public int getZoom() {
		return zoom;
	}

	public EarthCoordinates(double lat, double lng, int size, int zoom) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.size = size;
		this.zoom = zoom;
	}

	public void moveCenter(float deltaX, float deltaY) {
		deltaX /= size;
		deltaY /= size;

		double horSize = 360 / Math.pow(2, zoom - 2);
		double vertSize = 171 / Math.pow(2, zoom - 2);

		lng += horSize * deltaX;
		lat += vertSize * deltaY;
	}

	public void zoomIn(float x, float y) {
		double horSize = 360 / Math.pow(2, zoom - 1);
		double vertSize = 171 / Math.pow(2, zoom - 1);

		zoom += 1;

		lng += (x / size - 0.5) * horSize;
		lat += (0.5 - y / size) * vertSize;
	}

	public void zoomIn() {
		zoom += 1;
	}

	public void zoomOut() {
		zoom -= 1;
	}

	public String toOSMString() {
		String lattitude = String.format("%f", lat).replace(',', '.');
		String longitude = String.format("%f", lng).replace(',', '.');
		return String
				.format(
						"http://tah.openstreetmap.org/MapOf/?lat=%s&long=%s&z=%d&w=%d&h=%d&format=jpeg",
						lattitude, longitude, zoom, size, size);
	}

	public BoundingBox toBoundingBox() {
		double horSize = 360 / Math.pow(2, zoom - 1);
		double vertSize = 171 / Math.pow(2, zoom - 1);

		return new BoundingBox(lng - horSize * 0.6, lng + horSize * 0.6, lat
				- vertSize * 0.6, lat + vertSize * 0.6);
	}
}
