package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.duole.Duole;
import com.duole.utils.Constants;
import com.duole.utils.XmlUtils;

public class AntiFatigueReceiver extends BroadcastReceiver{

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Constants.Event_AppStart)){
			antiFatigueConfiguration();
			Duole.appref.gameCountDown.resume();
		}
		if(intent.getAction().equals(Constants.Event_AppEnd)){
			Duole.appref.gameCountDown.pause();
		}
		
	}

	/**
	 * Plan the anti fatigue schedule base on the entertainment period and rest period
	 */
	public void antiFatigueConfiguration(){
		
		String temp = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_LASTENSTART);
		long lastDay = Long.parseLong(temp.equals("") ? "0" : temp);
		
		Date lastDateInstance = new Date(lastDay);
		Date todayDate = new Date(System.currentTimeMillis());
		String lastDate = sdf.format(lastDateInstance);
		String today = sdf.format(todayDate);
		if(!lastDate.equals(today)){
			if(todayDate.after(lastDateInstance)){
				XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_LASTENSTART, System.currentTimeMillis() + "");
			}
		}
	}
}
