package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

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
				.getDefaultDisplay().getWidth());
		this.findViewById(R.id.map).setClickable(true);
		this.findViewById(R.id.map).setOnTouchListener(this);
		this.findViewById(R.id.map).setOnClickListener(this);
		this.findViewById(R.id.zoomOutButton).setOnClickListener(this);
		this.findViewById(R.id.saveButton).setOnClickListener(this);
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
			StreamUtil.safelyAcccess(new OutputStreamWriter(new FileOutputStream(file)), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					((Writer)stream).write(coordinates.toString());					
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
