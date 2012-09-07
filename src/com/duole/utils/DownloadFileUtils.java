package com.duole.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.entity.UrlEncodedFormEntity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;

/**
 * Download file thread
 * 
 * @author Taoliang
 * @version 1.0
 * 
 */
public class DownloadFileUtils extends Thread {

	static Asset asset;

	private boolean isPreFinished = true;

	public long threadStartMills = 0;
	public static boolean running = false;

	@Override
	public void run() {

		running = true;

		threadStartMills = java.lang.System.currentTimeMillis();

		if (downloadAll()) {
			if (Constants.AssetList.size() != Constants.alAsset.size()) {
				Constants.newItemExists = true;
			}
		}
		Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));

		running = false;
		super.run();
	}

	public void disturb() {
		running = false;
	}

	public void downloadNext() {
		isPreFinished = true;
	}

	public static boolean downloadAll() {

		// If there are several task in the list.
		try {

			Asset downAsset = Constants.dtq.pop_front();

			if (downAsset != null && !Constants.newItemExists) {
				Constants.newItemExists = true;
			}

			while (downAsset != null) {
				if (DuoleNetUtils.isNetworkAvailable(Duole.appref)) {
					Log.d("TAG", downAsset.toString());
					download(downAsset);
					Constants.queueMap.remove(downAsset.getUrl());
				} else {
					Log.e("TAG", "No useful network");
				}

				downAsset = Constants.dtq.pop_front();

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * Insert into the queue of duole download provider.
	 */
	public static void insertToContentProviderQueue(Asset asset) {

		String thumb = asset.getThumbnail();
		URL url = DuoleUtils.checkUrl(asset.getUrl());
		String strUrl = url.getPath();
		String filePath = "";
		String bg = asset.getBg();
		String bgPath = "";
		String type = asset.getType().toLowerCase();

		String thumbPath = Constants.CacheDir + "/thumbnail" + "/" + thumb.substring(thumb.lastIndexOf("/"));
		if (type.trim().equals("")) {
			// asset.setType(DuoleUtils.checkAssetType(asset));
			// type = asset.getType().toLowerCase();
			type = DuoleUtils.checkAssetType(asset);
		}

		if (asset.getBg() != null && !asset.getBg().trim().equals("")) {
			bgPath = Constants.CacheDir + "/thumbnail" + "/" + bg.substring(bg.lastIndexOf("/"));
		}

		// Download audio.
		if (type.equals(Constants.RES_AUDIO)) {
			filePath = Constants.CacheDir + "/mp3" + "/" + strUrl.substring(asset.getUrl().lastIndexOf("/"));
		}

		// Download game.
		if (type.equals(Constants.RES_GAME)) {
			filePath = Constants.CacheDir + "/game" + "/" + strUrl.substring(asset.getUrl().lastIndexOf("/"));
		}

		// Download video.
		if (type.equals(Constants.RES_VIDEO)) {
			if (!strUrl.startsWith("http") || strUrl.contains(Constants.DuoleSite)) {
				filePath = Constants.CacheDir + "/video" + "/" + strUrl.substring(strUrl.lastIndexOf("/"));
			}
		}
		if (type.equals(Constants.RES_APK) || strUrl.endsWith("apk")) {
			filePath = Constants.CacheDir + "/apk" + "/" + strUrl.substring(strUrl.lastIndexOf("/"));
		}

		if (type.equals(Constants.RES_FRONT)) {
			if ((!strUrl.startsWith("http") || strUrl.contains(Constants.DuoleSite)) && strUrl.endsWith(".zip")) {
				filePath = Constants.CacheDir + "/front" + "/" + asset.getId();
			}
		}

		Uri insert = Uri.parse("content://com.duole.download");
		ContentValues cv = new ContentValues();

		cv.put("url", strUrl);
		cv.put("target", filePath);
		cv.put("md5", asset.getMd5());
		cv.put("treat", true);

		Duole.appref.getContentResolver().insert(insert, cv);

		cv = new ContentValues();

		cv.put("url", thumb);
		cv.put("target", thumbPath);
		cv.put("md5", false);
		cv.put("treat", false);

		Duole.appref.getContentResolver().insert(insert, cv);

		if (bg != null && !bg.equals("")) {
			cv = new ContentValues();

			cv.put("url", bg);
			cv.put("target", bgPath);
			cv.put("md5", false);
			cv.put("treat", false);

			Duole.appref.getContentResolver().insert(insert, cv);

		}
	}

	/*
	 * Delete the current asset from the queue of duole download provider.
	 */
	public static void deleteFromContentProviderQueue(Asset asset) {

		Uri del = Uri.parse("content://com.duole.download");
		URL url = DuoleUtils.checkUrl(asset.getUrl());
		ContentValues cv = new ContentValues();
		cv.put("url", url.getPath());
		Duole.appref.getContentResolver().delete(del, "", null);

		cv = new ContentValues();
		url = DuoleUtils.checkUrl(asset.getThumbnail());
		cv.put("url", url.getPath());
		Duole.appref.getContentResolver().delete(del, "", null);
	}

	/**
	 * Download resources from server.
	 */
	public static void download(Asset asset) {
		try {

			String type = asset.getType().toLowerCase();

			if (type.trim().equals("")) {
				// asset.setType(DuoleUtils.checkAssetType(asset));
				// type = asset.getType().toLowerCase();
				type = DuoleUtils.checkAssetType(asset);
			}

			Log.d("TAG", "asset type " + type);

			String url = asset.getUrl().toLowerCase();
			// Download thumbnail.
			DuoleUtils.downloadPic(asset, asset.getThumbnail());

			if (asset.getBg() != null && !asset.getBg().trim().equals("")) {
				DuoleUtils.downloadPic(asset, asset.getBg());
			}
			// Download audio.
			if (type.equals(Constants.RES_AUDIO)) {
				DuoleUtils.downloadAudio(asset, asset.getUrl());
			}

			// Download game.
			if (type.equals(Constants.RES_GAME)) {
				// if(!asset.getUrl().startsWith("http") ||
				// url.contains(Constants.DuoleSite)){
				DuoleUtils.downloadGame(asset, asset.getUrl());
				// }
			}

			// Download video.
			if (type.equals(Constants.RES_VIDEO)) {
				if (!asset.getUrl().startsWith("http") || url.contains(Constants.DuoleSite)) {
					DuoleUtils.downloadVideo(asset, asset.getUrl());
				}
			}
			if (type.equals(Constants.RES_APK) || url.endsWith("apk")) {
				// if (!asset.getUrl().startsWith("http") ||
				// url.contains(Constants.DuoleSite)) {
				DuoleUtils.downloadApp(asset, asset.getUrl());
				// }
			}

			if (type.equals(Constants.RES_FRONT)) {
				if ((!url.startsWith("http") || url.contains(Constants.DuoleSite)) && asset.getUrl().endsWith(".zip")) {
					// if(asset.getUrl().endsWith(".zip")){
					DuoleUtils.downloadFront(asset, asset.getUrl());
				}
			}

			/**
			 * To do : deal with priority resource.
			 */
			Constants.newItemExists = true;
			Constants.viewrefreshenable = true;
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));

		} catch (Exception e) {
			Log.e("TAG", "downloading error : " + asset.toString());
			e.printStackTrace();
		}
	}

	public static boolean downloadCacheFile(URL url, File cacheFile) {

		try {
			// Open a connection.
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			// get the size of file.
			int fileSize = conn.getContentLength();

			byte[] buffer = new byte[8 * 1024];

			InputStream bis = null;
			FileOutputStream fos = null;

			// Create a file.
			cacheFile.createNewFile();

			bis = conn.getInputStream();
			fos = new FileOutputStream(cacheFile);
			int len = 0;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			bis.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static boolean resumeDownloadCacheFile(URL url, File cacheFile) {
		try {

			RandomAccessFile rAccess = new RandomAccessFile(cacheFile.getAbsoluteFile(), "rw");
			InputStream bis = null;
			FileOutputStream fos = null;
			long localSize = cacheFile.length();

			rAccess.seek(localSize);

			// Open a connection.
			URLConnection conn = url.openConnection();

			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("RANGE", "bytes=" + localSize + "-");
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);

			Log.v("TAG", cacheFile.getName() + localSize);
			// get the size of file.
			int fileSize = conn.getContentLength();

			Log.v("TAG", fileSize + ":filesize " + localSize + localSize);

			// set the resume point of the file
			if (fileSize != localSize) {

				byte[] buffer = new byte[8 * 1024];

				bis = conn.getInputStream();
				if (!cacheFile.exists()) {
					cacheFile.createNewFile();
				}
				fos = new FileOutputStream(cacheFile);
				int len = 0;
				while ((len = bis.read(buffer)) != -1) {
					rAccess.write(buffer, 0, len);
				}
				fos.close();
				bis.close();
				Log.v("TAG", "download complete");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("TAG", e.getMessage());
			Log.e("TAG", cacheFile.getAbsolutePath());
			return false;
		}

	}

	public void removeTaskFromMap(String asset) {
		Constants.queueMap.remove(asset);
	}

}
