package pl.edu.agh.tinsnake;

public class GPSPoint {
	
	public GPSPoint(double lat, double lng, String name,
			GPSPointClass pointClass) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.name = name;
		this.pointClass = pointClass;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GPSPointClass getPointClass() {
		return pointClass;
	}
	public void setPointClass(GPSPointClass pointClass) {
		this.pointClass = pointClass;
	}
	private double lat;
	private double lng;
	private String name;
	private GPSPointClass pointClass;
}
