package com.duole.thread;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;

import com.duole.pojos.asset.BaseApp;
import com.duole.service.download.dao.BaseAppDao;
import com.duole.utils.Constants;
import com.duole.utils.DownloadFileUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.HashUtils;

public class CheckBaseAppExistenceAndInstall extends Thread{
	
	JSONObject joBaseApps;
	ArrayList<BaseApp> alba;
	Context mContext;
	
	public CheckBaseAppExistenceAndInstall(Context context,JSONObject jsonObject){
		joBaseApps = jsonObject;
		mContext = context;
	}

	@Override 
	public void run() {
		
		updateLocalDatabase();
		
		checkExistenceAndInstall();
		
		super.run();
	}
	
	private boolean checkExistenceAndInstall(){
		
		File baseDir = new File(Constants.CacheDir + "/base");
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		
		for(BaseApp ba : alba){
			
			File file = null;
			if(ba.getbPath() != null && !ba.getbPath().equals("")){
				file = new File(Constants.CacheDir + "/base/" + ba.getbPath().substring(ba.getbPath().lastIndexOf("/")));
			}
			
			if( file!=null && !file.exists()){
				DuoleUtils.downloadBaseApp(ba, file.getAbsolutePath());
			}else if(file!= null && file.exists()){
				if(HashUtils.getMD5(file.getAbsolutePath()).equals(ba.getFilemd5())){
					
					String packagename = FileUtils.getPackagenameFromFile(mContext, file);
					if(!DuoleUtils.verifyInstallationOfAPK(mContext, packagename)){
						DuoleUtils.installApkFromFile(file);
					}
				}else{
					file.delete();
					DuoleUtils.downloadBaseApp(ba, file.getAbsolutePath());
				}
			}
			
		}
		return true;
	}
	
	private void updateLocalDatabase(){
		
		alba = new ArrayList<BaseApp>();
		ArrayList<String> baids = new ArrayList<String>();
		ArrayList<String> idDel = new ArrayList<String>();
		
		for(int i = 0 ; i < joBaseApps.length() ; i ++){
			try{
				JSONObject item = joBaseApps.getJSONObject("item" + i);
				
				BaseApp ba = new BaseApp(item);
				alba.add(ba);
				baids.add(ba.getBid());
			}catch (Exception e) {
			}
		}
		
		BaseAppDao bad = new BaseAppDao(mContext);
		Cursor all = bad.query();
		
		for(all.moveToFirst(); !all.isAfterLast() ; all.moveToNext()){			
			
			String id = all.getString(all.getColumnIndex("bid"));
			if(!baids.contains(id)){

				//delete
				bad.delete(id);
				
				String path = all.getString(all.getColumnIndex("bpath"));
				File file = new File(Constants.CacheDir + "/base/" + path.substring(path.lastIndexOf("/")));
				String pkg = FileUtils.getPackagenameFromFile(mContext, file);
				
				try{
					Runtime.getRuntime().exec("pm uninstall " + pkg);
					file.delete();
				}catch (Exception e) {
				}
				
			}
		}
		
		all.close();
		
		bad.save(alba);
		
	}

	
}
