package com.duole.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duole.Duole;
import com.duole.R;
import com.duole.pojos.asset.Asset;

public class DuoleUtils {

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
		
		//Create the temp 
		file = new File(Constants.CacheDir + "/temp/");
		if(!file.exists()){
			file.mkdir();
		}
		
		//Create the log 
		file = new File(Constants.CacheDir + "/log/");
		if(!file.exists()){
			file.mkdir();
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
		File file = new File(Constants.CacheDir
				+ "/video"
				+ "/"
				+ asset.getUrl().substring(
						asset.getUrl().lastIndexOf("/")));

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

		// the file used to save the video.
		File file = new File(Constants.CacheDir
				+ "/apk"
				+ "/"
				+ asset.getUrl().substring(
						asset.getUrl().lastIndexOf("/")));

		if (downloadSingleFile(asset, url, file)){
			try {
				Process p = Runtime.getRuntime().exec("pm install " + file.getAbsolutePath());
				p.waitFor();
				int result = p.exitValue();
				if(result == 0 ){
					return true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
		File file = new File(Constants.CacheDir + "/front" + "/"
				+ asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));
		
		Log.v("TAG", url.toString());
		
		try {
			//get the name of file
			String filename = file.getName();
			File cacheFile = new File(Constants.CacheDir + "/temp/" + file.getName());
			
			if (FileUtils.isCacheFileExists(filename)) {
				if (DownloadFileUtils.resumeDownloadCacheFile(url, cacheFile)) {
					FileUtils.Unzip(cacheFile.getAbsolutePath(), Constants.CacheDir + "/front" + "/" + asset.getId(), Constants.ZiPass);
					cacheFile.delete();
				}
			} else {
				if (DownloadFileUtils.downloadCacheFile(url, cacheFile)) {
					FileUtils.Unzip(cacheFile.getAbsolutePath(), Constants.CacheDir + "/front" + "/" + asset.getId(), Constants.ZiPass);
					cacheFile.delete();
				}
			}
			return true;
		} catch (Exception e) {
			Log.e("TAG", "download error " + "error:" + e.getMessage() + "url:" + asset.getUrl());
			Constants.AssetList.remove(asset);
			return false;
		}
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
		File file = new File(Constants.CacheDir
				+ "/game"
				+ "/"
				+ asset.getUrl().substring(
						asset.getUrl().lastIndexOf("/")));

		if (downloadSingleFile(asset,url, file))
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

		File file = new File(Constants.CacheDir
				+ "/mp3"
				+ "/"
				+ asset.getUrl().substring(
						asset.getUrl().lastIndexOf("/")));

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

		if(!asset.getThumbnail().equals("")){
			File file = new File(Constants.CacheDir
					+ "/thumbnail"
					+ "/"
					+ pic.substring(
							pic.lastIndexOf("/")));
			if (downloadSingleFile(asset, url, file))
				return true;
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

			} else {
				url = Constants.Duole + url;
				return new URL(url);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
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
	public static boolean downloadSingleFile(Asset asset ,URL url, File file) {
		try {
			//get the name of file
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
		} catch (Exception e) {
			Log.e("TAG", "download error " + "error:" + e.getMessage() + "url:" + asset.getUrl());
			Constants.AssetList.remove(asset);
			return false;
		}
	}
	
	/**
	 * Download a file from server.
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static boolean downloadSingleFile(URL url, File file) {
		try {
			Log.v("TAG", "download a file from " + url.toString());
			// Open a connection.
			URLConnection conn = url.openConnection();
			// get the size of file.
			int fileSize = conn.getContentLength();

			byte[] buffer = new byte[8 * 1024];

			InputStream bis = null;
			FileOutputStream fos = null;

			// Create a file.
			file.createNewFile();

			bis = conn.getInputStream();
			fos = new FileOutputStream(file);
			int len = 0;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			bis.close();
			return true;
		} catch (Exception e) {
			Log.e("TAG", "download error " + "error:" + e.getMessage() + "url:" + url.toString());
			e.printStackTrace();
			if(file != null){
				file.delete();
			}
			return false;
		}
	}

	/**
	 * To merge two ArrayList into one.
	 * 
	 * @param refer
	 * @param source
	 */
	public static ArrayList<Asset> getAssetDeleteList(
			HashMap<String, Asset> refer, ArrayList<Asset> source) {

		ArrayList<Asset> alReturn = new ArrayList<Asset>();

		if (source != null) {
			for (int i = 0; i < source.size(); i++) {
				Asset referAsset = source.get(i);
				if (!refer.containsKey(referAsset.getId())) {
					alReturn.add(referAsset);
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
		
		if(asset.getType().equals(Constants.RES_FRONT)){
			File front = new File(Constants.CacheDir + "/front/" + asset.getId());
			if(!front.exists()){
				return true;
			}else{
				return false;
			}
		}

		File file;
		//Thumbnail does not exists.
		if (!asset.getThumbnail().equals("")) {
			file = new File(Constants.CacheDir
					+ Constants.RES_THUMB
					+ asset.getThumbnail().substring(
							asset.getThumbnail().lastIndexOf("/")));
			if (!file.exists()) {
				return true;
			}
		}

		//Source file does not exists.
		if(asset.getType().equals(Constants.RES_APK)){
			file = new File(Constants.CacheDir + asset.getType() + asset.getUrl().substring(
					asset.getUrl().lastIndexOf("/")));
			
			if(asset.getUrl().startsWith("http")){
				return false;
			}
			if (!file.exists()) {
				return true;
			}else if(file.exists()){
				return false;
			}else{
				PackageManager pm = Duole.appref.getPackageManager();
				file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

				PackageInfo info;
				info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
				if(info != null){
					List<PackageInfo> infos = pm.getInstalledPackages(0);
					if(!infos.contains(infos)){
						if(DuoleUtils.installApkFromFile(file)){
							return false;
						}else{
							return true;
						}
					}
				}
			}
		}else if (!asset.getUrl().equals("")) {
			file = new File(Constants.CacheDir + asset.getType() + asset.getUrl().substring(
					asset.getUrl().lastIndexOf("/")));
			if(asset.getUrl().startsWith("http")){
				return false;
			}
			
			if (!file.exists()) {
				return true;
			}else{
				if(asset.getMd5() == null){
					if(!refer.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))){
						file.delete();
						return true;
					}
				}else if(asset.getMd5().equals("false")){
					return false;
				}else if(!asset.getMd5().equals(HashUtils.getMD5(file.getAbsolutePath()))){
					file.delete();
					return true;
				}
			}
		}

		return false;
	}

	public static boolean updateAssetListFile(ArrayList<Asset> assets) {

		try {
			XmlUtils.deleteAllItemNodes();

			XmlUtils.addNode(assets);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public static String getAndroidId() {
		String androidId = System.getString(Duole.appref.getContentResolver(),
				System.ANDROID_ID);
		return (androidId + " ");
	}
	
    public static void setChildrenDrawingCacheEnabled(ViewGroup vg,boolean enabled) {  
        final int count = vg.getChildCount();  
        for (int i = 0; i < count; i++) {  
            final View view = vg.getChildAt(i);  
            view.setDrawingCacheEnabled(true);  
            // Update the drawing caches  
             view.buildDrawingCache(true);  
        }  
    }  
    
    public static void clearChildrenCache(ViewGroup vg) {  
    	final int count = vg.getChildCount();  
    	for (int i = 0; i < count; i++) {  
    		final View view = vg.getChildAt(i);  
            view.setDrawingCacheEnabled(false);  
        }  
    }  
    
    //Add a netword manager icon in the list.
    public static void addNetworkManager(ArrayList<Asset> assets){
    	
    	Asset asset = new Asset();
    	
    	asset.setType(Constants.RES_CONFIG);
    	
    	asset.setFilename(Duole.appref.getString(R.string.system_tweak));
    	
    	assets.add(asset);
    }
    
    /**
     * Update the client.
     */
    public static synchronized void updateClient(){
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
			
			if(!path.equals("null") || !path.equals("")){
				File client = new File(Constants.CacheDir + "client.apk");
		    	File newClient = new File(Constants.CacheDir + "/temp/" + path.substring(path.lastIndexOf("/")));
		    	try {
		    		if(!client.exists()){
		    			if(DownloadFileUtils.resumeDownloadCacheFile(new URL(Constants.Duole + path ), newClient)){
		    				FileUtils.copyFile(newClient.getAbsolutePath(), client.getAbsolutePath());
		    				newClient.delete();
		    				XmlUtils.updateSingleNode(Constants.SystemConfigFile ,Constants.XML_VER,
									ver);
		    				XmlUtils.updateSingleNode(Constants.SystemConfigFile,Constants.XML_UPDATE, Constants.TRUE);
		    				XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_UPDATE_TIME,updateHour);
		    				Constants.clientApkDownloaded = true;
		    			}
		    		}else{
		    			if(!ver.equals(DuoleUtils.getPackageVersion(client))){
		    				client.delete();
		    				updateClient();
		    			}else{
		    				Constants.clientApkDownloaded = true;
		    			}
		    		}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * get apk version
     */
    public static String getPackageVersion(File file){
    	
    	PackageManager pm = Duole.appref.getPackageManager();

		PackageInfo info;
		info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
		if(info != null){
			return info.versionName;
		}else{
			return "";
		}
    }
    
    /**
     * Get app version
     */
    public static String getVersion(Context context){
    	try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
    }
    
    /**
     * To check whether file is complete
     */
    public static ArrayList<Asset> checkFilesExists(ArrayList<Asset> assets){
    	
    	ArrayList<Asset> temp = new ArrayList<Asset>();
    	File file = null;
    	for(int i = 0;i<assets.size();i++){
    		Asset asset = assets.get(i);
    		
    		String type = asset.getType();
    		String path = asset.getUrl();
    		String isfront = asset.getIsFront();
    		
    		if(!path.startsWith("http")){
    			try{
    				file = new File(Constants.CacheDir + type + path.substring(path.lastIndexOf("/")));
    			}catch (Exception e) {
					e.printStackTrace();
				}
        		if(file != null && file.exists() && isfront != null && isfront.equals("0")){
        			if(type.equals(Constants.RES_APK)){
        				String pkgname = asset.getPackag();
        				if(pkgname == null){
        					pkgname = FileUtils.getPackagenameFromAPK(Duole.appref, asset);
        				}
        				if(!DuoleUtils.verifyInstallationOfAPK(Duole.appref, pkgname))
        					DuoleUtils.installApkFromFile(file);
        			}
        			temp.add(asset);
        		}
    		}else{
    			temp.add(asset);
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
				try{
					JsonUtils.parserJson(alAsset, jsonObject);
				}catch(Exception e){
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
	 * @param assets
	 */
	public static void getMusicList(ArrayList<Asset> assets) {

		Constants.MusicList = new ArrayList<Asset>();

		for (Asset asset : assets) {
			if (asset.getType().equals(Constants.RES_AUDIO)) {
				Constants.MusicList.add(asset);
			}
		}

	}
	
	/**
	 * Install a apk from a file.
	 * @param file
	 * @return
	 */
	public static boolean installApkFromFile(File file){
		try {
			Log.v("TAG", "install apk from " + file.getAbsolutePath());
			Process p = Runtime.getRuntime().exec("pm install -r " + file.getAbsolutePath());
			p.waitFor();
			int result = p.exitValue();
			if(result == 0 ){
				return true;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	/**
	 * install a update
	 */
	public static boolean instalUpdateApk(Context context){
		File client = new File(Constants.CacheDir + "client.apk");
		if(client.exists()){
			PackageManager pm = context.getPackageManager();

			PackageInfo info = pm.getPackageArchiveInfo(client.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
			if(!DuoleUtils.getVersion(context).equals(info.versionName)){
				Log.v("TAG", "upate client");
				try{
					XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_CLIENTVERSIONUPLOAD, Constants.FALSE);
					Log.v("TAG", "pm install -r" + client.getAbsolutePath());
					DuoleUtils.installApkFromFile(client);
				}catch(Exception e){
					e.printStackTrace();
				}
				
				
				return true;
			}else{
				client.delete();
			}
		}
		
		return false;
	}
	
	//get how many minutes.
	public static int parseMillsToMinutes(long period){
		
		if(period == 0){
			return 20;
		}
		
		int mins = (int) (period / 60000);
		if(period % 1000 > 0){
			mins += 1;
		}
		
		return mins;
	}
	
    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
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
     * verify the installation of a apk.
     */
    public static boolean verifyInstallationOfAPK(Context context , String pkgname){
    	PackageManager pm = context.getPackageManager();
    	
    	List<ApplicationInfo> lai = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    	for(ApplicationInfo ai : lai){
    		if(ai.packageName.equals(pkgname)){
    			Log.v("TAG", pkgname + "   Installed.");
    			return true;
    		}
    	}
    	
    	return false;
    }
}