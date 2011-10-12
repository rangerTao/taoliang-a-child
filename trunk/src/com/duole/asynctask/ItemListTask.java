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

public class ItemListTask extends AsyncTask {

	ArrayList<Asset> alAsset;
	
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
			return false;
		}
		return true;
	}

	public boolean treatData() {
		HashMap<String, Asset> hmSource = new HashMap<String, Asset>();
		ArrayList<Asset> alAssetDeleteList = new ArrayList<Asset>();
		boolean gettedSourceList = false;
		
		if(!DuoleUtils.checkTFCard()){
			Toast.makeText(Duole.appref, "no tf", 2000);
			return false;
		}
		
		if(!Constants.DOWNLOAD_RUNNING){
			Constants.DOWNLOAD_RUNNING = true;
			
			//upload local client version
			DuoleNetUtils.uploadLocalVersion();
			
			gettedSourceList = getSourceList();
			if(!gettedSourceList){
				Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
			}
		}else{
			Log.v("TAG", "download task not finished");
			return false;
		}
		
		

		if(gettedSourceList){
			hmSource = new HashMap<String, Asset>();
			for (int i = 0; i < alAsset.size(); i++) {
				Asset ass = alAsset.get(i);
				if (ass != null) {
					hmSource.put(ass.getId(), ass);
				}
			}

			alAssetDeleteList = DuoleUtils.getAssetDeleteList(
					hmSource, Constants.AssetList);
			
			Constants.DownLoadTaskList = new ArrayList<Asset>();
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
				for (Asset asset : alAsset) {
					if (DuoleUtils.checkDownloadNecessary(asset,
							hmSource.get(asset.getId()))) {
						Constants.DownLoadTaskList.add(asset);
					}
				}
			}

		} else {
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
		
		if (alAssetDeleteList.size() > 0) {
			new DeleteAssetFilesThread(alAssetDeleteList).start();
		}

		if (DownloadFileUtils.downloadAll()) {
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
		}
		
		if(gettedSourceList && alAsset.size() > 0){
			DuoleUtils.updateAssetListFile(alAsset);
		}
		
		Constants.DOWNLOAD_RUNNING = false;
		return true;
	}

	/**
	 * Get resource list from server.
	 */
	public boolean getSourceList() {
		try {
			String url = //					"http://www.duoleyuan.com/e/member/child/ancJn.php?cc="	+ "7c71f33fce7335e4");
			"http://www.duoleyuan.com/e/member/child/ancJn.php?cc=" + DuoleUtils.getAndroidId();
			
			alAsset = new ArrayList<Asset>();
			String result = DuoleNetUtils.connect(url);
			if(result.equals("")){
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
					JsonUtils.parserJson(alAsset, jsonObject);
				}catch(Exception e){
					Constants.DOWNLOAD_RUNNING = false;
					e.printStackTrace();
					return false;
				}
				
			}

			return true;
		} catch (Exception e) {
			Constants.DOWNLOAD_RUNNING = false;
			return false;
		}

	}
}
