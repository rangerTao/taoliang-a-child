package com.duole.utils;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;

import com.duole.pojos.asset.Asset;
import com.duole.thread.DownloadTaskQueue;

public class Constants {

	public final static int WIFI_STATUS = 999;
	public final static int WIFI_CONNECTIONINFO = 998;
	public static Handler handler;
	// Download items thread
	public static DownloadFileUtils dfu = new DownloadFileUtils();

	// Image pool
	public static HashMap<String, SoftReference<Bitmap>> imagePool = new HashMap<String, SoftReference<Bitmap>>();

	// The asset list used to delete.
	public static ArrayList<Asset> alAssetDeleteList;
	// the map of download task queue.
	public static HashMap<String, Asset> queueMap = new HashMap<String, Asset>();
	// the download task queue.
	public static DownloadTaskQueue dtq = new DownloadTaskQueue();

	public static Asset assetSynchronized;

	public static String CONTENT_FILTER_JINZIXUAN = "duole/jinzixuan";

	// system flags
	public static boolean musicPlayerIsRunning = false;
	public static boolean clientApkDownloaded = false;
	public static boolean viewrefreshenable = true;

	public static boolean power_save = false;
	// Default storage dir.
	public static final String CacheDir = "/sdcard/DuoleCache/";

	public static final String ZiPass = "dley@910";

	public static final String PKG_FLASH = "com.adobe.flashplayer";
	public static final String PKG_PRIORITY = "com.duole.priorityres";

	public static final String Duole = "http://wvw.duoleyuan.com";
	public static final String siteName = "duoleyuan";
	public static final String UploadGamePeriod = Duole + "/e/member/fava/childuseup.php";
	public static final String ClientUpdate = Duole + "/e/member/child/ancJver.php";
	public static final String resourceUrl = Duole + "/e/member/child/ancJn.php?cc=";

	public static final String DuoleSite = "duoleyuan";

	public static final String ItemList = CacheDir + "itemlist.xml";
	public static final String SystemConfigFile = CacheDir + "config.xml";
	public static String restart = "";
	public static final String TIPSTARTNAME = "restart.mp3";

	public static final String defaultPasswd = "duoleyuan";

	public static boolean sleepTimeDelayed = false;

	public static boolean newItemExists = false;

	public static ArrayList<Asset> AssetList;
	public static ArrayList<Asset> MusicList;
	public static ArrayList<String> OnlineVideoUrlList;
	public static ArrayList<Asset> DownLoadTaskList;
	public static ArrayList<Asset> temp;
	public static ArrayList<Asset> alAsset;

	// Bitmap constant
	public static Bitmap bmpKe;
	public static Bitmap bmpPageDivider;
	public static Bitmap bmpPageDividerSelected;

	// Asset item cache array.
	public static HashMap<String, View> alAssetCache = new HashMap<String, View>();

	// Default num of items in one page.
	public static int APP_PAGE_SIZE = 12;

	public static int COLUMNS = 4;
	//
	public static final int REST_TIME = 1;
	public static final int NET_TRAFFIC = 11;
	public static final int STOP_ACTIVITY = 999;

	public static final int REFRESH_CONTENT = 998;
	public static final int RESTART_REFRESH = 997;

	// Whether app is running.
	public static boolean APP_RUNNING = true;
	// Whether download thread is running.
	public static boolean DOWNLOAD_RUNNING = false;
	// whether entainment time is run out.
	public static boolean ENTIME_OUT = false;
	public static boolean SLEEP_TIME = false;
	public static boolean SCREEN_ON = true;

	// Default type of resources.
	public static final String RES_GAME = "game";
	public static final String RES_AUDIO = "mp3";
	public static final String RES_VIDEO = "video";
	public static final String RES_THUMB = "thumbnail";
	public static final String RES_APK = "apk";
	public static final String RES_WIDGET = "widget";
	public static final String RES_CONFIG = "config";
	public static final String RES_JINZIXUAN = "jinzixuan";
	public static final String RES_CONFIG_STATUS = "status";
	public static final String RES_ABOUT = "about";
	public static final String RES_FRONT = "front";

