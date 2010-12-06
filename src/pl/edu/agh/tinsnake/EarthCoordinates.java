package pl.edu.agh.tinsnake;

/***
 * Represents the coordinates of a squarish slice of the map
 * 
 * @author obrok
 * 
 */
public class EarthCoordinates {
	/***
	 * Creates a slice approximately centered on lat and lng
	 * 
	 * @param lat
	 * @param lng
	 */
	private double lat, lng;
	private int size;
	private int zoom = 1;

	private EarthCoordinates(){
		super();
	}
	
	public EarthCoordinates(double lat, double lng, int size) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.size = size;
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
	
	@Override
	public String toString(){
		return String.format("%f:%f:%d:%d", lat, lng, size, zoom);
	}
	
	public static EarthCoordinates parse(String string){
		EarthCoordinates result = new EarthCoordinates();
		String[] parts = string.split(":");
		result.lat = Double.parseDouble(parts[0]);
		result.lng = Double.parseDouble(parts[1]);
		result.size = Integer.parseInt(parts[2]);
		result.zoom = Integer.parseInt(parts[3]);
		return result;
	}
}
