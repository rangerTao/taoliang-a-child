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

import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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

		// Whether itemlist.xml exists.
		file = new File(Constants.CacheDir + "/itemlist.xml");
		if (!file.exists()) {
			// return false;
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
					+ asset.getThumbnail().substring(
							asset.getThumbnail().lastIndexOf("/")));

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
				url = "http://www.duoleyuan.com" + url;
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
			e.printStackTrace();
			Constants.AssetList.remove(asset);
			file.delete();
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
			e.printStackTrace();
			file.delete();
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
		if (!asset.getUrl().equals("")) {
			file = new File(Constants.CacheDir + asset.getType() + asset.getUrl().substring(
					asset.getUrl().lastIndexOf("/")));
			
			if(asset.getUrl().startsWith("http")){
				return false;
			}
			if (!file.exists()) {
				return true;
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
    public static void updateClient(String path){
    	
    	File newClient = new File(Constants.CacheDir + path.substring(path.lastIndexOf("/")));
    	try {
			if(downloadSingleFile(new URL(Constants.Duole + path ), newClient)){
				
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    public static void checkFilesExists(ArrayList<Asset> assets){
    	
    	for(int i =0;i<assets.size();i++){
    		Asset asset = assets.get(i);
    		
    		String type = asset.getType();
    		String thumb = asset.getThumbnail();
    		String path = asset.getUrl();
    		File file = null;
    		if(!thumb.equals("")){
    			file = new File(Constants.CacheDir + Constants.RES_THUMB + thumb.substring(thumb.lastIndexOf("/")));
        		if(!file.exists()){
        			assets.remove(i);
        			continue;
        		}
    		}
    		
    		if(!path.startsWith("http")){
    			file = new File(Constants.CacheDir + type + path.substring(path.lastIndexOf("/")));
        		if(!file.exists()){
        			assets.remove(i);
        			continue;
        		}
    		}else{
    			continue;
    		}
    		
    	}
    	
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
			String url = //					"http://www.duoleyuan.com/e/member/child/ancJn.php?cc="	+ "7c71f33fce7335e4");
			"http://www.duoleyuan.com/e/member/child/ancJn.php?cc=" + DuoleUtils.getAndroidId();

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
}