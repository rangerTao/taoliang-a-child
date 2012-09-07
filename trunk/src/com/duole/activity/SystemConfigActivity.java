package com.duole.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings.SettingNotFoundException;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.duole.Duole;
import com.duole.R;
import com.duole.pojos.adapter.WifiNetworkAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.download.dao.ConfigDao;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.WifiUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.BatteryProgressBar;
import com.duole.widget.MemoryCardProgressBar;

/**
 * To config the system preperties.
 * 
 * @version 1.0
 * @author taoliang
 * 
 */
public class SystemConfigActivity extends Activity {

	private final static int TIME_REFRESH = 995;
	private boolean admin = false;
	private String adminType;
	private String adminType_WIFI = "wifi";
	private String adminType_Admin = "admin";

	private String passwd;

	RelativeLayout rlSettingMain;

	AudioManager am;
	WifiManager wifiManager;
	WifiInfo wifiInfo;

	BatteryProgressBar pbBattery;
	MemoryCardProgressBar mcPb;

	TextView tvMemoryLevel;
	TextView tvBatteryLevel;
	RelativeLayout rlBattery;

	static ImageView wifiStatus;
	ImageView ivBrightness;
	ImageView ivSound;

	ImageView ivDownload;

	static TextView tvCurrentTime;
	Thread refreshTime;
	boolean time_refresh = true;

	TextView tvEnTime;
	TextView tvRestTime;
	TextView tvEnTimeRemain;
	TextView tvSleepTime;
	ImageView ivExpand;

	TextView tvMachineID;
	TextView tvUserName;
	ImageView ivLess;
	RelativeLayout rlUserInfo;

	PopupWindow passwordInputWindow;
	ImageView ivBtnConfirm_password;
	ImageView ivBtnCancel_password;
	EditText etPassword;

	PopupWindow brightnessAndVolumePopup;
	ImageView ivLessTweak;
	ImageView ivFull;
	ImageView ivBtnClose;
	SeekBar sbTweak;

	PopupWindow wifiConnectionPopup;
	CheckBox cbWifiEnable;
	static TextView tvWifiStatus;
	Button btnWifiConnection;
	ImageView ivWifiConnectionClose;

	PopupWindow wifiConnectionResultPopup;
	ListView lvResults;
	ImageView ibtnClose;

	List<ScanResult> results;
	ScanResult sr;

	PopupWindow wifiConnectPasswordInputPopup;
	ImageView iBtnConnectWifiPasswordConfirm;
	ImageView iBtnConnectWifiPasswordCancel;
	EditText etConnectWifiPassword;

	Dialog advancedAdminMenuPopup;
	Button btnSetupWizard;
	Button btnCheckUpdate;
	Button btnResetLocal;
	Button btnChangePassword;
	CheckBox cbPowerSave;
	ImageView iBtnAdminMenuClose;
	DisplayMetrics dm = new DisplayMetrics();

	AlertDialog clearLocal;

	static SystemConfigActivity appref;

	public static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Constants.WIFI_STATUS:

				// Replace the icon of wifi due to the level of wifi.
				int level = (Integer) msg.obj;
				switch (level) {
				case 0:
					wifiStatus.setImageResource(R.drawable.wifi1);
					break;
				case 1:
					wifiStatus.setImageResource(R.drawable.wifi1);
					break;
				case 2:
					wifiStatus.setImageResource(R.drawable.wifi2);
					break;
				case 3:
					wifiStatus.setImageResource(R.drawable.wifi3);
					break;
				case 4:
					wifiStatus.setImageResource(R.drawable.wifi4);
					break;
				case 5:
					wifiStatus.setImageResource(R.drawable.wifi_disabled);
				default:
					break;
				}

				break;
			case Constants.WIFI_CONNECTIONINFO:
				String detail = (String) msg.obj;
				if (tvWifiStatus != null) {
					tvWifiStatus.setText(detail);
				}
				break;
			case TIME_REFRESH:

				if (tvCurrentTime != null) {
					tvCurrentTime.setText(DuoleUtils.getCurrentTime());
				}

				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appref = this;
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		setContentView(R.layout.system_setting_main);
		rlSettingMain = (RelativeLayout) findViewById(R.id.rlSettingMain);

