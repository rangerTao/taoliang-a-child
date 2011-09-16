package com.duole;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.duole.activity.BaseActivity;
import com.duole.activity.PasswordActivity;
import com.duole.asynctask.ItemListTask;
import com.duole.layout.ScrollLayout;
import com.duole.player.FlashPlayerActivity;
import com.duole.player.SingleMusicPlayerActivity;
import com.duole.pojos.DuoleCountDownTimer;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.BackgroundRefreshService;
import com.duole.service.UnLockScreenService;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.XmlUtils;

public class Duole extends BaseActivity {

	public static Duole appref;

	public static DuoleCountDownTimer gameCountDown;
	public static DuoleCountDownTimer restCountDown;

	private static final String TAG = "TAG";
	public ScrollLayout mScrollLayout;
	private static Context mContext;
	private static BackgroundRefreshService mBoundService;
	public static ArrayList<AssetItemAdapter> alAIA;

	public static ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {

			mBoundService = ((BackgroundRefreshService.LocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {

			mBoundService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
//		SetFullScreen();
		setContentView(R.layout.main);
		
		Intent screenLock = new Intent(this,UnLockScreenService.class);
		startService(screenLock);
		
		appref = this;
		
		mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayoutTest);

		appref = this;
		try {

			initContents();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initContents()  throws Exception{
		
		// Check whether tf card exists.
		if (DuoleUtils.checkTFCard()) {
			// init the cache folders.
			if (DuoleUtils.checkCacheFiles()) {
				// init the main view.
				initViews();

				setBackground();
				
				initCountDownTimer();
			} else {
				Toast.makeText(this, R.string.itemlist_lost, 2000).show();

			}

			new ItemListTask().execute();

		} else {
			Toast.makeText(this, "No TF Card", 2000).show();
			
			IntentFilter intentFilter = new IntentFilter(
			"android.intent.action.MEDIA_MOUNTED");
			try{
				registerReceiver(mountedReceiver, intentFilter);
			}catch(Exception e ){
				Log.v("TAG", e.getMessage());
			}
	
		}
		
	}
	
	BroadcastReceiver mountedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			try {
				if(appref.mScrollLayout.getChildCount() == 0){
					initContents();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	};
	
	public void initCountDownTimer() {

		long entime = Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000;
		long restime = Integer.parseInt(Constants.restime == "" ? "5" : Constants.restime) * 60 * 1000;
		
		//long entime = 1 * 60 * 1000;
		
		//
		
		// long restime = 1 * 60 * 1000;

		gameCountDown = new DuoleCountDownTimer(entime, Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
			}

			@Override
			public void onFinish() {
//				appref.startMusicPlay();
				Constants.ENTIME_OUT = true;
				this.setTotalTime(Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000);
				this.seek(0);
				restCountDown.start();
				// appref.startMusicPlay();
			}

		};

		restCountDown = new DuoleCountDownTimer(restime,
				Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				Constants.ENTIME_OUT = false;
				this.setTotalTime(Integer.parseInt(Constants.restime == "" ? "5" : Constants.restime) * 60 * 1000);
				this.seek(0);
			}

		};
	}

	public void setBackground() {

		if (!Constants.bgurl.equals("")) {
			File bg = new File(Constants.CacheDir
					+ Constants.bgurl.substring(Constants.bgurl
							.lastIndexOf("/")));
			if (bg.exists()) {
				Drawable d = Drawable.createFromPath(bg
						.getAbsolutePath());
				if(d != null){
					mScrollLayout.setBackgroundDrawable(d);
				}
				
			}
		}
	}

	public void initViews() throws IOException, TransformerException,
			SAXException, XmlPullParserException {

		// get all apps
		Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
				+ "itemlist.xml");
		ArrayList<Asset> temp = new ArrayList<Asset>();
		temp.addAll(Constants.AssetList);
		DuoleUtils.checkFilesExists(temp);
		DuoleUtils.addNetworkManager(temp);
		getMusicList(temp);

		// the total pages
		int PageCount = (int) Math.ceil(temp.size()
				/ Constants.APP_PAGE_SIZE);
		
		if(PageCount == 0 || (temp.size() % Constants.APP_PAGE_SIZE) > 0){
			PageCount += 1;
		}

		alAIA = new ArrayList<AssetItemAdapter>();
		for (int i = 0; i < PageCount; i++) {
			GridView appPage = new GridView(Duole.appref);
			// get the "i" page data
			appPage.setAdapter(new AssetItemAdapter(Duole.appref,
					temp, i));

			appPage.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			appPage.setNumColumns(Constants.COLUMNS);

			appPage.setPadding(0, 10, 0, 0);

			appPage.setVerticalSpacing(20);

			appPage.setOnItemClickListener(listener);
			mScrollLayout.addView(appPage);
		}

		DuoleUtils.setChildrenDrawingCacheEnabled(mScrollLayout, true);

	}

	public void getMusicList(ArrayList<Asset> assets) {

		Constants.MusicList = new ArrayList<Asset>();

		for (Asset asset : assets) {
			if (asset.getType().equals(Constants.RES_AUDIO)) {

				Constants.MusicList.add(asset);

			}
		}

	}

	/**
	 * The item click event of gridview
	 */
	public OnItemClickListener listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Asset assItem = (Asset) parent.getItemAtPosition(position);

			Intent intent;

			try {
				// launcher the package
				
				

				if (assItem.getType().equals(Constants.RES_AUDIO)) {
					//intent = new Intent(appref, SingleMusicPlayerActivity.class);
					intent = new Intent(appref , SingleMusicPlayerActivity.class);
					int index = Constants.MusicList.indexOf(assItem);

					intent.putExtra("index", index + "");

				} else if(assItem.getType().equals(Constants.RES_APK)){
					
					intent = new Intent();
					intent.setComponent(new ComponentName(assItem.getPackag(),assItem.getActivity()));
					
				}else if(assItem.getType().equals(Constants.RES_CONFIG)){
					intent = new Intent(appref,PasswordActivity.class);
					intent.putExtra("type", "0");
				}else{

					appref.sendBroadcast(new Intent(Constants.Event_AppStart));

					intent = new Intent(appref, FlashPlayerActivity.class);

					if (assItem.getUrl().startsWith("http:")) {
						intent.putExtra("filename", assItem.getUrl());
					} else {
						intent.putExtra(
								"filename",
								assItem.getUrl().substring(
										assItem.getUrl().lastIndexOf("/")));
					}
				}

				mContext.startActivity(intent);
			} catch (ActivityNotFoundException noFound) {
				Toast.makeText(mContext, "Package not found!",
						Toast.LENGTH_SHORT).show();
			}
		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	@Override
	protected void onResume(){
		        
//		new ItemListTask().execute();
		
		Log.v("TAG", appref.mScrollLayout.getChildCount() + "child counts");
		
		if(this.mScrollLayout.getChildCount() <= 0){
			try {
				appref.initContents();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        this.mScrollLayout.refresh();
		super.onResume();
	}

}
