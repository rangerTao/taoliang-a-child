package com.duole.utils;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.duole.Duole;
import com.duole.R;
import com.duole.activity.SystemConfigActivity;

public class WifiUtils {

	static WifiInfo wifiInfo;
	static WifiManager wifiManager;
	static SystemConfigActivity mContext;
	static BroadcastReceiver wifiReceiver;

	public static void unRegisterWifiReceiver() {
		mContext.unregisterReceiver(wifiReceiver);
	}

	public static void initWifiSetting(Context context) {

		wifiReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
					handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
				} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
					detectWifiStatus(wifiManager);
				} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
					handleStateChanged(((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
				}

			}

		};

		mContext = (SystemConfigActivity) context;
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		detectWifiStatus(wifiManager);

		connectSavedWifi(wifiReceiver);

	}

	public static boolean isWifiEnabled() {
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		} else {
			return false;
		}
	}

	public static void connectSavedWifi(BroadcastReceiver wifiReceiver) {

		IntentFilter intentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mContext.registerReceiver(wifiReceiver, intentFilter);

	}

	public static void detectWifiStatus(WifiManager wifiManager) {

		Message detail = new Message();
		detail.what = Constants.WIFI_CONNECTIONINFO;

		switch (wifiManager.getWifiState()) {

		case WifiManager.WIFI_STATE_DISABLED:

			detail.obj = Duole.appref.getString(R.string.wifi_closed);

			Message msg = new Message();
			msg.what = Constants.WIFI_STATUS;
			msg.obj = 5;
			mContext.mHandler.sendMessage(msg);
			break;
		case WifiManager.WIFI_STATE_DISABLING:

			detail.obj = Duole.appref.getString(R.string.wifi_closing);
			break;
		case WifiManager.WIFI_STATE_ENABLED:

			wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo.getNetworkId() != -1) {
				detail.obj = Duole.appref.getString(R.string.wifi_enabled) + wifiInfo.getSSID();
			} else {
				detail.obj = Duole.appref.getString(R.string.wifi_opened);
			}
			setWifiIcon();
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			wifiInfo = wifiManager.getConnectionInfo();
			detail.obj = Duole.appref.getString(R.string.wifi_enabling) + wifiInfo.getSSID();
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			break;
		}

		mContext.mHandler.sendMessage(detail);
	}

	public static int getCalculatedWifiLevel(WifiInfo info) {

		return WifiManager.calculateSignalLevel(info.getRssi(), 5);

	}

	private static void setWifiIcon() {
		wifiInfo = wifiManager.getConnectionInfo();
		int wifiLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
		switch (wifiLevel) {
		case 0:
			Message msg = new Message();
			msg.what = Constants.WIFI_STATUS;
			msg.obj = 0;
			mContext.mHandler.sendMessage(msg);
			break;
		case 1:
			Message msg1 = new Message();
			msg1.what = Constants.WIFI_STATUS;
			msg1.obj = 1;
			mContext.mHandler.sendMessage(msg1);
			break;
		case 2:
			Message msg2 = new Message();
			msg2.what = Constants.WIFI_STATUS;
			msg2.obj = 2;
			mContext.mHandler.sendMessage(msg2);
			break;
		case 3:
			Message msg3 = new Message();
			msg3.what = Constants.WIFI_STATUS;
			msg3.obj = 3;
			mContext.mHandler.sendMessage(msg3);
			break;
		case 4:
			Message msg4 = new Message();
			msg4.what = Constants.WIFI_STATUS;
			msg4.obj = 4;
			mContext.mHandler.sendMessage(msg4);
		default:
			break;
		}
	}

	private static void handleStateChanged(NetworkInfo.DetailedState state) {
		// WifiInfo is valid if and only if Wi-Fi is enabled.
		// Here we use the state of the check box as an optimization.
		if (state != null) {
			WifiInfo info = wifiManager.getConnectionInfo();
			if (info != null) {
				Message detail = new Message();
				detail.what = Constants.WIFI_CONNECTIONINFO;
				detail.obj = Summary.get(mContext, info.getSSID(), state);
				mContext.mHandler.sendMessage(detail);
				setWifiIcon();

			}
		}
	}

	public static List<ScanResult> getWifiScanResult() {
		wifiManager.startScan();
		return wifiManager.getScanResults();
	}

}

class Summary {
	static String get(Context context, String ssid, DetailedState state) {
		String[] formats = context.getResources().getStringArray((ssid == null) ? R.array.wifi_status : R.array.wifi_status_with_ssid);
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
