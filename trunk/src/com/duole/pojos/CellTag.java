package com.duole.pojos;

import java.util.ArrayList;

import android.util.Log;

import com.duole.Duole;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;

public class CellTag {

	private ArrayList<Asset> assetList;
	private AssetItemAdapter adapter;

	public ArrayList<Asset> getAssetList() {
		if (assetList == null)
			assetList = new ArrayList<Asset>();
		return assetList;
	}

	public void setAssetList(ArrayList<Asset> assetList) {
		this.assetList = assetList;
	}

	public AssetItemAdapter getAdapter() {
		if (adapter == null) {
			Log.e("TAG", "new adapter");
			adapter = new AssetItemAdapter(Duole.appref, assetList);
		}

		return adapter;
	}

	public void setAdapter(AssetItemAdapter adapter) {
		this.adapter = adapter;
	}

}
