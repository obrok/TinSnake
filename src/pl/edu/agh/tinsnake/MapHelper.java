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

// TODO: Auto-generated Javadoc
/**
 * The Class MapHelper.
 */
public class MapHelper {

	/**
	 * Save object.
	 *
	 * @param file the file
	 * @param object the object
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 */
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

	/**
	 * Gets the folder path.
	 *
	 * @param mapName the map name
	 * @return the folder path
	 */
	private static String getFolderPath(String mapName) {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "mapsfolder" + File.separator + mapName;
	}

	/**
	 * Download map images.
	 *
	 * @param map the map
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void downloadMapImages(Map map) throws FileNotFoundException,
			IOException {
		Log.d("DOWNLOAD", "about to download");
		File dir = new File(getFolderPath(map.getName()));
		dir.mkdirs();

		Log.d("DOWNLOAD", "max zoom: " + map.getMaxZoom());

		for (int zoom = 1; zoom <= map.getMaxZoom(); zoom++) {
			int width = 0;
			int height = 0;
			
			for (int i = 0; i < zoom; i++) {
				for (int j = 0; j < zoom; j++) {
					Log.d("DOWNLOAD", "downloading map image");
					Bitmap bitmap = downloadMapImage(map, zoom, i, j);
					width += bitmap.getWidth();
					height += bitmap.getHeight();
				}
			}
			width /= zoom;
			height /= zoom;
			map.setMapSize(width, height);
			Log.d("SaveMap", "w " + width + " height " + height);
			Log.d("SaveMap", "map " + zoom + " saved");
		}
	}

	/**
	 * Gets the map image file path.
	 *
	 * @param map the map
	 * @param zoom the zoom
	 * @param i the i
	 * @param j the j
	 * @return the map image file path
	 */
	public static String getMapImageFilePath(Map map, int zoom, int i, int j) {
		String fileName = String.format("%s_zoom%d_img%d_%d.jpg",
				map.getName(), zoom, i, j);
		return getFolderPath(map.getName()) + File.separator + fileName;
	}

	/** The to save. */
	private static Bitmap toSave;

	/**
	 * Download map image.
	 *
	 * @param map the map
	 * @param zoom the zoom
	 * @param i the i
	 * @param j the j
	 * @return the bitmap
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static Bitmap downloadMapImage(final Map map, final int zoom,
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
						toSave = downloadBitmap(current.toOSMString(1000));

						toSave.compress(Bitmap.CompressFormat.JPEG, 90,
								(OutputStream) stream);
					}
				});
		return toSave;
	}

	/**
	 * Save map.
	 *
	 * @param map the map
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void saveMap(Map map) throws FileNotFoundException,
			IOException {
		File file = new File(getFolderPath(map.getName()) + File.separator
				+ map.getName() + ".dat");
		saveObject(file, map);
	}

	/** The result. */
	private static Map result;

	/**
	 * Load map.
	 *
	 * @param mapName the map name
	 * @return the map
	 * @throws StreamCorruptedException the stream corrupted exception
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Downloads XML describing the given bounding box and returns it as the input stream.
	 *
	 * @param boundingBox the bounding box
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Parses the given XML input stream and returns the list of PGSPoints.
	 *
	 * @param is the input stream
	 * @return the list
	 */
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

	/**
	 * Downloads information about the given map (set of points located on the map).
	 *
	 * @param map the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void downloadMapInfo(Map map) throws IOException {
		InputStream is = downloadXML(map.getBoundingBox());
		List<GPSPoint> points = loadPoints(is);
		map.setPoints(points);
		saveMap(map);
	}

	/**
	 * Downloads bitmap from the given URL.
	 *
	 * @param stringUrl the string url
	 * @return the bitmap
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Search location.
	 *
	 * @param location the location
	 * @param width the width
	 * @return the earth coordinates
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws FactoryConfigurationError the factory configuration error
	 * @throws SAXException the sAX exception
	 */
	public static EarthCoordinates searchLocation(String location)
			throws IOException, ParserConfigurationException,
			FactoryConfigurationError, SAXException {
		
		Log.d("SEARCH", "entering");
		
		HttpURLConnection connection = null;
		URL url = new URL(String.format(
				"http://nominatim.openstreetmap.org/search?q=%s&format=xml",
				location));
		
		Log.d("SEARCH", url.toString());

		connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.connect();

		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		InputStream is = connection.getInputStream();
		Document d = db.parse(is);
		
		Log.d("SEARCH", "parsed");

		Node n = d.getElementsByTagName("place").item(0);

		NamedNodeMap map = n.getAttributes();

		String lat = map.getNamedItem("lat").getNodeValue().toString();
		String lon = map.getNamedItem("lon").getNodeValue().toString();

		return new EarthCoordinates(Double.parseDouble(lat), Double
				.parseDouble(lon));
	}
}