	// Configuration
	public static String bgurl = "";
	public static String bgRestUrl = "";
	public static String entime = "";
	public static String restime = "";
	public static String sleepstart = "";
	public static String sleepend = "";
	public static String ke = "";
	public static String resourceId = "";
	public static long timePool = 0;
	public static long gameStartMillis = 0l;

	// XmlNode
	public static final String XML_ITEMS = "items";
	public static final String XML_ITEM = "item";
	public static final String XML_ID = "id";
	public static final String XML_TITLE = "title";
	public static final String XML_PIC = "pic";
	public static final String XML_URL = "url";
	public static final String XML_FRONTID = "frontid";
	public static final String XML_ISFRONT = "isfront";
	public static final String XML_PACKAGE = "package";
	public static final String XML_ACTIVITY = "activity";
	public static final String XML_LASTMODIFIED = "lastmodified";
	public static final String XML_MD5 = "md5";
	public static final String XML_TYPE = "type";
	public static final String XML_BG = "bg";
	public static final String XML_RESTBG = "bg1";
	public static final String XML_BGURL = "bgurl";
	public static final String XML_RESTURL = "bgRestUrl";
	public static final String XML_ENTIME = "entime";
	public static final String XML_RESTIME = "restime";
	public static final String XML_SLEEPSTART = "sleepstart";
	public static final String XML_SLEEPEND = "sleepend";
	public static final String XML_THUMBNAIL = "thumbnail";
	public static final String XML_KE = "ke";
	public static final String XML_PASSWORD = "password";
	public static final String XML_VER = "ver";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String XML_UPDATE = "update";
	public static final String XML_UPDATE_TIME = "uptime";
	public static final String XML_LASTENSTART = "lastenstart";
	public static final String XML_TIMEPOOL = "timepool";
	public static final String XML_CLIENTVERSIONUPLOAD = "clientversionupload";
	public static final String XML_TIPSTART = "tipstart";

	// refresh frequences.
	public static final int frequence = 90000; // 1.5*60*1000

	public static final long countInterval = 1000;

	// Broadcast
	public static final String Refresh_Start = "com.duole.refresh.Start";
	public static final String Refresh_Complete = "com.duole.refresh.Complete";
	public static final String REFRESH_COMPLETE_DOWNLOAD_PROVIDER = "com.duole.refresh.complete.fromprovider";
	public static final String Event_AppStart = "com.duole.player.start";
	public static final String Event_AppEnd = "com.duole.player.end";
	public static final String EVENT_INSTALL_UPDATE = "com.duole.update.install";

	// Date formater
	public static final SimpleDateFormat sdf_hour = new SimpleDateFormat("HH");

	// System config preference.
	public static final String pre_power_save = "prePowerSave";
	public static final String Pre_network = "preNetwork";
	public static final String Pre_volume = "preVolume";
	public static final String Pre_bright = "preBright";
	public static final String Pre_deviceid = "preDeviceId";
	public static final String Pre_UserName = "preUsername";
	public static final String Pre_BabyName = "preBabyname";
	public static final String Pre_Birthday = "preBirthday";
	public static final String Pre_Sex = "preSex";
	public static final String Pre_GettingUserInfo = "preGettingUserInfo";
	public static final String Pre_Pc_UserInfo = "pcUserInfo";
	public static final String Pre_Register = "preRegister";
	public static final String Pre_CheckUpdate = "preCheckUpdate";
	public static final String Pre_ClearLocal = "preClearLocal";
	public static final String Pre_startSetup = "preSetupWizard";
	public static final String Pre_Storage = "preStorage";
	public static final String Pre_Wifi = "preWIFI";
	public static final String Pre_Screen_network = "preScreenNetwork";

	// System security
	public static final String Pre_Security_ChangePasswd = "preSecurityChangePasswd";
	public static final String Pre_Security_Exit = "preSecurityExit";
	public static String System_Password = "";
	public static String System_ver = "";
	public static String system_uptime = "";

}
