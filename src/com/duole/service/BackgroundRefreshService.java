package com.duole.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.duole.utils.Constants;

public class BackgroundRefreshService extends Service{

	WakeLock mWakeLock;
	BackgroundRefreshService brs;
	AlarmManager am;
	PendingIntent pii;
	@Override
	public IBinder onBind(Intent arg0) {

		acquireWakeLock();
		am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		
		Intent ii = new Intent(Constants.Refresh_Start);
		pii = PendingIntent.getBroadcast(this, 0, ii, 0);

		am.setRepeating(AlarmManager.RTC, Constants.frequence,
				Constants.frequence, pii);

		return null;
	}
	
	public class LocalBinder extends Binder {
		public BackgroundRefreshService getService() {
                return BackgroundRefreshService.this;
        }
	} 

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);
	}
	
	public BackgroundRefreshService getService(){
		brs = this;
		return brs;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		am.cancel(pii);
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		am.cancel(pii);
		releaseWakeLock();
		return super.onUnbind(intent);
	}
	
	private void acquireWakeLock(){
		
		if(null == mWakeLock){
			PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "TAG");
			
			if(null != mWakeLock){
				mWakeLock.acquire();
			}
		}
	}
	
	private void releaseWakeLock(){
		if(null != mWakeLock){
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	

}
