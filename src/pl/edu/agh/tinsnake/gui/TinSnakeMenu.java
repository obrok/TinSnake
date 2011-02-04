package pl.edu.agh.tinsnake.gui;

import pl.edu.agh.tinsnake.MapHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * An activity used to navigate in the application.
 */
public class TinSnakeMenu extends Activity implements OnClickListener {
	
	/**
	 * Called when the activity is first created.
	 *
	 * @param savedInstanceState the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.findViewById(R.id.prepareButton).setOnClickListener(this);
		this.findViewById(R.id.showButton).setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Intent intent = null;
		if (v.getId() == R.id.prepareButton) {
			intent = new Intent(v.getContext(), PrepareMap.class);
		} else if (v.getId() == R.id.showButton) {
			
	        final String[] items = MapHelper.getMapNames();
			
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