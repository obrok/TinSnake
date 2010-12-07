package pl.edu.agh.tinsnake;

import java.io.Serializable;

/***
 * Represents the coordinates of a squarish slice of the map
 * 
 * @author obrok
 * 
 */
public class EarthCoordinates implements Serializable{
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

	public EarthCoordinates(double lat, double lng, int size, int zoom) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.size = size;
		this.zoom = zoom;
	}

	public void zoomIn(double x, double y) {
		double horSize = 360 / Math.pow(2, zoom - 1);
		double vertSize = 171 / Math.pow(2, zoom - 1);

		zoom += 1;
		lng += (x / size - 0.5) * horSize;
		lat += (0.5 - y / size) * vertSize;
	}
	
	public void zoomOut(){
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
}
