package com.duole.pojos.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duole.Duole;
import com.duole.R;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;

public class AssetItemAdapter extends BaseAdapter {

	private ArrayList<Asset> mList;
	private Context mContext;

	public AssetItemAdapter(Context context, List<Asset> list, int page) {
		mContext = context;

		mList = new ArrayList<Asset>();
		int i = page * Constants.APP_PAGE_SIZE;
		int iEnd = i + Constants.APP_PAGE_SIZE;
		while ((i < list.size()) && (i < iEnd)) {
			mList.add(list.get(i));
			i++;
		}
	}
	
	public AssetItemAdapter(ArrayList<Asset> list){
		mContext = Duole.appref;
		mList = list;
	}
 
	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Asset asset = mList.get(position);

		AssetItem assItem;
		if (convertView == null) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.app_item,
					null);

			assItem = new AssetItem();
			assItem.ivKe = (ImageView) v.findViewById(R.id.ivKe);
			assItem.ivAssetThumb = (ImageView) v.findViewById(R.id.ivAppIcon);
			assItem.tvAssetName = (TextView) v.findViewById(R.id.tvAppName);

			v.setTag(assItem);
			convertView = v;
		} else {
			assItem = (AssetItem) convertView.getTag();
		}
		
		if(asset.getType().equals(Constants.RES_CONFIG)){
			
			assItem.ivAssetThumb.setImageResource(R.drawable.network);
			assItem.tvAssetName.setText(asset.getFilename());
			
			assItem.ivKe.setImageBitmap(BitmapFactory.decodeResource(Duole.appref.getResources(), R.drawable.ke));
			
			return convertView;
		}
		
		if(!asset.getThumbnail().equals("")){
			assItem.ivAssetThumb.setImageBitmap(BitmapFactory
					.decodeFile(Constants.CacheDir
							+ "/thumbnail/"	+ asset.getThumbnail().substring(asset.getThumbnail().lastIndexOf("/"))));
		}
		
		assItem.ivKe.setImageBitmap(BitmapFactory.decodeResource(Duole.appref.getResources(), R.drawable.ke));
		// set the icon
		
		// set the app name
		assItem.tvAssetName.setText(asset.getFilename());

		return convertView;
	}

	class AssetItem {
		ImageView ivKe;
		ImageView ivAssetThumb;
		TextView tvAssetName;
	}

}
