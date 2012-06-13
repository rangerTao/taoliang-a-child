package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.duole.Duole;
import com.duole.R;
import com.duole.Duole.PageDiv;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.BackgroundRefreshService;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.ScrollLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;

public class RefreshCompeleteReceiver extends BroadcastReceiver {

	public static ArrayList<AssetItemAdapter> alAssetAdapter = new ArrayList<AssetItemAdapter>();
	private boolean viewRefreshing = false;
	ArrayList<Asset> temp = new ArrayList<Asset>();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.Refresh_Complete)) {

			Log.v("TAG", "refresh complete : " + new SimpleDateFormat("yyyy MM dd HH mm ss").format(new Date(System.currentTimeMillis())));

			Log.d("TAG", "is new item exists : " + Constants.newItemExists);

			if (Constants.newItemExists) {

				Log.d("TAG", "is refresh enable " + Constants.viewrefreshenable);

				Duole.appref.mHandler.post(new Runnable() {

					public void run() {

						if (Constants.viewrefreshenable) {

							refreshView();

						}
					}
				});
			}

			Constants.newItemExists = false;

		}
	}

	class viewRefresh extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			viewRefreshing = true;
			Constants.temp = new ArrayList<Asset>();
			// Set the thread as single task.
			Constants.viewrefreshenable = false;

			// get all apps
			try {
				if (Constants.alAsset == null || Constants.alAsset.size() < 1) {
					Constants.alAsset = XmlUtils.readXML(null, Constants.CacheDir + "itemlist.xml");
				}

				Constants.temp.addAll(Constants.alAsset);

				Constants.temp = DuoleUtils.checkFilesExists(Constants.temp);

				// Add the jinzixuan
				// DuoleUtils.addJinzixuanManager(Constants.temp);
				// DuoleUtils.addNetworkManager(Constants.temp);

				DuoleUtils.getMusicList(Constants.temp);

				DuoleUtils.getOnlineVideoList(Constants.temp);

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			// the total pages

			final ScrollLayout sl = Duole.appref.mScrollLayout;

			Duole.appref.mHandler.post(new Runnable() {

				public void run() {

					int PageCount = 0;
					if (Constants.temp != null) {
						PageCount = (int) Math.ceil(Constants.temp.size() / Constants.APP_PAGE_SIZE);
					}

					if (PageCount == 0 || (Constants.temp.size() % Constants.APP_PAGE_SIZE) > 0) {
						PageCount += 1;
					}

					for (int i = 0; i < Constants.alAssetAdapter.size(); i++) {
						Constants.alAssetAdapter.get(i).notifyDataSetChanged();
					}

					int llChildCount = 0;

					if (Duole.appref.llPageDivider != null) {
						llChildCount = Duole.appref.llPageDivider.getChildCount();
					}

					if (PageCount <= llChildCount) {
						for (int i = llChildCount; i > PageCount; i--) {
							Duole.appref.llPageDivider.removeViewAt(i - 1);
						}
					} else {
						View view;
						for (int i = llChildCount; i < PageCount; i++) {
							view = LayoutInflater.from(Duole.appref).inflate(R.layout.pagedividerselected, null);

							PageDiv pd = Duole.appref.new PageDiv();
							pd.ivPageDiv = (ImageView) view.findViewById(R.id.ivBackground);
							view.setTag(pd);

							if (Duole.appref.llPageDivider != null) {
								Duole.appref.llPageDivider.addView(view, i);
							}
						}
					}

					Duole.appref.mScrollLayout.refresh();

					Constants.viewrefreshenable = true;

					viewRefreshing = false;
				}
			});

			return null;
		}

	}

	/**
	 * if there is anything changed,refresh the view.
	 */
	public synchronized static boolean refreshView() {

		// Set the thread as single task.
		Constants.viewrefreshenable = false;

		ArrayList<Asset> temp = null;

		// get all apps
		try {
			if (Constants.alAsset.size() < 1) {
				Constants.alAsset = XmlUtils.readXML(null, Constants.CacheDir + "itemlist.xml");
			}

			temp = new ArrayList<Asset>();
			temp.addAll(Constants.alAsset);

			temp = DuoleUtils.checkFilesExists(temp);

			// Add the jinzixuan
			DuoleUtils.addJinzixuanManager(temp);
			DuoleUtils.addNetworkManager(temp);

			DuoleUtils.getMusicList(temp);

			DuoleUtils.getOnlineVideoList(temp);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// the total pages

		int PageCount = 0;
		if (temp != null) {
			PageCount = (int) Math.ceil(temp.size() / Constants.APP_PAGE_SIZE);
		}

		if (PageCount == 0 || (temp.size() % Constants.APP_PAGE_SIZE) > 0) {
			PageCount += 1;
		}

		ScrollLayout sl = Duole.appref.mScrollLayout;

		sl.removeAllViews();

		for (int i = 0; i < PageCount; i++) {

			if (i > sl.getChildCount() - 1) {
				GridView appPage = new GridView(Duole.appref);
				// get the "i" page data
				AssetItemAdapter aia = new AssetItemAdapter(Duole.appref, temp, i);
				appPage.setAdapter(aia);

				appPage.setSelector(R.drawable.grid_selector);

				appPage.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

				appPage.setNumColumns(Constants.COLUMNS);

				appPage.setPadding(40, 10, 40, 0);

				appPage.setVerticalSpacing(30);

				appPage.setColumnWidth(110);

				appPage.setOnItemClickListener(Duole.appref.listener);
				Duole.appref.mScrollLayout.addView(appPage);

			} else {

				GridView appPage = (GridView) sl.getChildAt(i);
				// get the "i" page data
				AssetItemAdapter aia = new AssetItemAdapter(Duole.appref, temp, i);

				appPage.setAdapter(aia);

			}

		}

		int llChildCount = 0;

		if (Duole.appref.llPageDivider != null) {
			llChildCount = Duole.appref.llPageDivider.getChildCount();
		}

		if (PageCount <= llChildCount) {
			for (int i = llChildCount; i > PageCount; i--) {
				Duole.appref.llPageDivider.removeViewAt(i - 1);
			}
		} else {
			View view;
			for (int i = llChildCount; i < PageCount; i++) {
				view = LayoutInflater.from(Duole.appref).inflate(R.layout.pagedividerselected, null);

				PageDiv pd = Duole.appref.new PageDiv();
				pd.ivPageDiv = (ImageView) view.findViewById(R.id.ivBackground);
				view.setTag(pd);

				if (Duole.appref.llPageDivider != null) {
					Duole.appref.llPageDivider.addView(view, i);
				}
			}
		}

		Duole.appref.setBackground();

		Duole.appref.mScrollLayout.refresh();

		temp = null;

		Constants.viewrefreshenable = true;

		return true;

	}

}
