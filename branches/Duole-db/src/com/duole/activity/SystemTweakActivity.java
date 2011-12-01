package com.duole.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.duole.R;
import com.duole.utils.Constants;

public class SystemTweakActivity extends BaseActivity implements
		OnClickListener {

	public SystemTweakActivity appref;
	WifiManager wifiManager;
	WifiInfo wifiInfo;
	AudioManager am;

	TextView wifiStatus;
	Button btnClose;
	TextView batteryStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		appref = this;
		setContentView(R.layout.systemtweak);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiStatus = (TextView) findViewById(R.id.WifiStatus);
		btnClose = (Button) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(this);
		batteryStatus = (TextView) findViewById(R.id.BatteryStatus);

		initWifiSetting();

		registerReceiver(batteryChangedReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		
		volumeTweak();
		
		brightnessTweak();
	}
	
	private void initWifiSetting() {
		wifiManager = (WifiManager) appref
				.getSystemService(Context.WIFI_SERVICE);

		detectWifiStatus(wifiManager,wifiStatus);
		
		connectSavedWifi();

	}

	private void connectSavedWifi() {

		IntentFilter intentFilter = new IntentFilter(
				"android.net.wifi.WIFI_STATE_CHANGED");
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		appref.registerReceiver(wifiReceiver, intentFilter);

	}

	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 100);
				batteryStatus.setText(appref.getString(R.string.batterylevel) +  (level * 100 / scale) + "%");
			}
		}
	};

	// a broadcast receiver to receive broadcasts related with wifi.
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.v("TAG", intent.getAction());
			
			detectWifiStatus(wifiManager,wifiStatus);

		}
	};
	
	public void detectWifiStatus(WifiManager wifiManager, TextView tView) {
		switch (wifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			tView.setText(appref.getString(R.string.wifi_closed));
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			tView.setText(appref.getString(R.string.wifi_closing));
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo.getNetworkId() != -1) {
				tView.setText(getString(R.string.wifi_enabled)
						+ wifiInfo.getSSID());
			} else {
				tView.setText(getString(R.string.wifi_opened));
			}
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			wifiInfo = wifiManager.getConnectionInfo();
			tView.setText(appref.getString(R.string.wifi_enabling)
					+ wifiInfo.getSSID());
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			break;
		}
	}

	public void onClick(View v) {
		finish();
	}

	/**
	 * Volume tweak.
	 */
	public void volumeTweak() {

		try {
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int progress = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			final int orgProgress = progress;
			final SeekBar sb = (SeekBar)findViewById(R.id.sbVolume);

			sb.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			sb.setProgress(progress);
			
			sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					am.setStreamVolume(am.STREAM_MUSIC,
							sb.getProgress(), 0);
				}

				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Brightness tweak.
	 * 
	 * @throws SettingNotFoundException
	 */
	public void brightnessTweak(){
		try{
			
		
		int progress = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS);
		
		final int orgProgress = progress;
		final SeekBar sb = (SeekBar)findViewById(R.id.sbBrightness);

		sb.setMax(255);
		sb.setProgress(progress);

		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {

				Integer progress = sb.getProgress();
				WindowManager.LayoutParams lp = getWindow().getAttributes();

				if (0 < progress && progress <= 255) {
					lp.screenBrightness = progress / (float) 255;
				}

				getWindow().setAttributes(lp);
				
				progress = sb.getProgress();
				android.provider.Settings.System
						.putInt(getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS,
								progress);
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			public void onStopTrackingTouch(SeekBar sb) {

			}

		});
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		unregisterReceiver(batteryChangedReceiver);
		unregisterReceiver(wifiReceiver);
		finish();
		super.onPause();
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:

			finish();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
