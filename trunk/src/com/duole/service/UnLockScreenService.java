package com.duole.service;

import com.duole.Duole;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class UnLockScreenService extends Service{

	WakeLock mWakeLock;
	BroadcastReceiver brScreenOn;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {

		acquireWakeLock();
		IntentFilter intentFilter = new IntentFilter(
				"android.intent.action.SCREEN_ON");
		brScreenOn = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				KeyguardManager keyguardManager = (KeyguardManager) Duole.appref.getSystemService(Context.KEYGUARD_SERVICE);
				KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
				keyguardLock.disableKeyguard();
			}
			
		};
		Duole.appref.registerReceiver(brScreenOn, intentFilter);

		super.onStart(intent, startId);
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
	
	
	
	@Override
	public void onDestroy() {
		unregisterReceiver(brScreenOn);
		releaseWakeLock();
		super.onDestroy();
	}

	private void releaseWakeLock(){
		if(null != mWakeLock){
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
