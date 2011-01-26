package pl.edu.agh.tinsnake.util;

import pl.edu.agh.tinsnake.GPSPoint;
import pl.edu.agh.tinsnake.Map;
import pl.edu.agh.tinsnake.MapHelper;
import pl.edu.agh.tinsnake.Map.MapSize;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

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

	private Map map;
	private boolean scrollToPreviousPosition = false;
	private GPSPoint current;
	private int mapZoom = 1;
	private int lastX = 0;
	private int lastY = 0;

	public void setMap(Map map) {
		this.map = map;
		refreshMap();
	}

	public Map getMap() {
		return map;
	}

	@Override
	public boolean zoomIn() {
		if (mapZoom < map.getMaxZoom()) {
			mapZoom++;
			refreshMap(mapZoom - 1);
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStrokeWidth(5);
		paint.setColor(Color.RED);
		paint.setAlpha(70);
		super.onDraw(canvas);
		
		MapSize size = map.getMapSize(mapZoom);
		Log.d("DRAWING", size.getWidth() + " " + size.getHeight());

		for (int i = 0; i < map.getLocationHistory().size() - 1; i++) {
			GPSPoint from = map.getLocationHistory().get(i);
			GPSPoint to = map.getLocationHistory().get(i+1);
			
			int x1 = (int)(size.getWidth() * map.getBoundingBox().lngToFraction(from.getLng()));
			int y1 = (int)(size.getHeight() * map.getBoundingBox().latToFraction(from.getLat()));
			
			int x2 = (int)(size.getWidth() * map.getBoundingBox().lngToFraction(to.getLng()));
			int y2 = (int)(size.getHeight() * map.getBoundingBox().latToFraction(to.getLat()));
			
			canvas.drawLine(x1, y1, x2, y2, paint);
		}
	}

	@Override
	public boolean zoomOut() {
		if (mapZoom > 1) {
			mapZoom--;
			refreshMap(mapZoom + 1);
		}
		return false;
	};

	private float scrollStartX, scrollStartY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			scrollStartX = event.getX();
			scrollStartY = event.getY();
			break;

		case MotionEvent.ACTION_UP:

			float deltaX = scrollStartX - event.getX();
			float deltaY = event.getY() - scrollStartY;

			if (Math.abs(deltaX) + Math.abs(deltaY) < 10) {
				zoomIn();
			}

			break;

		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	private void refreshMap() {
		refreshMap(mapZoom);
	}

	private void refreshMap(int previousZoom) {

		double scale = (double) mapZoom / (double) previousZoom;

		lastX = (int) (scale * getScrollX() + (getWidth() / 2)
				* (mapZoom - previousZoom));
		lastY = (int) (scale * getScrollY() + (getHeight() / 2)
				* (mapZoom - previousZoom));

		scrollToPreviousPosition = true;

		this.setPictureListener(new PictureListener() {

			@Override
			public void onNewPicture(WebView arg0, Picture arg1) {
				if (scrollToPreviousPosition) {
					scrollTo(lastX, lastY);
					scrollToPreviousPosition = false;
					Log.d("SCROLLING TO", lastX + " " + lastY);
				}
			}
		});

		try {
			Log.d("HTML", generateHtml());
			this.loadDataWithBaseURL(null, generateHtml(), "text/html",
					"utf-8", null);
		} catch (Exception e) {
			Log.e("HTML", e.getClass() + " " + e.getMessage());
		}
	}

	private String createPoint(double lat, double lng, String color) {
		if (!map.getBoundingBox().contains(lat, lng)) {
			Log.d("CREATE POINT", "outside map");
			return "";
		}

		Log.d("HTML", lat + " " + lng);
		lat = map.getBoundingBox().latToFraction(lat);
		lng = map.getBoundingBox().lngToFraction(lng);
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
				String imgSrc = "file://"
						+ MapHelper.getMapImageFilePath(map, mapZoom, i, j);
				builder.append(String.format(
						"<img style='margin: 0px; padding: 0px;' src=\"%s\"/>",
						imgSrc));
			}
			builder.append("<br/>");
		}

		if (map.getPoints() != null) {
			for (GPSPoint point : map.getPoints()) {
				builder.append(createPoint(point.getLat(), point.getLng(),
						"rgba(0,0,255,0.5)"));
			}
		}

		if (current != null){
			builder.append(createPoint(current.getLat(), current.getLng(), "rgba(255,0,0,0.5)"));
		}

		builder.append("</div></body></html>");
		return builder.toString();
	}

	public void setCurrentLocation(GPSPoint location) {
		map.addCurrentLocation(location);
		current = location;
		try {
			MapHelper.saveMap(map);
		} catch (Exception e) {
			Log.e("SaveLocation", e.getClass() + " " + e.getMessage());
		}
		refreshMap();
	}
}
