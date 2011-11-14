package com.duole.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.RandomAccess;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

	public static boolean downloadAll() {

		int listsize = Constants.DownLoadTaskList.size();
		// If there are several task in the list.
		try{
			if (listsize > 0) {
				for (int i = 0; i < listsize; i++) {
					download(i);
				}
			}
		}catch(Exception e){
			Log.v("TAG", e.getMessage());
		}
		return true;
	}

	/**
	 * Download resources from server.
	 */
	public static void download(int index) {
		try {
			asset = Constants.DownLoadTaskList.get(index);
			
			String type = asset.getType().toLowerCase();
			
			//Download thumbnail.
			DuoleUtils.downloadPic(asset,asset.getThumbnail());
			
			if(asset.getBg() != null && !asset.getBg().equals("")){
				Log.v("TAG", "download bg " + asset.getBg());
				DuoleUtils.downloadPic(asset, asset.getBg());
			}
			//Download audio.
			if(type.equals(Constants.RES_AUDIO)){
				DuoleUtils.downloadAudio(asset,asset.getUrl());				
			}
			
			//Download game.
			if(type.equals(Constants.RES_GAME)){
				if(!asset.getUrl().startsWith("http")){
					DuoleUtils.downloadGame(asset,asset.getUrl());
				}
			}
			
			//Download video.
			if(type.equals(Constants.RES_VIDEO)){
				if (!asset.getUrl().startsWith("http")) {
					DuoleUtils.downloadVideo(asset, asset.getUrl());
				}
			}
			if(type.equals(Constants.RES_APK)){
				if (!asset.getUrl().startsWith("http")) {
					DuoleUtils.downloadApp(asset, asset.getUrl());
				}
			}
			
			if(type.equals(Constants.RES_FRONT)){
				if (!asset.getUrl().startsWith("http") && asset.getUrl().endsWith(".zip")) {
					DuoleUtils.downloadFront(asset, asset.getUrl());
				}
			}
			
			/**
			 * To do : deal with priority resource.
			 */
			
			Constants.newItemExists = true;
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean downloadCacheFile(URL url, File cacheFile){
		
		try{
			// Open a connection.
			URLConnection conn = url.openConnection();
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
			
			Log.v("TAG", cacheFile.getName() + localSize);
			// get the size of file.
			int fileSize = conn.getContentLength();

			
			//set the resume point of the file
			if(fileSize != localSize){
				
				
				byte[] buffer = new byte[8 * 1024];

				bis = conn.getInputStream();
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
			Log.v("TAG",e.getMessage());
			return false;
		}

	}
	
}
