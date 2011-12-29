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
		
		Log.e("TAG", gettedSourceList + "   whether source list is getted");

		if(gettedSourceList){
			hmSource = new HashMap<String, Asset>();
			for (int i = 0; i < Constants.alAsset.size(); i++) {
				Asset ass = Constants.alAsset.get(i);
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
				for (Asset asset : Constants.alAsset) {
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
			if(Constants.AssetList.size() != Constants.alAsset.size()){
				Constants.newItemExists = true;
			}
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
		}
		
		if(gettedSourceList){
			DuoleUtils.updateAssetListFile(Constants.alAsset);
			
			try {
				Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
								+ "itemlist.xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			Constants.resourceUrl + DuoleUtils.getAndroidId();
			
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
