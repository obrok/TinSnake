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
import java.net.URLEncoder;
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

import pl.edu.agh.tinsnake.gui.LocationSettings;
import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Collection of static helper methods for dealing with Map objects.
 */
public class MapHelper {

	public static String[] getMapNames() {
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator + "mapsfolder");

		return dir.list();
	}

	/**
	 * Safely saves the given object in the given file.
	 * 
	 * @param file
	 *            the file
	 * @param object
	 *            the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException
	 *             the file not found exception
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
	 * Gets the folder path where the map of the given name is (or will be)
	 * stored.
	 * 
	 * @param mapName
	 *            the map name
	 * @return the folder path
	 */
	private static String getFolderPath(String mapName) {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "mapsfolder" + File.separator + mapName;
	}

	/**
	 * Downloads and saves images used for displaying the given map offline
	 * (images are saved in the external storage).
	 * 
	 * @param map
	 *            the map
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void downloadMapImages(Map map, Handler handler)
			throws FileNotFoundException, IOException {
		Log.d("DOWNLOAD", "about to download");
		File dir = new File(getFolderPath(map.getName()));
		dir.mkdirs();

		Log.d("DOWNLOAD", "max zoom: " + map.getMaxZoom());

		int imagesCount = 0;

		for (int zoom = 1; zoom <= map.getMaxZoom(); zoom++) {
			imagesCount += Math.pow(Math.pow(2, zoom - 1), 2);
		}

		Log.d("DOWNLOAD", "COUNT: " + imagesCount);
		int currentImagesCount = 0;

		for (int zoom = 1; zoom <= map.getMaxZoom(); zoom++) {
			currentImagesCount = downloadZoomLevel(map, zoom, handler,
					imagesCount, currentImagesCount);
		}
	}

	public static void downloadNextZoomLevel(Map map, Handler handler)
			throws FileNotFoundException, IOException {
		int imagesCount = (int) Math.pow(Math.pow(2, map.getMaxZoom()), 2);
		downloadZoomLevel(map, map.getMaxZoom() + 1, handler, imagesCount, 0);
	}

	private static int downloadZoomLevel(Map map, int orgZoom, Handler handler,
			int imagesCount, int currentImagesCount)
			throws FileNotFoundException, IOException {
		int width = 0;
		int height = 0;

		int zoom = (int) Math.pow(2, (orgZoom - 1));

		for (int i = 0; i < zoom; i++) {
			for (int j = 0; j < zoom; j++) {
				Log.d("DOWNLOAD", "downloading map image");
				Bitmap bitmap = downloadMapImage(map, zoom, i, j);
				width += bitmap.getWidth();
				height += bitmap.getHeight();

				if (handler != null) {
					Message msg = handler.obtainMessage();
					currentImagesCount++;

					Bundle b = new Bundle();
					b.putDouble("total", (double) currentImagesCount
							/ (double) imagesCount);
					b.putBoolean("success", true);
					msg.setData(b);
					handler.sendMessage(msg);
					Log.d("DOWNLOADED", "COUNT: " + currentImagesCount);

				}

			}
		}

		width /= zoom;
		height /= zoom;

		map.setMapSize(orgZoom, width, height);
		Log.d("SaveMap", "w " + width + " height " + height);
		Log.d("SaveMap", "map " + orgZoom + " saved");

		return currentImagesCount;
	}

	/**
	 * Gets the map tile image file path using the given zoom and tile
	 * coordinates.
	 * 
	 * @param map
	 *            the map
	 * @param zoom
	 *            the current zoom
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @return the map image file path
	 */
	public static String getMapImageFilePath(Map map, int zoom, int i, int j) {
		String fileName = String.format("%s_zoom%d_img%d_%d.jpg",
				map.getName(), zoom, i, j);
		return getFolderPath(map.getName()) + File.separator + fileName;
	}

	/** The bitmap to save. */
	private static Bitmap toSave;

	/**
	 * Downloads the tile image whose position is defined by i and j
	 * coordinates.
	 * 
	 * @param map
	 *            the map
	 * @param zoom
	 *            the zoom
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @return the bitmap
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static Bitmap downloadMapImage(final Map map, final int zoom,
			final int i, final int j) throws IOException {
		String filepath = getMapImageFilePath(map, zoom, i, j);
		Log.d("DOWNLOAD", filepath);

		final File file = new File(filepath);

		if (!(file.exists() && file.length() > 0)) {
			StreamUtil.safelyAcccess(new FileOutputStream(file),
					new CloseableUser() {
						@Override
						public void performAction(Closeable stream)
								throws IOException {

							try {
								BoundingBox current = map.getBoundingBox()
										.getSubBoundingBox(zoom, i, j);
								toSave = downloadBitmap(current
										.toOSMString(1000));

								toSave.compress(Bitmap.CompressFormat.JPEG, 90,
										(OutputStream) stream);
							} catch (FileNotFoundException e) {
								file.delete();
								throw e;
							}
						}
					});
		} else {
			Log.d("DOWNLOAD", "skipping already downloaded file");
			toSave = BitmapFactory.decodeFile(file.getAbsolutePath());
		}
		return toSave;
	}

	/**
	 * Serializes the given map object.
	 * 
	 * @param map
	 *            the map
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void saveMap(Map map) throws FileNotFoundException,
			IOException {
		File file = new File(getFolderPath(map.getName()) + File.separator
				+ map.getName() + ".dat");
		file.delete();
		saveObject(file, map);
	}

	/** The result. */
	private static Map result;

	/**
	 * Deserializes the map object of the given name.
	 * 
	 * @param mapName
	 *            the map name
	 * @return the map
	 * @throws StreamCorruptedException
	 *             the stream corrupted exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
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
	 * Downloads XML describing the given bounding box and returns it as the
	 * input stream.
	 * 
	 * @param boundingBox
	 *            the bounding box
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
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
	 * Parses the given XML input stream and returns the list of GPSPoints.
	 * 
	 * @param is
	 *            the input stream
	 * @return the list
	 */
	private static List<MapPoint> loadPoints(InputStream is) {
		try {
			Log.d("POINT", "starting");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			NodeList nodeList = doc.getElementsByTagName("tag");

			List<MapPoint> result = new ArrayList<MapPoint>();

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
					result.add(new MapPoint(lat, lon, "restaurant"));
					Log.d("POINT", "parsed");
				}
			}
			return result;
		} catch (Exception e) {
			Log.e("SHOW EXCEPTION", e.getClass().getCanonicalName() + " "
					+ e.getMessage());
		}
		return new ArrayList<MapPoint>();
	}

	/**
	 * Downloads information about the given map (set of points located on the
	 * map).
	 * 
	 * @param map
	 *            the map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void downloadMapInfo(Map map) throws IOException {
		InputStream is = downloadXML(map.getBoundingBox());
		List<MapPoint> points = loadPoints(is);
		map.setPoints(points);
		saveMap(map);
	}

	/**
	 * Downloads bitmap from the given URL.
	 * 
	 * @param stringUrl
	 *            the string URL
	 * @return the bitmap
	 * @throws MalformedURLException
	 *             the malformed url exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
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
	 * Uses OSM API to find the location of the given location (address, name of
	 * the city, country etc.).
	 * 
	 * @param location
	 *            the location
	 * @param width
	 *            the width
	 * @return the earth coordinates
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws FactoryConfigurationError
	 *             the factory configuration error
	 * @throws SAXException
	 *             the sAX exception
	 */
	public static EarthCoordinates searchLocation(String location)
			throws IOException, ParserConfigurationException,
			FactoryConfigurationError, SAXException {

		Log.d("SEARCH", "entering");

		HttpURLConnection connection = null;
		URL url = new URL(String.format(
				"http://nominatim.openstreetmap.org/search?q=%s&format=xml",
				URLEncoder.encode(location)));

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

	public static void deleteMap(String mapName) {
		deleteDirectory(new File(getFolderPath(mapName)));
	}

	private static void deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		path.delete();
	}

	public static boolean mapExists(String mapName) {
		File file = new File(getFolderPath(mapName));
		return file.exists();
	}

	public static void setLastMap(String name, Context c) {
		SharedPreferences settings = c.getSharedPreferences(
				LocationSettings.SETTINGS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastMap", name);

		editor.commit();

	}

	public static void saveExport(final Bitmap export, Map map) throws FileNotFoundException, IOException {
		final File file = new File(getFolderPath(map.getName()) + File.separator + "export.jpg");

		StreamUtil.safelyAcccess(new FileOutputStream(file),
				new CloseableUser() {
					@Override
					public void performAction(Closeable stream)
							throws IOException {
						export.compress(Bitmap.CompressFormat.JPEG, 90,
								(OutputStream) stream);
					}
				});
		
	}
}
