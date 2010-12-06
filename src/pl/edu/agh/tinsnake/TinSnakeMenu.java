package pl.edu.agh.tinsnake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

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
			intent = new Intent(v.getContext(), ShowMap.class);
		}

		startActivity(intent);
	}
}