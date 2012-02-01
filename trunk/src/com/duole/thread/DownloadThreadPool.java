package com.duole.thread;

import java.util.ArrayList;

import android.util.Log;

import com.duole.utils.Constants;
import com.duole.utils.DownloadFileUtils;

public class DownloadThreadPool {

	private static ArrayList<DownloadFileUtils> dfus = new ArrayList<DownloadFileUtils>();
	
	public synchronized static boolean newDownloadThread(DownloadFileUtils dfu){
		
		if(dfu == null){
			dfu = new DownloadFileUtils();
		}
		
		if((System.currentTimeMillis() - dfu.threadStartMills) > 1*60*1000){
			if(dfus.size() >=5 ){
				DownloadFileUtils dfuPop = Constants.tq.pop_front();
				dfuPop.disturb();
				Constants.tq.push_back(dfu);
				Log.d("TAG", "down load thread queue is full ");
				return true;
			}else{
				Constants.tq.push_back(dfu);
				Constants.dfu = new DownloadFileUtils();
				Log.d("TAG", "down load thread interupted,add a new thread.");
				return true;
			}
			
			
		}
		
		return false;
		
	}
}
