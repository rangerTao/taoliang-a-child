package com.duole.utils;

import android.util.Log;

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

		if (listsize > 0) {
			for (int i = 0; i < listsize; i++) {
				download(i);
			}
		}
		return true;
	}

	/**
	 * Download resources from server.
	 */
	public static void download(int index) {
		try {
			asset = Constants.DownLoadTaskList.get(index);
			
			//Download thumbnail.
			DuoleUtils.downloadPic(asset,asset.getThumbnail());
			
			//Download audio.
			if(asset.getType().equals(Constants.RES_AUDIO)){
				DuoleUtils.downloadAudio(asset,asset.getUrl());				
			}
			
			//Download game.
			if(asset.getType().equals(Constants.RES_GAME)){
				if(!asset.getUrl().startsWith("http")){
					DuoleUtils.downloadGame(asset,asset.getUrl());
				}
			}
			
			//Download video.
			if(asset.getType().equals(Constants.RES_VIDEO)){
				if (!asset.getUrl().startsWith("http")) {
					DuoleUtils.downloadVideo(asset, asset.getUrl());
				}
			}
			if(asset.getType().equals(Constants.RES_APK)){
				if (!asset.getUrl().startsWith("http")) {
					DuoleUtils.downloadApp(asset, asset.getUrl());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
