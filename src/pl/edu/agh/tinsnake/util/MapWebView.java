package pl.edu.agh.tinsnake.util;

import pl.edu.agh.tinsnake.BoundingBox;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

public class MapWebView extends WebView {

	private int center;

	private BoundingBox boundingBox;

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	private String mapUrl;

	public MapWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MapWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MapWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		center = w;
		scrollToCenter();
		super.onSizeChanged(w, h, ow, oh);
		this.zoomOut();
	}

	private void scrollToCenter() {
		scrollTo(center, center);
	}

	public void setMapUrl(String string) {
		mapUrl = string;
		refreshMap();
	}

	public void refreshMap() {
		Log.d("HTML", boundingBox.getLeft() + " " + boundingBox.getRight()
				+ " " + boundingBox.getBottom() + " " + boundingBox.getTop());
		try {
			Log.d("HTML", generateHtml());
			this.loadDataWithBaseURL(null, generateHtml(), "text/html",
					"utf-8", null);
		} catch (Exception e) {
			Log.e("HTML", e.getClass() + " " + e.getMessage());
		}
	}

	private String createPoint(double lat, double lng) {
		Log.d("HTML", lat + " " + lng);
		lat = boundingBox.latToFraction(lat);
		lng = boundingBox.lngToFraction(lng);
		Log.d("HTML", lat + " " + lng);
		String position = String.format(
				"position: absolute; top: %d%%; left: %d%%;",
				(int) (100 * lat), (int) (100 * lng));
		return String
				.format(
						"<div style='width: 30px; height: 30px; background: rgba(0, 0, 255, 0.5); font-weight: bold; %s -webkit-border-radius: 15px; -moz-border-radius: 15px;'></div>",
						position);
	}

	private String generateHtml() {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body style='margin: 0px'><div style='position: absolute;'>");
		builder.append(String.format("<img src=\"%s\"/>", mapUrl));
		builder.append(createPoint(50, 0));
		builder.append(createPoint(50, 10));
		builder.append(createPoint(50, 20));
		builder.append("</div></body></html>");
		return builder.toString();
	}
}
