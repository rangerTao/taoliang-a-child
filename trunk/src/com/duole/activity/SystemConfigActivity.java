package com.duole.activity;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.duole.R;
import com.duole.pojos.adapter.WifiNetworkAdapter;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;

/**
 * To config the system preperties.
 * @version 1.0
 * @author taoliang
 * 
 */
public class SystemConfigActivity extends PreferenceActivity {

	boolean isGetted = false;

	Preference preID;
	Preference preUserName;
	Preference preBabyName;
	Preference preBirthday;
	Preference preSex;
	Preference preGettingUserInfo;
	PreferenceCategory pcUserInfo;
	Preference preStorage;
	CheckBoxPreference preWifi;
	Preference preListWifi;

	WifiManager wifiManager;
	WifiInfo wifiInfo;
	AudioManager am;
	List<ScanResult> scanResults;
	AlertDialog adWifi;
	AlertDialog wifiPass;

	static SystemConfigActivity appref;
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		appref = this;

		this.addPreferencesFromResource(R.xml.systemconfig);

		//init preferences.
		pcUserInfo = (PreferenceCategory) findPreference(Constants.Pre_Pc_UserInfo);
		preGettingUserInfo = this.findPreference(Constants.Pre_GettingUserInfo);
		preID = this.findPreference(Constants.Pre_deviceid);
		preID.setSummary(DuoleUtils.getAndroidId());

		preStorage = this.findPreference(Constants.Pre_Storage);

		//if tf card is ejected.
		if (!DuoleUtils.checkTFCard()) {
			preStorage.setTitle(getString(R.string.tf_unmounted));
		} else {
			//get the info of tf card.
			File sdcard = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(sdcard.getAbsolutePath());
			long totalSize = statfs.getBlockCount() * statfs.getBlockSize()
					/ 1024 / 1024;
			long usedSize = totalSize
					- (statfs.getFreeBlocks() * statfs.getBlockSize() / 1024 / 1024);
			if (totalSize > 1024) {
				float total = totalSize / (float) 1024;
				float used = usedSize / (float) 1024;
				preStorage.setTitle(getString(R.string.tf_total)
						+ DuoleUtils.round(total, 2, BigDecimal.ROUND_DOWN)
						+ "GB     " + getString(R.string.tf_used)
						+ DuoleUtils.round(used, 2, BigDecimal.ROUND_DOWN)
						+ "GB");
			} else {
				preStorage.setTitle(getString(R.string.tf_total) + totalSize
						+ "MB     " + getString(R.string.tf_used) + usedSize
						+ "MB");
			}
		}

		//init the settings of wifi.
		initWifiSetting();

