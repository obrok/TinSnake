package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class MapHelper {

	private static void saveObject(File file, final Object object)
			throws IOException, FileNotFoundException {
		StreamUtil.safelyAcccess(new ObjectOutputStream(new FileOutputStream(
				file)), new CloseableUser() {
			@Override
			public void performAction(Closeable stream) throws IOException {
				((ObjectOutputStream) stream).writeObject(object);
			}
		});
	}

	private static String getFolderPath(String mapName) {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "mapsfolder" + File.separator + mapName;
	}

	public static void downloadMapImages(Map map) throws FileNotFoundException,
			IOException {
		Log.d("DOWNLOAD", "about to download");
		File dir = new File(getFolderPath(map.getName()));
		dir.mkdirs();

		Log.d("DOWNLOAD", "max zoom: " + map.getMaxZoom());

		for (int zoom = 1; zoom <= map.getMaxZoom(); zoom++) {
			for (int i = 0; i < zoom; i++) {
				for (int j = 0; j < zoom; j++) {
					Log.d("DOWNLOAD", "downloading map image");
					downloadMapImage(map, zoom, i, j);
				}
			}
			Log.d("SaveMap", "map " + zoom + " saved");
		}
	}

	public static String getMapImageFilePath(Map map, int zoom, int i, int j) {
		String fileName = String.format("%s_zoom%d_img%d_%d.jpg",
				map.getName(), zoom, i, j);
		return getFolderPath(map.getName()) + File.separator + fileName;
	}

	private static void downloadMapImage(final Map map, final int zoom,
			final int i, final int j) throws FileNotFoundException, IOException {
		String filepath = getMapImageFilePath(map, zoom, i, j);
		Log.d("DOWNLOAD", filepath);
		File file = new File(filepath);

		StreamUtil.safelyAcccess(new FileOutputStream(file),
				new CloseableUser() {
					@Override
					public void performAction(Closeable stream)
							throws IOException {
						BoundingBox current = map.getBoundingBox()
								.getSubBoundingBox(zoom, i, j);
						Bitmap toSave = downloadBitmap(current
								.toOSMString(1000));
						toSave.compress(Bitmap.CompressFormat.JPEG, 90,
								(OutputStream) stream);
					}
				});
	}

	public static void saveMap(Map map) throws FileNotFoundException,
			IOException {
		File file = new File(getFolderPath(map.getName()) + File.separator
				+ map.getName() + ".dat");
		saveObject(file, map);
	}

	private static Map result;

	public static Map loadMap(String mapName) throws StreamCorruptedException,
			FileNotFoundException, IOException {
		String filepath = getFolderPath(mapName) + File.separator + mapName
				+ ".dat";

		StreamUtil.safelyAcccess(new ObjectInputStream(new FileInputStream(
				filepath)), new CloseableUser() {
			@Override
			public void performAction(Closeable stream) throws IOException {
				try {
					result = (Map) ((ObjectInputStream) stream).readObject();
				} catch (ClassNotFoundException e) {
				}
			}
		});

		return result;
	}

	private static InputStream downloadXML(BoundingBox boundingBox)
			throws IOException {
		HttpURLConnection conn;
		URL url = new URL(boundingBox.toXMLString());

		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();

		return is;
	}

	private static List<GPSPoint> loadPoints(InputStream is) {
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

	public static void downloadMapInfo(Map map) throws IOException {
		InputStream is = downloadXML(map.getBoundingBox());
		List<GPSPoint> points = loadPoints(is);
		map.setPoints(points);
		saveMap(map);
	}

	public static Bitmap downloadBitmap(String stringUrl)
			throws MalformedURLException, IOException {
		Log.d("DOWNLOAD BITMAP", stringUrl);
		HttpURLConnection conn;
		URL url = new URL(stringUrl);

		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();
		Bitmap b = BitmapFactory.decodeStream(is);
		return b;
	}

	public static EarthCoordinates searchLocation(String location, int width)
			throws IOException, ParserConfigurationException,
			FactoryConfigurationError, SAXException {
		HttpURLConnection connection = null;
		URL url = new URL(String.format(
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

		String lat = map.getNamedItem("lat").getNodeValue().toString();
		String lon = map.getNamedItem("lon").getNodeValue().toString();

		return new EarthCoordinates(Double.parseDouble(lat), Double
				.parseDouble(lon), width, 10);
	}
}
