package com.duole.service.download;

import java.io.File;

import com.duole.pojos.asset.Asset;

public interface OnDownloadCompleteListener {
	public void onDownloadComplete(Asset asset ,File cache,File target);

}
