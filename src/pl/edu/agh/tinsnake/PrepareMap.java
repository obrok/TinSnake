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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
			progressDialog.setMessage("Loading...");
			return progressDialog;
		case FAILURE_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Can't connect to the server. Try again in a few minutes.").setCancelable(false)
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

	private InputStream downloadXML(BoundingBox boundingBox)
			throws IOException {
		HttpURLConnection conn;
		URL url = new URL(boundingBox.toXMLString());

		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		return is;
	}
	
	private List<GPSPoint> loadPoints(InputStream is) {
		try {
			Log.d("POINT", "starting");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			NodeList nodeList = doc.getElementsByTagName("tag");

			List<GPSPoint> result = new ArrayList<GPSPoint>();

			int i = 0;
			Log.d("POINT", "length " + nodeList.getLength());
			while (i < nodeList.getLength()) {
				Node node = nodeList.item(i++);

				NamedNodeMap childAttr = node.getAttributes();
				Node key = childAttr.getNamedItem("k");
				Node value = childAttr.getNamedItem("v");

				if (key == null || value == null)
					continue;

				Node parent = node.getParentNode();

				if (!parent.getNodeName().equals("node"))
					continue;

				NamedNodeMap attr = parent.getAttributes();
				Node latNode = attr.getNamedItem("lat");
				Node lonNode = attr.getNamedItem("lon");

				if (latNode == null || lonNode == null)
					continue;

				double lat = Double.parseDouble(latNode.getNodeValue());
				double lon = Double.parseDouble(lonNode.getNodeValue());

				if (key.getNodeValue().equals("amenity")
						&& value.getNodeValue().equals("restaurant")) {
					result.add(new GPSPoint(lat, lon, "",
							GPSPointClass.Restaurant));
					Log.d("POINT", "parsed");
				}
			}
			return result;
		} catch (Exception e) {
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
		return new ArrayList<GPSPoint>();
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
							+ ".info");
					List<GPSPoint> points;
					try{
						Log.d("INFO", "downloading XML");
						InputStream toSave = downloadXML(coordinates.toBoundingBox());
						Log.d("INFO", "loading GPS points");
						points = loadPoints(toSave);
					}
					catch (Exception e){
						points = new ArrayList<GPSPoint>();
					}
					
					final List<GPSPoint> finalPoints = points;
					
					Log.d("INFO", "saving GPS points");
					
					StreamUtil.safelyAcccess(new ObjectOutputStream(
							new FileOutputStream(file)), new CloseableUser() {
						@Override
						public void performAction(Closeable stream)
								throws IOException {
							((ObjectOutputStream) stream)
									.writeObject(finalPoints);
						}
					});
					
					Log.d("INFO", "saved");

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
				HttpURLConnection connection = null;
				try {
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

					NamedNodeMap map = n.getAttributes();

					String lat = map.getNamedItem("lat").getNodeValue()
							.toString();
					String lon = map.getNamedItem("lon").getNodeValue()
							.toString();

					coordinates = new EarthCoordinates(Double.parseDouble(lat),
							Double.parseDouble(lon), getWindowManager()
									.getDefaultDisplay().getWidth(), 10);

					bundle.putBoolean("success", true);
				} catch (Exception e) {
					Log.e("Search ", e.getClass() + " " + e.getMessage());
					bundle.putBoolean("success", false);
				} finally {
					try {
						// conn.disconnect();
					} catch (Exception e) {
						// ignore
					}
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
