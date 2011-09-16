package com.duole.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.duole.pojos.asset.Asset;

public class JsonUtils {

	public static void parserJson(ArrayList<Asset> alAsset ,JSONObject jsonObject) throws MalformedURLException, JSONException{
		
		String version = jsonObject.getString("ver");
		if(!version.equals(Constants.System_ver)){
			String path = jsonObject.getString("vpath");
			
			DuoleUtils.updateClient(path);
		}
		for (int i = 0;i< jsonObject.length(); i++) {
			try{
				JSONObject jsonItem = jsonObject.getJSONObject("item" + i);
				if (jsonItem != null) {
					Asset asset = new Asset(jsonItem);
					alAsset.add(asset);
				}
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
			
			
		}
		File file = null;
		if(!Constants.bgurl.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.bgurl.substring(Constants.bgurl
							.lastIndexOf("/")));
		}
		
		
		if (file == null || !file.exists() || Constants.bgurl != jsonObject.getString("bg")) {
			
			Constants.bgurl = jsonObject.getString("bg");
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole + Constants.bgurl), file);
		}
		if(!Constants.bgRestUrl.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.bgRestUrl.substring(Constants.bgRestUrl
							.lastIndexOf("/")));
		}
		
		if(!Constants.ke.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.ke.substring(Constants.ke
							.lastIndexOf("/")));
		}
		
		if (file == null || !file.exists() || Constants.bgurl != jsonObject.getString("bg")) {
			
			Constants.ke = jsonObject.getString("ke");
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole + Constants.ke), file);
		}
		
		if (file == null || !file.exists()
				|| Constants.bgRestUrl != jsonObject.getString("bg1")) {
			Constants.bgRestUrl = jsonObject.getString("bg1");
			DuoleUtils.downloadSingleFile(new URL(Constants.Duole
					+ Constants.bgRestUrl), file);
		}
		Constants.ke = jsonObject.getString("ke");
		Constants.entime = jsonObject.getString("entime");
		Constants.restime = jsonObject.getString("restime");
		Constants.sleepstart = jsonObject.getString("sleepstart");
		Constants.sleepend = jsonObject.getString("sleepend");
		
	}
}
