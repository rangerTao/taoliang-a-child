package com.duole.service;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DownloadFileUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AssetDownloadService extends Service{
	
	private static boolean running = true;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		Log.d("TAG", "Asset download service start");
		
		Thread start = new Thread(){

			@Override
			public void run() {
				int index = 0;
				while(running){
					
					if(index == 10){
						Constants.newItemExists = true;
						Constants.viewrefreshenable = true;
						index = 0;
					}
					
					index ++;
					DownloadFileUtils.downloadAll();
					
					Constants.dtq.trim();
					
					try {
						Thread.sleep( 20 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
				}
			
				super.run();
			}
			
		};
		
		start.start();
		
		super.onStart(intent, startId);
	}
	
	public void destroyService(){
		running = false;
	}

	@Override
	public void onDestroy() {
		
		running = false;
		Constants.dtq.empty();
		
		super.onDestroy();
	}
	
	
	
	

}
