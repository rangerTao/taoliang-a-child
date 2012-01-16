package com.duole.asynctask;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.thread.DeleteAssetFilesThread;
import com.duole.utils.Constants;
import com.duole.utils.DownloadFileUtils;
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
		ArrayList<Asset> alAssetDeleteList = new ArrayList<Asset>();
		boolean gettedSourceList = false;
		
		//Whether sdcard exists.
		if(!DuoleUtils.checkTFCard()){
			Toast.makeText(Duole.appref, "no tf", 2000);
			return false;
		}
		
		//Whether download thread is running.
//		if(!Constants.DOWNLOAD_RUNNING){
		if(Constants.dfu != null || !Constants.dfu.isAlive()){
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
		}else{
			Log.v("TAG", "download task not finished");
			return false;
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
			alAssetDeleteList = DuoleUtils.getAssetDeleteList(
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

		Log.v("TAG", Constants.DownLoadTaskList.size() + " downloads");
		Log.v("TAG", alAssetDeleteList.size()  + " deletes");
		
		//there are assets need to delete.
		if (alAssetDeleteList.size() > 0) {
			new DeleteAssetFilesThread(alAssetDeleteList).start();
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
		
		Log.d("TAG", "Constants.dfu is running   " + Constants.dfu.isAlive());
		if(!Constants.dfu.isAlive()){
			Log.d("TAG", "start download thread");
			Constants.dfu = new DownloadFileUtils();
			Constants.dfu.start();
		}
		
//		//there are assets need to download.
//		if (DownloadFileUtils.downloadAll()) {
//			if(Constants.AssetList.size() != Constants.alAsset.size()){
//				Constants.newItemExists = true;
//			}
//			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
//		}
//		
//		Constants.DOWNLOAD_RUNNING = false;
		return true;
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
				Log.d("TAG", "connection time out");
				return false;
			}
			JSONObject jsonObject = new JSONObject(result);
			String error = null;
			try {
				error = jsonObject.getString("errstr");
			} catch (Exception e) {
				Log.e("TAG", "No error!");
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
