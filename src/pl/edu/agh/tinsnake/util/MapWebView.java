package pl.edu.agh.tinsnake.util;

import pl.edu.agh.tinsnake.EarthCoordinates;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class MapWebView extends WebView {

	private int center;
	
	private EarthCoordinates coordinates;
	
	public void setCoordinates(EarthCoordinates coordinates){
		this.coordinates = coordinates;
		scrollToCenter();
	}
	
	public EarthCoordinates getCoordinates(){
		
		if (coordinates == null){
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
	}

	private void scrollToCenter() {
		scrollTo(center, center);
	}
}
