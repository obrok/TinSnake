package pl.edu.agh.tinsnake.util;

import pl.edu.agh.tinsnake.EarthCoordinates;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

public class MapWebView extends WebView {

	private int center;

	private EarthCoordinates coordinates;

	private String mapUrl;

	public void setCoordinates(EarthCoordinates coordinates) {
		this.coordinates = coordinates;
		scrollToCenter();
	}

	public EarthCoordinates getCoordinates() {

		if (coordinates == null) {
			coordinates = new EarthCoordinates(0, 0, 320, 2);
		}

		coordinates.moveCenter(getScrollX(), getScrollY());
		return coordinates;
	}

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
		this.loadDataWithBaseURL(null, generateHtml(), "text/html", "utf-8",
				null);
		Log.d("HTML", generateHtml());
	}

	private String createPoint(double lng, double lat) {
		return "<div style='width: 30px; height: 30px; background: rgba(0, 0, 255, 0.5); font-weight: bold; position: absolute; left: 20px; top: 20px; -webkit-border-radius: 15px; -moz-border-radius: 15px;'></div>";
	}

	private String generateHtml() {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body style='margin: 0px'>");
		builder.append(String.format("<img src=\"%s\"/>", mapUrl));
		builder.append(createPoint(1,1));
		builder.append("</body></html>");
		return builder.toString();
	}
}
