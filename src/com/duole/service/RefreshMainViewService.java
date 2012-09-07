package com.duole.service;

import java.util.ArrayList;

import com.duole.Duole;
import com.duole.R;
import com.duole.Duole.PageDiv;
import com.duole.pojos.CellTag;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.ScrollLayout;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;

public class RefreshMainViewService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("TAG", "back refresh service start");
		// Set the thread as single task.
		Constants.viewrefreshenable = false;

		ArrayList<Asset> temp = null;

		// get all apps
		try {
			
			if(Constants.alAsset == null){
				Log.e("TAG", "COnstants.alasset is null");
				return 0;
			}
			if (Constants.alAsset.size() < 1) {
				Constants.alAsset = XmlUtils.readXML(null, Constants.CacheDir + "itemlist.xml");
			}

			temp = new ArrayList<Asset>();
			temp.addAll(Constants.alAsset);

			temp = DuoleUtils.checkFilesExists(temp);

			// Add the jinzixuan
			if (DuoleUtils.getContentFilterCount(Constants.CONTENT_FILTER_JINZIXUAN, Duole.appref) > 0) {
				DuoleUtils.addJinzixuanManager(temp);
			}

			DuoleUtils.addNetworkManager(temp);

			DuoleUtils.getMusicList(temp);

			DuoleUtils.getOnlineVideoList(temp);

		} catch (Exception e) {
			e.printStackTrace();
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

		Message msg = new Message();
		msg.what = Duole.SHOW_REFRESH;
		Duole.appref.mhandler.sendMessage(msg);

		long start = System.currentTimeMillis();
		sl.removeAllViews();

		// Message msg = new Message();
		// msg.what = Duole.appref.REMOVE_ITEMS;
		// Duole.appref.mhandler.sendMessage(msg);

		for (int i = 0; i < PageCount; i++) {

			ArrayList<Asset> mList = new ArrayList<Asset>();
			int iStart = i * Constants.APP_PAGE_SIZE;
			int iEnd = iStart + Constants.APP_PAGE_SIZE;
			while ((iStart < temp.size()) && (iStart < iEnd)) {
				mList.add(temp.get(iStart));
				iStart++;
			}

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

				// gvList.add(appPage);
				Duole.appref.mScrollLayout.addView(appPage);

			} else {
				GridView appPage = (GridView) sl.getChildAt(i);
				// get the "i" page data
				AssetItemAdapter aia = new AssetItemAdapter(Duole.appref, temp, i);
				appPage.setAdapter(aia);
			}

		}

		Duole.appref.mScrollLayout.refresh();

		Log.e("TAG_Cost", "refresh cost : " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
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

		Message msghide = new Message();
		msg.what = Duole.HIDE_REFRESH;
		Duole.appref.mhandler.sendMessage(msghide);

		Log.e("TAG_Cost", "refresh cost : " + (System.currentTimeMillis() - start));
		return super.onStartCommand(intent, flags, startId);
	}

}
