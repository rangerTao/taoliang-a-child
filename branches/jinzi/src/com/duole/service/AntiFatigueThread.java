package com.duole.service;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.os.Message;

import com.duole.utils.Constants;

public class AntiFatigueThread extends Thread{
	
		@Override
		public void run() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH");
			while(Constants.APP_RUNNING){
				
				String hour = sdf.format(new Date(System.currentTimeMillis()));
				
				if(hour.equals("10")){
					Message msg = new Message();
					msg.what = Constants.REST_TIME;
				}
				
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
		}

}
