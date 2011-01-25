package pl.edu.agh.tinsnake.util;

import java.util.List;

import pl.edu.agh.tinsnake.BoundingBox;
import pl.edu.agh.tinsnake.GPSPoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MapWebView extends WebView {

	public MapWebView(Context context) {
		super(context);
	}
	
	public MapWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MapWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private boolean scrollToPreviousPosition = false;
	private Location current;
	private BoundingBox boundingBox;
	private List<GPSPoint> points;

	private int mapZoom;
	private String mapUrl;
	
	private int lastX=0;
	private int lastY=0;
	
	public void setMapUrl(String string) {
		mapUrl = string;
		refreshMap();
	}

	public void setMapZoom(int mapZoom) {
		this.mapZoom = mapZoom;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void refreshMap() {
		lastX = getScrollX();
		lastY = getScrollY();
		
		scrollToPreviousPosition = true;
		
		this.setPictureListener( new PictureListener() {

			@Override
			public void onNewPicture(WebView arg0, Picture arg1) {
				if (scrollToPreviousPosition){
					scrollTo(lastX, lastY);
					scrollToPreviousPosition = false;
				}
				Log.d("SCROLLING TO", lastX + " " + lastY);		
			}
	    });
		
		//Log.d("HTML", boundingBox.getLeft() + " " + boundingBox.getRight()
			//	+ " " + boundingBox.getBottom() + " " + boundingBox.getTop());
		try {
			//Log.d("HTML", generateHtml());
			this.loadDataWithBaseURL(null, generateHtml(), "text/html",
					"utf-8", null);
		} catch (Exception e) {
			Log.e("HTML", e.getClass() + " " + e.getMessage());
		}
	}

	private String createPoint(Location l, String color) {
		if (l == null) {
			return "";
		}
		return createPoint(l.getLatitude(), l.getLongitude(), color);
	}

	private String createPoint(double lat, double lng, String color) {
		if (!getBoundingBox().contains(lat, lng)) {
			Log.d("CREATE POINT", "outside map");
			return "";
		}

		Log.d("HTML", lat + " " + lng);
		lat = boundingBox.latToFraction(lat);
		lng = boundingBox.lngToFraction(lng);
		Log.d("HTML", lat + " " + lng);
		String position = String.format(
				"position: absolute; top: %d%%; left: %d%%;",
				(int) (100 * lat), (int) (100 * lng));
		return String
				.format(
						"<div style='%s'><div style='width: 30px; height: 30px; background: %s; -webkit-border-radius: 15px; -moz-border-radius: 15px; position: relative; left: -15px; top: -15px;'></div></div>",
						position, color);
	}

	private String generateHtml() {
		StringBuilder builder = new StringBuilder();
		builder
				.append(String
						.format(
								"<html><body style='margin: 0px'><div style='position: absolute; width: %dpx'>",
								mapZoom * 1000));

		
			for (int j = mapZoom - 1; j >= 0; j--) {
				for (int i = 0; i < mapZoom; i++) {
				String imgSrc = String.format(mapUrl, mapZoom, i, j);
				builder.append(String.format(
						"<img style='margin: 0px; padding: 0px;' src=\"%s\"/>",
						imgSrc));
			}
			builder.append("<br/>");
		}

		if (points != null) {
			for (GPSPoint point : points) {
				builder.append(createPoint(point.getLat(), point.getLng(),
						"rgba(0,0,255,0.5)"));
			}
		}

		builder.append(createPoint(current, "rgba(255,0,0,0.5)"));

		builder.append("</div></body></html>");
		return builder.toString();
	}

	public void setCurrentLocation(Location location) {
		current = location;
		refreshMap();
	}

	public void setGPSPoints(List<GPSPoint> points) {
		this.points = points;
	}
}
