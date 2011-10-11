package com.duole;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ActivityManager;
import android.app.ExpandableListActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.activity.BaseActivity;
import com.duole.activity.PasswordActivity;
import com.duole.activity.SystemTweakActivity;
import com.duole.asynctask.ItemListTask;
import com.duole.listener.OnScrolledListener;
import com.duole.player.FlashPlayerActivity;
import com.duole.player.SingleMusicPlayerActivity;
import com.duole.player.VideoPlayerActivity;
import com.duole.pojos.DuoleCountDownTimer;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.BackgroundRefreshService;
import com.duole.service.UnLockScreenService;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.ScrollLayout;

public class Duole extends BaseActivity {

	public static Duole appref;

	public static DuoleCountDownTimer gameCountDown;
	public static DuoleCountDownTimer restCountDown;
	
	public LinearLayout llPageDivider;
	View view;
	LayoutInflater inflater;
	TextView tvIndex;
	
	Bitmap bmp;
	Bitmap bmp2;
	
	PageDiv pageDiv;

	private static final String TAG = "TAG";
	public ScrollLayout mScrollLayout;
	private static Context mContext;
	private static BackgroundRefreshService mBoundService;
	public static ArrayList<AssetItemAdapter> alAIA;
	
	ProgressBar pbEnTime;

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

