package com.duole.asynctask;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.service.BackgroundRefreshService;
import com.duole.thread.DeleteAssetFilesThread;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.JsonUtils;
import com.duole.utils.XmlUtils;

public class ItemListTask extends AsyncTask {

	static TextView tvDeviceId;
	static TextView tvUserName;
	static TextView tvPassword;
	static EditText etUserName;
	static EditText etPassword;

	@Override
	protected Object doInBackground(Object... arg0) {
		try {
			
			treatData();
		} catch (Exception e) {
			e.printStackTrace();
			Constants.DOWNLOAD_RUNNING = false;
			return false;
		}
		return true;
	}

	/*
	 * Treat with the asset list getted from server.
	 */
	public boolean treatData() {
		HashMap<String, Asset> hmSource = new HashMap<String, Asset>();
		Constants.alAssetDeleteList = new ArrayList<Asset>();
		boolean gettedSourceList = false;
		
		//Whether sdcard exists.
		if(!DuoleUtils.checkTFCard()){
			Toast.makeText(Duole.appref, "no tf", 2000);
			return false;
		}
		
		//Whether download thread is running.
//		if(!Constants.DOWNLOAD_RUNNING){
//		if(Constants.dfu != null && !Constants.dfu.isAlive()){
			
			//Set the download thread as running.
			Constants.DOWNLOAD_RUNNING = true;
			
			//upload local client version
			DuoleNetUtils.uploadLocalVersion();
			
			//Get asset list from server.
			gettedSourceList = getSourceList();
			
			//when error.
			if(!gettedSourceList){
				Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
			}
		
		if(gettedSourceList){
			hmSource = new HashMap<String, Asset>();
			//Put all asset into a hashmap.
			for (int i = 0; i < Constants.alAsset.size(); i++) {
				Asset ass = Constants.alAsset.get(i);
				if (ass != null) {
					hmSource.put(ass.getId(), ass);
				}
			}
			
			//The assets to be deleted.
			Constants.alAssetDeleteList = DuoleUtils.getAssetDeleteList(
					hmSource, Constants.AssetList);
			
			Constants.DownLoadTaskList = new ArrayList<Asset>();
			//Get the list of assets to be download.
			if (Constants.AssetList != null && Constants.AssetList.size() > 0) {
				for (int i = 0; i < Constants.AssetList.size(); i++) {
					Asset ass = Constants.AssetList.get(i);
					if (hmSource.containsKey(ass.getId())) {
						if (DuoleUtils.checkDownloadNecessary(ass,
								hmSource.get(ass.getId()))) {
							Constants.DownLoadTaskList.add(ass);
						}
					}
				}
			} else {
				for (Asset asset : Constants.alAsset) {
					if (DuoleUtils.checkDownloadNecessary(asset,
							hmSource.get(asset.getId()))) {
						Constants.DownLoadTaskList.add(asset);
					}
				}
			}

		} else {
			//When asset list is not here.
			//Check whether has files need to download.
			for (int i = 0; i < Constants.AssetList.size(); i++) {
				Asset ass = Constants.AssetList.get(i);

				if (DuoleUtils.checkDownloadNecessary(ass,
						hmSource.get(ass.getId()))) {
					Constants.DownLoadTaskList.add(ass);
				}

			}
		}

		//there are assets need to delete.
		if (Constants.alAssetDeleteList.size() > 0) {
			new DeleteAssetFilesThread(Constants.alAssetDeleteList).start();
		}

		//if there is noting wrong with the asset list.
		if(gettedSourceList){
			
			try {
				//Update the assetlist.
				DuoleUtils.updateAssetListFile(Constants.alAsset);
				
				Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
								+ "itemlist.xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		initDownloadQueue();
		
		return true;
	}
	
	private void initDownloadQueue(){

		int i = 0;
		for(Asset asset : Constants.DownLoadTaskList){
			
			if(!Constants.queueMap.containsKey(asset.getUrl())){
				i ++;
				Constants.queueMap.put(asset.getUrl(), asset);
				Constants.dtq.push_back(asset);
			}
		}
		
		Log.d("TAG", String.format("%d task has been add to the queue.", i));
		
		i = 0;
		for(Asset asset : Constants.alAssetDeleteList){
			
			if(Constants.queueMap.containsKey(asset.getUrl())){
				i ++ ;
				Constants.queueMap.remove(asset.getUrl());
				Constants.dtq.remove(asset);
			}
		}
		
		Log.d("TAG", String.format("%d task has been removed from the queue.", i));
		
		try{
			Duole.appref.bindService(new Intent(Duole.appref,
					BackgroundRefreshService.class), Duole.appref.mConnection,
					Context.BIND_AUTO_CREATE);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get resource list from server.
	 */
	public boolean getSourceList() {
		try {
			String url = Constants.resourceUrl + DuoleUtils.getAndroidId();//					"http://www.duoleyuan.com/e/member/child/ancJn.php?cc="	+ "7c71f33fce7335e4");
			
			Constants.alAsset = new ArrayList<Asset>();
			String result = DuoleNetUtils.connect(url);
			if(result.equals("")){
				Log.e("TAG", "connection time out");
				return false;
			}
			JSONObject jsonObject = new JSONObject(result);
			String error = null;
			try {
				error = jsonObject.getString("errstr");
			} catch (Exception e) {
			}

			if (error == null){
				try{
					JsonUtils.parserJson(Constants.alAsset, jsonObject);
				}catch(Exception e){
					Constants.DOWNLOAD_RUNNING = false;
					e.printStackTrace();
					return false;
				}
				
			}else{
				Log.d("TAG", "error occurs");
				Constants.DOWNLOAD_RUNNING = false;
				return false;
			}

			return true;
		} catch (Exception e) {
			Constants.DOWNLOAD_RUNNING = false;
			return false;
		}

	}
}
