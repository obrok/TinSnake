package pl.edu.agh.tinsnake;

import java.io.Serializable;

public class BoundingBox implements Serializable {
	private static final long serialVersionUID = 1L;
	private double left, right, top, bottom;

	public BoundingBox(double left, double right, double bottom, double top) {
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	private int calculateScale(int targetWidth) {
		return (int) (397569610.0 / targetWidth * Math.abs(left - right));
	}

	private String dotted(double number) {
		return new Double(number).toString().replace(",", ".");
	}

	public double latToFraction(double lat) {
		return 1 - (lat - bottom) / (top - bottom);
	}

	public double lngToFraction(double lng) {
		return (lng - left) / (right - left);
	}

	public String toOSMString(int targetWidth) {
		return String
				.format(
						"http://tile.openstreetmap.org/cgi-bin/export?bbox=%s,%s,%s,%s&scale=%d&format=jpeg",
						dotted(left), dotted(bottom), dotted(right),
						dotted(top), calculateScale(targetWidth));
	}

	public String toXMLString() {
		return String.format(
				"http://api.openstreetmap.org/api/0.6/map?bbox=%s,%s,%s,%s",
				dotted(left), dotted(bottom), dotted(right), dotted(top));
	}

	public boolean contains(double lat, double lng) {
		return lat > bottom && lat < top && lng > left && lng < right;
	}
}