		//get the detail info of user.
		getUserInfo();
	}

	private void initWifiSetting() {
		preWifi = (CheckBoxPreference) findPreference(Constants.Pre_Wifi);
		wifiManager = (WifiManager) appref
				.getSystemService(Context.WIFI_SERVICE);
		preListWifi = (Preference) findPreference("preListWifi");

		//register a receiver to receive wifi related broadcasts.
		connectSavedWifi();

		boolean isWifiEnabled = isWifiEnabled();
		if (isWifiEnabled) {
			wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo.getNetworkId() != -1) {
				preWifi.setSummary(getString(R.string.wifi_enabled)
						+ wifiInfo.getSSID());
			} else {
				preWifi.setSummary(getString(R.string.wifi_opened));
			}
		} else {
			preWifi.setSummary(getString(R.string.wifi_closed));
		}

		preWifi.setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference arg0) {
				boolean isChecked = preWifi.isChecked();
				wifiManager.setWifiEnabled(isChecked);
				preListWifi.setEnabled(isChecked);

				return false;
			}

		});

		preWifi.setChecked(isWifiEnabled);
		preListWifi.setEnabled(isWifiEnabled);

	}

	private void connectSavedWifi() {

		IntentFilter intentFilter = new IntentFilter(
				"android.net.wifi.WIFI_STATE_CHANGED");
		appref.registerReceiver(wifiReceiver, intentFilter);

	}

	//a broadcast receiver to receive broadcasts related with wifi.
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			switch (wifiManager.getWifiState()) {
			case WifiManager.WIFI_STATE_DISABLED:
				preWifi.setSummary(appref.getString(R.string.wifi_closed));
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				preWifi.setSummary(appref.getString(R.string.wifi_closing));
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo.getNetworkId() != -1) {
					preWifi.setSummary(getString(R.string.wifi_enabled)
							+ wifiInfo.getSSID());
				} else {
					preWifi.setSummary(getString(R.string.wifi_opened));
				}
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				wifiInfo = wifiManager.getConnectionInfo();
				preWifi.setSummary(appref.getString(R.string.wifi_enabling)
						+ wifiInfo.getSSID());
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				break;
			}
		}

	};

	private boolean isWifiEnabled() {

		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		getUserInfo();
		super.onResume();
	}

	public void getUserInfo() {
		if (!isGetted) {
			if (DuoleNetUtils.isNetworkAvailable(appref)) {
				preGettingUserInfo.setKey(Constants.Pre_GettingUserInfo);
				preGettingUserInfo.setTitle(appref
						.getString(R.string.getting_user_info));
				GetUserInfoTask guit = new GetUserInfoTask();
				guit.execute();
			} else {
				preGettingUserInfo
						.setTitle(getString(R.string.network_unavailable));
				preGettingUserInfo.setKey(Constants.Pre_network);
			}
		}

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

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		Intent intent = null;
		// volumen tweak.
		if (preference.getKey().equals(Constants.Pre_volume)) {
			volumeTweak();
		}
		// brightness tweak.
		if (preference.getKey().equals(Constants.Pre_bright)) {
			try {
				brightnessTweak();
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// wifi.
		if (preference.getKey().equals("preListWifi")) {
			configWifi();
		}

		//password
		if (preference.getKey().equals(Constants.Pre_Security_ChangePasswd)) {
			intent = new Intent(appref, PasswordActivity.class);
			intent.putExtra("type", "1");
		}

		// exit
		if (preference.getKey().equals(Constants.Pre_Security_Exit)) {
			android.os.Process.killProcess(android.os.Process.myPid());
		}

		// check update.
		if (preference.getKey().equals(Constants.Pre_CheckUpdate)) {
			intent = new Intent(appref, CheckUpdateActivity.class);
		}

		if (intent != null) {
			startActivity(intent);
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private void configWifi(){
		ListView lvWifi = new ListView(appref);
		lvWifi.setCacheColorHint(Color.parseColor("#00000000"));

		adWifi = new AlertDialog.Builder(appref)
				.setTitle("Wifi")
				.setView(lvWifi)
				.setNegativeButton(R.string.btnClose,
						new OnClickListener() {

							public void onClick(DialogInterface arg0,
									int arg1) {
								// TODO Auto-generated method stub

							}

						}).create();

		wifiManager.startScan();
		scanResults = wifiManager.getScanResults();
		lvWifi.setAdapter(new WifiNetworkAdapter(scanResults, appref));

		lvWifi.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final ScanResult sr = scanResults.get(position);
				final EditText etPassword = new EditText(appref);
				boolean isConfiged = false;
				etPassword
						.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				List<WifiConfiguration> wificonfigs = wifiManager
						.getConfiguredNetworks();
				for (WifiConfiguration temp : wificonfigs) {

					if (temp.SSID.equals("\"" + sr.SSID + "\"")) {
						wifiManager.enableNetwork(temp.networkId, true);
						isConfiged = true;
						if (adWifi != null)
							adWifi.dismiss();
					}
				}
				if (!isConfiged) {
					wifiPass = new AlertDialog.Builder(appref)
							.setTitle(R.string.password)
							.setView(etPassword)
							.setNegativeButton(
									getString(R.string.btnNegative),
									new OnClickListener() {

										public void onClick(
												DialogInterface arg0,
												int arg1) {

										}

									})
							.setPositiveButton(
									getString(R.string.btnPositive),
									new OnClickListener() {

										public void onClick(
												DialogInterface arg0,
												int arg1) {
											if (etPassword.getText()
													.toString().length() <= 0) {
												Toast.makeText(
														appref,
														R.string.password_cannot_null,
														2000).show();
											} else {
												WifiConfiguration wc = new WifiConfiguration();
												// wc.BSSID = sr.BSSID;
												wc.SSID = "\"" + sr.SSID
														+ "\"";

												wc.hiddenSSID = true;

												wc.status = WifiConfiguration.Status.ENABLED;

												DuoleNetUtils
														.setWifiConfigurationSettings(
																wc,
																sr.capabilities);

												wc.preSharedKey = "\""
														+ etPassword
																.getText()
																.toString()
														+ "\"";
												int res = wifiManager
														.addNetwork(wc);

												Log.v("TAG", "network id"
														+ res);
												if (res == -1) {
													Toast.makeText(
															appref,
															R.string.password_wrong,
															2000).show();
												} else {
													if (wifiManager
															.enableNetwork(
																	res,
																	true)) {
														wifiManager
																.saveConfiguration();
														wifiPass.dismiss();
													} else {

													}
												}
											}
										}

									}).create();

				}
			}

		});

		adWifi.show();

	}

	/**
	 * Volume tweak.
	 */
	public void volumeTweak() {

		try {
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int progress = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			final int orgProgress = progress;
			final SeekBar sb = new SeekBar(this);

			sb.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			Log.v("TAG", am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) + "");
			sb.setProgress(progress);

			new AlertDialog.Builder(this)
					.setTitle(R.string.volume_tweak)
					.setView(sb)
					.setNegativeButton(getString(R.string.btnNegative),
							new AlertDialog.OnClickListener() {

								public void onClick(DialogInterface arg0,
										int arg1) {
									am.setStreamVolume(am.STREAM_MUSIC,
											orgProgress, 0);
								}

							})
					.setPositiveButton(getString(R.string.btnPositive),
							new AlertDialog.OnClickListener() {

								public void onClick(DialogInterface arg0,
										int arg1) {
									am.setStreamVolume(am.STREAM_MUSIC,
											sb.getProgress(), 0);
								}

							}).show();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Brightness tweak.
	 * @throws SettingNotFoundException
	 */
	public void brightnessTweak() throws SettingNotFoundException {

		int progress = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS);
		final int orgProgress = progress;
		final SeekBar sb = new SeekBar(this);

		sb.setMax(255);
		sb.setProgress(progress);

		new AlertDialog.Builder(this)
				.setTitle(R.string.bright_tweak)
				.setView(sb)
				.setNegativeButton(getString(R.string.btnNegative),
						new AlertDialog.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								android.provider.Settings.System.putInt(
										getContentResolver(),
										Settings.System.SCREEN_BRIGHTNESS,
										orgProgress);

								WindowManager.LayoutParams lp = getWindow()
										.getAttributes();

								if (0 < orgProgress && orgProgress <= 255) {
									lp.screenBrightness = orgProgress
											/ (float) 255;
								}

								getWindow().setAttributes(lp);
							}

						})
				.setPositiveButton(getString(R.string.btnPositive),
						new AlertDialog.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {

								int progress = sb.getProgress();
								android.provider.Settings.System
										.putInt(getContentResolver(),
												android.provider.Settings.System.SCREEN_BRIGHTNESS,
												progress);
							}

						}).show();

		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {

				Integer progress = sb.getProgress();
				WindowManager.LayoutParams lp = getWindow().getAttributes();

				if (0 < progress && progress <= 255) {
					lp.screenBrightness = progress / (float) 255;
				}

				getWindow().setAttributes(lp);
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			public void onStopTrackingTouch(SeekBar sb) {

			}

		});
	}

	/**
	 * Get detail user info.
	 * @author taoliang
	 *
	 */
	class GetUserInfoTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			String url = "http://www.duoleyuan.com/e/member/child/ancJinfo.php?cc="
					+ DuoleUtils.getAndroidId();

			String result = DuoleNetUtils.connect(url);
			if (!result.equals("")) {
				try {
					JSONObject json = new JSONObject(result);

					String username = json.getString("username");
					String babyname = json.getString("truename");
					String birthday = json.getString("birthd");
					String sex = json.getString("sex");
					String userid = json.getString("userid");

					if (!userid.equals("null")) {
						Preference preUserName = new Preference(appref);
						Preference preBabyName = new Preference(appref);
						Preference preBirthday = new Preference(appref);
						Preference preSex = new Preference(appref);
						preUserName.setEnabled(false);
						preBabyName.setEnabled(false);
						preBirthday.setEnabled(false);
						preSex.setEnabled(false);
						preUserName.setSelectable(false);
						preBabyName.setSelectable(false);
						preBirthday.setSelectable(false);
						preSex.setSelectable(false);
						preUserName.setTitle(R.string.username);
						preUserName.setSummary(username);
						preBabyName.setTitle(R.string.babyname);
						preBabyName.setSummary(babyname);
						preBirthday.setTitle(R.string.birthday);
						preBirthday.setSummary(birthday);
						preSex.setTitle(R.string.sex);
						if (sex.equals("0")) {
							sex = appref.getString(R.string.sex_male);
						} else if (sex.equals("1")) {
							sex = appref.getString(R.string.sex_female);
						} else {
							sex = appref.getString(R.string.sex_unborn);
						}
						preSex.setSummary(sex);

						pcUserInfo.removeAll();

						if (appref
								.findPreference(Constants.Pre_GettingUserInfo) != null) {
							pcUserInfo
									.removePreference(appref
											.findPreference(Constants.Pre_GettingUserInfo));
						}
						pcUserInfo.addPreference(preID);
						pcUserInfo.addPreference(preUserName);
						pcUserInfo.addPreference(preBabyName);
						pcUserInfo.addPreference(preBirthday);
						pcUserInfo.addPreference(preSex);

						appref.isGetted = true;
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				if (pcUserInfo != null) {
					Preference getuserinfo = appref
							.findPreference(Constants.Pre_GettingUserInfo);
					if (getuserinfo != null) {
						pcUserInfo.removePreference(getuserinfo);
					}

					Preference preRegister = new Preference(appref);
					preRegister.setKey(Constants.Pre_Register);
					preRegister.setTitle(appref
							.getString(R.string.device_active));
					preRegister.setSummary(appref
							.getString(R.string.register_device));
				}

			}
			return null;
		}

	}

}
