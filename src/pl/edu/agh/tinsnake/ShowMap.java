package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.MapWebView;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class ShowMap extends Activity implements LocationListener {
	private BoundingBox boundingBox;
	private MapWebView webView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.show);
			initializeMapView();
			((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							0, 0, this);
		} catch (Exception e) {
			Log.e("ShowMap", e.getClass() + " " + e.getMessage());
		}
	}

	private void initializeMapView() {
		try {
			Intent intent = getIntent();

			String mapName = intent.getStringExtra("mapName");

			String base = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "mapsfolder"
					+ File.separator
					+ mapName
					+ File.separator + mapName;

			webView = ((MapWebView) this.findViewById(R.id.showMap));

			StreamUtil.safelyAcccess(new ObjectInputStream(new FileInputStream(
					base + ".txt")), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					try {
						boundingBox = (BoundingBox) ((ObjectInputStream) stream)
								.readObject();
					} catch (ClassNotFoundException e) {
						boundingBox = new BoundingBox(0, 1, 0, 1);
					}
				}
			});
			
			StreamUtil.safelyAcccess(new ObjectInputStream(new FileInputStream(
					base + ".info")), new CloseableUser() {
				@SuppressWarnings("unchecked")
				@Override
				public void performAction(Closeable stream) throws IOException {
					try {
						webView.setGPSPoints((List<GPSPoint>) ((ObjectInputStream) stream)
								.readObject());
					} catch (ClassNotFoundException e) {
						webView.setGPSPoints(new ArrayList<GPSPoint>());
					}
				}
			});

			webView.setBoundingBox(boundingBox);
			webView.setMapUrl("file://" + base + "_zoom%d_img%d_%d.jpg");

		} catch (Exception e) {
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		webView.setCurrentLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// webView.loadData(provider.toString() + "disabled", "text/plain",
		// "ASCII");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// webView.loadData(provider.toString() + "enabled", "text/plain",
		// "ASCII");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Ignore
	}
}
