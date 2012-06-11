package com.duole.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import android.content.Intent;
import android.util.Log;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;

/**
 * Download file thread
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
		
		if(downloadAll()){
			if(Constants.AssetList.size() != Constants.alAsset.size()){
				Constants.newItemExists = true;
			}
		}
		Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
		
		running = false;
		super.run();
	}
	
	public void disturb(){
		running = false;
	}
	
	public void downloadNext(){
		isPreFinished = true;
	}

	public static boolean downloadAll() {

		// If there are several task in the list.
		try{
			
			Asset downAsset = Constants.dtq.pop_front();
			
			if(downAsset != null && !Constants.newItemExists){
				Constants.newItemExists = true;
			}
			
			while(downAsset != null){
				if(DuoleNetUtils.isNetworkAvailable(Duole.appref)){
					Log.d("TAG", downAsset.toString());
					download(downAsset);
					Constants.queueMap.remove(downAsset.getUrl());
				}else{
					Log.e("TAG", "No useful network");
				}
				
				downAsset = Constants.dtq.pop_front();
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Download resources from server.
	 */
	public static void download(Asset asset) {
		try {
			
			String type = asset.getType().toLowerCase();
			
			if(type.trim().equals("")){
//				asset.setType(DuoleUtils.checkAssetType(asset));
//				type = asset.getType().toLowerCase();
				type = DuoleUtils.checkAssetType(asset);
			}
			
			Log.d("TAG", "asset type " + type);
			
			String url = asset.getUrl().toLowerCase();
			//Download thumbnail.
			DuoleUtils.downloadPic(asset,asset.getThumbnail());
			
			if(asset.getBg() != null && !asset.getBg().trim().equals("")){
				DuoleUtils.downloadPic(asset, asset.getBg());
			}
			//Download audio.
			if(type.equals(Constants.RES_AUDIO)){
				DuoleUtils.downloadAudio(asset,asset.getUrl());				
			}
			
			//Download game.
			if(type.equals(Constants.RES_GAME)){
//				if(!asset.getUrl().startsWith("http") || url.contains(Constants.DuoleSite)){
					DuoleUtils.downloadGame(asset,asset.getUrl());
//				}
			}
			
			//Download video.
			if(type.equals(Constants.RES_VIDEO)){
				if (!asset.getUrl().startsWith("http") || url.contains(Constants.DuoleSite)) {
					DuoleUtils.downloadVideo(asset, asset.getUrl());
				}
			}
			if(type.equals(Constants.RES_APK) || url.endsWith("apk")){
//				if (!asset.getUrl().startsWith("http") || url.contains(Constants.DuoleSite)) {
					DuoleUtils.downloadApp(asset, asset.getUrl());
//				}
			}
			
			if(type.equals(Constants.RES_FRONT)){
				if ((!url.startsWith("http") || url.contains(Constants.DuoleSite) ) && asset.getUrl().endsWith(".zip")) {
//				if(asset.getUrl().endsWith(".zip")){
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
	
	public static boolean downloadCacheFile(URL url, File cacheFile){
		
		try{
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
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	public static boolean resumeDownloadCacheFile(URL url, File cacheFile) {
		try {
			
			RandomAccessFile rAccess = new RandomAccessFile(cacheFile.getAbsoluteFile(),"rw");
			InputStream bis = null;
			FileOutputStream fos = null;
			long localSize = cacheFile.length();
			
			rAccess.seek(localSize);
			
			// Open a connection.
			URLConnection conn = url.openConnection();
			
			conn.setAllowUserInteraction(true); 
			conn.setRequestProperty("RANGE","bytes=" + localSize + "-");
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			
			Log.v("TAG", cacheFile.getName() + localSize);
			// get the size of file.
			int fileSize = conn.getContentLength();
			
			Log.v("TAG", fileSize + ":filesize " + localSize + localSize);
			
			//set the resume point of the file
			if(fileSize != localSize){
				
				byte[] buffer = new byte[8 * 1024];

				bis = conn.getInputStream();
				if(!cacheFile.exists()){
					cacheFile.createNewFile();
				}
				fos = new FileOutputStream(cacheFile);
				int len = 0;
				while ((len = bis.read(buffer)) != -1) {
					rAccess.write(buffer, 0, len);
				}
				fos.close();
				bis.close();
				Log.v("TAG","download complete");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("TAG",e.getMessage());
			Log.e("TAG", cacheFile.getAbsolutePath());
			return false;
		}

	}
	
	public void removeTaskFromMap(String asset){
		Constants.queueMap.remove(asset);
	}
	
}
