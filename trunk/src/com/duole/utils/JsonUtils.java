package com.duole.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.service.download.dao.ConfigDao;
import com.duole.thread.CheckBaseAppExistenceAndInstall;

public class JsonUtils {

	/**
	 * Deal with the json array.
	 * @param alAsset
	 * @param jsonObject
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public static void parserJson(ArrayList<Asset> alAsset ,JSONObject jsonObject) throws MalformedURLException, JSONException{
		File client;
		//Get the verison of client.
		String version = "";
		ConfigDao configDao = new ConfigDao(Duole.appref);
		try{
			version = jsonObject.getString("ver");
		}catch (Exception e) {
			Log.e("TAG", "no new verison");
		}
		
		
		if(!version.equals("null") && !version.equals("")){
			//Version is exists.
			client = new File(Constants.CacheDir + "client.apk");
			try{
				//whether apk is exists.
				if(!version.equals(Constants.System_ver)){
					client = new File(Constants.CacheDir + "client.apk");
					if(client.exists()){
						//exists.
						if(!version.equals(DuoleUtils.getPackageVersion(client)) && !Constants.clientApkDownloaded){
							DuoleUtils.updateClient();
						}
					}else{
						DuoleUtils.updateClient();
					}
				}else{
					//Local version is equal with the version getted.
					client = new File(Constants.CacheDir + "client.apk");
					if(client.exists()){
						client.delete();
					}
				}
			}catch (Exception e) {
				Log.e("TAG", "Error occurs when getting updates.");
			}
			
		}
		
		//Get the priority resources.
		if(jsonObject.has("front")){
			JSONArray jsonArray = jsonObject.getJSONArray("front");
			
			Log.v("TAG", jsonArray.length() + " front length");
		}
		
		//Get the resource items.
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
		
		//Get the priority resource.
		try{
			if(jsonObject.has("front")){
				JSONArray jsonArray = jsonObject.getJSONArray("front");
				
				Log.v("TAG", jsonArray.length() + " front length");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//Deal with the base apps.
		if(jsonObject.has("base")){
			
			CheckBaseAppExistenceAndInstall cbaeai = new CheckBaseAppExistenceAndInstall(Duole.appref, jsonObject.getJSONObject("base"));
			cbaeai.start();
			
		}
		
		File file = null;
		//Background.
		if(!Constants.bgurl.equals("")){
			try{
				file = new File(Constants.CacheDir
						+ Constants.bgurl.substring(Constants.bgurl
								.lastIndexOf("/")));
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		//file not exists or background has been changed.
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
			DuoleUtils.downloadPicSingle(new URL(Constants.Duole + bgurl), file);
			Constants.bgurl = bgurl;
			
			configDao.save(Constants.XML_BG,file.getAbsolutePath());
			
			Constants.newItemExists = true;
		}
		
		//Background of rest time.
		if(!Constants.bgRestUrl.equals("")){
			try{
				file = new File(Constants.CacheDir
						+ Constants.bgRestUrl.substring(Constants.bgRestUrl
								.lastIndexOf("/")));
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		//file not exists or background has been changed.
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
			DuoleUtils.downloadPicSingle(new URL(Constants.Duole
					+ bgurl), file);
			Constants.bgRestUrl = bgurl;
			
			configDao.save(Constants.XML_BGURL,file.getAbsolutePath());
			
			Constants.newItemExists = true;
		}
		
		//Get the tip sound .
		if(!Constants.restart.equals("")){
			file = new File(Constants.CacheDir
					+ Constants.TIPSTARTNAME);
		}
		
		//file not exists or tip has been changed.
		if (file == null || !file.exists()
				|| !Constants.restart.equals(jsonObject.getString("tipsd"))) {
			Log.v("TAG", "new tip");
			Constants.restart = jsonObject.getString("tipsd");
			DuoleUtils.downloadPicSingle(new URL(Constants.Duole
					+ Constants.restart), file);
			configDao.save(Constants.XML_TIPSTART, file.getAbsolutePath());
		}
		
		String en = Constants.entime;
		String rest = Constants.restime;
		
		//The period of entermaintent.
		Constants.entime = jsonObject.getString("entime");
		//The period of rest.
		Constants.restime = jsonObject.getString("restime");
		
		//If necessary.Reset the game count down.
		AntiFatigureUtils.resetCountdownTimer(en, rest);
		
		//when sleep time is on.
		Constants.sleepstart = jsonObject.getString("sleepstart");
		//when sleep time is out.
		Constants.sleepend = jsonObject.getString("sleepend");
		
		//the url of  tip sound.
		Constants.restart = jsonObject.getString("tipsd");
		
		try{
			configDao.save("base",Constants.CacheDir);
			configDao.save(Constants.XML_ENTIME, Constants.entime);
			configDao.save(Constants.XML_RESTIME, Constants.restime);
			configDao.save(Constants.XML_SLEEPEND, Constants.sleepend);
			configDao.save(Constants.XML_SLEEPSTART, Constants.sleepstart);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//Update the valus getted from json.
		XmlUtils.updateSingleNode(Constants.XML_ENTIME, Constants.entime);
		XmlUtils.updateSingleNode(Constants.XML_RESTIME, Constants.restime);
		XmlUtils.updateSingleNode(Constants.XML_SLEEPEND, Constants.sleepend);
		XmlUtils.updateSingleNode(Constants.XML_SLEEPSTART, Constants.sleepstart);
		XmlUtils.updateSingleNode(Constants.XML_TIPSTART, Constants.restart);
		
	}
}
