package pl.edu.agh.tinsnake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
					+ File.separator + "mapsfolder");
			dir.mkdirs();

			File file = new File(dir.getPath() + File.separator + name + ".jpg");

			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
