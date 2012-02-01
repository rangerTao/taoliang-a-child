package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.duole.Duole;
import com.duole.asynctask.ItemListTask;
import com.duole.player.MusicPlayerActivity;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleSysConfigUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.XmlUtils;

import android.R.xml;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

public class BackgroundRefreshReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Constants.Refresh_Start)) {
			
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm ss");
			String time = sdf.format(date);
			Log.v("TAG", "Background refresh " + time);
			String sleepStart = time.substring(0,11) + Constants.sleepstart + " 00";
			String sleepEnd = time.substring(0,11) + Constants.sleepend + " 00";
			
			if(Constants.system_uptime.equals("")){
				Constants.system_uptime = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_UPDATE_TIME);
			}
			
			if(Duole.appref != null && !DateFormat.is24HourFormat(Duole.appref)){
				ContentResolver cv = Duole.appref.getContentResolver();
				android.provider.Settings.System.putString(cv, android.provider.Settings.System.TIME_12_24, "24");
			}
			
			try{
				Date dateStart = sdf.parse(sleepStart);
				Date dateEnd = sdf.parse(sleepEnd);
				
				Log.v("TAG"	, dateStart.toString());
				Log.v("TAG", date.toString());
				Log.v("TAG",dateEnd.toString());
				
				boolean isSleepTime = checkSleepTime(date,dateStart,dateEnd);
				
				if (isSleepTime) {
					Constants.SLEEP_TIME = true;
					
					//swipe the cache dir.
					FileUtils.clearUselessResource();
					try{
						if (!Constants.musicPlayerIsRunning) {

							DuoleSysConfigUtils.disableWifi(context);

							// Take main task to front
							Intent intentMain = new Intent(Duole.appref,
									Duole.class);
							Duole.appref.startActivity(intentMain);

							Duole.appref.uploadGamePeriod();

							Intent intent1 = new Intent(Duole.appref,
									MusicPlayerActivity.class);
							intent1.putExtra("index", "1");

							Duole.appref.startActivity(intent1);
							Constants.musicPlayerIsRunning = true;
						}

						String curHour = Constants.sdf_hour.format(date);
						String uptime = XmlUtils.readNodeValue(
								Constants.SystemConfigFile,
								Constants.XML_UPDATE_TIME);

						if (curHour.equals(uptime.substring(0, 2))) {
							DuoleUtils.instalUpdateApk(context);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
					
				} else if (Constants.SLEEP_TIME) {
					Constants.SLEEP_TIME = false;

					DuoleSysConfigUtils.enableWifi(context);
					
					Duole.appref.sendBroadcast(new Intent(
							"com.duole.restime.out"));
					
					Duole.appref.initCountDownTimer();
					Constants.musicPlayerIsRunning = false;
				}
			}catch(Exception e){
				e.printStackTrace();
				Constants.SLEEP_TIME = false;
			}
			
//			if (Constants.dfu != null && !Constants.dfu.isAlive()) {
			if (Constants.SCREEN_ON) {
				new ItemListTask().execute();
			}
//			}else{
//				Log.v("TAG", "download is running" );
//			}
//			
			//upload game time
			new Thread(){

				@Override
				public void run() {
					
					DuoleNetUtils.uploadGamePeriodLength();
					super.run();
				}
				
			}.start();
			
		}
	}
	
	public boolean checkSleepTime(Date date,Date dateStart,Date dateEnd){
		
		if(Constants.sleepTimeDelayed){
			return false;
		}
	
		if(dateStart.before(dateEnd)){
			if(date.after(dateStart) && date.before(dateEnd)){
				return true;
			}
		}
		
		if(dateStart.after(dateEnd)){
			if(date.after(dateStart) || date.before(dateEnd)){
				return true;
			}
		}
		
		return false;
	}
}
