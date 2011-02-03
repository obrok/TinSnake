package pl.edu.agh.tinsnake;

import android.location.Location;

public class GPSPoint extends MapPoint {

	private double altitude;
	private double date;
	
	public double getDate() {
		return date;
	}

	public void setDate(double date) {
		this.date = date;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GPSPoint(Location location) {
		super(location.getLatitude(), location.getLongitude(), "pozycja");
		altitude = location.getAltitude();
		date = location.getTime();
	}

}
