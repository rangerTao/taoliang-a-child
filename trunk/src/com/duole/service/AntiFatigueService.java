package com.duole.service;

import java.util.Timer;
import java.util.TimerTask;

import com.duole.Duole;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;

public class AntiFatigueService extends Service{
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		
		super.onStart(intent, startId);
	}
	
	

}
