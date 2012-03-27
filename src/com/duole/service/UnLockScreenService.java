package com.duole.service;

import com.duole.Duole;
import com.duole.asynctask.ItemListTask;
import com.duole.service.download.dao.ConfigDao;
import com.duole.utils.Constants;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class UnLockScreenService extends Service {

	WakeLock mWakeLock;
	BroadcastReceiver brScreenOn;
	BroadcastReceiver brScreenOff;
	
	private static final int RELEASE_WAKELOCK = 999;
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case RELEASE_WAKELOCK:
				releaseWakeLock();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

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
		IntentFilter intentFilterOff = new IntentFilter(
				"android.intent.action.SCREEN_OFF");
		brScreenOff = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.v("TAG", "Screen off");
				Constants.SCREEN_ON = false;
				
				if(Constants.power_save){
					Message msg = new Message();
					msg.what = RELEASE_WAKELOCK;
					mHandler.sendMessageDelayed(msg , 2 * 60 * 1000);
				}
			}
		};

		brScreenOn = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
				
				Log.v("TAG", "Screen on");

				Constants.SCREEN_ON = true;
				Constants.DOWNLOAD_RUNNING = false;

				KeyguardManager keyguardManager = (KeyguardManager) Duole.appref
						.getSystemService(Context.KEYGUARD_SERVICE);
				KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
				keyguardLock.disableKeyguard();
				
				if(mHandler.hasMessages(RELEASE_WAKELOCK)){
					mHandler.removeMessages(RELEASE_WAKELOCK);
				}
				
				acquireWakeLock();
			}

		};
		
		ConfigDao cd = new ConfigDao(getApplicationContext());
		
		Cursor cursor = cd.query("power_save");

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				if (cursor.getString(0).equals("0")
						|| cursor.getString(0) == null) {
					Constants.power_save = false;
				} else if (cursor.getString(0).equals("1")) {
					Constants.power_save = true;
				}
			}
		} else {
			Constants.power_save = false;
		}

		cursor.close();
		
		Duole.appref.registerReceiver(brScreenOn, intentFilter);
		Duole.appref.registerReceiver(brScreenOff, intentFilterOff);

		super.onStart(intent, startId);
	}

	private void acquireWakeLock() {

		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "TAG");

			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(brScreenOn);
		unregisterReceiver(brScreenOff);
		releaseWakeLock();
		super.onDestroy();
	}

	private void releaseWakeLock() {
		
		Log.d("TAG", "release wake lock");
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
