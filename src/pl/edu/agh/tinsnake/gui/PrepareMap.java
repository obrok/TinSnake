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

public class PrepareMap extends Activity implements OnTouchListener,
		android.content.DialogInterface.OnClickListener {
	private static final int MAP_NAME_DIALOG = 0;
	private static final int SEARCH_LOCATION_DIALOG = 1;
	private static final int PROGRESS_DIALOG = 2;
	private static final int FAILURE_DIALOG = 3;

	private EarthCoordinates coordinates;
	private Bitmap bitmap;
	private int currentDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prepare);

		coordinates = new EarthCoordinates(0, 0, this.getWindowManager()
				.getDefaultDisplay().getWidth(), 2);
		this.findViewById(R.id.map).setClickable(true);
		this.findViewById(R.id.map).setOnTouchListener(this);

		refreshMap();
	}

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

	private void refreshMap() {
		try {
			bitmap = MapHelper.downloadBitmap(coordinates.toOSMString());
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Can't refresh map :(",
					Toast.LENGTH_SHORT).show();
			Log.e("SaveMap", e.getClass() + " " + e.getMessage());
		}

		((ImageView) this.findViewById(R.id.map)).setImageBitmap(bitmap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		return true;
	};

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
			coordinates.zoomIn();
			refreshMap();
			return true;
		case R.id.zoomOutMenuItem:
			coordinates.zoomOut();

			refreshMap();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private EditText input;

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
				Map map = new Map(name, coordinates.toBoundingBox(), 3);
				
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

	private void searchLocation(final String location) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				refreshMap();

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
					coordinates = MapHelper.searchLocation(location, getWindowManager()
							.getDefaultDisplay().getWidth());
					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("Search ", e.getClass() + " " + e.getMessage());
					bundle.putBoolean("success", false);
				}
				handler.sendMessage(message);
			}
		}).start();
	}

	float scrollStartX, scrollStartY;

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

			coordinates.moveCenter(deltaX, deltaY);

			if (Math.abs(deltaX) + Math.abs(deltaY) < 10) {
				coordinates.zoomIn(event.getX(), event.getY());
			}
			refreshMap();

			break;

		default:
			break;
		}

		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (currentDialog) {
		case MAP_NAME_DIALOG:
			saveMap(input.getText().toString());
			break;
		case SEARCH_LOCATION_DIALOG:
			searchLocation(input.getText().toString());
			refreshMap();
			break;
		default:
			break;
		}

	}
}
