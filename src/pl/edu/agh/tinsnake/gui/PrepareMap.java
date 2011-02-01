package pl.edu.agh.tinsnake.gui;

import pl.edu.agh.tinsnake.EarthCoordinates;
import pl.edu.agh.tinsnake.Map;
import pl.edu.agh.tinsnake.MapHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * An activity used to prepare and save map.
 */
public class PrepareMap extends Activity implements OnTouchListener,
		android.content.DialogInterface.OnClickListener {
	
	/** The Constant MAP_NAME_DIALOG. */
	private static final int MAP_NAME_DIALOG = 0;
	
	/** The Constant SEARCH_LOCATION_DIALOG. */
	private static final int SEARCH_LOCATION_DIALOG = 1;
	
	/** The Constant PROGRESS_DIALOG. */
	private static final int PROGRESS_DIALOG = 2;
	
	/** The Constant FAILURE_DIALOG. */
	private static final int FAILURE_DIALOG = 3;

	/** The coordinates of the currently displayed map. */
	private EarthCoordinates coordinates;
	
	/** The bitmap containing the currently displayed map. */
	private Bitmap bitmap;
	
	/** The current dialog. */
	private int currentDialog;
	
	/** The current zoom level */
	private int zoom = 2;

	/**
	 * Called when the activity is first created.
	 *
	 * @param savedInstanceState the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prepare);

		coordinates = new EarthCoordinates(0, 0);
		this.findViewById(R.id.map).setClickable(true);
		this.findViewById(R.id.map).setOnTouchListener(this);

		refreshMap();
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
	 * Refreshes map - downloads the new image of the map using the updated coordinates.
	 */
	private void refreshMap() {
		try {
			bitmap = MapHelper.downloadBitmap(coordinates.toOSMString(this.getWindowManager()
					.getDefaultDisplay().getWidth(), zoom));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Can't refresh map :(",
					Toast.LENGTH_SHORT).show();
			Log.e("SaveMap", e.getClass() + " " + e.getMessage());
		}

		((ImageView) this.findViewById(R.id.map)).setImageBitmap(bitmap);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		return true;
	};

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.searchLocationMenuItem:
			showInputDialog("Input location", SEARCH_LOCATION_DIALOG);
			return true;
		case R.id.saveMapMenuItem:
			showInputDialog("Input map name", MAP_NAME_DIALOG);
			return true;

		case R.id.zoomInMenuItem:
			zoom++;
			refreshMap();
			return true;
		case R.id.zoomOutMenuItem:
			zoom--;
			refreshMap();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** The input. */
	private EditText input;

	private boolean foundLocation;

	/**
	 * Shows input dialog.
	 *
	 * @param title the title
	 * @param currentDialog the current dialog
	 */
	private void showInputDialog(String title, int currentDialog) {
		this.currentDialog = currentDialog;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);

		// Set an EditText view to get user input
		input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Ok", this);
		alert.show();
	}

	/**
	 * Saves map of the currently displayed region using the given map name.
	 *
	 * @param name the name
	 */
	private void saveMap(final String name) {
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
				Log.d("SaveMap", "about to save");
				Map map = new Map(name, coordinates.toBoundingBox(zoom), 2);
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				message.setData(bundle);
				try {
					MapHelper.downloadMapImages(map);
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

	/**
	 * Searches the given location and refreshes map if the given location has been found.
	 *
	 * @param location the location
	 */
	private void searchLocation(final String location) {
		foundLocation = true;
		
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				refreshMap();

				if (!msg.getData().getBoolean("success")) {
					showDialog(FAILURE_DIALOG);
					foundLocation = false;
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
					coordinates = MapHelper.searchLocation(location);
					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("Search ", e.getClass() + " " + e.getMessage());
					bundle.putBoolean("success", false);
				}
				handler.sendMessage(message);
			}
		}).start();
	}

	/** Used for determining if user scrolled or zoom the map. */
	float scrollStartX, scrollStartY;

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			scrollStartX = event.getX();
			scrollStartY = event.getY();
			break;

		case MotionEvent.ACTION_UP:

			float deltaX = scrollStartX - event.getX();
			float deltaY = event.getY() - scrollStartY;

			coordinates.moveCenter(deltaX, deltaY, this.getWindowManager()
					.getDefaultDisplay().getWidth(), zoom);

			if (Math.abs(deltaX) + Math.abs(deltaY) < 10) {
				coordinates.zoomIn(event.getX(), event.getY(), this.getWindowManager()
						.getDefaultDisplay().getWidth(), zoom);
				zoom++;
			}
			refreshMap();

			break;

		default:
			break;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (currentDialog) {
		case MAP_NAME_DIALOG:
			saveMap(input.getText().toString());
			break;
		case SEARCH_LOCATION_DIALOG:
			searchLocation(input.getText().toString());
			
			if (foundLocation){
				zoom = 10;
				refreshMap();
			}
			
			
			break;
		default:
			break;
		}

	}
}
