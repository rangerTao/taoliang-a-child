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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class RefreshCompeleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.Refresh_Complete)) {

			Log.v("TAG",
					"refresh complete : "
							+ new SimpleDateFormat("yyyy MM dd HH mm ss")
									.format(new Date(System.currentTimeMillis())));

			if(Constants.newItemExists){
				Duole.appref.mHandler.post(new Runnable() {

					public void run() {
						
						refreshView();
						
					}

				});
			}

			Duole.appref.bindService(new Intent(Duole.appref,
					BackgroundRefreshService.class), Duole.appref.mConnection,
					Context.BIND_AUTO_CREATE);
			Constants.newItemExists = false;
			
			//Clear the temp folder
			FileUtils.clearTempFolder(Constants.CacheDir + "/temp/");

		}
		
		
	}
	
	private synchronized void refreshView(){
		
		ArrayList<Asset> temp = null;
		// get all apps
		try {
			if(Constants.alAsset.size() < 1){
				Constants.alAsset = XmlUtils.readXML(null, Constants.CacheDir
					+ "itemlist.xml");
			}
//			Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
//					+ "itemlist.xml");
			
			temp = new ArrayList<Asset>();
			temp.addAll(Constants.alAsset);
			temp = DuoleUtils.checkFilesExists(temp);
			
			Log.v("TAG", temp.size() + "temp size");
			
			DuoleUtils.addNetworkManager(temp);
			
			DuoleUtils.getMusicList(temp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// the total pages
		int PageCount = (int) Math.ceil(temp.size()
				/ Constants.APP_PAGE_SIZE);
		
		if(PageCount == 0 || (temp.size() % Constants.APP_PAGE_SIZE) > 0){
			PageCount += 1;
		}
		
		ScrollLayout sl = Duole.appref.mScrollLayout;

		sl.removeAllViews();
		
		for (int i = 0; i < PageCount; i++) {
			if(i > sl.getChildCount() - 1 ){
				GridView appPage = new GridView(Duole.appref);
				// get the "i" page data
				AssetItemAdapter aia = new AssetItemAdapter(Duole.appref, temp,
						i);
				appPage.setAdapter(aia);

				appPage.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				
				appPage.setNumColumns(Constants.COLUMNS);
				
				appPage.setPadding(40, 10, 40, 0);
				
				appPage.setVerticalSpacing(30);
				
				appPage.setColumnWidth(110);

				appPage.setOnItemClickListener(Duole.appref.listener);
				Duole.appref.mScrollLayout.addView(appPage);

			}else{
				GridView appPage = (GridView) sl.getChildAt(i);
				// get the "i" page data
				AssetItemAdapter aia = new AssetItemAdapter(Duole.appref, temp,
						i);
				appPage.setAdapter(aia);

			}
			
			
		}
		
		int llChildCount = Duole.appref.llPageDivider.getChildCount();
		if(PageCount <= llChildCount){
			for(int i = llChildCount; i > PageCount ; i --){
				Duole.appref.llPageDivider.removeViewAt(i - 1);
			}
		}else{
			View view;
			for(int i = llChildCount; i < PageCount ; i ++){
				view = LayoutInflater.from(Duole.appref).inflate(R.layout.pagedividerselected, null);
				
				PageDiv pd = Duole.appref.new PageDiv();
				pd.ivPageDiv = (ImageView) view.findViewById(R.id.ivBackground);
				view.setTag(pd);
				
				Duole.appref.llPageDivider.addView(view,i);
			}
		}
		
		Duole.appref.setBackground();
		
		Duole.appref.mScrollLayout.refresh();
		
		Constants.DOWNLOAD_RUNNING = false;
		
		temp = null;
	
		
	}
	
}
