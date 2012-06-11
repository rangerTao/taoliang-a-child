package com.duole.receiver;

import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadStartReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
			
		for(Asset asset : Constants.DownLoadTaskList){
			
			if(!Constants.queueMap.containsKey(asset.getUrl())){
				Constants.queueMap.put(asset.getUrl(), asset);
				Constants.dtq.push_back(asset);
			}
		}
		
		for(Asset asset : Constants.alAssetDeleteList){
			
			if(Constants.queueMap.containsKey(asset.getUrl())){
				Constants.queueMap.remove(asset.getUrl());
				Constants.dtq.remove(asset);
			}
		}
		
	}

}
