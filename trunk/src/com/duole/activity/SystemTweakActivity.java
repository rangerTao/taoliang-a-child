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
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.duole.R;

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

		connectSavedWifi();

		registerReceiver(batteryChangedReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		
		volumeTweak();
		
		brightnessTweak();
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
				batteryStatus.setText("电池电量：" + (level * 100 / scale) + "%");
			}
		}
	};

	// a broadcast receiver to receive broadcasts related with wifi.
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.v("TAG", intent.getAction());

			if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent
					.getAction())) {
				handleStateChanged(WifiInfo
						.getDetailedStateOf((SupplicantState) intent
								.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
					.getAction())) {
				switch (wifiManager.getWifiState()) {
				case WifiManager.WIFI_STATE_DISABLED:
					wifiStatus.setText(appref.getString(R.string.wifi_closed));
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					wifiStatus.setText(appref.getString(R.string.wifi_closing));
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					wifiInfo = wifiManager.getConnectionInfo();
					if (wifiInfo.getNetworkId() != -1) {
						wifiStatus.setText(getString(R.string.wifi_enabled)
								+ wifiInfo.getSSID());
					} else {
						wifiStatus.setText(getString(R.string.wifi_opened));
					}
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					wifiInfo = wifiManager.getConnectionInfo();
					wifiStatus.setText(appref.getString(R.string.wifi_enabling)
							+ wifiInfo.getSSID());
					break;
				case WifiManager.WIFI_STATE_UNKNOWN:
					break;
				}
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent
					.getAction())) {
				handleStateChanged(((NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
						.getDetailedState());
			}

		}

	};

	private void handleStateChanged(NetworkInfo.DetailedState state) {
		// WifiInfo is valid if and only if Wi-Fi is enabled.
		// Here we use the state of the check box as an optimization.
		WifiInfo info = wifiManager.getConnectionInfo();
		if (info != null) {
			Log.v("TAG", state.name() + "     " + state.ordinal());
			wifiStatus.setText(Summary.get(appref, info.getSSID(), state));
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

}
