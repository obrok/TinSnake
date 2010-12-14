package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PrepareMap extends Activity implements OnTouchListener,
OnClickListener, android.content.DialogInterface.OnClickListener {
	private static final int MAP_NAME_DIALOG = 0;
	private static final int SEARCH_LOCATION_DIALOG = 1;
	
	private EarthCoordinates coordinates;
	private float lastX, lastY;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prepare);

		coordinates = new EarthCoordinates(0, 0, this.getWindowManager()
				.getDefaultDisplay().getWidth(), 2);
		this.findViewById(R.id.map).setClickable(true);
		this.findViewById(R.id.map).setOnTouchListener(this);
		this.findViewById(R.id.map).setOnClickListener(this);
		
		refreshMap();
	}

	private Bitmap bitmap;

	private int currentDialog;

	private void refreshMap() {
		try {
			bitmap = downloadBitmap(1);
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.prepareDebug)).append(e
					.getClass().getCanonicalName());
		} finally {
			try {
				// conn.disconnect();
			} catch (Exception e) {
				// ignore
			}
		}

		((ImageView) this.findViewById(R.id.map)).setImageBitmap(bitmap);
	}

	private Bitmap downloadBitmap(int multiplier) throws MalformedURLException, IOException {
		HttpURLConnection conn;
		URL url = new URL(coordinates.toOSMString(multiplier));
		((TextView) this.findViewById(R.id.prepareDebug)).append(url
				.toString());
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();
		Bitmap b = BitmapFactory.decodeStream(is);
		return b;
	}

	private void searchLocation(String location) {
		HttpURLConnection connection = null;

		try {
			((TextView) this.findViewById(R.id.prepareDebug)).append(location);
			URL url = new URL(
					String
							.format(
									"http://nominatim.openstreetmap.org/search?q=%s&format=xml",
									location));

			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			InputStream is = connection.getInputStream();
			Document d = db.parse(is);

			Node n = d.getElementsByTagName("place").item(0);

			((TextView) this.findViewById(R.id.prepareDebug)).append("node ");

			((TextView) this.findViewById(R.id.prepareDebug)).append(n
					.getNodeName());

			NamedNodeMap map = n.getAttributes();

			((TextView) this.findViewById(R.id.prepareDebug)).append("attr ");

			String lat = map.getNamedItem("lat").getNodeValue().toString();
			String lon = map.getNamedItem("lon").getNodeValue().toString();

			coordinates = new EarthCoordinates(Double.parseDouble(lat), Double
					.parseDouble(lon), this.getWindowManager()
					.getDefaultDisplay().getWidth(), 10);
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.prepareDebug)).append(e
					.getClass().getCanonicalName()
					+ " " + e.getMessage());
		} finally {
			try {
				// conn.disconnect();
			} catch (Exception e) {
				// ignore
			}
		}
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
			
			Toast.makeText(getApplicationContext(), "deltaX: " + coordinates.deltaX + " lon: " + coordinates.getLon(), Toast.LENGTH_LONG).show();
			
			refreshMap();
			return true;
		
		case R.id.zoomOutMenuItem:
			coordinates.zoomOut();
			
			Toast.makeText(getApplicationContext(), coordinates.getLat() + " " + coordinates.getLon(), Toast.LENGTH_SHORT).show();
			
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

	private void saveMap(String name) {
		try {		
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator + "mapsfolder" + File.separator + name);
			dir.mkdirs();

			File file = new File(dir.getPath() + File.separator + name + ".jpg");
			StreamUtil.safelyAcccess(new FileOutputStream(file),
					new CloseableUser() {
						@Override
						public void performAction(Closeable stream)
								throws IOException {
							Bitmap toSave = downloadBitmap(3);
							toSave.compress(Bitmap.CompressFormat.JPEG, 90,
									(OutputStream) stream);
						}
					});

			file = new File(dir.getPath() + File.separator + name + ".txt");
			StreamUtil.safelyAcccess(new ObjectOutputStream(
					new FileOutputStream(file)), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					((ObjectOutputStream) stream).writeObject(coordinates);
				}
			});
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.prepareDebug)).setText(e
					.getClass().getCanonicalName()
					+ " " + e.getMessage());
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		lastX = event.getX();
		lastY = event.getY();
		((TextView) this.findViewById(R.id.prepareDebug)).setText(event.getX()
				+ "\n" + event.getY());
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.map) {
			coordinates.zoomIn(lastX, lastY);
			refreshMap();
		}
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
