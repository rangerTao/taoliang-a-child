package com.duole.service;

import com.duole.Duole;
import com.duole.utils.Constants;

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

public class UnLockScreenService extends Service {

	WakeLock mWakeLock;
	BroadcastReceiver brScreenOn;
	BroadcastReceiver brScreenOff;

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
			}

		};
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
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
