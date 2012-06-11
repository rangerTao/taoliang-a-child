package com.duole.utils;

import android.content.Intent;
import android.util.Log;

import com.duole.Duole;

public class AntiFatigureUtils {

	
	/**
	 * Base on the new variables getted from the site.
	 * Reset the count down timers.
	 */
	public static synchronized boolean resetCountdownTimer(String olden,String oldrest){
		
		if(olden.equals(Constants.entime) && oldrest.equals(Constants.restime)){
			return false;
		}
		
		String temp = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_LASTENSTART);
		long enStart = Long.parseLong(temp.equals("") || temp == null ? System.currentTimeMillis() + "" : temp);
		
		long pool = System.currentTimeMillis() - enStart;
		
		long newEntime = Integer.parseInt(Constants.entime) * 60 * 1000;
		long newRestime = Integer.parseInt(Constants.restime) * 60 * 1000;
		
		Constants.timePool = newEntime + newRestime;
		
		long poolRemain = pool % (newEntime + newRestime);
		
		Log.d("TAG", "enstart  :" + enStart);
		Log.d("TAG", "newEntime  : " + newEntime);
		Log.d("TAG", "newRestime   :" + newRestime);
		Log.d("TAG", "pool    " + pool);
		
		Log.d("TAG", poolRemain + "    " + " new En  " + newEntime + "   newRest  " + newRestime);
		
		// Start rest time.
		if (pool > 0 && pool < newEntime) {
			// Reset the entime.
			if (Duole.restCountDown.isRunning()) {
				Duole.appref.sendBroadcast(new Intent("com.duole.restime.out"));
				
				Duole.gameCountDown.setTotalTime(newEntime);
				Duole.gameCountDown.seekToMills(pool);
				Log.d("TAG","new game time  " + Duole.gameCountDown.getRemainTime());
				if (Duole.gameCountDown.getPb() != null) {

					Duole.gameCountDown.getPb().setMax((int) newEntime);
					Duole.gameCountDown.getPb().setProgress((int)pool);
				}

				if (!Duole.gameCountDown.isRunning()) {
					Duole.gameCountDown.resume();
				}
				
				return true;
			}

			Log.d("TAG", "new en   " + Constants.entime);
			if (!Constants.entime.equals(olden)) {

				Duole.gameCountDown.setTotalTime(newEntime);
				Duole.gameCountDown.seekToMills(pool);
				Log.d("TAG","new game time  " + Duole.gameCountDown.getRemainTime());
				if (Duole.gameCountDown.getPb() != null) {

					Duole.gameCountDown.getPb().setMax((int) newEntime);
					Duole.gameCountDown.getPb().setProgress((int) (pool));
				}

				if (!Duole.gameCountDown.isRunning()) {
					Duole.gameCountDown.resume();
				}
			}
			
			return true;
		}

		if (pool > 0 && pool >= newEntime) {
			// if rest time is playing, reset the resttime.
			// otherwise, start rest time.
			Log.d("TAG", Constants.ENTIME_OUT + "    " + Constants.musicPlayerIsRunning);
			if (!Constants.ENTIME_OUT) {
				
				Duole.appref.startMusicPlay();
				Constants.ENTIME_OUT = true;
				
				Duole.restCountDown.setTotalTime(newRestime);
				Duole.restCountDown.seekToMills(0);
				Log.d("TAG", "new rest time   " + Duole.restCountDown.getRemainTime());
				if(Duole.restCountDown.getPb() != null){
					Duole.restCountDown.getPb().setMax((int)newRestime);
					Duole.restCountDown.getPb().setProgress(0);
				}
				
				return true;
			}
			
			Log.d("TAG", "new rest   " + Constants.restime);
			if(!Constants.restime.equals(oldrest)){
				
				Duole.restCountDown.setTotalTime(newRestime);
				Duole.restCountDown.seekToMills((int)(newRestime - Duole.restCountDown.getRemainMills()));
				Log.d("TAG", "new rest time   " + Duole.restCountDown.getRemainTime());
				if(Duole.restCountDown.getPb() != null){
					Duole.restCountDown.getPb().setMax((int)newRestime);
					Duole.restCountDown.getPb().setProgress((int)(newRestime - Duole.restCountDown.getRemainMills()));
				}
			}
			
			return true;
		}
		
//		if(poolRemain < newEntime){
//			
//			if(Duole.restCountDown.isRunning()){
//				Duole.appref.sendBroadcast(new Intent("com.duole.restime.out"));
//			}
//			
//			Log.d("TAG", "new en   " + Constants.entime);
//			if(!Constants.entime.equals(olden)){
//
//				Duole.gameCountDown.setTotalTime(newEntime);
//				Duole.gameCountDown.seekToMills(poolRemain);
//				Log.d("TAG", "new game time  " + Duole.gameCountDown.getRemainTime());
//				if(Duole.gameCountDown.getPb() != null){
//
//					Duole.gameCountDown.getPb().setMax((int)newEntime);
//					Duole.gameCountDown.getPb().setProgress((int)poolRemain);
//				}
//				
//				if(!Duole.gameCountDown.isRunning()){
//					Duole.gameCountDown.resume();
//				}
//			}
//			
//		}else if (poolRemain < (newEntime + newRestime)){
//			
//			Log.d("TAG", Constants.ENTIME_OUT + "    " + Constants.musicPlayerIsRunning);
//			if (!Constants.ENTIME_OUT) {
//				Duole.appref.startMusicPlay();
//				Constants.ENTIME_OUT = true;
//			}
//			
//			Log.d("TAG", "new rest   " + Constants.restime);
//			if(!Constants.restime.equals(oldrest)){
//				
////				Duole.restCountDown.setTotalTime((int)(newEntime + newRestime - poolRemain));
////				Duole.restCountDown.seek(0);
//				Duole.restCountDown.setTotalTime(newRestime);
//				Duole.restCountDown.seekToMills((int)(poolRemain - newEntime));
//				Log.d("TAG", "new rest time   " + Duole.restCountDown.getRemainTime());
//				if(Duole.restCountDown.getPb() != null){
////					Duole.restCountDown.getPb().setMax((int)(newEntime + newRestime - poolRemain));
////					Duole.restCountDown.getPb().setProgress(0);
//					Duole.restCountDown.getPb().setMax((int)newRestime);
//					Duole.restCountDown.getPb().setProgress((int)(poolRemain - newEntime));
//				}
//			}
//		}
		
		return true;
	}
}
