package pl.edu.agh.tinsnake;

import java.io.Serializable;

/**
 * The Class GPSPoint represents the point on the map.
 */
public class GPSPoint implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new gPS point.
	 *
	 * @param lat the lat
	 * @param lng the lng
	 * @param name the name of the point
	 * @param pointClass the class of the point
	 */
	public GPSPoint(double lat, double lng, String name,
			GPSPointClass pointClass) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.name = name;
		this.pointClass = pointClass;
	}
	
	/**
	 * Gets the lat.
	 *
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}
	
	/**
	 * Sets the lat.
	 *
	 * @param lat the new lat
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	/**
	 * Gets the lng.
	 *
	 * @return the lng
	 */
	public double getLng() {
		return lng;
	}
	
	/**
	 * Sets the lng.
	 *
	 * @param lng the new lng
	 */
	public void setLng(double lng) {
		this.lng = lng;
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
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the point class.
	 *
	 * @return the point class
	 */
	public GPSPointClass getPointClass() {
		return pointClass;
	}
	
	/**
	 * Sets the point class.
	 *
	 * @param pointClass the new point class
	 */
	public void setPointClass(GPSPointClass pointClass) {
		this.pointClass = pointClass;
	}
	
	/** The lat. */
	private double lat;
	
	/** The lng. */
	private double lng;
	
	/** The name. */
	private String name;
	
	/** The class of the point. */
	private GPSPointClass pointClass;
}
