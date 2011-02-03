package pl.edu.agh.tinsnake.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


public class LocationSettings extends Activity implements OnClickListener {
	
	/**
	 * Called when the activity is first created.
	 *
	 * @param savedInstanceState the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.location_settings);
		findViewById(R.id.settingsSave).setOnClickListener(this);
		
		trackingCheckBox = (CheckBox)findViewById(R.id.trackingCheckBox);
		gpsRadioButton = (RadioButton)findViewById(R.id.gpsRadioButton);
		networkRadioButton = (RadioButton)findViewById(R.id.networkRadioButton);
		secondsEditText = (EditText)findViewById(R.id.editSeconds);
		metersEditText = (EditText)findViewById(R.id.editMeters);
		
		loadSettings();
	}
	
	private final String SETTINGS_NAME = "location_settings";
	private final String TRACKING = "tracking";
	private final String GPS = "gps";
	private final String NETWORK = "network";
	private final String SECONDS = "seconds";
	private final String METERS = "meters";
	
	private CheckBox trackingCheckBox;
	private RadioButton gpsRadioButton;
	private RadioButton networkRadioButton;
	private EditText secondsEditText;
	private EditText metersEditText;
	
	private SharedPreferences settings;

	private void loadSettings() {
		settings = getSharedPreferences(SETTINGS_NAME, 0);
		
		trackingCheckBox.setChecked(settings.getBoolean(TRACKING, true));
		gpsRadioButton.setChecked(settings.getBoolean(GPS, true));
		networkRadioButton.setChecked(settings.getBoolean(NETWORK, false));
		
		secondsEditText.setText(String.format("%d", settings.getInt(SECONDS, 60)));
		metersEditText.setText(String.format("%d", settings.getInt(METERS, 100)));
	}
	
	private void saveSettings() {
		boolean tracking = trackingCheckBox.isChecked();
		boolean gps_provider = gpsRadioButton.isChecked();
		boolean network_provider = networkRadioButton.isChecked();
		
		int seconds = Integer.parseInt(secondsEditText.getText().toString());
		int meters = Integer.parseInt(metersEditText.getText().toString());
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(TRACKING, tracking);
		editor.putBoolean(GPS, gps_provider);
		editor.putBoolean(NETWORK, network_provider);
		editor.putInt(METERS, meters);
		editor.putInt(SECONDS, seconds);

		editor.commit();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.settingsSave:

			try{
				saveSettings();
				finish();
			}
			catch (Exception e){
				Toast.makeText(getApplicationContext(), "Invalid settings", Toast.LENGTH_LONG).show();
			}
			
			break;

		default:
			break;
		}
	}
}