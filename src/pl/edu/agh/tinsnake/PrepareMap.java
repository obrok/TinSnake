package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PrepareMap extends Activity implements OnTouchListener,
		OnClickListener, android.content.DialogInterface.OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prepare);

		coordinates = new EarthCoordinates(0, 0, this.getWindowManager()
				.getDefaultDisplay().getWidth() / 2, 1);
		this.findViewById(R.id.map).setClickable(true);
		this.findViewById(R.id.map).setOnTouchListener(this);
		this.findViewById(R.id.map).setOnClickListener(this);
		this.findViewById(R.id.zoomOutButton).setOnClickListener(this);
		this.findViewById(R.id.saveButton).setOnClickListener(this);
		this.findViewById(R.id.searchLocationButton).setOnClickListener(this);
		refreshMap();
	}

	private EarthCoordinates coordinates;
	private float lastX, lastY;
	private Bitmap bitmap;

	private void refreshMap() {
		HttpURLConnection conn = null;

		try {
			URL url = new URL(coordinates.toOSMString());
			((TextView) this.findViewById(R.id.prepareDebug)).append(url
					.toString());
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
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
		} else if (v.getId() == R.id.zoomOutButton) {
			coordinates.zoomOut();
			refreshMap();
		} else if (v.getId() == R.id.saveButton) {
			showInputDialog();
		} else if (v.getId() == R.id.searchLocationButton){
			searchLocation();
			refreshMap();
		}
	}

	private void searchLocation() {
		HttpURLConnection conn = null;

		try {
			String location = ((EditText) this.findViewById(R.id.searchLocationEditText)).getText().toString();
			((TextView) this.findViewById(R.id.prepareDebug)).append(location);
			URL url = new URL(String.format("http://nominatim.openstreetmap.org/search?q=%s&format=xml", location));
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			((TextView) this.findViewById(R.id.prepareDebug)).append("conn ");
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			InputStream is = conn.getInputStream();
			Document d = db.parse(is);
			
			((TextView) this.findViewById(R.id.prepareDebug)).append("parsed ");
			
			Node n = d.getElementsByTagName("place").item(0);
			
			((TextView) this.findViewById(R.id.prepareDebug)).append("node ");
			
			((TextView) this.findViewById(R.id.prepareDebug)).append(n.getNodeName());
			
			NamedNodeMap map = n.getAttributes();
			
			((TextView) this.findViewById(R.id.prepareDebug)).append("attr ");

			String lat = map.getNamedItem("lat").getNodeValue().toString();
			String lon = map.getNamedItem("lon").getNodeValue().toString();
			
			coordinates = new EarthCoordinates(Double.parseDouble(lat), Double.parseDouble(lon), this.getWindowManager()
					.getDefaultDisplay().getWidth() / 2, 10);
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.prepareDebug)).append(e
					.getClass().getCanonicalName() + " " + e.getMessage());
		} finally {
			try {
				// conn.disconnect();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private EditText input;

	private void showInputDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Input map name");

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
			StreamUtil.safelyAcccess(new FileOutputStream(file), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, (OutputStream)stream);
				}
			});

			file = new File(dir.getPath() + File.separator + name + ".txt");
			StreamUtil.safelyAcccess(new ObjectOutputStream(new FileOutputStream(file)), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					((ObjectOutputStream)stream).writeObject(coordinates);					
				}
			});
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.prepareDebug)).setText(e
					.getClass().getCanonicalName()
					+ " " + e.getMessage());
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		saveMap(input.getText().toString());
	}
}
