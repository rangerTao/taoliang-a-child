package com.duole.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.duole.Duole;
import com.duole.R;
import com.duole.pojos.asset.Asset;
import com.duole.pojos.asset.BaseApp;
import com.duole.service.download.FileMultiThreadDownloader;
import com.duole.service.download.OnDownloadCompleteListener;
import com.duole.service.download.OnDownloadErrorListener;
import com.duole.service.download.dao.AssetDao;
import com.duole.service.download.dao.MusicListDao;

public class DuoleUtils {

	public static MusicListDao mld;

	static {
		mld = new MusicListDao(Duole.appref);
	}

	public static String getCurrentTime() {

		// Set the time format as Year-month-day hour:minute:second.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date(java.lang.System.currentTimeMillis()));

		return time;
	}

	/**
	 * read info from file
	 */
	public static String readFromFile(File file) {
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(file);

			InputStreamReader isr = new InputStreamReader(fis);

			BufferedReader br = new BufferedReader(isr);

			String temp = br.readLine();
			while (temp != null) {
				sb.append(temp);
				temp = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static String getUserinfoFormatted(String result) {

		StringBuffer userinfo = new StringBuffer();

		try {
			JSONObject json = new JSONObject(result);

			String username = json.getString("username");
			String babyname = json.getString("truename");
			String birthday = json.getString("birthd");
			String sex = json.getString("sex");
			String userid = json.getString("userid");

			if (!userid.equals("null")) {

				userinfo.append(Duole.appref.getString(R.string.username) + ":" + username + "\n");
				userinfo.append(Duole.appref.getString(R.string.babyname) + ":" + babyname + "\n");
				userinfo.append(Duole.appref.getString(R.string.birthday) + ":" + birthday + "\n");
				if (sex.equals("0")) {
					sex = Duole.appref.getString(R.string.sex_male);
				} else if (sex.equals("1")) {
					sex = Duole.appref.getString(R.string.sex_female);
				} else {
					sex = Duole.appref.getString(R.string.sex_unborn);
				}
				userinfo.append(Duole.appref.getString(R.string.sex) + ":" + sex + "\n");

			} else {
				userinfo.append(Duole.appref.getString(R.string.user_unregiste));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return userinfo.toString();
	}

	/**
	 * To check whether cache folders exists.
	 * 
	 * @return true if exists.other false.
	 */
	public static boolean checkCacheFiles() {

		// Whether main cache folder exists.
		File file = new File(Constants.CacheDir);
		if (!file.exists()) {
			file.mkdir();
		}

		// Whether flash folder exists.
		file = new File(Constants.CacheDir + "/" + Constants.RES_GAME + "/");
		if (!file.exists()) {
			file.mkdir();
		}
		// Whether music folder exists.
		file = new File(Constants.CacheDir + "/" + Constants.RES_AUDIO + "/");
		if (!file.exists()) {
			file.mkdir();
		}
		// Whether video folder exists.
		file = new File(Constants.CacheDir + "/" + Constants.RES_VIDEO + "/");
		if (!file.exists()) {
			file.mkdir();
		}

		// Whether thumbnail folder exists.
		file = new File(Constants.CacheDir + "/thumbnail/");
		if (!file.exists()) {
			file.mkdir();
		}

		// Whether app folder exists.
		file = new File(Constants.CacheDir + Constants.RES_APK);
		if (!file.exists()) {
			file.mkdir();
		}

		// Create the temp
		file = new File(Constants.CacheDir + "/temp/");
		if (!file.exists()) {
			file.mkdir();
		}

		// Create the log
		file = new File(Constants.CacheDir + "/log/");
		if (!file.exists()) {
			file.mkdir();
		}

		file = new File(Constants.CacheDir + "config.xml");
		if (!file.exists()) {
			DocumentBuilder dBuilder;
			try {
				dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				XmlUtils.initConfiguration(Constants.CacheDir, dBuilder, file);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public static boolean forceStopActivity(Context context, String name) {

		if (name != null && !name.equals("")) {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

			Method method;
			try {
				method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
				method.invoke(am, name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * To Check whether SDCard is properly installed.
	 * 
	 * @return
	 */
	public static boolean checkTFCard() {

		// Whether TF card inserted.
		String status = Environment.getExternalStorageState();

		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}

		return true;
	}

	public static boolean checkTFUsage() {

		File sdcard = Environment.getExternalStorageDirectory();
		StatFs statfs = new StatFs(sdcard.getAbsolutePath());

		long totalSize = FileUtils.countUp(statfs.getBlockCount(), statfs.getBlockSize());
		long usedSize = totalSize - FileUtils.countUp(statfs.getFreeBlocks(), statfs.getBlockSize());

		if (usedSize >= totalSize) {
			return false;
		}

		if (usedSize > totalSize * 0.8f) {
			return false;
		}

		return true;
	}

	/**
	 * Get bytes from input stream.
	 * 
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public static byte[] readFromInput(InputStream inStream) throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];

		int len = 0;

		while ((len = inStream.read(buffer)) != -1) {

			outStream.write(buffer, 0, len);

		}

		inStream.close();

		return outStream.toByteArray();

	}

	/**
	 * Down load video from server.
	 * 
	 * @param asset
	 * @param video
	 * @return
	 */
	public static boolean downloadVideo(Asset asset, String video) {

		// Reorganize the url.
		URL url = checkUrl(video);

		// the file used to save the video.
		File file = new File(Constants.CacheDir + "/video" + "/" + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		if (file.exists()) {
			if (HashUtils.getMD5(file.getAbsolutePath()).equals(asset.getMd5())) {
				return true;
			} else {
				file.delete();
			}
		}

		if (downloadSingleFile(asset, url, file))
			return true;

		return false;

	}

	/**
	 * Down load app from server.
	 * 
	 * @param asset
	 * @param video
	 * @return
	 */
	public static boolean downloadApp(Asset asset, String video) {

		// Reorganize the url.
		URL url = checkUrl(video);

		Log.v("TAG", url.toString());

		// the file used to save the video.
		File file = new File(Constants.CacheDir + "/apk" + "/" + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		if (file.exists()) {
			return true;
		}

		File cacheFile = new File(Constants.CacheDir + "/temp/");

		FileMultiThreadDownloader ftd = new FileMultiThreadDownloader(Duole.appref, asset, url.toString(), cacheFile, file, 7);

		ftd.setOnDownloadCompleteListener(new OnDownloadCompleteListener() {

			public void onDownloadComplete(Asset asset, File cache, File target) {

				if (((asset.getUrl().startsWith("http") || asset.getUrl().startsWith("ftp")) && !asset.getUrl().contains(Constants.siteName))
						|| asset.getMd5().equals("false") || asset.getMd5().equals(HashUtils.getMD5(cache.getAbsolutePath()))) {

					FileUtils.copyFile(cache.getAbsolutePath(), target.getAbsolutePath());

					try {
						Process p = Runtime.getRuntime().exec("pm install -r" + target.getAbsolutePath());
						p.waitFor();

						Constants.newItemExists = true;
						Constants.viewrefreshenable = true;
						Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				cache.delete();
			}
		});

		ftd.setOnDownloadErrorListener(new OnDownloadErrorListener() {

			public void onError() {

			}
		});

		ftd.start();

		synchronized (ftd) {
			try {
				ftd.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return false;

	}

	/**
	 * Down load app from server.
	 * 
	 * @param asset
	 * @param video
	 * @return
	 */
	public static boolean downloadBaseApp(BaseApp ba, String path) {

		// Reorganize the url.
		URL url = checkUrl(Constants.Duole + ba.getbPath());

		Log.v("TAG", url.toString());

		// the file used to save the video.
		File file = new File(path);

		if (file.exists()) {
			return true;
		}

		File cacheFile = new File(Constants.CacheDir + "/temp/");

		Asset asset = new Asset();
		asset.setMd5(ba.getFilemd5());
		asset.setUrl(ba.getbPath());

		FileMultiThreadDownloader ftd = new FileMultiThreadDownloader(Duole.appref, asset, url.toString(), cacheFile, file, 7);

		ftd.setOnDownloadCompleteListener(new OnDownloadCompleteListener() {

			public void onDownloadComplete(Asset asset, File cache, File target) {

				if (((asset.getUrl().startsWith("http") || asset.getUrl().startsWith("ftp")) && !asset.getUrl().contains(Constants.siteName))
						|| asset.getMd5().equals("false") || asset.getMd5().equals(HashUtils.getMD5(cache.getAbsolutePath()))) {

					FileUtils.copyFile(cache.getAbsolutePath(), target.getAbsolutePath());

					try {
						Process p = Runtime.getRuntime().exec("pm install -r " + target.getAbsolutePath());
						p.waitFor();

					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				cache.delete();
			}
		});

		ftd.setOnDownloadErrorListener(new OnDownloadErrorListener() {

			public void onError() {

			}
		});

		ftd.start();

		synchronized (ftd) {
			try {
				ftd.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return false;

	}

	/**
	 * Get front resource
	 */
	public static boolean downloadFront(Asset asset, String video) {
		// Reorganize the url.
		URL url = checkUrl(video);

		// the file used to save the video.
		File file = new File(Constants.CacheDir + "/front" + "/" + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		if (file.exists()) {
			return true;
		}

		File cacheFile = new File(Constants.CacheDir + "/temp/");

		FileMultiThreadDownloader ftd = new FileMultiThreadDownloader(Duole.appref, asset, url.toString(), cacheFile, file, 5);

		ftd.setOnDownloadCompleteListener(new OnDownloadCompleteListener() {

			public void onDownloadComplete(Asset asset, File cache, File target) {

				if (asset.getMd5().equals("false") || asset.getMd5().equals(HashUtils.getMD5(cache.getAbsolutePath()))) {
					Log.d("TAG", "upzip");
					FileUtils.Unzip(cache.getAbsolutePath(), Constants.CacheDir + "/front" + "/" + asset.getId(), Constants.ZiPass);
				}

				cache.delete();
			}
		});

		ftd.setOnDownloadErrorListener(new OnDownloadErrorListener() {

			public void onError() {
			}
		});

		ftd.start();

		synchronized (ftd) {
			try {
				ftd.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return false;

	}

	/**
	 * Download game from server.
	 * 
	 * @param asset
	 * @param game
	 * @return
	 */
	public static boolean downloadGame(Asset asset, String game) {

		// Reorganize the url.
		URL url = checkUrl(game);

		// the file used to save the game.
		File file = new File(Constants.CacheDir + "/game" + "/" + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		if (file.exists()) {
			return true;
		}

		if (downloadSingleFile(asset, url, file))
			return true;

		return false;

	}

	/**
	 * Download audio from server.
	 * 
	 * @param asset
	 * @param audio
	 * @return
	 */
	public static boolean downloadAudio(Asset asset, String audio) {

		URL url = checkUrl(audio);

		File file = new File(Constants.CacheDir + "/mp3" + "/" + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		if (file.exists()) {
			return true;
		}

		if (downloadSingleFile(asset, url, file))
			return true;

		return false;

	}

	/**
	 * Download thumbnail from server.
	 * 
	 * @param asset
	 * @param pic
	 * @return
	 */
	public static boolean downloadPic(Asset asset, String pic) {

		URL url = checkUrl(pic);

		if (!asset.getThumbnail().equals("")) {
			File file = new File(Constants.CacheDir + "/thumbnail" + "/" + pic.substring(pic.lastIndexOf("/")));

			if (file.exists()) {
				return true;
			}

			if (downloadPicSingle(url, file)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * To check whether url contains 'http'
	 * 
	 * @param url
	 * @return
	 */
	public static URL checkUrl(String url) {
		try {
			if (url.startsWith("http://")) {

				return new URL(url);

			} else if (url.contains("duoleyuan")) {
				return new URL("http://" + url);
			} else {
				url = Constants.Duole + url;
				return new URL(url);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Download a file from server.
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static boolean downloadSingleFile(Asset asset, URL url, File file) {
		try {
			// get the name of file
			downloadSingleFile(url, file, asset.getMd5());

			return true;
		} catch (Exception e) {
			Log.e("TAG", "download error " + "error:" + e.getMessage() + "url:" + asset.getUrl());
			Constants.AssetList.remove(asset);
			return false;
		}
	}

	/**
	 * Download a picture from server.
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static boolean downloadPicSingle(URL url, File file) {

		Log.v("TAG", "download a file from " + url.toString());

		String filename = file.getName();
		File cacheFile = new File(Constants.CacheDir + "/temp/" + file.getName());

		if (FileUtils.isCacheFileExists(filename)) {
			if (DownloadFileUtils.resumeDownloadCacheFile(url, cacheFile)) {
				FileUtils.copyFile(cacheFile.getAbsolutePath(), file.getAbsolutePath());
				cacheFile.delete();
			}
		} else {
			if (DownloadFileUtils.downloadCacheFile(url, cacheFile)) {
				FileUtils.copyFile(cacheFile.getAbsolutePath(), file.getAbsolutePath());
				cacheFile.delete();
			}
		}

		return true;
	}

	/**
	 * Download a file from server.
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static boolean downloadSingleFile(URL url, File file, final String md5) {

		Log.v("TAG", "download a file from " + url.toString());

		File cacheFile = new File(Constants.CacheDir + "/temp/");

		FileMultiThreadDownloader ftd = new FileMultiThreadDownloader(Duole.appref, url.toString(), cacheFile, file, 5);
		ftd.setOnDownloadCompleteListener(new OnDownloadCompleteListener() {

			public void onDownloadComplete(Asset asset, File cache, File target) {

				if (md5.equals("false") || md5.equals(HashUtils.getMD5(cache.getAbsolutePath()))) {
					FileUtils.copyFile(cache.getAbsolutePath(), target.getAbsolutePath());
				}

				cache.delete();
			}
		});

		ftd.setOnDownloadErrorListener(new OnDownloadErrorListener() {

			public void onError() {
			}
		});

		ftd.start();

		synchronized (ftd) {
			try {
				ftd.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * To merge two ArrayList into one.
	 * 
	 * @param refer
	 * @param source
	 */
	public static ArrayList<Asset> getAssetDeleteList(HashMap<String, Asset> refer, ArrayList<Asset> source) {

		ArrayList<Asset> alReturn = new ArrayList<Asset>();

		if (source != null) {
			if (source.size() == 0) {
				alReturn.addAll(source);
			} else {
				for (int i = 0; i < source.size(); i++) {
					Asset referAsset = source.get(i);
					if (!refer.containsKey(referAsset.getId())) {
						alReturn.add(referAsset);

						/*----------------2012.03.05-----------------------------*/
						source.remove(referAsset);
						/*----------------2012.03.05-----------------------------*/
					}
				}
			}

		}

		return alReturn;

	}

	/**
	 * To check whether a asset is necessary to download.
	 * 
	 * @param asset
	 * @return
	 */
	public static boolean checkDownloadNecessary(Asset asset, Asset refer) {

		// if(refer == null){
		// Log.e("TAG", "refer is null,download the new item.");
		// return true;
		// }

		if (asset == null || refer == null) {
			return false;
		}

		// If id is different.true.
		if (!asset.getId().equals(refer.getId())) {
			return true;
		}

		// if url is different,true.
		if (!asset.getUrl().equals(refer.getUrl())) {
			return true;
		}

		// if lastmodified is different,true.
		if (!asset.getLastmodified().equals(refer.getLastmodified())) {
			return true;
		}

		String type = asset.getType().trim().toLowerCase();
		if (type.equals("")) {
			type = checkAssetType(asset);
		}

		if (type.equals(Constants.RES_FRONT)) {
			File front = new File(Constants.CacheDir + "/front/" + asset.getId());
			if (!front.exists()) {
				return true;
			} else {
				return false;
			}
		}

		File file;
		// Thumbnail does not exists.
		if (!asset.getThumbnail().equals("")) {
			file = new File(Constants.CacheDir + Constants.RES_THUMB + asset.getThumbnail().substring(asset.getThumbnail().lastIndexOf("/")));
			if (!file.exists()) {
				return true;
			}
		}

		// Source file does not exists.
		if (type.equals(Constants.RES_APK) || type.equals(Constants.RES_WIDGET)) {
			file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

			if (!file.exists()) {
				return true;
			} else if (file.exists()) {

				if ((asset.getUrl().startsWith("ftp") || asset.getUrl().startsWith("http")) && !asset.getUrl().contains(Constants.siteName)) {
					return false;
				}

				if (asset.getMd5() == null) {
					if (!refer.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))) {
						file.delete();
						return true;
					}
				} else if (asset.getMd5().equals("false")) {
					return false;
				} else if (!asset.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))) {
					file.delete();
					return true;
				}
				return false;
			} else {
				PackageManager pm = Duole.appref.getPackageManager();
				file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

				PackageInfo info = null;
				try {
					info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
				} catch (Exception e) {
					e.printStackTrace();
					file.delete();
				}

				if (info != null) {
					List<PackageInfo> infos = pm.getInstalledPackages(0);
					if (!infos.contains(infos)) {
						if (DuoleUtils.installApkFromFile(file)) {

							Constants.newItemExists = true;
							Constants.viewrefreshenable = true;
							Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));

							return false;
						} else {
							return true;
						}
					}
				}
			}
		} else if (!asset.getUrl().equals("")) {
			file = new File(Constants.CacheDir + type + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));
			if (asset.getUrl().startsWith("http") && !asset.getUrl().contains("duoleyuan")) {
				if (Constants.OnlineVideoUrlList != null && !Constants.OnlineVideoUrlList.contains(asset.getUrl())) {
					Log.d("TAG", asset.getUrl());
					Constants.newItemExists = true;
				}
				return false;
			}

			if (!file.exists()) {
				return true;
			} else {
				if (asset.getMd5() == null) {
					if (!refer.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))) {
						file.delete();
						return true;
					}
				} else if (asset.getMd5().equals("false")) {
					return false;
				} else if (!asset.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))) {
					file.delete();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Update the asset file on the sdcard.
	 * 
	 * @param assets
	 *            The list of assets.
	 * @return
	 * @throws TransformerException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static boolean updateAssetListFile(ArrayList<Asset> assets) throws SAXException, IOException, TransformerException {

		try {
			XmlUtils.deleteAllItemNodes();

			XmlUtils.addNode(assets);

			AssetDao ad = new AssetDao(Duole.appref);
			ad.addToDB(assets);

		} catch (Exception e) {
			File file = new File(Constants.ItemList);
			file.delete();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			FileInputStream iStream = null;

			try {

				File backup = new File(Constants.ItemList + ".bak");
				dBuilder = dbf.newDocumentBuilder();
				iStream = new FileInputStream(backup);

				FileUtils.copyFile(backup.getAbsolutePath(), Constants.ItemList);
			} catch (Exception ex) {

				XmlUtils.initFile(iStream, dBuilder, file);

				XmlUtils.addNode(assets);

			}

		}

		return true;
	}

	/**
	 * Get the android of current device.
	 * 
	 * @return
	 */
	public static String getAndroidId() {
		String androidId = System.getString(Duole.appref.getContentResolver(), System.ANDROID_ID);
		return (androidId + " ");
	}

	/**
	 * Set the childrendrawingcacheenabled.
	 * 
	 * @param vg
	 * @param enabled
	 */
	public static void setChildrenDrawingCacheEnabled(ViewGroup vg, boolean enabled) {
		final int count = vg.getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = vg.getChildAt(i);
			view.setDrawingCacheEnabled(true);
			// Update the drawing caches
			view.buildDrawingCache(true);
		}
	}

	/**
	 * Clear the children chache.
	 * 
	 * @param vg
	 */
	public static void clearChildrenCache(ViewGroup vg) {
		final int count = vg.getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = vg.getChildAt(i);
			view.setDrawingCacheEnabled(false);
		}
	}

	// Add a netword manager icon in the list.
	public static void addNetworkManager(ArrayList<Asset> assets) {

		Asset asset = new Asset();

		asset.setType(Constants.RES_CONFIG);

		asset.setFilename(Duole.appref.getString(R.string.system_tweak));

		asset.setUrl("");

		assets.add(asset);
	}

	// Add a jinzixuan manager icon in first place of the list.
	public static ArrayList<ArrayList<Asset>> addJinzixuanManager(ArrayList<Asset> assets) {

		File file = new File("/sdcard/jinzixuan/index/jinzixuan.xml");
		if (!file.exists()) {
			return null;
		}

		return XmlUtils.parseJinzixuanXML(file);
	}

	/**
	 * Update the client.
	 */
	public static synchronized void updateClient() {
		String path;

		String url = Constants.ClientUpdate;

		String version = DuoleUtils.getVersion(Duole.appref);
		String mCode = DuoleUtils.getAndroidId();

		url = url + "?cver=" + version + "&cmcode=" + mCode;

		String result = DuoleNetUtils.connect(url);

		try {
			JSONObject json = new JSONObject(result);

			String ver = json.getString("ver");
			path = json.getString("path");
			String updateHour = json.getString("uptime");
			String md5 = json.getString("filemd5");

			if (!path.equals("null") || !path.equals("")) {
				File client = new File(Constants.CacheDir + "client.apk");

				try {
					File newClient = new File(Constants.CacheDir + "/temp/" + path.substring(path.lastIndexOf("/")));

					if (!client.exists()) {
						if (DownloadFileUtils.resumeDownloadCacheFile(new URL(Constants.Duole + path), newClient)) {

							if (!md5.equals("") && md5 != null) {
								if (!HashUtils.getMD5(newClient.getAbsolutePath()).equals(md5)) {
									newClient.delete();
								}
							}

							if (newClient.exists()) {

								FileUtils.copyFile(newClient.getAbsolutePath(), client.getAbsolutePath());
								newClient.delete();
								XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_VER, ver);
								XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_UPDATE, Constants.TRUE);
								XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_UPDATE_TIME, updateHour);

								Constants.clientApkDownloaded = true;

								DuoleUtils.instalUpdateApk(Duole.appref);

							}

						}
					} else {
						if (!ver.equals(DuoleUtils.getPackageVersion(client))) {
							client.delete();
							updateClient();
						} else {
							Constants.clientApkDownloaded = true;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static long getUpdateMills() {

		String uptime = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_UPDATE_TIME);

		Date date = new Date(java.lang.System.currentTimeMillis());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm ss");

		String time = sdf.format(date);

		String update = time.substring(0, 11) + uptime + " 00";

		Date dateU = null;
		try {
			dateU = sdf.parse(update);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dateU.getTime();

	}

	/**
	 * get apk version
	 */
	public static String getPackageVersion(File file) {

		PackageManager pm = Duole.appref.getPackageManager();

		PackageInfo info = null;
		try {
			info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			if (file.exists()) {
				Log.d("TAG", "update package error. Delete");
				file.delete();
			}

		}

		if (info != null) {
			return info.versionName;
		} else {
			return "";
		}
	}

	/**
	 * Get app version
	 */
	public static String getVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * To check whether file is complete
	 */
	public static ArrayList<Asset> checkFilesExists(ArrayList<Asset> assets) {

		ArrayList<Asset> temp = new ArrayList<Asset>();
		File file = null;
		for (int i = 0; i < assets.size(); i++) {
			Asset asset = assets.get(i);

			String type = asset.getType();
			if (type.trim().toLowerCase().equals("")) {
				type = DuoleUtils.checkAssetType(asset);
			}

			if (asset.getType().equals(Constants.RES_WIDGET)) {
				type = Constants.RES_APK;
			}

			String path = asset.getUrl();
			String isfront = asset.getIsFront();

			if (DuoleNetUtils.isNetworkAvailable(Duole.appref) && path.contains("youku"))
				temp.add(asset);
			else {
				try {
					file = new File(Constants.CacheDir + type + path.substring(path.lastIndexOf("/")));
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (file != null && file.exists() && isfront != null && isfront.equals("0")) {

					if (type.equals(Constants.RES_APK)) {
						String pkgname = asset.getPackag();
						if (pkgname == null) {
							pkgname = FileUtils.getPackagenameFromAPK(Duole.appref, asset);
						}
						if (!DuoleUtils.verifyInstallationOfAPK(Duole.appref, pkgname)) {
							installThread it = new installThread(file);
							it.start();
						} else {
							temp.add(asset);
						}
					} else {
						temp.add(asset);
					}

				}

			}

			// if (asset.getType().equals(Constants.RES_JINZIXUAN)) {
			// temp.add(asset);
			// // continue;
			// }

		}

		// Remove duplicate content
		for (int i = 0; i < temp.size() - 1; i++) {
			for (int j = i + 1; j < temp.size(); j++) {
				if (temp.get(i).getId().equals(temp.get(j).getId())) {
					temp.remove(j);
					j--;
				}
			}
		}

		return temp;

	}

	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, roundingMode);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}

	/**
	 * Get resource list from server.
	 */
	public static boolean getSourceList(ArrayList<Asset> alAsset) {
		try {
			String url = Constants.Duole + "/e/member/child/ancJn.php?cc=" + DuoleUtils.getAndroidId();

			alAsset = new ArrayList<Asset>();
			String result = DuoleNetUtils.connect(url);
			JSONObject jsonObject = new JSONObject(result);
			Log.v("TAG", result);
			String error = null;
			try {
				error = jsonObject.getString("errstr");
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (error != null) {
			} else {
				try {
					JsonUtils.parserJson(alAsset, jsonObject);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return true;
		} catch (Exception e) {
			Log.v("TAG", e.getMessage());
			return false;
		}

	}

	/**
	 * Get the music list from sources
	 * 
	 * @param assets
	 */
	public static void getMusicList(ArrayList<Asset> assets) {

		Constants.MusicList = new ArrayList<Asset>();

		for (Asset asset : assets) {
			if (asset.getType().equals(Constants.RES_AUDIO)) {
				Constants.MusicList.add(asset);
			}
		}

		mld = new MusicListDao(Duole.appref);
		mld.save(Constants.MusicList);
	}

	/**
	 * Get the list of online video
	 * 
	 * 
	 */
	public static void getOnlineVideoList(ArrayList<Asset> assets) {

		Constants.OnlineVideoUrlList = new ArrayList<String>();

		for (Asset asset : assets) {
			if (asset.getUrl() != null && !asset.getUrl().equals("")) {
				String url = asset.getUrl();
				if (url.startsWith("http") && !url.contains("duole")) {
					Constants.OnlineVideoUrlList.add(url);
				}

			}
		}
	}

	/**
	 * Install a apk from a file.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean installApkFromFile(File file) {
		try {
			Log.v("TAG", "install apk from " + file.getAbsolutePath());
			Process p = Runtime.getRuntime().exec("pm install -r " + file.getAbsolutePath());
			p.waitFor();
			int result = p.exitValue();
			if (result == 0) {
				return true;
			}

		} catch (Exception e) {
			file.delete();
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * install a update
	 */
	public static boolean instalUpdateApk(final Context context) {

		new Thread() {

			@Override
			public void run() {

				File client = new File(Constants.CacheDir + "client.apk");
				if (client.exists()) {
					PackageManager pm = context.getPackageManager();

					try {
						PackageInfo info = pm.getPackageArchiveInfo(client.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
						if (!DuoleUtils.getVersion(context).equals(info.versionName)) {
							Log.v("TAG", "upate client");

							XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_CLIENTVERSIONUPLOAD, Constants.FALSE);
							Log.v("TAG", "pm install -r" + client.getAbsolutePath());
							DuoleUtils.installApkFromFile(client);
							client.delete();

						} else {
							client.delete();
						}
					} catch (Exception e) {
						client.delete();
						e.printStackTrace();
					}
				}

				super.run();
			}

		}.start();

		return false;
	}

	// get how many minutes.
	public static int parseMillsToMinutes(long period) {

		if (period == 0) {
			return 20;
		}

		int mins = (int) (period / 60000);
		if (period % 1000 > 0) {
			mins += 1;
		}

		return mins;
	}

	/**
	 * Query the package manager for MAIN/LAUNCHER activities in the supplied
	 * package.
	 */
	public static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);

		final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
		return apps != null ? apps : new ArrayList<ResolveInfo>();
	}

	/**
	 * verify the installation of a apk. installed true. not installed false.
	 */
	public static boolean verifyInstallationOfAPK(Context context, String pkgname) {
		PackageManager pm = context.getPackageManager();

		List<ApplicationInfo> lai = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo ai : lai) {
			if (ai.packageName.equals(pkgname)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removal of duplicate content
	 * 
	 * @param input
	 * @return
	 */
	public synchronized static HashMap<String, Asset> deDuplicate(HashMap<String, Asset> input) {

		HashMap<String, Asset> output = new HashMap<String, Asset>();
		Set<String> si = input.keySet();

		Iterator it = si.iterator();
		while (it.hasNext()) {
			output.put(it.next().toString(), input.get(it.next()));
		}

		return output;
	}

	/**
	 * Execute a command as root.
	 * 
	 * @param command
	 * @return
	 */
	public static boolean execAsRoot(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

	public static String checkAssetType(Asset item) {

		String url = item.getUrl();

		if (item.getType().equals(Constants.RES_CONFIG)) {
			return Constants.RES_CONFIG;
		} else if (url.endsWith(".mp3")) {
			return Constants.RES_AUDIO;
		} else if (url.endsWith(".apk")) {
			return Constants.RES_APK;
		} else if (url.endsWith(".swf") || url.endsWith(".flv")) {
			return Constants.RES_GAME;
		} else if (url.endsWith(".zip")) {
			return Constants.RES_FRONT;
		} else if (url.endsWith(".zip")) {
			return Constants.RES_FRONT;
		} else if (url.endsWith(".mp4") || url.endsWith(".avi") || url.endsWith(".mkv") || url.endsWith(".rmvb") || url.endsWith(".rm")) {
			return Constants.RES_VIDEO;
		} else {
			return "";
		}

	}

	public static int getContentFilterCount(String filter, Context context) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType(filter);

		List<ResolveInfo> rList;

		PackageManager mPm = context.getPackageManager();

		rList = mPm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

		return rList.size();
	}

	public static boolean isServiceWorked(Context context) {
		ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString().equals("com.duole.service.AssetDownloadService")) {
				return true;
			}
		}
		return false;
	}
}

/**
 * A thread used to install apks.
 * 
 * @author taoliang
 * 
 */
class installThread extends Thread {

	File installFile;

	public installThread(File file) {
		installFile = file;
	}

	public void run() {
		DuoleUtils.installApkFromFile(installFile);
		Duole.appref.sendBroadcast(new Intent(Constants.Refresh_Complete));
	}

}