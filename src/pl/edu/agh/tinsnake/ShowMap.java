package pl.edu.agh.tinsnake;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import pl.edu.agh.tinsnake.util.CloseableUser;
import pl.edu.agh.tinsnake.util.StreamUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowMap extends Activity {
	private EarthCoordinates coordinates;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);
		try {
			Intent intent = getIntent();

			String mapName = intent.getStringExtra("mapName");

			((TextView) this.findViewById(R.id.showDebug)).setText(mapName);

			String base = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "mapsfolder"
					+ File.separator
					+ mapName
					+ File.separator + mapName;
			Bitmap bitmap = BitmapFactory.decodeFile(base + ".jpg");

			((ImageView) this.findViewById(R.id.showMap))
					.setImageBitmap(bitmap);

			StreamUtil.safelyAcccess(new ObjectInputStream(
					new FileInputStream(base + ".txt")), new CloseableUser() {
				@Override
				public void performAction(Closeable stream) throws IOException {
					try {
						coordinates = (EarthCoordinates)((ObjectInputStream)stream).readObject();
					} catch (ClassNotFoundException e) {
						coordinates = new EarthCoordinates(0, 0, 0);
					}
				}
			});
			((TextView) this.findViewById(R.id.showDebug))
					.setText(coordinates.toOSMString());
		} catch (Exception e) {
			((TextView) this.findViewById(R.id.showDebug)).setText(e
					.getClass().getCanonicalName()
					+ " " + e.getMessage());
			// TODO do something with exceptions
		}
	}
}
