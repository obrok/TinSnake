package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
import android.widget.Toast;

public class ShowMap extends Activity implements LocationListener {
	private BoundingBox boundingBox;
	private MapWebView webView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);
		initializeMapView();
		((LocationManager) getSystemService(Context.LOCATION_SERVICE))
				.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
						this);
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
			webView.setMapUrl("file://" + base + ".jpg");

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

			webView.setBoundingBox(boundingBox);
		} catch (Exception e) {
			Toast.makeText(this.getApplicationContext(), e.getClass()
					.getCanonicalName()
					+ " " + e.getMessage(), Toast.LENGTH_LONG);
			// TODO do something with exceptions
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//webView.loadData(location.toString(), "text/plain", "ASCII");
	}

	@Override
	public void onProviderDisabled(String provider) {
		//webView.loadData(provider.toString() + "disabled", "text/plain", "ASCII");
	}

	@Override
	public void onProviderEnabled(String provider) {
		//webView.loadData(provider.toString() + "enabled", "text/plain", "ASCII");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Ignore
	}
}
