package com.duole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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
import com.duole.asynctask.ItemListTask;
import com.duole.listener.OnScrolledListener;
import com.duole.player.FlashPlayerActivity;
import com.duole.player.MusicPlayerActivity;
import com.duole.player.SingleMusicPlayerActivity;
import com.duole.player.VideoPlayerActivity;
import com.duole.pojos.DuoleCountDownTimer;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.AssetDownloadService;
import com.duole.service.BackgroundRefreshService;
import com.duole.service.UnLockScreenService;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
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

	public ScrollLayout mScrollLayout;
	private static Context mContext;
	public static BackgroundRefreshService mBoundService;
	public static ArrayList<AssetItemAdapter> alAIA;
	
	ProgressBar pbEnTime;
	
	public int curPageDiv = 0;
	
	Asset assItem;
	
	Thread startActivityForResult;
	
	/**
	 * The connection of background refresh service.
	 */
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

//		systemSettings();
		
		setContentView(R.layout.main);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pagedivider);
		bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.pagedividerselected);
		Constants.bmpKe = BitmapFactory.decodeResource(getResources(), R.drawable.ke);

		Intent screenLock = new Intent(this,UnLockScreenService.class);
		Intent downService = new Intent(this,AssetDownloadService.class);
		
		startService(screenLock);
		startService(downService);
		
		appref = this;
		
		mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayoutTest);

		appref = this;
		try {

			enableWifiState();
			
			initContents();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mScrollLayout.setOnScrolledListener(new OnScrolledListener(){

			@Override
			public void scrolled(final int last, final int index) {
				setPageDividerSelected(index);
			}
		});
	}
	
	private void systemSettings(){
		
		//Disable the screen lock.
		Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCK_PATTERN_ENABLED, 0);
		//disable usb debug
		Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
		//Disable the auto rotation.
		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
		//Disable the wifi available_notification.
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0);
		//Enable the auto time.
		Settings.System.putInt(getContentResolver(), Settings.System.AUTO_TIME, 1);
		//Set the time out of screen.
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 5 * 60 * 1000);
		
	}
	
	/**
	 * Enable the wifi.
	 */
	private void enableWifiState(){
		
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(!wm.isWifiEnabled()){
			wm.setWifiEnabled(true);
		}
		
	}
	
	/**
	 * Intitialize the content.
	 * @throws Exception
	 */
	public void initContents()  throws Exception{
		// Check whether tf card exists.
		if (DuoleUtils.checkTFCard()) {
			// init the cache folders.
			if (DuoleUtils.checkCacheFiles()) {
				// init the main view.
				
				//If a update is exists.install it.
				DuoleUtils.instalUpdateApk(appref);
				//Verify the installation of flash player.
				verifyFlashPlayerInstallation();
				//Init the view.
				initViews();

				//Set the background picture.
				setBackground();
				
				//Init the count down timer of game and rest time.
				initCountDownTimer();
				
				//Upload local version.
				DuoleNetUtils.uploadLocalVersionForce();
				
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
	
	/**
	 * Initialize the entermaintent progress bar.
	 */
	private void initEnTimeProgressBar(){
		pbEnTime = (ProgressBar)findViewById(R.id.pbEnTime);
		
		pbEnTime.setMax(appref.gameCountDown.getTotalTime());
		pbEnTime.setBackgroundColor(Color.RED);
		
		gameCountDown.setPb(pbEnTime);
	}
	
	public void verifyFlashPlayerInstallation(){
		if(!DuoleUtils.verifyInstallationOfAPK(this, Constants.PKG_FLASH)){
			
			boolean installed = false;
			Log.e("TAG", "flash player not installed");
			
			File file = new File("/sdcard/");
			for(File tempapk : file.listFiles()){
				if(tempapk.getName().toLowerCase().endsWith(".apk")){
					installed = DuoleUtils.installApkFromFile(tempapk);
				}
			}
			
			if(!installed){
				AssetManager am = null;
				am = getAssets();
				
				try {
					InputStream is = am.open("flashplayer.apk");
					
					int bytesum = 0;
					int byteread = 0;
					if (is != null) {
						file = new File("/sdcard/flashplayer.apk");
						file.createNewFile();
						FileOutputStream fs = new FileOutputStream(file);
						byte[] buffer = new byte[512];
						while ((byteread = is.read(buffer)) != -1) {
							bytesum += byteread;
							fs.write(buffer, 0, byteread);
						}
						is.close();
						fs.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(file.exists()){
					if(DuoleUtils.installApkFromFile(file)){
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				}else{
					Log.e("TAG", "error redirect to assets");
				}
			}
			
		}
	}
	
	/**
	 * BroadcastReceiver to receive tf mounted event.
	 */
	BroadcastReceiver mountedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			try {
				if(appref.mScrollLayout.getChildCount() == 0){
					initContents();
				}
				
			} catch (Exception e) {
			}
			
		}
		
	};
	
	/**
	 * Init count down timers.
	 */
	public void initCountDownTimer() {
		
		long currentTimeMillis = System.currentTimeMillis();
		String enstart = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_LASTENSTART);
		long current = Long.parseLong(enstart.equals("") ? currentTimeMillis+"" : enstart);

		long entime = Integer.parseInt(Constants.entime == "" ? "30" : Constants.entime) * 60 * 1000;
		long restime = Integer.parseInt(Constants.restime == "" ? "30" : Constants.restime) * 60 * 1000;
		
		long poor = System.currentTimeMillis() - current;
		
		String pool = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_TIMEPOOL);
		
		long total = 0;
		try{
			total = Integer.parseInt(pool);
		}catch (Exception e) {
			total = entime + restime;
		}

		Constants.timePool = total;
		long now = (poor) % (total);
		
		now = Math.abs(now);
		
		if(poor < total){
			if(poor > 0 && poor < entime){
				entime -= poor;
			}else if(poor > 0 && poor < total){
				restime = total - poor;
				Constants.ENTIME_OUT = true;
			}
			
			Log.d("TAG", entime + " entime , " + restime + " rest time");
		}
		
		Log.v("TAG","entiem " + entime);
		Log.v("TAG","restime " + restime);

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
				if(restCountDown != null){
					time = Integer.parseInt(Constants.restime == "" ? "30" : Constants.restime) * 60 * 1000;
					restCountDown.setTotalTime(time);
				}
				if (!Constants.SLEEP_TIME) {
					appref.startMusicPlay();
				}
				
				initEnTimeProgressBar();
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
				int time = Integer.parseInt(Constants.restime == "" ? "30" : Constants.restime) * 60 * 1000;
				this.setTotalTime(time);
				this.seek(0);
				this.stop();
				getPb().setMax(time);
				getPb().setProgress(0);
				
				appref.sendBroadcast(new Intent("com.duole.restime.out"));
			}
		};
		
		initEnTimeProgressBar();
	}

	/**
	 * Set the background of main page.
	 */
	public void setBackground() {
		LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
		if (!Constants.bgurl.equals("")) {
			File bg = new File(Constants.CacheDir
					+ Constants.bgurl.substring(Constants.bgurl
							.lastIndexOf("/")));
			if (bg.exists()) {
				try {
					Drawable d = Drawable.createFromPath(bg.getAbsolutePath());
					if (d != null) {
						llMain.setBackgroundDrawable(d);
					}else{
						bg.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Init the grid scroll view.
	 * @throws IOException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws XmlPullParserException
	 * @throws ParserConfigurationException
	 */
	public void initViews() throws IOException, TransformerException,
			SAXException, XmlPullParserException, ParserConfigurationException {
		//Get the current version of the system.
		Constants.System_ver = DuoleUtils.getVersion(appref);
		// get all apps
		Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
				+ "itemlist.xml");
		
		//Get the system configuration.
		XmlUtils.readConfiguration();
		ArrayList<Asset> temp = new ArrayList<Asset>();
		//About to deal with the source list.
		temp.addAll(Constants.AssetList);
		//Drop the resources which is not complete.
		temp = DuoleUtils.checkFilesExists(temp);
		//Add the system tweak function to the list.
		DuoleUtils.addNetworkManager(temp);
		//Get the music list.
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
		
		setPageDividerSelected(0);
		
		DuoleUtils.setChildrenDrawingCacheEnabled(mScrollLayout, true);
		
		mScrollLayout.refresh();

	}
	
	/**
	 * Set the page divider.
	 * @param last
	 * @param index
	 */
	void setPageDividerSelected(final int index){
		mHandler.post(new Runnable(){

			public void run() {
				try{
					
					if(curPageDiv != index){
						view = llPageDivider.getChildAt(curPageDiv);
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
					
					curPageDiv = index;
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			
		});
	}

	/**
	 * Get the music list.
	 * @param assets
	 */
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
			assItem = (Asset) parent.getItemAtPosition(position);
			Constants.gameStartMillis = System.currentTimeMillis();
			Constants.resourceId = assItem.getId();

			SharedPreferences sp = getSharedPreferences("com.duole",
					MODE_WORLD_READABLE);
			Editor editor = sp.edit();
			editor.putString("antiFatigue", getString(R.string.entime) + " : "
					+ Duole.appref.gameCountDown.getRemainTime() + ";"
					+ getString(R.string.sleepstart) + " : "
					+ Constants.sleepstart + ":00" + "     "
					+ getString(R.string.sleepend) + " : " + Constants.sleepend
					+ ":00");
			editor.commit();
			
			String frontid = assItem.getFrontID();
			if(frontid != null && !frontid.equals("0")){
				if(DuoleUtils.verifyInstallationOfAPK(appref, Constants.PKG_PRIORITY)){
					startActivityForResultByPackageName(Constants.PKG_PRIORITY,frontid,Constants.CacheDir + "/front/",assItem);
				}else{
					startItem(assItem);
				}
			}else{
				startItem(assItem);
			}
			
		}

	};
	
	/**
	 * Start the item selected.
	 * @param assItem
	 */
	private void startItem(Asset assItem) {
		
		Intent intent = null;
		try {
			// launcher the package
			
			if(assItem.getType().trim().equals("")){
				assItem.setType(DuoleUtils.checkAssetType(assItem));
			}
			
			String url = assItem.getUrl().toLowerCase();
			
			if (url.endsWith(Constants.RES_AUDIO)) {

				intent = new Intent(appref, SingleMusicPlayerActivity.class);
				int index = Constants.MusicList.indexOf(assItem);

				intent.putExtra("index", index + "");

			}
			
			// Launch a application.
			if (url.endsWith(Constants.RES_APK)) {

				pkgName = assItem.getPackag();
				if(pkgName == null){
					pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
				}
				startActivityByPkgName(pkgName);
			}
			// Launch the configure function.
			if (assItem.getType().equals(Constants.RES_CONFIG)) {
				intent = new Intent(appref, PasswordActivity.class);
				intent.putExtra("type", "0");
			}
			
			// Play a video.
			if (url.endsWith(".flv") || url.endsWith(".rmvb")
					|| url.endsWith(".rm") || url.endsWith(".mp4")
					|| url.endsWith(".3gp") || url.endsWith(".mkv")
					|| url.endsWith(".avi")) {
				intent = new Intent(appref, VideoPlayerActivity.class);

				Uri uri = null;
				if (assItem.getUrl().startsWith("http:")) {
					uri = Uri.parse(assItem.getUrl());
				} else {
					uri = Uri.parse("file://" + Constants.CacheDir + "/"+ assItem.getType() +"/" + assItem.getUrl().substring(
							assItem.getUrl().lastIndexOf("/")));
				}
				
				Log.d("TAG",uri.getPath());
				
				intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setType("duolevideo/*");
				intent.setDataAndType(uri, "duolevideo/*");

				try {
					appref.startActivity(intent);
				} catch (Exception e) {
					new AlertDialog.Builder(appref)
							.setTitle(R.string.player_notfound_title)
							.setMessage(R.string.player_tip)
							.setNegativeButton(R.string.btnClose, null)
							.create().show();
				}
			}
			// Play a flash.
			if (assItem.getUrl().toLowerCase().endsWith("swf")) {

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

			// Entertainment time out.
//			if (!Constants.ENTIME_OUT) {
				appref.sendBroadcast(new Intent(Constants.Event_AppStart));
//			}

			// If not a application.
			if (!assItem.getType().equals(Constants.RES_APK)) {
				mContext.startActivity(intent);
				overridePendingTransition(R.anim.scalein, R.anim.scaleout);
			}
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Use a package name to launch a activity installed.
	 * @param pkgname
	 */
	private void startActivityByPkgName(String pkgname){
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		
		try{
			List<ResolveInfo> lri = DuoleUtils
					.findActivitiesForPackage(appref, pkgName);

			if (lri.size() > 0) {
				for (ResolveInfo ri : lri) {
					intent.setComponent(new ComponentName(pkgName,
							ri.activityInfo.name));
					startActivity(intent);
					overridePendingTransition(R.anim.scalein, R.anim.scaleout);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Start a activity result of package.
	 * @param packagename
	 * @param frontid
	 * @param basePath
	 */
	private void startActivityForResultByPackageName(String packagename,String frontid,String basePath, Asset asset){
		
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		
		SharedPreferences sp = getSharedPreferences("com.duole",
				MODE_WORLD_READABLE);
		Editor editor = sp.edit();
		editor.putString("id", frontid);
		editor.putString("base", basePath);

		editor.commit();

		try{
			List<ResolveInfo> lri = DuoleUtils
					.findActivitiesForPackage(appref, packagename);

			if (lri.size() > 0) {
				for (ResolveInfo ri : lri) {
					intent.setComponent(new ComponentName(packagename,
							ri.activityInfo.name));
					if(asset.getType().equals(Constants.RES_APK)){
						pkgName = assItem.getPackag();
						if(pkgName == null){
							pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
						}
						editor.putString("package", pkgName);
						editor.commit();
						startActivity(intent);
					}else{
						startActivityForResult(intent, 1);
					}
					overridePendingTransition(R.anim.scalein, R.anim.scaleout);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Activity result.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.d("TAG"	, resultCode + "   reaultde");
		if(startActivityForResult != null){
			startActivityForResult.notify();
		}else{
			startItem(assItem);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Unbind the background auto refresh service.
	 */
	public void unBindAutoRefreshService(){
		try{
			unbindService(mConnection);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Bind a auto refresh service.
	 */
	public void bindAutoRefreshService(){
		Duole.appref.bindService(getIntent(), appref.mConnection,
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * When activity destroed.
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			unbindService(mConnection);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

		super.onDestroy();
	}
	
	/**
	 * On resume.
	 */
	@Override
	protected void onResume(){
		
		Log.e("TAG", "on resume");
		
		overridePendingTransition(R.anim.scalein, R.anim.scaleout);
		if(pkgName != null && !pkgName.equals("")){
			forceStopActivity();
			uploadGamePeriod();
			pkgName = "";
			
			//Clear the package name in the shared preference.
			SharedPreferences sp = getSharedPreferences("com.duole",
					MODE_WORLD_READABLE);
			Editor editor = sp.edit();
			editor.clear();
			editor.commit();
		}
		
		if(this.mScrollLayout.getChildCount() <= 0){
			try {
				appref.initContents();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        this.mScrollLayout.refresh();
        
		if ((Constants.ENTIME_OUT && !Constants.musicPlayerIsRunning)) {
			Log.e("TAG","start music player rest");
			startMusicPlay();
		}
		
		super.onResume();
	}
	

//	@Override
//	public void onAttachedToWindow() {
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
//		super.onAttachedToWindow();
//	}
//	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_HOME:
//			
//			mScrollLayout.snapToScreen(0);
//			
//			break;
//		}
//		return true;
//	}
	
	/**
	 * The class of divider.
	 * @author taoliang
	 *
	 */
	public class PageDiv {
		public ImageView ivPageDiv;
		public TextView tvIndex;
	}
}


