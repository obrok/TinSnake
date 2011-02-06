package pl.edu.agh.tinsnake.util;

import pl.edu.agh.tinsnake.GPSPoint;
import pl.edu.agh.tinsnake.Map;
import pl.edu.agh.tinsnake.MapHelper;
import pl.edu.agh.tinsnake.MapPoint;
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
		Log.d("ZOOM IN", "max " + map.getMaxZoom());
		Log.d("ZOOM IN", "cur " + mapZoom);
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

		for (int i = 0; i < map.getLocationHistory().size() - 1; i++) {
			MapPoint from = map.getLocationHistory().get(i);
			MapPoint to = map.getLocationHistory().get(i+1);
			
			int x1 = lngToPixel(from.getLng());
			int y1 = latToPixel(from.getLat());
			
			int x2 = lngToPixel(to.getLng());
			int y2 = latToPixel(to.getLat());
			
			canvas.drawLine(x1, y1, x2, y2, paint);
		}
		
		MapPoint current = map.getCurrentLocation();
		if (current != null){
			canvas.drawCircle(lngToPixel(current.getLng()), latToPixel(current.getLat()), 20, paint);
		}
	}
	
	private int lngToPixel(double lng){
		MapSize size = map.getMapSize(mapZoom);
		return (int)(size.getWidth() * map.getBoundingBox().lngToFraction(lng));
	}
	
	private int latToPixel(double lat){
		MapSize size = map.getMapSize(mapZoom);
		return (int)(size.getHeight() * map.getBoundingBox().latToFraction(lat));
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

		MapSize previousSize = map.getMapSize(previousZoom);
		final MapSize currentSize = map.getMapSize(mapZoom);
		
		final double xPercentage = (double) (getScrollX() + getWidth() / 2) / (double) previousSize.getWidth();
		final double yPercentage = (double) (getScrollY() + getHeight() / 2) / (double) previousSize.getHeight();

		scrollToPreviousPosition = true;

		this.setPictureListener(new PictureListener() {

			@Override
			public void onNewPicture(WebView arg0, Picture arg1) {
				if (scrollToPreviousPosition) {
					scrollTo(
							(int)(xPercentage * (double)currentSize.getWidth()) - getWidth() / 2,
							(int)(yPercentage * (double)currentSize.getHeight()) - getHeight() / 2
					);
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
		int zoom = (int)Math.pow(2, (mapZoom - 1));
		StringBuilder builder = new StringBuilder();
		builder
				.append(String
						.format(
								"<html><body style='margin: 0px'><div style='position: absolute; width: %dpx'>",
								zoom * 1000));

		for (int j = zoom - 1; j >= 0; j--) {
			for (int i = 0; i < zoom; i++) {
				String imgSrc = "file://"
						+ MapHelper.getMapImageFilePath(map, zoom, i, j);
				builder.append(String.format(
						"<img style='margin: 0px; padding: 0px;' src=\"%s\"/>",
						imgSrc));
			}
			builder.append("<br/>");
		}

		if (map.getPoints() != null) {
			for (MapPoint point : map.getPoints()) {
				builder.append(createPoint(point.getLat(), point.getLng(),
						"rgba(0,0,255,0.5)"));
			}
		}

		builder.append("</div></body></html>");
		return builder.toString();
	}

	public void setCurrentLocation(GPSPoint location) {
		map.addCurrentLocation(location);
		try {
			MapHelper.saveMap(map);
		} catch (Exception e) {
			Log.e("SaveLocation", e.getClass() + " " + e.getMessage());
		}
		refreshMap();
	}

	public int getMapZoom() {
		return mapZoom;
	}
}
