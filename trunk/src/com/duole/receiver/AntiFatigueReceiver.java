package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.duole.Duole;
import com.duole.utils.Constants;
import com.duole.utils.XmlUtils;

public class AntiFatigueReceiver extends BroadcastReceiver{

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Constants.Event_AppStart)){
			antiFatigueConfiguration();
			
			if(Duole.appref.gameCountDown != null){
				Duole.appref.gameCountDown.resume();
			}else{
				Duole.appref.initCountDownTimer();
				Duole.appref.gameCountDown.resume();
			}
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
		Log.v("TAG", "last start" +  temp);
		long lastDay = Long.parseLong(temp.equals("") ? "0" : temp);
		
		long current = System.currentTimeMillis();
		
		Log.v("TAG", "current millis" + current);
		
		Log.v("TAG", "poor" + Math.abs((current - lastDay)));
		Log.v("TAG", "period " + ((Integer.parseInt(Constants.entime) + Integer.parseInt(Constants.restime)) * 60 * 1000));
		if((int)Math.abs((current - lastDay)) > (Integer.parseInt(Constants.entime) + Integer.parseInt(Constants.restime)) * 60 * 1000){
			XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_LASTENSTART, System.currentTimeMillis() + "");
			
			Log.v("TAG", "enstart changed " + System.currentTimeMillis());
			
		}
		
	}
}
