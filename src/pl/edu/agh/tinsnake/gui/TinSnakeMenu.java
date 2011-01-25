package pl.edu.agh.tinsnake.gui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class TinSnakeMenu extends Activity implements OnClickListener {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.findViewById(R.id.prepareButton).setOnClickListener(this);
		this.findViewById(R.id.showButton).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		if (v.getId() == R.id.prepareButton) {
			intent = new Intent(v.getContext(), PrepareMap.class);
		} else if (v.getId() == R.id.showButton) {
			
	        File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator + "mapsfolder");
			
			final String[] items = dir.list();
			
			if (items.length == 0){
				Toast.makeText(getApplicationContext(), "No maps :(", Toast.LENGTH_SHORT).show();
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick a map");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			        String chosen = items[item].toString();
			        
			        Intent i = new Intent(getBaseContext(), ShowMap.class);
			        i.putExtra("mapName", chosen);
			        startActivity(i);
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
			
			return;
		}

		startActivity(intent);
	}
}