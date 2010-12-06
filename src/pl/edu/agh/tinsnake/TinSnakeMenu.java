package pl.edu.agh.tinsnake;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class TinSnakeMenu extends Activity implements OnClickListener {
	
	private Spinner mapNameSpinner;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.findViewById(R.id.prepareButton).setOnClickListener(this);
		this.findViewById(R.id.showButton).setOnClickListener(this);
		
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator + "mapsfolder");

		mapNameSpinner = (Spinner) findViewById(R.id.chooseMapSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, dir.list());

		mapNameSpinner.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		if (v.getId() == R.id.prepareButton) {
			intent = new Intent(v.getContext(), PrepareMap.class);
		} else if (v.getId() == R.id.showButton) {
			intent = new Intent(v.getContext(), ShowMap.class);
			intent.putExtra("mapName", mapNameSpinner.getSelectedItem().toString());
		}

		startActivity(intent);
	}
}