		// Get the password of user.
		passwd = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_PASSWORD);
		if (passwd.equals("")) {
			passwd = Constants.defaultPasswd;
		}

		ivDownload = (ImageView) findViewById(R.id.ivDownload);

		// Wifi related.
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiStatus = (ImageView) findViewById(R.id.ivWifiStatus);
		wifiStatus.setOnClickListener(wifiStatusOnClickListener);

		// Brightness Tweak.
		ivBrightness = (ImageView) findViewById(R.id.ivBrightness);
		ivSound = (ImageView) findViewById(R.id.ivVolume);
		ivBrightness.setOnClickListener(brightnessAndVolumeOnClickListener);
		ivSound.setOnClickListener(brightnessAndVolumeOnClickListener);

		// MemoryCard.
		tvMemoryLevel = (TextView) findViewById(R.id.tvMemoryLevel);
		mcPb = (MemoryCardProgressBar) findViewById(R.id.pbMemoryCard);
		mcPb.setMax(100);
		tvMemoryLevel = (TextView) findViewById(R.id.tvMemoryLevel);

		// Battery.
		pbBattery = (BatteryProgressBar) findViewById(R.id.pbBattery);
		rlBattery = (RelativeLayout) findViewById(R.id.rlBattery);
		pbBattery.setMax(100);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);

		// Init the time view.
		initTimeDisplay();

		// initiate the status of store.
		initDownload();

		// brightness
		brightnessTweak();
		// volume
		volumeTweak();
		// status of memory card.
		getUsageOfSdcard();
		// info
		initAntiFatigureViews();
	}

	private void initTimeDisplay() {
		// Time
		tvCurrentTime = (TextView) findViewById(R.id.tvTime);
		tvCurrentTime.setText(DuoleUtils.getCurrentTime());

		refreshTime = new Thread() {

			@Override
			public void run() {
				while (time_refresh) {
					try {
						sleep(10 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = new Message();
					msg.what = TIME_REFRESH;
					mHandler.sendMessage(msg);

				}

				super.run();
			}

		};

		refreshTime.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		admin = false;
		WifiUtils.unRegisterWifiReceiver();
		unregisterReceiver(batteryChangedReceiver);
		time_refresh = false;
		super.onStop();
	}

	@Override
	protected void onResume() {
		try {
			WifiUtils.initWifiSetting(appref);
			// Register a battery receiver.
			registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		} catch (Exception e) {
		}
		super.onResume();
	}

	/**
	 * Init the status of store icon.
	 */
	private void initDownload() {

		if (DuoleUtils.getContentFilterCount("duole/store", appref) < 1) {
			ivDownload.setVisibility(View.INVISIBLE);
		} else {
			// If store is installed.
			ivDownload.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setType("duole/store");

					startActivity(intent);
				}
			});
		}

	}

	/**
	 * init the anti fatigure infos.
	 */
	private void initAntiFatigureViews() {

		tvEnTime = (TextView) findViewById(R.id.tvEnTime);
		tvEnTimeRemain = (TextView) findViewById(R.id.tvEnRemain);
		tvSleepTime = (TextView) findViewById(R.id.tvSleepDetail);
		tvRestTime = (TextView) findViewById(R.id.tvRestTime);

		tvEnTime.setText(getString(R.string.enterteiment) + ":  " + Constants.entime + getString(R.string.minute));
		tvRestTime.setText(getString(R.string.anti_rest) + ":  " + Constants.restime + getString(R.string.minute));
		String entime = getString(R.string.entime) + " : " + Duole.gameCountDown.getRemainTime();
		String sleepTime = getString(R.string.sleepstart) + " : " + Constants.sleepstart + ":00" + "     " + getString(R.string.sleepend) + " : "
				+ Constants.sleepend + ":00";
		tvEnTimeRemain.setText(entime);
		tvSleepTime.setText(sleepTime);

		initUserInfo();
	}

	/**
	 * The view of check password.
	 */
	private void doCheckPassword() {

		if (admin) {
			wifiConfigOrAdvance();
		} else {
			if (passwordInputWindow == null) {
				View view = getLayoutInflater().inflate(R.layout.advanced_password_input, null);
				passwordInputWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
				ivBtnConfirm_password = (ImageView) view.findViewById(R.id.ivBtnConfirm);
				ivBtnCancel_password = (ImageView) view.findViewById(R.id.ivBtnCancel);
				etPassword = (EditText) view.findViewById(R.id.etPassword);

				ivBtnConfirm_password.setOnClickListener(passwordPopupWindowOnClickListener);
				ivBtnCancel_password.setOnClickListener(passwordPopupWindowOnClickListener);
			}

			etPassword.setText("");
			passwordInputWindow.showAtLocation(rlSettingMain, Gravity.CENTER, 0, 0);

			etPassword.requestFocus();
		}

	}

	/**
	 * When click on the brightness and volume icon.
	 */
	OnClickListener brightnessAndVolumeOnClickListener = new OnClickListener() {

		public void onClick(View view) {

			if (brightnessAndVolumePopup == null) {
				View contentView = getLayoutInflater().inflate(R.layout.brightness_volume_tweak, null);
				brightnessAndVolumePopup = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

				ivLessTweak = (ImageView) contentView.findViewById(R.id.ivLessTweak);
				ivFull = (ImageView) contentView.findViewById(R.id.ivFullTweak);
				ivBtnClose = (ImageView) contentView.findViewById(R.id.ivBtnClose);
				sbTweak = (SeekBar) contentView.findViewById(R.id.sbTweakBrightnessAndVolume);

				ivBtnClose.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						brightnessAndVolumePopup.dismiss();
						sbTweak.setOnSeekBarChangeListener(null);
					}
				});
			}

			// Brightness icon.
			if (view.getId() == ivBrightness.getId()) {

				ivLessTweak.setImageResource(R.drawable.brightness_last);
				ivFull.setImageResource(R.drawable.sun_small);

				try {
					int brightMax = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
					sbTweak.setMax(255);
					sbTweak.setProgress(brightMax);

					sbTweak.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
							WindowManager.LayoutParams lp = getWindow().getAttributes();

							if (0 < progress && progress <= 255) {
								lp.screenBrightness = progress / (float) 255;
							}

							getWindow().setAttributes(lp);

							progress = sb.getProgress();
							android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS,
									progress);

							brightnessTweak();
						}

						public void onStartTrackingTouch(SeekBar arg0) {

						}

						public void onStopTrackingTouch(SeekBar arg0) {

						}
					});
				} catch (Exception e) {
				}
				brightnessAndVolumePopup.showAsDropDown(ivBrightness);
			}

			// Volume icon,
			if (view.getId() == ivSound.getId()) {
				ivLessTweak.setImageResource(R.drawable.volume_last);
				ivFull.setImageResource(R.drawable.volume_full);

				sbTweak.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
				sbTweak.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));

				sbTweak.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar VerticalSeekBar, int progress, boolean fromUser) {
						am.setStreamVolume(AudioManager.STREAM_MUSIC, sbTweak.getProgress(), 0);

						volumeTweak();
					}

					public void onStartTrackingTouch(SeekBar VerticalSeekBar) {

					}

					public void onStopTrackingTouch(SeekBar VerticalSeekBar) {

					}
				});

				brightnessAndVolumePopup.showAsDropDown(ivSound);
			}
		}
	};

	/**
	 * When enter pressed.
	 */
	OnEditorActionListener passwordOnEditorActionListener = new OnEditorActionListener() {

		public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
			InputMethodManager imm = (InputMethodManager) appref.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(appref.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return false;
		}

	};

	/**
	 * When click on the wifi icon.
	 */
	OnClickListener wifiStatusOnClickListener = new OnClickListener() {

		public void onClick(View arg0) {

			// set the admin type to adminType_WIFI.
			adminType = adminType_WIFI;
			// Check password.
			doCheckPassword();
		}

	};

	/**
	 * When clicked on the menu icon.
	 * 
	 * @param view
	 */
	public void advancedMenuOnClickListener(View view) {
		adminType = adminType_Admin;
		doCheckPassword();
	}

	/**
	 * The listener of buttons on the password popup.
	 */
	OnClickListener passwordPopupWindowOnClickListener = new OnClickListener() {

		public void onClick(View view) {

			// When confirm was pressed.
			if (view.getId() == ivBtnConfirm_password.getId()) {

				String pass = etPassword.getText().toString();
				if (pass.equals(passwd)) {
					admin = true;
					passwordInputWindow.dismiss();
					wifiConfigOrAdvance();
				} else {
					new AlertDialog.Builder(appref).setTitle(appref.getString(R.string.password_wrong)).setMessage(R.string.password_retype)
							.setPositiveButton("ok", new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface arg0, int arg1) {

								}
							}).show();

					etPassword.setText("");
				}

			}

			// When cancel was pressed.
			if (view.getId() == ivBtnCancel_password.getId()) {
				passwordInputWindow.dismiss();
			}

		}
	};

	/**
	 * Show the wifi config window or advanced menu.
	 */
	private void wifiConfigOrAdvance() {

		if (adminType.equals(adminType_WIFI)) {

			if (wifiConnectionPopup == null) {
				View convert = getLayoutInflater().inflate(R.layout.wifi_config_popup, null);
				wifiConnectionPopup = new PopupWindow(convert, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				tvWifiStatus = (TextView) convert.findViewById(R.id.tvWifiConfigStatus);
				cbWifiEnable = (CheckBox) convert.findViewById(R.id.cbWifiConfigEnable);
				btnWifiConnection = (Button) convert.findViewById(R.id.ivWifiConfig_detail);
				ivWifiConnectionClose = (ImageView) convert.findViewById(R.id.ivWifiConfigClose);

				boolean isWifiEnabled = WifiUtils.isWifiEnabled();
				cbWifiEnable.setChecked(isWifiEnabled);
				if (isWifiEnabled) {
					wifiInfo = wifiManager.getConnectionInfo();
					if (wifiInfo.getNetworkId() != -1) {
						tvWifiStatus.setText(getString(R.string.wifi_enabled) + wifiInfo.getSSID());
					} else {
						tvWifiStatus.setText(getString(R.string.wifi_opened));
					}
				} else {
					tvWifiStatus.setText(getString(R.string.wifi_closed));
				}

				cbWifiEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						boolean isChecked = cbWifiEnable.isChecked();
						wifiManager.setWifiEnabled(isChecked);
						btnWifiConnection.setEnabled(isChecked);
					}
				});

				ivWifiConnectionClose.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						wifiConnectionPopup.dismiss();
					}
				});

				btnWifiConnection.setOnClickListener(wifiConnectionListOnClickListenser);

			}

			wifiConnectionPopup.showAtLocation(rlSettingMain, Gravity.CENTER, 0, 0);
		} else if (adminType.equals(adminType_Admin)) {
			dealAdvanceMenu();
		}

	}

	private void dealAdvanceMenu() {

		if (advancedAdminMenuPopup == null) {
			View view = getLayoutInflater().inflate(R.layout.advanced_admin_menu, null);
			advancedAdminMenuPopup = new Dialog(this, R.style.CustomDialog);

			advancedAdminMenuPopup.setContentView(view);
			
			WindowManager.LayoutParams lp = advancedAdminMenuPopup.getWindow().getAttributes();
			lp.alpha = 0.8f;
			advancedAdminMenuPopup.getWindow().setAttributes(lp);
			advancedAdminMenuPopup.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

			btnSetupWizard = (Button) view.findViewById(R.id.btnSettingWizard);
			btnCheckUpdate = (Button) view.findViewById(R.id.btnCheckingUpdate);
			btnResetLocal = (Button) view.findViewById(R.id.btnLocalReset);
			cbPowerSave = (CheckBox) view.findViewById(R.id.cbPowerSave);
			btnChangePassword = (Button) view.findViewById(R.id.btnChangepassword);
			iBtnAdminMenuClose = (ImageView) view.findViewById(R.id.ivAdvanceMenuClose);

			if (DuoleUtils.getContentFilterCount("duole/setup", appref) < 1) {
				btnSetupWizard.setEnabled(false);
			}

			iBtnAdminMenuClose.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					advancedAdminMenuPopup.cancel();
				}
			});

			initPowersaveSetting();
		}

		advancedAdminMenuPopup.show();
	}

	/**
	 * initiate the power save setting.
	 */
	private void initPowersaveSetting() {

		final ConfigDao cd = new ConfigDao(getApplicationContext());
		Cursor cursor = cd.query("power_save");

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				if (cursor.getString(0).equals("0") || cursor.getString(0) == null) {
					cbPowerSave.setChecked(false);
				} else if (cursor.getString(0).equals("1")) {
					cbPowerSave.setChecked(true);
				}
			}
		} else {
			cbPowerSave.setChecked(false);
		}

		cursor.close();

		// When the state of power save checkbox is changed.
		cbPowerSave.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

				boolean save = cbPowerSave.isChecked();

				if (save) {
					new AlertDialog.Builder(appref).setTitle(R.string.caution).setMessage(R.string.power_save_tip)
							.setNegativeButton(R.string.btnPositive, null).create().show();

					cd.save("power_save", "1");
				} else {
					cd.save("power_save", "0");
				}

			}
		});
	}

	/*
	 * the click listener of advanced menu.
	 */
	public void advanceMenuOnClick(View view) {

		Intent intent = null;
		switch (view.getId()) {

		// Setting Wizard.
		case R.id.btnSettingWizard:
			startSetupWizard();
			break;
		// Client update.
		case R.id.btnCheckingUpdate:
			intent = new Intent(appref, CheckUpdateActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
		// Change password.
		case R.id.btnChangepassword:
			intent = new Intent(appref, PasswordActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("type", "1");
			break;
		// Clear local resource.
		case R.id.btnLocalReset:
			clearLocalResource();
			break;

		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}

	};

	/**
	 * Clear local resources.
	 */
	private void clearLocalResource() {

		TextView tvTip = new TextView(getApplicationContext());
		tvTip.setText("Caution");

		if (clearLocal == null) {
			clearLocal = new AlertDialog.Builder(appref).setTitle(R.string.caution).setMessage(R.string.clear_local)
					.setPositiveButton(R.string.btnPositive, new DialogInterface.OnClickListener() {

						@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int which) {
							// pdClearLocal.show();
							new ClearLocalRes().execute();
						}
					}).setNegativeButton(R.string.btnNegative, null).create();
		}

		clearLocal.show();
	}

	/*
	 * Start the setup wizard.
	 */
	private void startSetupWizard() {

		ConfigDao cd = new ConfigDao(getApplicationContext());
		cd.save("setup", "0");

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("duole/setup");
		try {
			getApplicationContext().startActivity(intent);
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
	}

	/*
	 * when click on one of the item of list.
	 */
	OnClickListener wifiConnectionListOnClickListenser = new OnClickListener() {

		public void onClick(View arg0) {
			results = WifiUtils.getWifiScanResult();

			if (wifiConnectionResultPopup == null) {
				View view = getLayoutInflater().inflate(R.layout.wifi_scanresults, null);
				wifiConnectionResultPopup = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

				lvResults = (ListView) view.findViewById(R.id.lvWifiResults);
				ibtnClose = (ImageView) view.findViewById(R.id.btnWifiResultPopupClose);

				ibtnClose.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						wifiConnectionResultPopup.dismiss();
					}
				});
			}

			lvResults.setAdapter(new WifiNetworkAdapter(results, appref));

			lvResults.setOnItemClickListener(wifiResultItemOnClickListener);

			wifiConnectionResultPopup.showAtLocation(rlSettingMain, Gravity.CENTER, 0, 0);
		}
	};

	/*
	 * 
	 */
	OnItemClickListener wifiResultItemOnClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			sr = results.get(position);
			final EditText etPassword = new EditText(appref);
			boolean isConfiged = false;
			etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
			List<WifiConfiguration> wificonfigs = wifiManager.getConfiguredNetworks();

			// find out whether there is any wifi is configured.
			for (WifiConfiguration temp : wificonfigs) {

				if (temp.SSID.equals("\"" + sr.SSID + "\"")) {

					isConfiged = wifiManager.enableNetwork(temp.networkId, true);

					WifiInfo wifiinfo = wifiManager.getConnectionInfo();

					if (wifiinfo.getNetworkId() == -1) {
						wifiManager.removeNetwork(temp.networkId);
						isConfiged = false;
					}

					if (wifiConnectionResultPopup != null)
						wifiConnectionResultPopup.dismiss();
				}
			}

			// if none
			if (!isConfiged) {

				if (DuoleNetUtils.getSecurity(sr) == 0) {
					WifiConfiguration config = new WifiConfiguration();
					config.SSID = "\"" + sr.SSID + "\"";
					config.allowedKeyManagement.set(KeyMgmt.NONE);
					int networkId = wifiManager.addNetwork(config);
					if (networkId != -1) {
						wifiManager.enableNetwork(networkId, false);
						wifiManager.saveConfiguration();
						if (wifiConnectionResultPopup != null) {
							wifiConnectionResultPopup.dismiss();
						}
					}

				} else {

					if (wifiConnectPasswordInputPopup == null) {
						View viewPassword = getLayoutInflater().inflate(R.layout.advanced_password_input, null);
						wifiConnectPasswordInputPopup = new PopupWindow(viewPassword, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
						iBtnConnectWifiPasswordConfirm = (ImageView) viewPassword.findViewById(R.id.ivBtnConfirm);
						iBtnConnectWifiPasswordCancel = (ImageView) viewPassword.findViewById(R.id.ivBtnCancel);
						etConnectWifiPassword = (EditText) viewPassword.findViewById(R.id.etPassword);

						iBtnConnectWifiPasswordCancel.setOnClickListener(new OnClickListener() {

							public void onClick(View arg0) {
								wifiConnectPasswordInputPopup.dismiss();
							}
						});
					}

					iBtnConnectWifiPasswordConfirm.setOnClickListener(wifiScanResultItemOnClickListener);

					etConnectWifiPassword.setText("");
					wifiConnectPasswordInputPopup.showAtLocation(rlSettingMain, Gravity.CENTER, 0, 0);
				}

			}
		}

	};

	/*
	 * 
	 */
	OnClickListener wifiScanResultItemOnClickListener = new OnClickListener() {

		public void onClick(View arg0) {

			String strWifiPass;

			if (etConnectWifiPassword.getText().toString().length() <= 0) {
				strWifiPass = "";
			} else {
				strWifiPass = etConnectWifiPassword.getText().toString();
			}

			WifiConfiguration wc = new WifiConfiguration();
			// wc.BSSID = sr.BSSID;
			wc.SSID = "\"" + sr.SSID + "\"";

			wc.hiddenSSID = true;

			wc.status = WifiConfiguration.Status.ENABLED;

			DuoleNetUtils.setWifiConfigurationSettings(wc, sr.capabilities, strWifiPass);
			int res = wifiManager.addNetwork(wc);

			if (res == -1) {
				Toast.makeText(appref, R.string.password_wrong, 2000).show();
			} else {
				if (wifiManager.enableNetwork(res, true)) {
					wifiManager.saveConfiguration();
					wifiConnectPasswordInputPopup.dismiss();
					if (wifiConnectionResultPopup != null) {
						wifiConnectionResultPopup.dismiss();
					}
				} else {
					Toast.makeText(appref, "can not connect", 2000).show();
				}
			}
			// }

		}
	};

	/*
	 * initiate the user info.
	 */
	@SuppressWarnings("unchecked")
	private void initUserInfo() {

		ivExpand = (ImageView) findViewById(R.id.btnExpand);

		ivExpand.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ivExpand.setVisibility(View.GONE);

				rlUserInfo = (RelativeLayout) findViewById(R.id.rlUserInfo);
				rlUserInfo.setVisibility(View.VISIBLE);

			}
		});

		ivLess = (ImageView) findViewById(R.id.ivLess);
		ivLess.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ivExpand.setVisibility(View.VISIBLE);

				if (rlUserInfo != null) {
					rlUserInfo.setVisibility(View.GONE);
				}
			}
		});

		tvMachineID = (TextView) findViewById(R.id.tvMachineID);
		tvMachineID.setText(getString(R.string.device_id) + " : " + DuoleUtils.getAndroidId());
		tvUserName = (TextView) findViewById(R.id.tvUserName);

		try {
			File file = new File("/sdcrad/DuoleCache/");
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File("/sdcard/DuoleCache/userinfo.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			String localInfo = DuoleUtils.readFromFile(file);
			String userinfo = DuoleUtils.getUserinfoFormatted(localInfo);

			if (!userinfo.equals("")) {
				tvUserName.setText(userinfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		new GetUserInfoTask().execute();
	}

	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_NOT_CHARGING);

			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					rlBattery.setBackgroundResource(R.drawable.battery_charging);
					pbBattery.setVisibility(View.INVISIBLE);
				} else {
					pbBattery.setVisibility(View.VISIBLE);
					rlBattery.setBackgroundResource(R.drawable.battery_empty);
				}
				tvBatteryLevel.setVisibility(View.VISIBLE);
				rlBattery.setVisibility(View.VISIBLE);
				int level = intent.getIntExtra("level", 0);
				tvBatteryLevel.setText(level + "%");
				if (level < 15) {
					tvBatteryLevel.setTextColor(Color.RED);
				} else {
					tvBatteryLevel.setTextColor(Color.BLACK);
				}
				pbBattery.setProgress(level);

			}

		}
	};

	/**
	 * Get the usage of sdcard.
	 */
	private void getUsageOfSdcard() {
		// if tf card is ejected.
		if (!DuoleUtils.checkTFCard()) {
		} else {
			// get the info of tf card.
			File sdcard = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(sdcard.getAbsolutePath());

			long totalSize = countUp(statfs.getBlockCount(), statfs.getBlockSize());
			long usedSize = totalSize - countUp(statfs.getFreeBlocks(), statfs.getBlockSize());
			mcPb.setProgress((int) (((float) usedSize / totalSize) * 100));
			tvMemoryLevel.setTextColor(Color.BLACK);
			tvMemoryLevel.setText(getString(R.string.data_last) + "\n" + "  " + parseDataSize(totalSize - usedSize));
		}
	}

	private String parseDataSize(long inputSize) {
		if (inputSize > 1000) {
			float total = inputSize / (float) 1000;
			return round(total, 2, BigDecimal.ROUND_DOWN) + "GB";
		} else {
			return round(inputSize, 2, BigDecimal.ROUND_DOWN) + "MB";
		}
	}

	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, roundingMode);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}

	/**
	 * Get the size of used and free space on sdcard.
	 * 
	 * @param block
	 *            counts.
	 * @param block
	 *            size.
	 * @return
	 */
	private long countUp(int a, int b) {
		BigDecimal bc = new BigDecimal(a);
		BigDecimal bs = new BigDecimal(b);
		return Integer.parseInt(bc.multiply(bs).divide(new BigDecimal(1000).multiply(new BigDecimal(1000))).setScale(0, BigDecimal.ROUND_UP)
				.toString());
	}

	/**
	 * Volume tweak.
	 */
	public void volumeTweak() {

		try {
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int progress = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			final int orgProgress = progress;

			if (orgProgress == 0) {
				ivSound.setImageResource(R.drawable.sound_disable);
			} else {
				int level = orgProgress / 5;
				switch (level) {
				case 0:
					ivSound.setImageResource(R.drawable.sound1);
					break;
				case 1:
					ivSound.setImageResource(R.drawable.sound2);
					break;
				case 2:
				case 3:
					ivSound.setImageResource(R.drawable.sound3);
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Brightness tweak.
	 * 
	 * @throws SettingNotFoundException
	 */
	public void brightnessTweak() {
		try {

			int progress = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);

			final int orgProgress = progress;

			int level = orgProgress / 80;

			switch (level) {
			case 0:
				ivBrightness.setImageResource(R.drawable.sun01);
				break;

			case 1:
				ivBrightness.setImageResource(R.drawable.sun02);
				break;

			case 2:
			case 3:
				ivBrightness.setImageResource(R.drawable.sun03);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get detail user info.
	 * 
	 * @author taoliang
	 * 
	 */
	@SuppressWarnings("rawtypes")
	class GetUserInfoTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			String url = "http://wvw.duoleyuan.com/e/member/child/ancJinfo.php?cc=" + DuoleUtils.getAndroidId();

			String result = DuoleNetUtils.connect(url);

			final String userinfo = DuoleUtils.getUserinfoFormatted(result);
			SystemConfigActivity.mHandler.post(new Runnable() {

				public void run() {
					SystemConfigActivity.appref.tvUserName.setText(userinfo.toString());
				}

			});

			try {
				FileWriter fw = new FileWriter(new File("/sdcard/DuoleCache/userinfo.txt"), false);

				fw.write(result);

				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	class ClearLocalRes extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {

			for (File apk : new File(Constants.CacheDir + Constants.RES_APK).listFiles()) {

				String pkgName = FileUtils.getPackagenameFromFile(getApplicationContext(), apk);

				try {
					Runtime.getRuntime().exec("pm uninstall " + pkgName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			FileUtils.emptyFolder(new File(Constants.CacheDir));

			Constants.newItemExists = true;
			Constants.viewrefreshenable = true;
			Constants.AssetList = new ArrayList<Asset>();
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));

			DuoleUtils.checkCacheFiles();

			Duole.appref.mScrollLayout.snapToScreen(0);
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			// clearLocal.dismiss();
			super.onPostExecute(result);
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
}
