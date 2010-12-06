package pl.edu.agh.tinsnake;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowMap extends Activity {

	private Bitmap bitmap;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);

		Intent intent = getIntent();

		String mapName = intent.getStringExtra("mapName");

		((TextView) this.findViewById(R.id.showDebug)).setText(mapName);

		bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator + "mapsfolder" + File.separator + mapName + File.separator + mapName + ".jpg");		

		((ImageView) this.findViewById(R.id.showMap)).setImageBitmap(bitmap);
	}
}
