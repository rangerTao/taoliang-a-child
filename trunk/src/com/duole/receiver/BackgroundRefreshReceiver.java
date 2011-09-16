package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.duole.Duole;
import com.duole.asynctask.ItemListTask;
import com.duole.player.MusicPlayerActivity;
import com.duole.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BackgroundRefreshReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Constants.Refresh_Start)) {
			
			Log.v("TAG", "Background refresh " + new SimpleDateFormat("yyyy MM dd HH mm ss")
					.format(new Date(System.currentTimeMillis())));
			
			int hour = Integer.parseInt(Constants.sdf_hour.format(new Date(System.currentTimeMillis())));
			
			try{
				if(Constants.SLEEP_TIME && hour >= Integer.parseInt(Constants.sleepstart) || hour <= Integer.parseInt(Constants.sleepend)){
					Constants.SLEEP_TIME = true;
					Intent intent1 = new Intent(Duole.appref,MusicPlayerActivity.class);
					intent1.putExtra("index", "1");
					if(Constants.MusicList.size() > 0){
						Duole.appref.startActivity(intent1);
					}
				}else{
					Constants.SLEEP_TIME = false;
				}
			}catch(Exception e){
				Constants.SLEEP_TIME = false;
			}
			
			if(!Constants.DOWNLOAD_RUNNING){
				Constants.DOWNLOAD_RUNNING = true;
				new ItemListTask().execute();
			}
		}

	}

}
