package pl.edu.agh.tinsnake.gui;

import pl.edu.agh.tinsnake.GPSPoint;
import pl.edu.agh.tinsnake.Map;
import pl.edu.agh.tinsnake.MapHelper;
import pl.edu.agh.tinsnake.util.MapWebView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * An activity used to display one of the saved maps.
 * 
 * @author mn
 * 
 */
public class ShowMap extends Activity implements OnClickListener, LocationListener {

	private static final int PROGRESS_DIALOG = 1;
	protected static final int FAILURE_DIALOG = 0;
	private static final int LOCATION_SETTINGS_REQUEST_CODE = 0;

	private SharedPreferences locationSettings;
	
	/** The web view. */
	private MapWebView webView;

	private Map map;
	private LocationManager locationManager;
	private AlertDialog deleteAlert;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.show);
			this.findViewById(R.id.showZoomIn).setOnClickListener(this);
			this.findViewById(R.id.showZoomOut).setOnClickListener(this);
			initializeMapView();
			locationSettings = getSharedPreferences(LocationSettings.SETTINGS_NAME, 0);
			initializeLocationListener();
		} catch (Exception e) {
			Log.e("ShowMap", e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * Initializes location listener to update the positions of the user.
	 */
	private void initializeLocationListener() {
		locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
		refreshLocationSettings();
	}

	/**
	 * Initializes map view (loads the appropriate map using the map name stored
	 * in the intent).
	 */
	private void initializeMapView() {
		try {
			Intent intent = getIntent();
			String mapName = intent.getStringExtra("mapName");

			webView = ((MapWebView) this.findViewById(R.id.showMap));
			
			map = MapHelper.loadMap(mapName);
			Log.d("ShowMap", "map loaded");
			webView.setMap(map);
			Log.d("ShowMap", "map set");

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Can't show map :(",
					Toast.LENGTH_SHORT).show();
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_menu, menu);
		return true;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.showClear:
			map.clearLocationHistory();
			try {
				MapHelper.saveMap(map);
			} catch (Exception e) {
				Log.e("CLEAR LOCATION HISTORY", e.getClass().getCanonicalName() + " "
						+ e.getMessage());
				return false;
			} 
			return true;
		case R.id.showNextZoom:
			downloadNextZoomLevel();
			return true;
		case R.id.showInfo:
			downloadMapInfo();
			return true;
		case R.id.showSettings:
			Intent intent = new Intent(getApplicationContext(), LocationSettings.class);
			
			startActivityForResult(intent, LOCATION_SETTINGS_REQUEST_CODE);
			return true;
		case R.id.showDeleteMaps:
			showDeleteMapsDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showDeleteMapsDialog() {
		final String[] items = MapHelper.getMapNames();
		
		if (items.length == 0){
			Toast.makeText(getApplicationContext(), "No maps :(", Toast.LENGTH_SHORT).show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a map to delete");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        String chosen = items[item].toString();
		        MapHelper.deleteMap(chosen);
		        deleteAlert.dismiss();
		    }
		});
		deleteAlert = builder.create();
		deleteAlert.show();
		
		return;
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case LOCATION_SETTINGS_REQUEST_CODE:
			if (resultCode == 0) {
				refreshLocationSettings();
			}
			break;

		default:
			break;
		}
	}

	private void refreshLocationSettings() {
		Log.d("LOCATION SETTINGS", "refreshing");
		locationManager.removeUpdates(this);
		Log.d("LOCATION SETTINGS", "updates removed");
		
		if (locationSettings.getBoolean(LocationSettings.TRACKING, LocationSettings.TRACKINGDefault)){
			Log.d("LOCATION SETTINGS", "reqistering");
			boolean gps_provider = locationSettings.getBoolean(LocationSettings.GPS, LocationSettings.GPSDefault);
			String provider = gps_provider ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
			Log.d("LOCATION SETTINGS", "got provider");
			int seconds = locationSettings.getInt(LocationSettings.SECONDS, LocationSettings.SECONDSDefault);
			int meters = locationSettings.getInt(LocationSettings.METERS, LocationSettings.METERSDefault);
			Log.d("LOCATION SETTINGS", "got ints");
			locationManager.requestLocationUpdates(provider, seconds*1000, meters, this);
		}
	}

	private void downloadNextZoomLevel() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				dismissDialog(PROGRESS_DIALOG);

				if (!msg.getData().getBoolean("success")) {
					showDialog(FAILURE_DIALOG);
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
					MapHelper.downloadNextZoomLevel(map);
					MapHelper.saveMap(map);
					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("SaveMap", e.getClass() + " " + e.getMessage());
					bundle.putBoolean("success", false);
				}
				handler.sendMessage(message);
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * 
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
		case FAILURE_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Can't connect to the server. Try again in a few minutes.")
					.setCancelable(false).setPositiveButton("OK", null);
			return builder.create();

		default:
			return null;
		}
	}

	/**
	 * If there is an internet connection downloads xml with map description and
	 * extracts restaurants from it. The information is saved for later.
	 */
	private void downloadMapInfo() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				dismissDialog(PROGRESS_DIALOG);

				if (!msg.getData().getBoolean("success")) {
					Toast.makeText(getApplicationContext(),
							"Can't download map info :(", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Map info downloaded :)", Toast.LENGTH_SHORT)
							.show();
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
	
	@Override
	public void onClick(View v) {
		Log.d("BUTTON", "any");
		switch (v.getId()) {
		case R.id.showZoomIn:
			Log.d("BUTTON", "zoom in");
			webView.zoomIn();
			break;
		
		case R.id.showZoomOut:
			Log.d("BUTTON", "zoom out");
			webView.zoomOut();
			break;

		default:
			break;
		}
	}

	@Override
	public void onStatusChanged(String provider, int status,
			Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		refreshLocationSettings();
	}

	@Override
	public void onProviderDisabled(String provider) {
		try {
			locationManager.removeUpdates(this);
		} catch (Exception e) {

		}		
	}

	@Override
	public void onLocationChanged(Location location) {
		try {
			webView.setCurrentLocation(new GPSPoint(location));
		} catch (Exception e) {

		}
	}
}
