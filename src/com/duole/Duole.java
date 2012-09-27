package com.duole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duole.activity.BaseActivity;
import com.duole.activity.SystemConfigActivity;
import com.duole.asynctask.ItemListTask;
import com.duole.listener.OnScrolledListener;
import com.duole.player.FlashPlayerActivity;
import com.duole.player.SingleMusicPlayerActivity;
import com.duole.player.VideoPlayerActivity;
import com.duole.pojos.CellTag;
import com.duole.pojos.DuoleCountDownTimer;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.AssetDownloadService;
import com.duole.service.BackgroundRefreshService;
import com.duole.service.UnLockScreenService;
import com.duole.service.download.dao.ConfigDao;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.WidgetUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.ScrollLayout;

public class Duole extends BaseActivity {

	public static Duole appref;

	public static DuoleCountDownTimer gameCountDown;
	public static DuoleCountDownTimer restCountDown;

	public static final int INIT_COUNTDOWN = 999;
	public static final int START_WIZARD = 998;
	public static final int SHOW_REFRESH = 997;
	public static final int HIDE_REFRESH = 996;

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
	private boolean viewRefreshing = false;
	private boolean viewRefreshed = false;

	initView iView = new initView();

	ProgressBar pbEnTime;
	ProgressBar pbRefresh;

	public int curPageDiv = 0;

	Asset assItem;

	Thread startActivityForResult;

	public Handler mhandler = new Handler() {

		@SuppressWarnings("unused")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_REFRESH:
				if (pbRefresh != null)
					pbRefresh.setVisibility(View.VISIBLE);
				break;
			case HIDE_REFRESH:
				if (pbRefresh != null)
					pbRefresh.setVisibility(View.INVISIBLE);
				break;

			case INIT_COUNTDOWN:
				initContents();
				break;
			case START_WIZARD:
				ConfigDao cd = new ConfigDao(getApplicationContext());
				Cursor cursor = cd.query("setup");
				cursor.moveToFirst();

				if (cursor == null) {
					break;
				}

				if (cursor.getCount() > 0) {
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
						if (cursor.getString(0).equals("0") || cursor.getString(0) == null) {
							startSetupWizard();
						}
					}
				} else {
					startSetupWizard();
				}

