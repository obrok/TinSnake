package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

public class PrepareMap extends Activity implements OnTouchListener,
		android.content.DialogInterface.OnClickListener {
	private static final int MAP_NAME_DIALOG = 0;
	private static final int SEARCH_LOCATION_DIALOG = 1;
	private static final int PROGRESS_DIALOG = 2;
	private static final int FAILURE_DIALOG = 3;

	private EarthCoordinates coordinates;

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
			progressDialog.setMessage("Downloading...");
			return progressDialog;
		case FAILURE_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Critical server failure.").setCancelable(false)
					.setPositiveButton("OK", null);
			return builder.create();

		default:
			return null;
		}
	}

	private Bitmap bitmap;

	private int currentDialog;

	private void refreshMap() {
		try {
			bitmap = downloadBitmap(coordinates.toOSMString());
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

	private Bitmap downloadBitmap(String stringUrl)
			throws MalformedURLException, IOException {
		HttpURLConnection conn;
		URL url = new URL(stringUrl);

		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();
		Bitmap b = BitmapFactory.decodeStream(is);
		return b;
	}

	private String downloadXML(BoundingBox boundingBox, File file)
			throws IOException {
		HttpURLConnection conn;
		URL url = new URL(boundingBox.toXMLString());

		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		OutputStream out = new FileOutputStream(file);

		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0)
			out.write(buf, 0, len);
		out.close();
		is.close();

		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		return writer.toString();
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
			refreshMap();
			return true;
		case R.id.zoomOutMenuItem:
			coordinates.zoomOut();

			Toast.makeText(getApplicationContext(),
					coordinates.getLat() + " " + coordinates.getLon(),
					Toast.LENGTH_SHORT).show();

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
				Message message = new Message();
				Bundle bundle = new Bundle();
				message.setData(bundle);
				try {
					File dir = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ File.separator
							+ "mapsfolder"
							+ File.separator
							+ name);
					dir.mkdirs();

					File file = new File(dir.getPath() + File.separator + name
							+ ".xml");

					String toSave = downloadXML(coordinates.toBoundingBox(), file);
					Log.d("XML", toSave);

					Log.d("XML", "saved");					

					file = new File(dir.getPath() + File.separator + name
							+ ".jpg");
					StreamUtil.safelyAcccess(new FileOutputStream(file),
							new CloseableUser() {
								@Override
								public void performAction(Closeable stream)
										throws IOException {
									Bitmap toSave = downloadBitmap(coordinates
											.toBoundingBox().toOSMString(1000));
									toSave.compress(Bitmap.CompressFormat.JPEG,
											90, (OutputStream) stream);
								}
							});

					file = new File(dir.getPath() + File.separator + name
							+ ".txt");

					StreamUtil.safelyAcccess(new ObjectOutputStream(
							new FileOutputStream(file)), new CloseableUser() {
						@Override
						public void performAction(Closeable stream)
								throws IOException {
							((ObjectOutputStream) stream)
									.writeObject(coordinates.toBoundingBox());
						}
					});
					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("SaveMap", e.getClass() + " " + e.getMessage());
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
