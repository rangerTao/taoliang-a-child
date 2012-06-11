package com.duole.service.download.dao;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MusicListDao {

	
	//the instance of musiclistproviderOpenHelper.
	private DuoleDBHelper mlpo;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
	
	public MusicListDao(Context context){
		mlpo = DuoleDBHelper.getInstance(context);
	}
	
	public Cursor query() {

		SQLiteDatabase db = mlpo.getReadableDatabase();
		
		Cursor cursor = db.query("musiclist", null, "", null, "", "", "");
		
		return cursor;

	}
	
	/**
	 * save the length of downloaded data.
	 * 
	 * @param path
	 * @param map
	 */
	public void save(ArrayList<Asset> inputs){

		//Remove all old data in the table.
		delete();
		
		//get a new instance of db.
		SQLiteDatabase db = mlpo.getWritableDatabase();
		
		if(!db.isDbLockedByOtherThreads() || !db.isDbLockedByCurrentThread()){
			try
			{
				db.beginTransaction();
				
				for (Asset asset : inputs)
				{
					ContentValues cValues = new ContentValues();
					
					cValues.put("name", asset.getName());
					cValues.put("thumb", getThumbPath(asset.getThumbnail()));
					cValues.put("path", getFilePath(asset.getUrl()));
					cValues.put("modify_time", getCurrentTime());
					
					db.insert("musiclist", null, cValues);
					
				}
				
				db.setTransactionSuccessful();
			}  catch (Exception e) {
				e.printStackTrace();
			} finally
			{
				db.endTransaction();
			}
		}
		
		db.close();
	}
	
	/**
	 * update the music list..
	 * 
	 * @param path
	 * @param map
	 */
	public void update(String path, Map<Integer, Integer> map) {
		SQLiteDatabase db = mlpo.getWritableDatabase();
		
		try {
			db.beginTransaction();
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				db.execSQL(
						"update musiclist set downlength=? where downpath=? and threadid=?",
						new Object[] { entry.getValue(), path, entry.getKey() });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		db.close();

	}

	/**
	 * when download successful finished.delete the current log.
	 * 
	 * @param path
	 */
	public void delete()
	{
		SQLiteDatabase db = mlpo.getWritableDatabase();
		if(!db.isDbLockedByOtherThreads() || !db.isDbLockedByCurrentThread()){
			db.execSQL("delete from musiclist",
					new Object[] {});
		}
		db.close();
	}
	
	private String getThumbPath(String url){
		return Constants.CacheDir
				+ "/thumbnail/"	+ url.substring(url.lastIndexOf("/"));
	}
	
	private String getCurrentTime(){
		
		Date date = new Date(System.currentTimeMillis());
		return sdf.format(date);
		
	}
	
	private String getFilePath(String urlbase){

		String url = "";
		
		if (urlbase.startsWith("http:")) {
		} else {
			url = urlbase.substring(urlbase.lastIndexOf("/"));
		}

		if (urlbase.startsWith("http")) {
			url = urlbase;
		} else {
			url = Constants.CacheDir + Constants.RES_AUDIO + url;
		}
		
		return url;
	}
	
}
