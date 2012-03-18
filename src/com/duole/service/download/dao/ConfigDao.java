package com.duole.service.download.dao;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.duole.utils.Constants;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ConfigDao {

	
	//the instance of musiclistproviderOpenHelper.
	private DuoleDBHelper mlpo;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
	
	public ConfigDao(Context context){
		mlpo = DuoleDBHelper.getInstance(context);
	}
	
	public Cursor queryAll() {

		SQLiteDatabase db = mlpo.getReadableDatabase();
		
		Cursor cursor = db.query("configure", null, "", null, "", "", "");
		
		return cursor;

	}
	
	public Cursor query(String column) {

		SQLiteDatabase db = mlpo.getReadableDatabase();
		
		Cursor cursor = db.query("configure", new String[]{"value"}, "name='" + column + "'", null, "", "", "");
		
		return cursor;

	}
	
	/**
	 * save the length of downloaded data.
	 * 
	 * @param path
	 * @param map
	 */
	public void save(ContentValues cv){
		
		String name = cv.getAsString("name");
		String value = cv.getAsString("value");
		
		if(name != null && value != null){
			
			Cursor cursor = query(name);
			
			int size = cursor.getCount();
			cursor.close();
			
			Log.d("TAG",size + "   count");
			
			if(size > 0){
				update(name, value);
			}else{
				insert(name, value);
			}
		}
		
	}
	
	/**
	 * save the length of downloaded data.
	 * 
	 * @param path
	 * @param map
	 */
	public void save(String name,String value){
		
		if(name != null && value != null){
			
			Cursor cursor = query(name);
			
			int size = cursor.getCount();
			cursor.close();
			
			if(size > 0){
				update(name, value);
			}else{
				insert(name, value);
			}
		}
		
	}
	
	/**
	 * update the Configuration.
	 * 
	 * @param path
	 * @param map
	 */
	public void update(String name,String value) {
		SQLiteDatabase db = mlpo.getWritableDatabase();
		
		try {
			db.beginTransaction();
			
			ContentValues cv = new ContentValues();
			cv.put("value", value);
			
			db.update("configure", cv, "name='" + name + "'", null);
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		db.close();

	}
	
	/**
	 * update the Configuration.
	 * 
	 * @param path
	 * @param map
	 */
	public void insert(String name,String value) {
		SQLiteDatabase db = mlpo.getWritableDatabase();
		
		try {
			db.beginTransaction();
			
			ContentValues cv = new ContentValues();
			cv.put("name",name);
			cv.put("value", value);
			
			db.insert("configure", null, cv);
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		db.close();

	}
	
}