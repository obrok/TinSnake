package pl.edu.agh.tinsnake;

import java.io.Serializable;

/**
 * The Class BoundingBox - represents a region described by its edges' geographical coordinates.
 */
public class BoundingBox implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The geographical coordinates of the edges. */
	private double left, right, top, bottom;

	/**
	 * Instantiates a new bounding box.
	 *
	 * @param left the geographical coordinate of the left edge
	 * @param right the the geographical coordinate of the right edge
	 * @param bottom the the geographical coordinate of the bottom edge
	 * @param top the the geographical coordinate of the top edge
	 */
	public BoundingBox(double left, double right, double bottom, double top) {
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	/**
	 * Gets the coordinate of the left edge.
	 *
	 * @return the left
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * Sets the coordinate of the left edge.
	 *
	 * @param left the new left
	 */
	public void setLeft(double left) {
		this.left = left;
	}

	/**
	 * Gets the coordinate of the right edge.
	 *
	 * @return the right
	 */
	public double getRight() {
		return right;
	}

	/**
	 * Sets the coordinate of the right edge.
	 *
	 * @param right the new right
	 */
	public void setRight(double right) {
		this.right = right;
	}

	/**
	 * Gets the coordinate of the top edge.
	 *
	 * @return the top
	 */
	public double getTop() {
		return top;
	}

	/**
	 * Sets the coordinate of the top edge.
	 *
	 * @param top the new top
	 */
	public void setTop(double top) {
		this.top = top;
	}

	/**
	 * Gets the coordinate of the bottom edge.
	 *
	 * @return the bottom
	 */
	public double getBottom() {
		return bottom;
	}

	/**
	 * Sets the coordinate of the bottom edge.
	 *
	 * @param bottom the new bottom
	 */
	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	/**
	 * Calculates the scale of the map given the target width of the map in pixels.
	 *
	 * @param targetWidth the target width
	 * @return the calculated scale
	 */
	private int calculateScale(int targetWidth) {
		return (int) (397569610.0 / targetWidth * Math.abs(left - right));
	}

	/**
	 * Returns the dotted version of the number.
	 *
	 * @param number the number
	 * @return the string
	 */
	private String dotted(double number) {
		return new Double(number).toString().replace(",", ".");
	}

	/**
	 * Returns the position of the given latitude as the percentage of the region contained in the bounding box.
	 *
	 * @param lat the latitude
	 * @return the percentage
	 */
	public double latToFraction(double lat) {
		return 1 - (lat - bottom) / (top - bottom);
	}

	/**
	 * Returns the position of the given longitude as the percentage of the region contained in the bounding box.
	 *
	 * @param lng the longitude
	 * @return the percentage
	 */
	public double lngToFraction(double lng) {
		return (lng - left) / (right - left);
	}

	/**
	 * Returns the URL used for downloading map image of the bounding box.
	 *
	 * @param targetWidth the width of the image
	 * @return the URL
	 */
	public String toOSMString(int targetWidth) {
		return String
				.format(
						"http://tile.openstreetmap.org/cgi-bin/export?bbox=%s,%s,%s,%s&scale=%d&format=jpeg",
						dotted(left), dotted(bottom), dotted(right),
						dotted(top), calculateScale(targetWidth));
	}

	/**
	 * Returns the URL used for downloading the XML describing the bounding box.
	 *
	 * @return the URL
	 */
	public String toXMLString() {
		return String.format(
				"http://api.openstreetmap.org/api/0.6/map?bbox=%s,%s,%s,%s",
				dotted(left), dotted(bottom), dotted(right), dotted(top));
	}

	/**
	 * Checks if the bounding box contains the given point.
	 *
	 * @param lat the latitude
	 * @param lng the longitude
	 * @return true, if successful
	 */
	public boolean contains(double lat, double lng) {
		return lat > bottom && lat < top && lng > left && lng < right;
	}

	/**
	 * Gets the sub bounding box (used for generating the tiles of the map for bigger zoom levels).
	 *
	 * @param zoom the zoom
	 * @param i the i coordinate
	 * @param j the j coordinate
	 * @return the sub bounding box
	 */
	public BoundingBox getSubBoundingBox(int zoom, int i, int j) {
		
		double horizontalSize = right - left;
		double verticalSize = top - bottom;
		
		double subHorizontalSize = horizontalSize / zoom;
		double subVerticalSize = verticalSize / zoom;
		
		double subLeft = left + i*subHorizontalSize;
		double subRight = subLeft + subHorizontalSize;
		
		double subBottom = bottom + j*subVerticalSize;
		double subTop = subBottom + subVerticalSize;
		
		return new BoundingBox(subLeft, subRight, subBottom, subTop);
	}
}
