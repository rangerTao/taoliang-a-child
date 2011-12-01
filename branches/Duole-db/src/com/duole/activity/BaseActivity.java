package com.duole.activity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.duole.Duole;
import com.duole.player.MusicPlayerActivity;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.XmlUtils;

public class BaseActivity extends Activity {

	public static String pkgName;
	
	public long playStart = 0;
	public String resourceId = "";
	
	private static ActivityManager gDefault;
	
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case Constants.REST_TIME:
				startMusicPlay();
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	public void startMusicPlay(){
		Intent MusicPlay = new Intent(this,MusicPlayerActivity.class);
		MusicPlay.putExtra("index", "1");
		MusicPlay.putExtra("type", "rest");
		
		//Take main task to front
		Intent intent = new Intent(this,Duole.class);
		startActivity(intent);
		
		startActivity(MusicPlay);
	}
	
	public void SetFullScreen() {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	}	
	
	//set the screen on
	public void setScreenON(){
		ContentResolver mContentResolver = getContentResolver();
		
		android.provider.Settings.System.putInt(mContentResolver, android.provider.Settings.System.LOCK_PATTERN_ENABLED, 0);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	public boolean forceStopActivity(){
		
		Log.d("TAG", "force to stop a activity :  name " + pkgName);
		if(pkgName != null && !pkgName.equals("")){
			ActivityManager am = (ActivityManager)getSystemService(
	                Context.ACTIVITY_SERVICE);
			
			Method method;
			try {
				method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
				method.invoke(am, pkgName);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return true;
	}
	
	public boolean uploadGamePeriod() {

		long playEnd = System.currentTimeMillis();
		long period = playEnd - Constants.gameStartMillis;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

		int min = DuoleUtils.parseMillsToMinutes(period);

		String[] var = new String[3];
		resourceId = Constants.resourceId;
		
		if (!"".equals(resourceId) && null != resourceId) {
			var[0] = resourceId;
			var[1] = min + "";
			var[2] = sdf.format(new Date(Constants.gameStartMillis));
			
			Log.v("TAG", "times "+ var[0] + "  " + var[1] + "    " + var[2]);
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyy-MM-dd");
			String currentday = sdfFileName.format(date);

			FileUtils.saveTxt(var, Constants.CacheDir + "log/" + currentday);
			
			resourceId = "";
		}

		return true;

	}
}