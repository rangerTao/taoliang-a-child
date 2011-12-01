package com.duole.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.duole.Duole;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.database.CursorJoiner.Result;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class DuoleNetUtils {

	/**
	 * down load content.
	 * @param url
	 * @return
	 */
	public static String connect(String url) {

		try {
			URL urlCon = new URL(url);

			Log.v("TAG", url);
			HttpURLConnection conn = (HttpURLConnection) urlCon
					.openConnection();

			conn.setRequestMethod("GET");

			conn.setConnectTimeout(5 * 1000);

			InputStream inStream = conn.getInputStream();
			byte[] data = DuoleUtils.readFromInput(inStream);

			String html = new String(data, "gbk");

			return html;
		} catch (Exception e) {
			Constants.DOWNLOAD_RUNNING = false;
			Log.v("TAG", "Connect " + e.getMessage());
			return "";
		}
	}
	
	/**
	 * down load content.
	 * @param url
	 * @return
	 */
	public static String dopost(String url,ArrayList<String[]> pars) {
		
		try {
			URL urlCon = new URL(url);

			DefaultHttpClient dhc = new DefaultHttpClient();
			
			HttpPost httpPost = new HttpPost(urlCon.toURI());
			
			ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
			
			StringBuffer favaid = new StringBuffer();
			StringBuffer usetime = new StringBuffer();
			StringBuffer lastime = new StringBuffer();
					
			int size = pars.size();
			if(pars.size() > 0 ){
				for (int i = 0; i < size; i++) {
					String[] tem = pars.get(i);
					try{
						Integer.parseInt(tem[0]);
						
						if (size - 1 > i) {
							favaid.append(tem[0] + ",");
							usetime.append(tem[1] + ",");
							lastime.append(tem[2] + ",");
						} else {
							favaid.append(tem[0]);
							usetime.append(tem[1]);
							lastime.append(tem[2]);
						}
						
					}catch (Exception e) {
						Log.v("TAG", tem[0]);
						break;
					}
					
				}
				
				pairs.add(new BasicNameValuePair("favaid", favaid.toString()));
				pairs.add(new BasicNameValuePair("usetime", usetime.toString()));
				pairs.add(new BasicNameValuePair("lastime", lastime.toString()));
				
				Log.v("TAG", "favaid" + favaid.toString());
				Log.v("TAG", "usetime" + usetime.toString());
				Log.v("TAG", "lastime" + lastime.toString());
				
				
				UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
				
				httpPost.setEntity(p_entity);
				
				HttpResponse hr = dhc.execute(httpPost);
				HttpEntity he = hr.getEntity();
				InputStream is = he.getContent();
				
				String html = convertStreamToString(is);
				Log.v("TAG", html);
				return html;
			}
			
			return "";
		} catch (Exception e) {
			Log.v("TAG", "Connect " + e.getMessage());
			Constants.DOWNLOAD_RUNNING = false;
			return "";
		}
	}
	
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	
	/** 
     * check whether network is aviable.
     */  
    public static boolean isNetworkAvailable(Activity mActivity){  
    	
        Context context = mActivity.getApplicationContext();  
        ConnectivityManager connectivity =(ConnectivityManager)  
        context.getSystemService(Context.CONNECTIVITY_SERVICE);  

        if(connectivity == null){  
            return false;  
        }else {  
            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
            if(info != null){  
                for(int i= 0;i<info.length;i++){  
                    if(info[i].getState() == NetworkInfo.State.CONNECTED){  
                        return true;  
                    }  
                }  
            }  
        }  
        return false;  
    }  
    
    /**
     * Set WifiConfiguration
     */
    public static void setWifiConfigurationSettings(WifiConfiguration wc,String capa,String pass){
    	
    	
    	if(capa.contains("WEP")){
    		wc.priority = 40;
    	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
    	    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
    	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
    	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
    	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    	    wc.wepKeys[0] = "\"" + pass + "\"";
    	}else{

			wc.preSharedKey = "\""
					+ pass
					+ "\"";
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
		
    	}
    	
    }
    
    /**
     * Upload the local client version to server.
     */
    public static void uploadLocalVersion(){
    	
    	
    	String url = Constants.ClientUpdate + "?cver=" + DuoleUtils.getVersion(Duole.appref) + "&cmcode=" + DuoleUtils.getAndroidId();
		
    	String loaded =XmlUtils.readNodeValue( Constants.SystemConfigFile, Constants.XML_CLIENTVERSIONUPLOAD);
    	if(loaded.equals("false") || loaded.equals("") ){
    		String result = DuoleNetUtils.connect(url);
        	
        	Log.v("TAG", "upload local version url : "+ url);
        	try{
        		JSONObject json = new JSONObject(result);
        		XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_CLIENTVERSIONUPLOAD, "true");
        	}catch(Exception e){
        		XmlUtils.updateSingleNode(Constants.SystemConfigFile, Constants.XML_CLIENTVERSIONUPLOAD, "false");
        	}
    	}
    }
    
	public static void uploadGamePeriodLength() {
		ArrayList<String[]> records = new ArrayList<String[]>();
		String[] ids = new String[3];

		StringBuffer url = new StringBuffer();
		url.append(Constants.UploadGamePeriod);

		File logFolder = new File(Constants.CacheDir + "log");
		File[] logs = logFolder.listFiles();

		Date date = new Date(System.currentTimeMillis());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentday = sdf.format(date);

		for (File file : logs) {
			String name = file.getName();
			if (!name.equals(currentday)) {
				FileUtils.readTxt(records, file.getAbsolutePath());
			}
		}

		if(records.size() > 0){
			String result = DuoleNetUtils.dopost(url.toString(), records);

			Log.v("TAG", "upload result " + result);
			
			try {
				JSONObject jsonObject = new JSONObject(result);

				String status = null;
				status = jsonObject.getString("status");
				Log.v("TAG", status + "  game time status");

				if ("1".equals(status)) {
					for (File file : logs) {
						file.delete();
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		

	}
}