				cursor.close();
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * The connection of background refresh service.
	 */
	public static ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {

			mBoundService = ((BackgroundRefreshService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {

			mBoundService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;

		// final int CWJ_HEAP_SIZE = 32 * 1024 * 1024;
		// VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
		// VMRuntime.getRuntime().setTargetHeapUtilization(0.75f);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// systemSettings();

		setContentView(R.layout.main);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pagedivider);
		bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.pagedividerselected);
		Constants.bmpKe = BitmapFactory.decodeResource(getResources(), R.drawable.ke);
		pbRefresh = (ProgressBar) findViewById(R.id.pbRefresh);

		tvTrafficStats = (TextView) findViewById(R.id.tvTrafficStats);

		Intent screenLock = new Intent(this, UnLockScreenService.class);
		startService(screenLock);

		appref = this;

		mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayoutTest);

		appref = this;
		try {

			enableWifiState();

			initContents();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the setup wizard.
	 */
	private void startTheSetupWizard() {

		Message msg = new Message();
		msg.what = START_WIZARD;
		mhandler.sendMessageDelayed(msg, 1000);

	}

	private void startSetupWizard() {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("duole/setup");
		try {
			startActivity(intent);
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}

	}

	@SuppressWarnings("deprecation")
	private void systemSettings() {

		// Disable the screen lock.
		Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCK_PATTERN_ENABLED, 0);
		// disable usb debug
		Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
		// Disable the auto rotation.
		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
		// Disable the wifi available_notification.
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0);
		// Enable the auto time.
		Settings.System.putInt(getContentResolver(), Settings.System.AUTO_TIME, 1);
		// Set the time out of screen.
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 8 * 60 * 1000);

	}

	/**
	 * Enable the wifi.
	 */
	private void enableWifiState() {

		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wm.isWifiEnabled()) {
			wm.setWifiEnabled(true);
		}

	}

	/**
	 * Intitialize the content.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void initContents() {

		// Check whether tf card exists.
		if (DuoleUtils.checkTFCard()) {
			// init the cache folders.
			if (DuoleUtils.checkCacheFiles()) {

				inited = true;

				// Verify the installation of flash player.
				verifyFlashPlayerInstallation();

				// Init the view.
				try {
					// initViews();
					if (!viewRefreshing) {
						new initView().execute();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
			}

			startTheSetupWizard();

			mHandler.postDelayed(new Runnable() {

				public void run() {
					new ItemListTask().execute();
				}
			}, 60 * 1000);

		} else {
			@SuppressWarnings("unused")
			IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
			try {
				Message msg = new Message();
				msg.what = Constants.REFRESH_CONTENT;
				mHandler.sendMessageDelayed(msg, 2000);
			} catch (Exception e) {
				Log.v("TAG", e.getMessage());
			}
		}

	}

	@Override
	protected void refresh_content() {
		super.refresh_content();
		try {
			initContents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the entermaintent progress bar.
	 */
	private void initEnTimeProgressBar() {

		pbEnTime = (ProgressBar) findViewById(R.id.pbEnTime);

		pbEnTime.setMax(gameCountDown.getTotalTime());
		pbEnTime.setBackgroundColor(Color.RED);
		pbEnTime.setProgress((int) (gameCountDown.getTotalTime() - gameCountDown.getRemainMills()));

		gameCountDown.setPb(pbEnTime);

		if (pbEnTime.getProgress() > 0) {
			gameCountDown.resume();
		}

	}

	public void verifyFlashPlayerInstallation() {

		new Runnable() {

			public void run() {
				long start = System.currentTimeMillis();
				if (!DuoleUtils.verifyInstallationOfAPK(appref, Constants.PKG_FLASH)) {

					boolean installed = false;
					Log.e("TAG", "flash player not installed");

					File file = new File("/sdcard/flashplayer.apk");
					installed = DuoleUtils.installApkFromFile(file);

					if (!installed) {
						AssetManager am = null;
						am = getAssets();

						try {
							InputStream is = am.open("flashplayer.apk");

							int byteread = 0;
							if (is != null) {
								file = new File("/sdcard/flashplayer.apk");
								file.createNewFile();
								FileOutputStream fs = new FileOutputStream(file);
								byte[] buffer = new byte[512];
								while ((byteread = is.read(buffer)) != -1) {
									fs.write(buffer, 0, byteread);
								}
								is.close();
								fs.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (file.exists()) {
							if (DuoleUtils.installApkFromFile(file)) {
								Log.d("TAG", "install flash apk " + (System.currentTimeMillis() - start) / 1000);
								android.os.Process.killProcess(android.os.Process.myPid());
							}
						} else {
							Log.e("TAG", "error redirect to assets");
						}
					}

				}
			}
		}.run();
	}

	/**
	 * BroadcastReceiver to receive tf mounted event.
	 */
	BroadcastReceiver mountedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			try {
				if (appref.mScrollLayout.getChildCount() == 0) {
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
		long current = Long.parseLong(enstart.equals("") ? currentTimeMillis + "" : enstart);

		long entime = Integer.parseInt(Constants.entime == "" ? "120" : Constants.entime) * 60 * 1000;
		long restime = Integer.parseInt(Constants.restime == "" ? "10" : Constants.restime) * 60 * 1000;
		long enSeek = 0;
		long restSeek = 0;

		long poor = System.currentTimeMillis() - current;

		long pool = entime + restime;

		long total = 0;
		try {
			total = pool;
		} catch (Exception e) {
			total = entime + restime;
		}

		Constants.timePool = total;
		long now = (poor) % (total);

		now = Math.abs(now);

		Log.d("TAG", now + "  now");

		if (poor < total) {
			if (poor > 0 && poor < entime) {
				enSeek = poor;
			} else if (poor > 0 && poor < total) {
				restSeek = poor - entime;
				Constants.ENTIME_OUT = true;
				startMusicPlay();
			}
		}

		Log.v("TAG", "entiem " + entime);
		Log.v("TAG", "restime " + restime);

		gameCountDown = new DuoleCountDownTimer(entime, Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
				if (getPb() != null) {
					this.getPb().setProgress((int) (this.getTotalTime() - millisUntilFinished));
				}
			}

			@Override
			public void onFinish() {
				Constants.ENTIME_OUT = true;
				int time = Integer.parseInt(Constants.entime == "" ? "120" : Constants.entime) * 60 * 1000;
				this.setTotalTime(time);
				this.seek(0);
				this.stop();
				getPb().setMax(time);
				getPb().setProgress(0);
				if (restCountDown != null) {
					time = Integer.parseInt(Constants.restime == "" ? "10" : Constants.restime) * 60 * 1000;
					restCountDown.setTotalTime(time);
					restCountDown.seek(0);
				}
				if (!Constants.SLEEP_TIME) {
					appref.startMusicPlay();
				}

				initEnTimeProgressBar();
			}

		};

		restCountDown = new DuoleCountDownTimer(restime, Constants.countInterval) {

			@Override
			public void onTick(long millisUntilFinished, int percent) {
				if (getPb() != null) {
					this.getPb().setProgress((int) (this.getTotalTime() - millisUntilFinished));
				}
			}

			@Override
			public void onFinish() {
				Constants.ENTIME_OUT = false;
				int time = Integer.parseInt(Constants.restime == "" ? "10" : Constants.restime) * 60 * 1000;
				this.setTotalTime(time);
				this.seek(0);
				this.stop();
				if (getPb() != null) {
					getPb().setMax(time);
					getPb().setProgress(0);
				}

				if (gameCountDown != null) {
					time = Integer.parseInt(Constants.entime == "" ? "120" : Constants.entime) * 60 * 1000;
					gameCountDown.setTotalTime(time);
					gameCountDown.seek(0);

					initEnTimeProgressBar();
				}

				appref.sendBroadcast(new Intent("com.duole.restime.out"));
			}
		};

		gameCountDown.seekMills(enSeek);
		restCountDown.seekMills(restSeek);

		initEnTimeProgressBar();
	}

	/**
	 * Set the background of main page.
	 */
	public void setBackground() {
		LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
		if (!Constants.bgurl.equals("")) {
			File bg = new File(Constants.CacheDir + Constants.bgurl.substring(Constants.bgurl.lastIndexOf("/")));
			if (bg.exists()) {
				try {
					Drawable d = Drawable.createFromPath(bg.getAbsolutePath());
					if (d != null) {
						llMain.setBackgroundDrawable(d);
					} else {
						bg.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class initView extends AsyncTask {

		ArrayList<AssetItemAdapter> alAssetAdapter;

		ArrayList<ArrayList<Asset>> alJinzixuan;

		@Override
		protected Object doInBackground(Object... arg0) {
			Log.d("TAG", "do in background");
			ArrayList<Asset> temp = new ArrayList<Asset>();
			viewRefreshing = true;
			alAssetAdapter = new ArrayList<AssetItemAdapter>();
			try {
				// Get the current version of the system.
				Constants.System_ver = DuoleUtils.getVersion(appref);
				// get all apps
				Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir + "itemlist.xml");

				// Get the system configuration.
				XmlUtils.readConfiguration();

				// About to deal with the source list.
				temp.addAll(Constants.AssetList);
				// Drop the resources which is not complete.
				temp = DuoleUtils.checkFilesExists(temp);
				// Add the jinzixuan
				if (DuoleUtils.getContentFilterCount(Constants.CONTENT_FILTER_JINZIXUAN, getApplicationContext()) > 0) {
					alJinzixuan = DuoleUtils.addJinzixuanManager(temp);
				}

				// Add the system tweak function to the list.
				DuoleUtils.addNetworkManager(temp);
				// Get the music list.
				getMusicList(temp);

				/** 2012.05.07 **/
				DuoleUtils.getOnlineVideoList(temp);

				// the total pages
				int PageCount = (int) Math.ceil(temp.size() / Constants.APP_PAGE_SIZE);

				if (PageCount == 0 || (temp.size() % Constants.APP_PAGE_SIZE) > 0) {
					PageCount += 1;
				}

				alAIA = new ArrayList<AssetItemAdapter>();

				llPageDivider = (LinearLayout) findViewById(R.id.llPageDividerTip);

				if (alJinzixuan != null) {
					for (ArrayList<Asset> jinzi : alJinzixuan) {
						alAssetAdapter.add(new AssetItemAdapter(Duole.appref, jinzi));
					}
				}

				for (int i = 0; i < PageCount; i++) {
					alAssetAdapter.add(new AssetItemAdapter(Duole.appref, temp, i));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {

			if (!viewRefreshed) {
				viewRefreshed = true;
				mScrollLayout.removeAllViews();

				for (int i = 0; i < alAssetAdapter.size(); i++) {
					GridView appPage = new GridView(Duole.appref);
					// get the "i" page data
					AssetItemAdapter tempAdapter = alAssetAdapter.get(i);
					appPage.setAdapter(tempAdapter);

					appPage.setSelector(R.drawable.grid_selector);

					appPage.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

					appPage.setNumColumns(Constants.COLUMNS);

					appPage.setPadding(40, 10, 40, 0);

					appPage.setVerticalSpacing(30);
					appPage.setGravity(Gravity.CENTER);
					appPage.setColumnWidth(110);

					appPage.setOnItemClickListener(listener);

					CellTag pageTag = new CellTag();
					pageTag.setAssetList(tempAdapter.getAssetList());
					pageTag.setAdapter(tempAdapter);
					appPage.setTag(pageTag);

					mScrollLayout.addView(appPage);

					view = LayoutInflater.from(appref).inflate(R.layout.pagedividerselected, null);

					PageDiv pd = new PageDiv();
					pd.ivPageDiv = (ImageView) view.findViewById(R.id.ivBackground);
					view.setTag(pd);

					llPageDivider.addView(view);

					mScrollLayout.refresh();

					mScrollLayout.postInvalidate();
				}

				setPageDividerSelected(0);

				DuoleUtils.setChildrenDrawingCacheEnabled(mScrollLayout, true);

				// Set the background picture.
				setBackground();

				// Init the count down timer of game and rest time.
				initCountDownTimer();

				// Upload local version.
				DuoleNetUtils.uploadLocalVersionForce();

				// Start refreshing the network traffic status.
				tvTrafficStats = (TextView) findViewById(R.id.tvTrafficStats);

				// Start the asset download service.
				try {
					Uri downloadUri = Uri.parse("content://com.duole.download");
					getContentResolver().insert(downloadUri, new ContentValues());

					sendBroadcast(new Intent("com.duole.init.complete"));

					tvTrafficStats.setVisibility(View.INVISIBLE);
				} catch (Exception e) {
					mhandler.postDelayed(new Runnable() {

						public void run() {
							Intent downService = new Intent(appref, AssetDownloadService.class);
							appref.startService(downService);
						}
					}, 10000);

					Message msgRefresh = new Message();
					msgRefresh.what = Constants.NET_TRAFFIC;
					mHandler.sendMessageDelayed(msgRefresh, 5000);
				}

				// If a update is exists.install it.
				DuoleUtils.instalUpdateApk(appref);

				mScrollLayout.refresh();
				viewRefreshing = false;

				Log.d("TAG", "refresh main view from main finished");

				mScrollLayout.setOnScrolledListener(new OnScrolledListener() {

					@Override
					public void scrolled(final int last, final int index) {
						setPageDividerSelected(index);
					}
				});

				mHandler.postDelayed(new Runnable() {

					public void run() {
						viewRefreshed = false;
					}
				}, 30 * 1000);
				super.onPostExecute(result);
			} else {
				Log.d("TAG", "other bug escapsed ");
			}
		}
	}

	/**
	 * Set the page divider.
	 * 
	 * @param last
	 * @param index
	 */
	void setPageDividerSelected(final int index) {
		mHandler.post(new Runnable() {

			public void run() {
				try {

					if (curPageDiv != index) {
						view = llPageDivider.getChildAt(curPageDiv);
						if (view != null) {
							pageDiv = (PageDiv) view.getTag();
							pageDiv.ivPageDiv.setImageBitmap(bmp);
						}

						view = llPageDivider.getChildAt(index);
						if (view != null) {
							pageDiv = (PageDiv) view.getTag();
							pageDiv.ivPageDiv.setImageBitmap(bmp2);
						}
					} else {
						view = llPageDivider.getChildAt(index);
						if (view != null) {
							pageDiv = (PageDiv) view.getTag();
							pageDiv.ivPageDiv.setImageBitmap(bmp2);
						}

					}

					curPageDiv = index;
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
	}

	/**
	 * Get the music list.
	 * 
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

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			assItem = (Asset) parent.getItemAtPosition(position);
			Constants.gameStartMillis = System.currentTimeMillis();
			Constants.resourceId = assItem.getId();

			SharedPreferences sp = getSharedPreferences("com.duole", MODE_WORLD_READABLE);
			Editor editor = sp.edit();
			editor.putString("antiFatigue", getString(R.string.entime) + " : " + Duole.gameCountDown.getRemainTime() + ";"
					+ getString(R.string.sleepstart) + " : " + Constants.sleepstart + ":00" + "     " + getString(R.string.sleepend) + " : "
					+ Constants.sleepend + ":00");
			editor.commit();

			String frontid = assItem.getFrontID();
			if (frontid != null && !frontid.equals("0")) {
				if (DuoleUtils.getContentFilterCount("duole/pres", getApplicationContext()) > 0) {
					startContentFilterOfPResByPackageName("", frontid, Constants.CacheDir + "/front/", assItem);
				} else if (DuoleUtils.verifyInstallationOfAPK(appref, Constants.PKG_PRIORITY)) {
					startActivityForResultByPackageName(Constants.PKG_PRIORITY, frontid, Constants.CacheDir + "/front/", assItem);
				} else {
					startItem(assItem);
				}
			} else {
				startItem(assItem);
			}

		}

	};

	/**
	 * Start the item selected.
	 * 
	 * @param assItem
	 */
	private void startItem(Asset assItem) {

		Intent intent = null;
		try {
			// launcher the package

			if (assItem.getType().trim().equals("")) {
				assItem.setType(DuoleUtils.checkAssetType(assItem));
			}

			String url = assItem.getUrl().toLowerCase();

			if (url.endsWith(Constants.RES_AUDIO)) {

				intent = new Intent(appref, SingleMusicPlayerActivity.class);
				int index = Constants.MusicList.indexOf(assItem);

				// intent.putExtra("index", index + "");
				intent.putExtra("thumb", assItem.getThumbnail());
				intent.putExtra("mp3", assItem.getUrl());
				intent.putExtra("bg", assItem.getBg());

			}

			// Launch a application.
			if (url.endsWith(Constants.RES_APK) && !assItem.getType().equals(Constants.RES_WIDGET)) {

				pkgName = assItem.getPackag();
				if (pkgName == null) {
					pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
				}
				startActivityByPkgName(pkgName);
			} else if (url.endsWith(Constants.RES_APK)) {
				pkgName = assItem.getPackag();
				if (pkgName == null) {
					pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
				}
				WidgetUtils.startConfigureActivityByPkgName(getApplicationContext(), pkgName);
				overridePendingTransition(R.anim.scalein, R.anim.scaleout);
			}

			// Launch the configure function.
			if (assItem.getType().equals(Constants.RES_CONFIG)) {
				// intent = new Intent(appref, PasswordActivity.class);
				// intent.putExtra("type", "0");
				intent = new Intent(appref, SystemConfigActivity.class);
			}

			// Launch the jinzixuan.
			if (assItem.getType().equals(Constants.RES_JINZIXUAN)) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				Uri uri = Uri.parse("1");
				intent.setDataAndType(uri, assItem.getPackag());
				intent.putExtra("index", assItem.getActivity());
			}

			// Play a video.
			if (url.endsWith(".flv") || url.endsWith(".rmvb") || url.endsWith(".rm") || url.endsWith(".mp4") || url.endsWith(".3gp")
					|| url.endsWith(".mkv") || url.endsWith(".avi")) {
				intent = new Intent(appref, VideoPlayerActivity.class);

				Uri uri = null;
				if (assItem.getUrl().startsWith("http:")) {
					uri = Uri.parse(assItem.getUrl());
				} else {
					uri = Uri.parse("file://" + Constants.CacheDir + "/" + assItem.getType() + "/"
							+ assItem.getUrl().substring(assItem.getUrl().lastIndexOf("/")));
				}

				Log.d("TAG", uri.getPath());

				intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setType("duolevideo/*");
				intent.setDataAndType(uri, "duolevideo/*");

				try {
					appref.startActivity(intent);
				} catch (Exception e) {
					new AlertDialog.Builder(appref).setTitle(R.string.player_notfound_title).setMessage(R.string.player_tip)
							.setNegativeButton(R.string.btnClose, null).create().show();
				}
			}
			// Play a flash.
			if (assItem.getUrl().toLowerCase().endsWith("swf")) {

				intent = new Intent(appref, FlashPlayerActivity.class);

				if (assItem.getUrl().startsWith("http:")) {
					intent.putExtra("filename", assItem.getUrl());
				} else {
					intent.putExtra("filename", assItem.getUrl().substring(assItem.getUrl().lastIndexOf("/")));
				}
			}

			// Entertainment time out.
			// if (!Constants.ENTIME_OUT) {
			appref.sendBroadcast(new Intent(Constants.Event_AppStart));
			// }

			// If not a application.
			if (!assItem.getType().equals(Constants.RES_APK) && !assItem.getType().equals(Constants.RES_WIDGET)) {
				mContext.startActivity(intent);
				overridePendingTransition(R.anim.scalein, R.anim.scaleout);
			}
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use a package name to launch a activity installed.
	 * 
	 * @param pkgname
	 */
	private void startActivityByPkgName(String pkgname) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		try {
			List<ResolveInfo> lri = DuoleUtils.findActivitiesForPackage(appref, pkgName);

			if (lri.size() > 0) {
				for (ResolveInfo ri : lri) {
					intent.setComponent(new ComponentName(pkgName, ri.activityInfo.name));
					startActivity(intent);
					overridePendingTransition(R.anim.scalein, R.anim.scaleout);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void startContentFilterOfPResByPackageName(String pkgname, String frontid, String basepath, Asset asset) {

		Intent intent = new Intent(Intent.ACTION_VIEW);

		try {
			if (asset.getType().equals(Constants.RES_APK)) {
				pkgName = assItem.getPackag();
				if (pkgName == null) {
					pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
				}
				Uri uri = Uri.parse(basepath + "," + frontid + "," + pkgName);

				intent.setDataAndType(uri, "duole/pres");

				startActivity(intent);
				pkgName = pkgname;
			} else {
				Uri uri = Uri.parse(basepath + "," + frontid + ", ");

				intent.setDataAndType(uri, "duole/pres");

				startActivityForResult(intent, 1);
			}
			overridePendingTransition(R.anim.scalein, R.anim.scaleout);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Start a activity result of package.
	 * 
	 * @param packagename
	 * @param frontid
	 * @param basePath
	 */
	private void startActivityForResultByPackageName(String packagename, String frontid, String basePath, Asset asset) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		SharedPreferences sp = getSharedPreferences("com.duole", MODE_WORLD_READABLE);
		Editor editor = sp.edit();
		editor.putString("id", frontid);
		editor.putString("base", basePath);

		editor.commit();

		try {
			List<ResolveInfo> lri = DuoleUtils.findActivitiesForPackage(appref, packagename);

			if (lri.size() > 0) {
				for (ResolveInfo ri : lri) {
					intent.setComponent(new ComponentName(packagename, ri.activityInfo.name));
					if (asset.getType().equals(Constants.RES_APK)) {
						pkgName = assItem.getPackag();
						if (pkgName == null) {
							pkgName = FileUtils.getPackagenameFromAPK(appref, assItem);
						}
						editor.putString("package", pkgName);
						editor.commit();
						startActivity(intent);
						pkgName = packagename;
					} else {
						startActivityForResult(intent, 1);
					}
					overridePendingTransition(R.anim.scalein, R.anim.scaleout);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Activity result.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.d("TAG", resultCode + "   reaultde");
		if (startActivityForResult != null) {
			startActivityForResult.notify();
		} else if (resultCode == 2) {
			startItem(assItem);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Unbind the background auto refresh service.
	 */
	public void unBindAutoRefreshService() {
		try {
			unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Bind a auto refresh service.
	 */
	public void bindAutoRefreshService() {
		mhandler.post(new Runnable() {

			public void run() {
				Duole.appref.bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);
			}
		});
	}

	/**
	 * When activity destroed.
	 */
	@Override
	protected void onDestroy() {

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
	protected void onResume() {

		super.onResume();

		Log.e("TAG", "on resume");

		overridePendingTransition(R.anim.scalein, R.anim.scaleout);

		if (pkgName != null && !pkgName.equals("")) {
			forceStopActivity();
			uploadGamePeriod();
			pkgName = "";

			// Clear the package name in the shared preference.
			SharedPreferences sp = getSharedPreferences("com.duole", MODE_WORLD_READABLE);
			Editor editor = sp.edit();
			editor.clear();
			editor.commit();
		}

		if (this.mScrollLayout.getChildCount() <= 0) {
			try {
				appref.initContents();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.mScrollLayout.refresh();

		Log.d("TAG", "Constants.entime_out  " + Constants.ENTIME_OUT);

		if ((Constants.ENTIME_OUT && !Constants.musicPlayerIsRunning)) {
			startMusicPlay();
		}

	}

	// @Override
	// public void onAttachedToWindow() {
	// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	// super.onAttachedToWindow();
	// }
	//
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	//
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_HOME:
	//
	// mScrollLayout.snapToScreen(0);
	//
	// break;
	// }
	// return true;
	// }

	/**
	 * The class of divider.
	 * 
	 * @author taoliang
	 * 
	 */
	public class PageDiv {
		public ImageView ivPageDiv;
		public TextView tvIndex;
	}
}
