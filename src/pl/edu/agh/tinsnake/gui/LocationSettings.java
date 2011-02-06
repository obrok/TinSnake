package pl.edu.agh.tinsnake.gui;

import pl.edu.agh.tinsnake.Map;
import pl.edu.agh.tinsnake.MapHelper;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


public class LocationSettings extends Activity implements OnClickListener {
	
	private Map map;

	/**
	 * Called when the activity is first created.
	 *
	 * @param savedInstanceState the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		map = (Map) getIntent().getExtras().getSerializable("map");
		
		setContentView(R.layout.location_settings);
		findViewById(R.id.settingsSave).setOnClickListener(this);
		findViewById(R.id.settingsClearLog).setOnClickListener(this);
		findViewById(R.id.settingsSaveLog).setOnClickListener(this);
		findViewById(R.id.settingsLoadLog).setOnClickListener(this);
		
		trackingCheckBox = (CheckBox)findViewById(R.id.trackingCheckBox);
		gpsRadioButton = (RadioButton)findViewById(R.id.gpsRadioButton);
		networkRadioButton = (RadioButton)findViewById(R.id.networkRadioButton);
		secondsEditText = (EditText)findViewById(R.id.editSeconds);
		metersEditText = (EditText)findViewById(R.id.editMeters);
		
		loadSettings();
		
		setResult(1);
	}
	
	public static final String TRACKING = "tracking";
	public static final String GPS = "gps";
	public static final String NETWORK = "network";
	public static final String SECONDS = "seconds";
	public static final String METERS = "meters";
	public static final boolean GPSDefault = true;
	public static final boolean NETWORKDefault = false;
	public static final boolean TRACKINGDefault = true;
	public static final int SECONDSDefault = 60;
	public static final int METERSDefault = 100;
	public static final String SETTINGS_NAME = null;
	
	private CheckBox trackingCheckBox;
	private RadioButton gpsRadioButton;
	private RadioButton networkRadioButton;
	private EditText secondsEditText;
	private EditText metersEditText;
	
	private SharedPreferences settings;

	private void loadSettings() {
		settings = getSharedPreferences(SETTINGS_NAME, 0);
		
		trackingCheckBox.setChecked(settings.getBoolean(TRACKING, TRACKINGDefault));
		gpsRadioButton.setChecked(settings.getBoolean(GPS, GPSDefault));
		networkRadioButton.setChecked(settings.getBoolean(NETWORK, NETWORKDefault));
		
		secondsEditText.setText(String.format("%d", settings.getInt(SECONDS, SECONDSDefault)));
		metersEditText.setText(String.format("%d", settings.getInt(METERS, METERSDefault)));
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
				setResult(0);
				finish();
			}
			catch (Exception e){
				Toast.makeText(getApplicationContext(), "Invalid settings", Toast.LENGTH_LONG).show();
			}
			
			break;
			
		case R.id.settingsClearLog:
			map.clearLocationHistory();
			try {
				MapHelper.saveMap(map);
			} catch (Exception e) {
				Log.e("CLEAR LOCATION HISTORY", e.getClass().getCanonicalName() + " "
						+ e.getMessage());
				return;
			} 
			return;

		default:
			break;
		}
	}
}