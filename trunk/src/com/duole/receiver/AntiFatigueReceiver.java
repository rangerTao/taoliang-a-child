package com.duole.receiver;

import java.text.SimpleDateFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.duole.Duole;
import com.duole.service.download.dao.ConfigDao;
import com.duole.utils.Constants;
import com.duole.utils.XmlUtils;

public class AntiFatigueReceiver extends BroadcastReceiver{

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Constants.Event_AppStart)){
			antiFatigueConfiguration(context);
			
			if(Duole.gameCountDown != null){
				Duole.gameCountDown.resume();
			}else{
				Duole.appref.initCountDownTimer();
				Duole.gameCountDown.resume();
			}
		}
		if(intent.getAction().equals(Constants.Event_AppEnd)){
			Duole.gameCountDown.pause();
		}
		
	}

	/**
	 * Plan the anti fatigue schedule base on the entertainment period and rest period
	 */
	public void antiFatigueConfiguration(Context context){
		
		String temp = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_LASTENSTART);
		Log.v("TAG", "last start" +  temp);
		long lastDay = Long.parseLong(temp.equals("") || temp == null ? "0" : temp);
		
		long current = System.currentTimeMillis();
		
		Log.v("TAG", "current millis" + current);
		
		if(Math.abs((current - lastDay)) > Constants.timePool){
			ConfigDao configDao = new ConfigDao(context);
			
			configDao.save(Constants.XML_LASTENSTART, System.currentTimeMillis() + "");
			XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_LASTENSTART, System.currentTimeMillis() + "");
			
			long time1 = Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000;
			Duole.gameCountDown.setTotalTime(time1);
			long time2 = Integer.parseInt(Constants.restime == "" ? "120" : Constants.restime) * 60 * 1000;
			Duole.restCountDown.setTotalTime(time2);
			
			Constants.timePool = time1 + time2;
			
			XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_TIMEPOOL, Constants.timePool + "");
			
			configDao.save(Constants.XML_TIMEPOOL, Constants.timePool + "");
			
			Log.d("TAG", Constants.timePool + "  time pool");
			
			Log.v("TAG", "enstart changed " + System.currentTimeMillis());
			
		}
		
	}
}