		setContentView(R.layout.main);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pagedivider);
		bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.pagedividerselected);
		Constants.bmpKe = BitmapFactory.decodeResource(getResources(), R.drawable.ke);

		Intent screenLock = new Intent(this,UnLockScreenService.class);
		startService(screenLock);
		
		appref = this;
		
		mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayoutTest);

		appref = this;
		try {

			initContents();
			
			enableWifiState();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mScrollLayout.setOnScrolledListener(new OnScrolledListener(){

			@Override
			public void scrolled(final int last, final int index) {
				setPageDividerSelected(last,index);
			}
		});
	}
	
	private void enableWifiState(){
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(!wm.isWifiEnabled()){
			wm.setWifiEnabled(true);
		}
	}
	
	private void initEnTimeProgressBar(){
		
		pbEnTime = (ProgressBar)findViewById(R.id.pbEnTime);
		
		pbEnTime.setMax(appref.gameCountDown.getTotalTime());
		pbEnTime.setBackgroundColor(Color.RED);
		
		gameCountDown.setPb(pbEnTime);
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
				
				initEnTimeProgressBar();
			} else {
				Toast.makeText(this, R.string.itemlist_lost, 2000).show();
			}

			new ItemListTask().execute();

		} else {
			Toast.makeText(this, R.string.tf_unmounted, 2000).show();
			
			IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
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

		long currentTimeMillis = System.currentTimeMillis();
		String enstart = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_LASTENSTART);
		final long current = Long.parseLong(enstart.equals("") ? currentTimeMillis+"" : enstart);

		long entime = Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000;
		long restime = Integer.parseInt(Constants.restime == "" ? "5" : Constants.restime) * 60 * 1000;
		
		long poor = System.currentTimeMillis() - current;
		long now = (poor) % (entime + restime);
		
		now = Math.abs(now);

		Log.v("TAG", now + "");
		if(now > 0 && now < entime){
			entime -= now;
			Log.v("TAG", entime + "entime");
		}else if(now > 0 && now < entime + restime){
			appref.startMusicPlay();
			Log.v("TAG", restime + "entime");
		}

		gameCountDown = new DuoleCountDownTimer(entime, Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
				if(getPb() != null){
					this.getPb().setProgress((int)(this.getTotalTime() - millisUntilFinished));
				}
			}

			@Override
			public void onFinish() {
				Constants.ENTIME_OUT = true;
				int time = Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000;
				this.setTotalTime(time);
				this.seek(0);
				this.stop();
				getPb().setMax(time);
				getPb().setProgress(0);
				if(!Constants.SLEEP_TIME){
					appref.startMusicPlay();
				}
			}

		};
		
		restCountDown = new DuoleCountDownTimer(restime,
				Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
				if(getPb() != null){
					this.getPb().setProgress((int)(this.getTotalTime() - millisUntilFinished));
				}
				
			}

			@Override
			public void onFinish() {
				Constants.ENTIME_OUT = false;
				int time = Integer.parseInt(Constants.restime == "" ? "5" : Constants.restime) * 60 * 1000;
				this.setTotalTime(time);
				this.seek(0);
				this.stop();
				getPb().setMax(time);
				getPb().setProgress(0);
				appref.sendBroadcast(new Intent("com.duole.restime.out"));
			}
		};

	}

	public void setBackground() {
		
		LinearLayout llMain = (LinearLayout)findViewById(R.id.llMain);
		if (!Constants.bgurl.equals("")) {
			File bg = new File(Constants.CacheDir
					+ Constants.bgurl.substring(Constants.bgurl
							.lastIndexOf("/")));
			if (bg.exists()) {
				Drawable d = Drawable.createFromPath(bg
						.getAbsolutePath());
				if(d != null){
					llMain.setBackgroundDrawable(d);
				}
			}
		}
	}

	public void initViews() throws IOException, TransformerException,
			SAXException, XmlPullParserException, ParserConfigurationException {

		Constants.System_ver = DuoleUtils.getVersion(appref);
		// get all apps
		Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
				+ "itemlist.xml");
		XmlUtils.readConfiguration();
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

			appPage.setPadding(40, 10, 40,0);

			appPage.setVerticalSpacing(30);
			appPage.setColumnWidth(110);

			appPage.setOnItemClickListener(listener);
			mScrollLayout.addView(appPage);
		}
		
		llPageDivider = (LinearLayout) findViewById(R.id.llPageDividerTip);

		for(int i = 0 ; i< PageCount; i++){
			view = LayoutInflater.from(this).inflate(R.layout.pagedividerselected, null);
			
			PageDiv pd = new PageDiv();
			pd.ivPageDiv = (ImageView) view.findViewById(R.id.ivBackground);
			view.setTag(pd);
			
			llPageDivider.addView(view);
		}
		
		setPageDividerSelected(0,0);
		
		DuoleUtils.setChildrenDrawingCacheEnabled(mScrollLayout, true);

	}
	
	void setPageDividerSelected(final int last,final int index){

		mHandler.post(new Runnable(){

			public void run() {
				try{
					if(last != index){
						view = llPageDivider.getChildAt(last);
						if(view != null){
							pageDiv = (PageDiv) view.getTag();
							pageDiv.ivPageDiv.setImageBitmap(bmp);
						}
						
						view = llPageDivider.getChildAt(index);
						pageDiv = (PageDiv) view.getTag();
						pageDiv.ivPageDiv.setImageBitmap(bmp2);
					}else{
						view = llPageDivider.getChildAt(index);
						if(view != null){
							pageDiv = (PageDiv) view.getTag();
							pageDiv.ivPageDiv.setImageBitmap(bmp2);
						}

					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			
		});
		
		
		
	
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
			playStart = System.currentTimeMillis();
			resourceId = assItem.getId();
			Intent intent = null;
			try {
				// launcher the package
				if (assItem.getType().equals(Constants.RES_AUDIO)) {

					intent = new Intent(appref , SingleMusicPlayerActivity.class);
					int index = Constants.MusicList.indexOf(assItem);

					intent.putExtra("index", index + "");

				} else if(assItem.getType().equals(Constants.RES_APK)){
					
					
					intent = new Intent();
					intent.setComponent(new ComponentName(assItem.getPackag(),assItem.getActivity()));
					intent.setAction(Intent.ACTION_MAIN);  
					intent.addCategory(Intent.CATEGORY_LAUNCHER);  
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
					intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
					
					pkgName = assItem.getPackag();
					
					boolean startactivity = true;
					int activities = 1;
					while(startactivity){
						try{
							startActivity(intent);
							startactivity = false;
						}catch(Exception e){
							e.printStackTrace();
							
							PackageManager pm = Duole.appref.getPackageManager();
							File file = new File(Constants.CacheDir + Constants.RES_APK + assItem.getUrl().substring(assItem.getUrl().lastIndexOf("/")));

							PackageInfo info;
							info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
							
							intent.setComponent(new ComponentName(assItem.getPackag(),info.activities[activities].name));

							intent.setAction(Intent.ACTION_MAIN);  
							intent.addCategory(Intent.CATEGORY_LAUNCHER);  
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
							intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
							
							if(activities < info.activities.length - 1){
								activities ++;
							}else{
								
								startactivity = false;
							}
							
						}
						
					}
					
					
					
				}else if(assItem.getType().equals(Constants.RES_CONFIG)){
					intent = new Intent(appref,PasswordActivity.class);
					intent.putExtra("type", "0");
				}else if(assItem.getType().equals(Constants.RES_CONFIG_STATUS)){
						intent = new Intent(appref,SystemTweakActivity.class);
						
				}else if (assItem.getType().equals(Constants.RES_VIDEO) && !assItem.getUrl().endsWith(".swf") && !assItem.getUrl().endsWith(".flv")){
					intent = new Intent(appref , VideoPlayerActivity.class);

					if (assItem.getUrl().startsWith("http:")) {
						intent.putExtra("filename", assItem.getUrl());
					} else {
						intent.putExtra(
								"filename",
								assItem.getUrl().substring(
										assItem.getUrl().lastIndexOf("/")));
					}
				}else{

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
				
				if(!Constants.ENTIME_OUT){
					appref.sendBroadcast(new Intent(Constants.Event_AppStart));
				}
				

				if(!assItem.getType().equals(Constants.RES_APK)){
					mContext.startActivity(intent);
				}
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
			
			Log.v("TAG", "power off");
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}
	
	@Override
	protected void onResume(){
		
		Log.v("TAG", "on resume");
		if(pkgName != null && !pkgName.equals(""))
			forceStopActivity();
		
		uploadGamePeriod();
		
		if(this.mScrollLayout.getChildCount() <= 0){
			try {
				appref.initContents();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        this.mScrollLayout.refresh();
		super.onResume();
	}
	
	public class PageDiv {
		public ImageView ivPageDiv;
		public TextView tvIndex;
	}

}


