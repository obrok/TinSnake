package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

			webView.setBoundingBox(boundingBox);
			webView.setMapUrl("file://" + base + ".jpg");

			List<GPSPoint> points = loadPoints(base);
			webView.setGPSPoints(points);

		} catch (Exception e) {
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
	}

	private List<GPSPoint> loadPoints(String base) {
		try {
			Log.d("POINT", "starting");
			File file = new File(base + ".xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
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
