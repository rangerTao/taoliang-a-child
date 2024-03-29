package com.duole.asynctask;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.service.AssetDownloadService;
import com.duole.service.BackgroundRefreshService;
import com.duole.thread.DeleteAssetFilesThread;
import com.duole.utils.Constants;
import com.duole.utils.DownloadFileUtils;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.JsonUtils;
import com.duole.utils.XmlUtils;

@SuppressWarnings("rawtypes")
public class ItemListTask extends AsyncTask {

	static TextView tvDeviceId;
	static TextView tvUserName;
	static TextView tvPassword;
	static EditText etUserName;
	static EditText etPassword;

	@Override
	protected Object doInBackground(Object... arg0) {
		try {

			Uri downloadUri = Uri.parse("content://com.duole.download");
			Duole.appref.getContentResolver().insert(downloadUri, new ContentValues());

			Duole.appref.sendBroadcast(new Intent("com.duole.init.complete"));

			Duole.appref.tvTrafficStats.setVisibility(View.INVISIBLE);

		} catch (Exception e) {

			if (!DuoleUtils.isServiceWorked(Duole.appref)) {
				Intent downService = new Intent(Duole.appref, AssetDownloadService.class);
				Duole.appref.startService(downService);

				Duole.appref.tvTrafficStats.setVisibility(View.VISIBLE);
				Message msgRefresh = new Message();
				msgRefresh.what = Constants.NET_TRAFFIC;
				Duole.appref.mHandler.sendMessageDelayed(msgRefresh, 5000);
			}
		}

		try {
			treatData();
		} catch (Exception e) {
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

		// Whether sdcard exists.
		if (!DuoleUtils.checkTFCard()) {
			return false;
		}

		Log.d("TAG", "duoleutils.checktfusage :" + DuoleUtils.checkTFUsage());
		if (!DuoleUtils.checkTFUsage()) {

			// swipe the cache dir.
			FileUtils.clearUselessResource();
		}

		if (!Constants.DOWNLOAD_RUNNING) {
			// Set the download thread as running.
			Constants.DOWNLOAD_RUNNING = true;

			// upload local client version
			DuoleNetUtils.uploadLocalVersion();

			// Get asset list from server.
			gettedSourceList = getSourceList();

			// when error.
			if (!gettedSourceList) {
				Duole.appref.mhandler.post(new Runnable() {

					public void run() {
						Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
					}
				});
				Constants.DOWNLOAD_RUNNING = false;
			}
		}

		if (gettedSourceList) {
			hmSource = new HashMap<String, Asset>();
			// Put all asset into a hashmap.
			for (int i = 0; i < Constants.alAsset.size(); i++) {
				Asset ass = Constants.alAsset.get(i);
				if (ass != null) {
					hmSource.put(ass.getId(), ass);
				}
			}

			// The assets to be deleted.
			Constants.alAssetDeleteList = DuoleUtils.getAssetDeleteList(hmSource, Constants.AssetList);

			// Reset the hash map.
			hmSource = new HashMap<String, Asset>();
			// Put all asset into a hashmap.
			for (int i = 0; i < Constants.AssetList.size(); i++) {
				Asset ass = Constants.AssetList.get(i);
				if (ass != null) {
					hmSource.put(ass.getId(), ass);
				}
			}

			Constants.DownLoadTaskList = new ArrayList<Asset>();

			// Get the list of assets to be download.
			if (Constants.AssetList != null && Constants.AssetList.size() > 0) {

				for (int i = 0; i < Constants.alAsset.size(); i++) {
					// for (int i = 0; i < Constants.AssetList.size(); i++) {
					/*----------------2012.03.05-----------------------------*/
					// Asset ass = Constants.AssetList.get(i);
					// if (hmSource.containsKey(ass.getId())) {
					// if (DuoleUtils.checkDownloadNecessary(ass,
					// hmSource.get(ass.getId()))) {
					// Constants.DownLoadTaskList.add(ass);
					// }
					// }

					Asset ass = Constants.alAsset.get(i);
					if (hmSource.containsKey(ass.getId())) {
						if (DuoleUtils.checkDownloadNecessary(ass, hmSource.get(ass.getId()))) {
							Constants.DownLoadTaskList.add(ass);
						}
					} else {
						Constants.DownLoadTaskList.add(ass);
					}
					/*----------------2012.03.05-----------------------------*/
				}

			} else {
				for (Asset asset : Constants.alAsset) {
					if (DuoleUtils.checkDownloadNecessary(asset, hmSource.get(asset.getId()))) {
						Constants.DownLoadTaskList.add(asset);
					}
				}
			}

		} else {
			// When asset list is not here.
			// Check whether has files need to download.
			for (int i = 0; i < Constants.AssetList.size(); i++) {
				Asset ass = Constants.AssetList.get(i);

				if (DuoleUtils.checkDownloadNecessary(ass, hmSource.get(ass.getId()))) {
					Constants.DownLoadTaskList.add(ass);
				}

			}
		}

		// there are assets need to delete.
		if (Constants.alAssetDeleteList.size() > 0) {
			new DeleteAssetFilesThread(Constants.alAssetDeleteList).start();
		}

		// if there is noting wrong with the asset list.
		if (gettedSourceList) {

			try {
				// Update the assetlist.
				DuoleUtils.updateAssetListFile(Constants.alAsset);

				Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir + "itemlist.xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		initDownloadQueue();

		Constants.DOWNLOAD_RUNNING = false;

		return true;
	}

	private void initDownloadQueue() {

		int i = 0;

		if (Constants.DownLoadTaskList != null) {
			for (Asset asset : Constants.DownLoadTaskList) {

				if (!Constants.queueMap.containsKey(asset.getUrl())) {
					i++;
					try {
						DownloadFileUtils.insertToContentProviderQueue(asset);
					} catch (Exception e) {
						Constants.queueMap.put(asset.getUrl(), asset);
						Constants.dtq.push_back(asset);
					}
				}
			}
		}

		Log.d("TAG", String.format("%d task has been add to the queue.", i));

		i = 0;
		if (Constants.alAssetDeleteList != null) {
			for (Asset asset : Constants.alAssetDeleteList) {

				if (Constants.queueMap.containsKey(asset.getUrl())) {
					i++;
					try {
						DownloadFileUtils.deleteFromContentProviderQueue(asset);
					} catch (Exception e) {
						Constants.queueMap.remove(asset.getUrl());
						Constants.dtq.remove(asset);
					}
				}
			}
		}

		Log.d("TAG", String.format("%d task has been removed from the queue.", i));

		try {
			Duole.appref.bindService(new Intent(Duole.appref, BackgroundRefreshService.class), Duole.appref.mConnection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = Constants.RESTART_REFRESH;
			Duole.appref.mHandler.sendMessageDelayed(msg, 1 * 60 * 1000);
		}
	}

	/**
	 * Get resource list from server.
	 */
	public boolean getSourceList() {
		try {
			String url = Constants.resourceUrl + DuoleUtils.getAndroidId();

			Constants.alAsset = new ArrayList<Asset>();
			String result = DuoleNetUtils.connect(url);
			if (result.equals("")) {
				Log.e("TAG", "connection time out");
				return false;
			}
			JSONObject jsonObject = new JSONObject(result);
			String error = null;
			try {
				error = jsonObject.getString("errstr");
			} catch (Exception e) {
			}

			if (error == null) {
				try {
					JsonUtils.parserJson(Constants.alAsset, jsonObject);
				} catch (Exception e) {
					Constants.DOWNLOAD_RUNNING = false;
					e.printStackTrace();
					return false;
				}

			} else {
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
