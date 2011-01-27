package pl.edu.agh.tinsnake;

import java.io.Serializable;

/**
 * *
 * Represents the coordinates of a squarish slice of the map.
 *
 * @author obrok
 */
public class EarthCoordinates implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** * Creates a slice approximately centered on lat and lng. */
	private double lat, lng;

	/**
	 * Gets the latitude.
	 *
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * Gets the longitude.
	 *
	 * @return the lon
	 */
	public double getLon() {
		return lng;
	}

	/**
	 * Instantiates a new earth coordinates.
	 *
	 * @param lat the lat
	 * @param lng the lng
	 */
	public EarthCoordinates(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}

	/**
	 * Moves the coordinates (used for moving the preview of the map).
	 *
	 * @param deltaX the delta x in pixels
	 * @param deltaY the delta y in pixels
	 * @param size the size of the image
	 * @param zoom the zoom of the image
	 */
	public void moveCenter(float deltaX, float deltaY, int size, int zoom) {
		deltaX /= size;
		deltaY /= size;

		double horSize = 360 / Math.pow(2, zoom - 2);
		double vertSize = 171 / Math.pow(2, zoom - 2);

		lng += horSize * deltaX;
		lat += vertSize * deltaY;
	}

	/**
	 * Moves the coordinates (used for zooming in the preview of the map).
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param size the size of the image
	 * @param zoom the zoom of the image
	 */
	public void zoomIn(float x, float y, int size, int zoom) {
		double horSize = 360 / Math.pow(2, zoom - 1);
		double vertSize = 171 / Math.pow(2, zoom - 1);

		lng += (x / size - 0.5) * horSize;
		lat += (0.5 - y / size) * vertSize;
	}

	/**
	 * Returns the URL used for downloading the square map image in the place represented by the coordinates. 
	 *
	 * @param size the size of the output image
	 * @param zoom the zoom of the output image
	 * @return the URL
	 */
	public String toOSMString(int size, int zoom) {
		String lattitude = String.format("%f", lat).replace(',', '.');
		String longitude = String.format("%f", lng).replace(',', '.');
		return String
				.format(
						"http://tah.openstreetmap.org/MapOf/?lat=%s&long=%s&z=%d&w=%d&h=%d&format=jpeg",
						lattitude, longitude, zoom, size, size);
	}

	/**
	 * Creates a bounding box covering a slightly larger area using the given zoom.
	 *
	 * @param zoom the zoom
	 * @return the bounding box
	 */
	public BoundingBox toBoundingBox(int zoom) {
		double horSize = 360 / Math.pow(2, zoom - 1);
		double vertSize = 171 / Math.pow(2, zoom - 1);

		return new BoundingBox(lng - horSize * 0.6, lng + horSize * 0.6, lat
				- vertSize * 0.6, lat + vertSize * 0.6);
	}
}
