package com.duole.activity;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
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
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.duole.Duole;
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
	Preference preCheckUpdate;
	Preference preCurTime;
	
	Preference preTimeEclipsed;
	Preference preSleep;

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
		preCheckUpdate = (Preference)findPreference("preCheckUpdate");
		preCurTime = (Preference) findPreference("curTime");
		
		preCheckUpdate.setSummary("\u5f53\u524d\u7248\u672c\uff1a" + DuoleUtils.getVersion(this));

		preStorage = this.findPreference(Constants.Pre_Storage);
		
		getCurrentTime();

		//Get the usage of sd card.
		getUsageOfSdcard();
		
		//init the settings of wifi.
		initWifiSetting();

		//get the detail info of user.
		getUserInfo();
		
		//init the content of anti fatigure views.
		initAntiFatigureViews();
	}
	
	private void getCurrentTime(){
		
		//Set the time format as Year-month-day hour:minute:second.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date(System.currentTimeMillis()));
		
		preCurTime.setTitle(time);
	}
	
	/**
	 * Get the usage of sdcard.
	 */
	private void getUsageOfSdcard(){
		//if tf card is ejected.
		if (!DuoleUtils.checkTFCard()) {
			preStorage.setTitle(getString(R.string.tf_unmounted));
		} else {
			//get the info of tf card.
			File sdcard = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(sdcard.getAbsolutePath());
			
			long totalSize = countUp(statfs.getBlockCount(),statfs.getBlockSize());
			Log.v("TAG", totalSize + "totalSize");
			long usedSize = totalSize
					- countUp(statfs.getFreeBlocks(), statfs.getBlockSize());
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
	}
	
	/**
	 * init the anti fatigure infos.
	 */
	private void initAntiFatigureViews(){
		
		preTimeEclipsed = findPreference("preTimeEclipsed");
		preSleep = findPreference("preSleep");
		
		preTimeEclipsed.setTitle(getString(R.string.enterteiment) + ":  " + Constants.entime + getString(R.string.minute) + "        " + getString(R.string.anti_rest)+ ":  " + Constants.restime + getString(R.string.minute));
		String entime = getString(R.string.entime) + " : " + Duole.appref.gameCountDown.getRemainTime(); 
		String sleepTime = getString(R.string.sleepstart) + " : " + Constants.sleepstart +":00" + "     " + getString(R.string.sleepend) + " : " + Constants.sleepend+":00";
		preTimeEclipsed.setSummary(entime);
		preSleep.setSummary(sleepTime);
		
	}

	/**
	 * init the view of wifi settings.
	 */
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

	/**
	 * when selected to connect a saved wifi connection.
	 */
	private void connectSavedWifi() {

		IntentFilter intentFilter = new IntentFilter(
				"android.net.wifi.WIFI_STATE_CHANGED");
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		appref.registerReceiver(wifiReceiver, intentFilter);

	}

	//a broadcast receiver to receive broadcasts related with wifi.
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.v("TAG", intent.getAction());
			
			if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
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
            }else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                handleStateChanged(((NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
            }
			

		}

	};
	
    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        if (state != null && preWifi.isChecked()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
            	Log.v("TAG",state.name() + "     " + state.ordinal());
            	preWifi.setSummary(Summary.get(appref, info.getSSID(), state));
            }
        }
    }

	private boolean isWifiEnabled() {

		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
//		getUserInfo();
		super.onResume();
	}

	/**
	 * Get the info of user related to this machine.
	 */
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
	protected void onDestroy() {
		unregisterReceiver(wifiReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		Intent intent = null;

		// wifi.
		if (preference.getKey().equals("preListWifi")) {
			try{
				configWifi();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}

		//password
		if (preference.getKey().equals(Constants.Pre_Security_ChangePasswd)) {
			intent = new Intent(appref, PasswordActivity.class);
			intent.putExtra("type", "1");
		}

		// exit
		if (preference.getKey().equals(Constants.Pre_Security_Exit)) {
				
			DuoleUtils.execAsRoot("reboot -p");
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
	
	/**
	 * Get the size of used and free space on sdcard.
	 * @param block counts.
	 * @param block size.
	 * @return
	 */
	private long countUp(int a, int b){
		BigDecimal bc = new BigDecimal(a);
		BigDecimal bs = new BigDecimal(b);
		return Integer.parseInt(bc.multiply(bs).divide(new BigDecimal(1000).multiply(new BigDecimal(1000))).setScale(0,BigDecimal.ROUND_UP).toString());
	}
	
	/**
	 * Config wifi connections.
	 */
	private void configWifi(){
		ListView lvWifi = new ListView(appref);
		lvWifi.setCacheColorHint(Color.parseColor("#00000000"));

		//The dialog of connections.
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

		//When click one of the connections show in the dialog.
		lvWifi.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final ScanResult sr = scanResults.get(position);
				final EditText etPassword = new EditText(appref);
				boolean isConfiged = false;
				etPassword
						.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD| InputType.TYPE_CLASS_TEXT);
				List<WifiConfiguration> wificonfigs = wifiManager
						.getConfiguredNetworks();
				
				//find out whether there is any wifi is configured.
				for (WifiConfiguration temp : wificonfigs) {
					
					if (temp.SSID.equals("\"" + sr.SSID + "\"")) {
						
						isConfiged = wifiManager.enableNetwork(temp.networkId, true);
						
						WifiInfo wifiinfo = wifiManager.getConnectionInfo();
						
						Log.v("TAG", wifiinfo.getNetworkId() + "");
						if(wifiinfo.getNetworkId() == -1){
							wifiManager.removeNetwork(temp.networkId);
							isConfiged = false;
						}
						
						if (adWifi != null)
							adWifi.dismiss();
					}
				}
				
				//if none
				if (!isConfiged) {

					int res = 0;
					if (DuoleNetUtils.getSecurity(sr) == 0) {
						WifiConfiguration config = new WifiConfiguration();
						config.SSID = "\"" + sr.SSID + "\"";
						config.allowedKeyManagement.set(KeyMgmt.NONE);
						int networkId = wifiManager.addNetwork(config);
						if(networkId != -1){
							wifiManager.enableNetwork(networkId, false);
							wifiManager.saveConfiguration();
							if (adWifi != null) {
								adWifi.dismiss();
							}
						}
						
						
						
					} else {

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
												// if (etPassword.getText()
												// .toString().length() <= 0) {
												// Toast.makeText(
												// appref,
												// R.string.password_cannot_null,
												// 2000).show();
												// } else {

												String strWifiPass;

												if (etPassword.getText()
														.toString().length() <= 0) {
													strWifiPass = "";
												} else {
													strWifiPass = etPassword
															.getText()
															.toString();
												}

												WifiConfiguration wc = new WifiConfiguration();
												// wc.BSSID = sr.BSSID;
												wc.SSID = "\"" + sr.SSID + "\"";

												wc.hiddenSSID = true;

												wc.status = WifiConfiguration.Status.ENABLED;

												DuoleNetUtils
														.setWifiConfigurationSettings(
																wc,
																sr.capabilities,
																strWifiPass);
												int res = wifiManager
														.addNetwork(wc);

												Log.v("TAG", "network id" + res);
												if (res == -1) {
													Toast.makeText(
															appref,
															R.string.password_wrong,
															2000).show();
												} else {
													if (wifiManager
															.enableNetwork(res,
																	true)) {
														wifiManager
																.saveConfiguration();
														wifiPass.dismiss();
														if (adWifi != null) {
															adWifi.dismiss();
														}
													} else {
														Toast.makeText(
																appref,
																"can not connect",
																2000).show();
													}
												}
												// }
											}

										}).create();
						wifiPass.show();
					}

				}
			}

		});

		adWifi.show();

	}

	/**
	 * Get detail user info.
	 * @author taoliang
	 *
	 */
	class GetUserInfoTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			String url = "http://wvw.duoleyuan.com/e/member/child/ancJinfo.php?cc="
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


class Summary {
    static String get(Context context, String ssid, DetailedState state) {
        String[] formats = context.getResources().getStringArray((ssid == null)
                ? R.array.wifi_status : R.array.wifi_status_with_ssid);
        int index = state.ordinal();

        if (index >= formats.length || formats[index].length() == 0) {
            return null;
        }
        return String.format(formats[index], ssid);
    }

    static String get(Context context, DetailedState state) {
        return get(context, null, state);
    }
}
