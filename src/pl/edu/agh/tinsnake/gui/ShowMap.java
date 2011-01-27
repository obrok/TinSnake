package pl.edu.agh.tinsnake.gui;

import pl.edu.agh.tinsnake.GPSPoint;
import pl.edu.agh.tinsnake.GPSPointClass;
import pl.edu.agh.tinsnake.MapHelper;
import pl.edu.agh.tinsnake.util.MapWebView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity used to display one of the saved maps.
 * @author mn
 *
 */
public class ShowMap extends Activity {
	
	/** The Constant PROGRESS_DIALOG. */
	private static final int PROGRESS_DIALOG = 1;
	
	/** The web view. */
	private MapWebView webView;

	/**
	 * Called when the activity is first created.
	 *
	 * @param savedInstanceState the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.show);
			initializeMapView();
			initializeLocationListener();
		} catch (Exception e) {
			Log.e("ShowMap", e.getClass() + " " + e.getMessage());
		}
	}

	
	/**
	 * Initializes location listener to update the positions of the user.
	 */
	private void initializeLocationListener() {
		((LocationManager) getSystemService(Context.LOCATION_SERVICE))
		.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, new LocationListener() {
					
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
						// TODO Auto-generated method stub

					}
					
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(Location location) {
						try{
							webView.setCurrentLocation(new GPSPoint(location.getLatitude(), location
									.getLongitude(), "position", GPSPointClass.Location));
						}
						catch(Exception e){
							
						}
					}
				});
		
	}
	
	/**
	 * Initializes map view (loads the appropriate map using the map name stored in the intent).
	 */
	private void initializeMapView() {
		try {
			Intent intent = getIntent();
			String mapName = intent.getStringExtra("mapName");

			webView = ((MapWebView) this.findViewById(R.id.showMap));
			webView.setMap(MapHelper.loadMap(mapName));
			Log.d("ShowMap", "map loaded");

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Can't show map :(", Toast.LENGTH_SHORT).show();
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_menu, menu);
		return true;
	};
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.showDownloadInfoMenuItem:
			downloadMapInfo();
			return true;
			
		case R.id.showZoomInMenuItem:
			webView.zoomIn();
			return true;

		case R.id.showZoomOutMenuItem:
			webView.zoomOut();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected android.app.Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading...");
			return progressDialog;

		default:
			return null;
		}
	}

	/**
	 * If there is an internet connection downloads xml with map description and extracts restaurants from it.
	 * The information is saved for later.
	 */
	private void downloadMapInfo() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				dismissDialog(PROGRESS_DIALOG);

				if (!msg.getData().getBoolean("success")) {
					Toast.makeText(getApplicationContext(), "Can't download map info :(", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getApplicationContext(), "Map info downloaded :)", Toast.LENGTH_SHORT).show();
				}
			}
		};

		showDialog(PROGRESS_DIALOG);

		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = new Message();
				Bundle bundle = new Bundle();
				message.setData(bundle);
				try {
					MapHelper.downloadMapInfo(webView.getMap());
					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("SaveMap", e.getClass() + " " + e.getMessage());
					bundle.putBoolean("success", false);
				}
				handler.sendMessage(message);
			}
		}).start();
		
	}
}
