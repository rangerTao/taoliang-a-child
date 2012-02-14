package com.duole.service.download;

import android.content.Intent;
import android.util.Log;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

public class FileDownloadUtils {

	public void download(Asset asset) {

		try {

			Log.e("TAG", String.format("downloading %s ", asset.getId()));

			String type = asset.getType().toLowerCase();

			if (type.trim().equals("")) {
				asset.setType(DuoleUtils.checkAssetType(asset));
			}

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
				DuoleUtils.downloadGame(asset, asset.getUrl());
			}

			// Download video.
			if (type.equals(Constants.RES_VIDEO)) {
				if (!asset.getUrl().startsWith("http")
						|| url.contains(Constants.DuoleSite)) {
					DuoleUtils.downloadVideo(asset, asset.getUrl());
				}
			}
			if (type.equals(Constants.RES_APK) || url.endsWith("apk")) {
				DuoleUtils.downloadApp(asset, asset.getUrl());
			}

			if (type.equals(Constants.RES_FRONT)) {
				if ((!url.startsWith("http") || url
						.contains(Constants.DuoleSite))
						&& asset.getUrl().endsWith(".zip")) {
					DuoleUtils.downloadFront(asset, asset.getUrl());
				}
			}

			/**
			 * To do : deal with priority resource.
			 */

			Constants.newItemExists = true;
			Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));

		} catch (Exception e) {
			Log.e("TAG", "downloading error : " + asset.toString());
			e.printStackTrace();
		}

	}
}
