package com.duole.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.duole.pojos.asset.Asset;

public class JsonUtils {

	public static void parserJson(ArrayList<Asset> alAsset ,JSONObject jsonObject) throws MalformedURLException, JSONException{
		File client;
		String version = jsonObject.getString("ver");
		if(!version.equals("null") && !version.equals("")){
			client = new File(Constants.CacheDir + "client.apk");
			try{
				if(!version.equals(Constants.System_ver)){
					client = new File(Constants.CacheDir + "client.apk");
					if(client.exists()){
						if(!version.equals(DuoleUtils.getPackageVersion(client)) && !Constants.clientApkDownloaded){
							DuoleUtils.updateClient();
						}
					}else{
						DuoleUtils.updateClient();
					}
				}else{
					client = new File(Constants.CacheDir + "client.apk");
					if(client.exists()){
						client.delete();
					}
				}
			}catch (Exception e) {
				Log.e("TAG", "Error occurs when getting updates.");
			}
			
		}
		
		if(jsonObject.has("front")){
			JSONArray jsonArray = jsonObject.getJSONArray("front");
			
			Log.v("TAG", jsonArray.length() + " front length");
		}
		for (int i = 0;i< jsonObject.length(); i++) {
			try{
				JSONObject jsonItem = jsonObject.getJSONObject("item" + i);
				
				if (jsonItem != null) {
					Asset asset = new Asset(jsonItem);
					alAsset.add(asset);
				}
			}catch(Exception e){
				break;
			}
			try {
				JSONObject jsonFront = jsonObject.getJSONObject("front" + i);
				if (jsonFront != null) {
					Asset asset = new Asset(jsonFront);
					alAsset.add(asset);
				}
			} catch (Exception e) {
			}
			
		}
		
		try{
			if(jsonObject.has("front")){
				JSONArray jsonArray = jsonObject.getJSONArray("front");
				
				Log.v("TAG", jsonArray.length() + " front length");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		File file = null;
		if(!Constants.bgurl.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.bgurl.substring(Constants.bgurl
							.lastIndexOf("/")));
		}
		
		
		if (file == null || !file.exists() || !Constants.bgurl.equals(jsonObject.getString("bg"))) {
			
			String bgurl = jsonObject.getString("bg");
			try{
				file.delete();
			}catch (Exception e) {
				e.printStackTrace();
			}
			file = new File(Constants.CacheDir
					+bgurl.substring(bgurl
							.lastIndexOf("/")));
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole + bgurl), file);
			Constants.bgurl = bgurl;
			
			Constants.newItemExists = true;
		}
		
		if(!Constants.bgRestUrl.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.bgRestUrl.substring(Constants.bgRestUrl
							.lastIndexOf("/")));
		}
		
//		if(!Constants.ke.equals("")){
//			file = new File(Constants.CacheDir
//					+ Constants.ke.substring(Constants.ke
//							.lastIndexOf("/")));
//		}
//		
//		if (file == null || !file.exists() || Constants.bgurl != jsonObject.getString("bg")) {
//			
//			Constants.ke = jsonObject.getString("ke");
//			DuoleUtils.downloadSingleFile(new URL(Constants.Duole + Constants.ke), file);
//		}
		
		if (file == null || !file.exists()
				|| !Constants.bgRestUrl.equals(jsonObject.getString("bg1"))) {
			String bgurl = jsonObject.getString("bg1");
			try{
				file.delete();
				file = new File(Constants.CacheDir
						+bgurl.substring(bgurl
								.lastIndexOf("/")));
			}catch (Exception e) {
				e.printStackTrace();
			}
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole
					+ bgurl), file);
			Constants.bgRestUrl = bgurl;
			
			Constants.newItemExists = true;
		}
		
//		Log.v("TAG", Constants.restart);
		if(!Constants.restart.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.TIPSTARTNAME);
		}
		
		if (file == null || !file.exists()
				|| !Constants.restart.equals(jsonObject.getString("tipsd"))) {
			Log.v("TAG", "new tip");
			Constants.restart = jsonObject.getString("tipsd");
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole
					+ Constants.restart), file);
		}
		
		Constants.entime = jsonObject.getString("entime");
		Constants.restime = jsonObject.getString("restime");
		Constants.sleepstart = jsonObject.getString("sleepstart");
		Constants.sleepend = jsonObject.getString("sleepend");
		Constants.restart = jsonObject.getString("tipsd");
		
		XmlUtils.updateSingleNode(Constants.XML_ENTIME, Constants.entime);
		XmlUtils.updateSingleNode(Constants.XML_RESTIME, Constants.restime);
		XmlUtils.updateSingleNode(Constants.XML_SLEEPEND, Constants.sleepend);
		XmlUtils.updateSingleNode(Constants.XML_SLEEPSTART, Constants.sleepstart);
		XmlUtils.updateSingleNode(Constants.XML_TIPSTART, Constants.restart);
		
	}
}
