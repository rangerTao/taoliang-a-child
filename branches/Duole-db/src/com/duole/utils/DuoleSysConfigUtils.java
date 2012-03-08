package com.duole.utils;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.InputType;
import android.widget.EditText;

public class DuoleSysConfigUtils {

	/**
	 * When sleep time ,disable the wifi network.
	 * @param context
	 * @return
	 */
	public static boolean disableWifi(Context context) {

		WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
		
		if(wm.isWifiEnabled()){
			wm.setWifiEnabled(false);
		}
		
		return true;
	}
	
	/**
	 * When sleep time out,enable the wifi network.
	 * @param context
	 * @return
	 */
	public static boolean enableWifi(Context context) {

		WifiManager wm = (WifiManager) context
				.getSystemService(context.WIFI_SERVICE);
		
		wm.setWifiEnabled(true);
		return true;
	}
}