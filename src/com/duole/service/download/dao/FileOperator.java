package com.duole.service.download.dao;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FileOperator {
	
	private DownLoadDBHelper fileHelper;
	
	public FileOperator(Context context){
		fileHelper = DownLoadDBHelper.getInstance(context);
	}
	
	/**
	 * Get the length of saved data
	 * 
	 * @param path
	 * @return
	 */
	public Map<Integer, Integer> getData(String path)
	{
		SQLiteDatabase db = fileHelper.getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"select threadid, downlength from filedownlog where downpath=?",
						new String[] { path });
		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		while (cursor.moveToNext())
		{
			data.put(cursor.getInt(0), cursor.getInt(1));
		}
		cursor.close();
		db.close();
		return data;
	}
	
	/**
	 * save the length of downloaded data.
	 * 
	 * @param path
	 * @param map
	 */
	public void save(String path, Map<Integer, Integer> map)
	{// int threadid, int position
		SQLiteDatabase db = fileHelper.getWritableDatabase();
		if(!db.isDbLockedByOtherThreads() || !db.isDbLockedByCurrentThread()){
			try
			{
				db.beginTransaction();
				for (Map.Entry<Integer, Integer> entry : map.entrySet())
				{
					db.execSQL(
							"insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
							new Object[] { path, entry.getKey(), entry.getValue() });
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
	 * update the log.
	 * 
	 * @param path
	 * @param map
	 */
	public void update(String path, Map<Integer, Integer> map) {
		SQLiteDatabase db = fileHelper.getWritableDatabase();
		
		try {
			db.beginTransaction();
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				db.execSQL(
						"update filedownlog set downlength=? where downpath=? and threadid=?",
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
	public void delete(String path)
	{
		SQLiteDatabase db = fileHelper.getWritableDatabase();
		if(!db.isDbLockedByOtherThreads() || !db.isDbLockedByCurrentThread()){
			db.execSQL("delete from filedownlog where downpath=?",
					new Object[] { path });
		}
		db.close();
	}

}
