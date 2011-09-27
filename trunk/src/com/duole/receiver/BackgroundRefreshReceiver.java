package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.duole.Duole;
import com.duole.asynctask.ItemListTask;
import com.duole.player.MusicPlayerActivity;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.XmlUtils;

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

				if(date.after(dateStart) && date.before(dateEnd)){
					if (!Constants.musicPlayerIsRunning) {
						Constants.SLEEP_TIME = true;
						Intent intent1 = new Intent(Duole.appref,
								MusicPlayerActivity.class);
						intent1.putExtra("index", "1");

						Duole.appref.startActivity(intent1);
						Constants.musicPlayerIsRunning = true;
					}
					
					String curHour = Constants.sdf_hour.format(date);
					if(curHour.equals(Constants.system_uptime.substring(0, 2))){
						Log.v("TAG",Constants.system_uptime);
						DuoleUtils.instalUpdateApk(context);
					}
					
				}else if(Constants.SLEEP_TIME){
					Log.v("TAG", "time out");
					Constants.SLEEP_TIME = false;
					Duole.appref.sendBroadcast(new Intent("com.duole.restime.out"));
					Constants.musicPlayerIsRunning = false;
				}
			}catch(Exception e){
				Log.v("TAG"	, e.getMessage());
				Constants.SLEEP_TIME = false;
			}
			
			if(!Constants.DOWNLOAD_RUNNING){
				new ItemListTask().execute();
			}
		}
	}
}
