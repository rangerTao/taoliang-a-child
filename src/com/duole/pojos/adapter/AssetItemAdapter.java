package com.duole.pojos.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duole.Duole;
import com.duole.R;
import com.duole.pojos.asset.Asset;
import com.duole.service.download.dao.WidgetDao;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.WidgetUtils;

public class AssetItemAdapter extends BaseAdapter {

	private ArrayList<Asset> mList;
	private Context mContext;
	BitmapFactory.Options opts = new BitmapFactory.Options();

	// private Bitmap musicBmp;
	// private Bitmap keBmp;

	public AssetItemAdapter(Context convert, ArrayList<Asset> list) {
		mContext = convert;

		mList = list;
	}

	public AssetItemAdapter(Context context, List<Asset> list, int page) {
		mContext = context;

		mList = new ArrayList<Asset>();
		int i = page * Constants.APP_PAGE_SIZE;
		int iEnd = i + Constants.APP_PAGE_SIZE;
		while ((i < list.size()) && (i < iEnd)) {
			mList.add(list.get(i));
			i++;
		}

		// keBmp = FileUtils.toRoundCorner(Constants.bmpKe, 7);
		// musicBmp =
		// FileUtils.toRoundCorner(BitmapFactory.decodeResource(Duole.appref.getResources(),
		// R.drawable.ke_music), 7);
	}

	public ArrayList<Asset> getAssetList() {
		return mList;
	}

	public AssetItemAdapter(ArrayList<Asset> list) {
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
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Asset asset = mList.get(position);

		AssetItem assItem;
		if (convertView == null) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.app_item, null);

			assItem = new AssetItem();
			assItem.ivKe = (ImageView) v.findViewById(R.id.ivKe);
			assItem.ivAssetThumb = (ImageView) v.findViewById(R.id.ivAppIcon);
			assItem.tvAssetName = (TextView) v.findViewById(R.id.tvAppName);

			v.setTag(assItem);
			convertView = v;
		} else {
			try {
				assItem = (AssetItem) convertView.getTag();
			} catch (Exception e) {
				View v = LayoutInflater.from(mContext).inflate(R.layout.app_item, null);

				assItem = new AssetItem();
				assItem.ivKe = (ImageView) v.findViewById(R.id.ivKe);
				assItem.ivAssetThumb = (ImageView) v.findViewById(R.id.ivAppIcon);
				assItem.tvAssetName = (TextView) v.findViewById(R.id.tvAppName);

				v.setTag(assItem);
				convertView = v;
			}

		}

		if (asset.getType().equals(Constants.RES_CONFIG)) {

			assItem.ivAssetThumb.setImageResource(R.drawable.network);
			assItem.tvAssetName.setText(asset.getFilename());

			assItem.ivKe.setImageResource(R.drawable.ke);

			return convertView;
		}

		if (asset.getType().equals(Constants.RES_JINZIXUAN)) {

			File file = new File("/sdcard/jinzixuan/index/thumb/" + asset.getThumbnail());
			if (file.exists()) {
				setImageViewByFilePath(assItem.ivAssetThumb, file.getAbsolutePath(), true);
			} else {
				assItem.ivAssetThumb.setImageResource(R.drawable.nopic);

			}
			assItem.tvAssetName.setText(asset.getName());

			assItem.ivKe.setImageResource(R.drawable.ke);

			return convertView;

		}

		if (asset.getType().equals(Constants.RES_WIDGET)) {

			WidgetDao wd = new WidgetDao(mContext);

			String wid = "";
			String packagename = "";
			if (asset.getPackag() != null && !asset.getPackag().equals("")) {
				packagename = asset.getPackag();
				wid = wd.findWidgetId(packagename);

			} else {
				packagename = FileUtils.getPackagenameFromAPK(mContext, asset);
				wid = wd.findWidgetId(packagename);
			}
			if (!wid.equals("")) {
				RelativeLayout rLayout = (RelativeLayout) convertView.findViewById(R.id.rlApp_Item);
				// return WidgetUtils.getWidgetViewByWidgetID(mContext,
				// wid,packagename);
				View view = WidgetUtils.getWidgetViewByWidgetID(mContext, wid, packagename);
				view.setPadding(0, 5, 0, 0);
				rLayout.addView(view);
				return convertView;
			} else {
				RelativeLayout rLayout = (RelativeLayout) convertView.findViewById(R.id.rlApp_Item);
				View view = WidgetUtils.getWidgetViewByWidgetPackageName(mContext, packagename);
				view.setPadding(0, 5, 0, 0);
				rLayout.addView(view);
				return convertView;
				// return WidgetUtils.getWidgetViewByWidgetPackageName(mContext,
				// packagename);
			}
		}

		// if (Constants.alAssetCache.containsKey(asset.getId())) {
		// return Constants.alAssetCache.get(asset.getId());
		// }

		if (!asset.getThumbnail().equals("")) {
			File file = new File(Constants.CacheDir + "/thumbnail/" + asset.getThumbnail().substring(asset.getThumbnail().lastIndexOf("/")));
			if (file.exists()) {
				setImageViewByFilePath(assItem.ivAssetThumb, file.getAbsolutePath(), true);
			} else {
				assItem.ivAssetThumb.setImageResource(R.drawable.nopic);

			}
			file = null;
		}

		if (asset.getType().toLowerCase().equals(Constants.RES_AUDIO)) {
			assItem.ivKe.setImageResource(R.drawable.ke_music);
		} else {
			assItem.ivKe.setImageResource(R.drawable.ke);
		}

		// set the icon

		// set the app name
		assItem.tvAssetName.setText(asset.getName());

		// if (Constants.alAssetCache.size() < 120) {
		// Constants.alAssetCache.put(asset.getId(), convertView);
		// }

		return convertView;
	}

	private void setImageViewByFilePath(ImageView iv, String path, boolean save) {

		if (Constants.imagePool.containsKey(path)) {
			if (Constants.imagePool.get(path) == null) {
				if (save) {
					addBitmapIntoImagePool(iv, path);
				}
			} else {
				getBitmapFromImagePool(iv, path);
			}
		} else {
			if (save) {
				addBitmapIntoImagePool(iv, path);
			}
		}

	}

	private void getBitmapFromImagePool(ImageView iv, String path) {
		Bitmap bmpBitmap = Constants.imagePool.get(path).get();
		if (bmpBitmap == null) {
			File file = new File(path);
			if (file.exists()) {
				addBitmapIntoImagePool(iv, path);
			} else {
				iv.setImageResource(R.drawable.nopic);
			}
		} else {
			iv.setImageBitmap(bmpBitmap);
		}

	}

	private void addBitmapIntoImagePool(ImageView iv, String path) {
		Bitmap bmp = BitmapFactory.decodeFile(path);
		if (bmp == null) {
			iv.setImageResource(R.drawable.nopic);
		} else {
			SoftReference<Bitmap> sr = new SoftReference<Bitmap>(bmp);
			Constants.imagePool.put(path, sr);
			bmp = null;
			Bitmap bmpBitmap = Constants.imagePool.get(path).get();
			if (bmpBitmap != null) {
				iv.setImageBitmap(bmpBitmap);
			} else {
				iv.setImageResource(R.drawable.nopic);
			}
		}

	}

	class AssetItem {
		ImageView ivKe;
		ImageView ivAssetThumb;
		TextView tvAssetName;
	}

}
