package com.duole.service.download.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WidgetDao {

	// the instance of musiclistproviderOpenHelper.
	private DuoleDBHelper ddh;

	public WidgetDao(Context context) {
		ddh = DuoleDBHelper.getInstance(context);
	}

	public Cursor query(String pack) {

		SQLiteDatabase db = ddh.getReadableDatabase();

		Cursor cursor = db.query("widget", new String[] { "widgetid" },
				"package='" + pack + "'", null, "", "", "");

		return cursor;

	}

	public String findWidgetId(String pack) {
		SQLiteDatabase db = ddh.getReadableDatabase();

		Cursor cursor = db.query("widget", new String[] { "widgetid" },
				"package='" + pack + "'", null, "", "", "");
		
		String wid = "";
		
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			wid = cursor.getString(cursor.getColumnIndex("widgetid"));
		}
		
		cursor.close();
		db.close();
		
		return wid == null? "" : wid;
	}

	/**
	 * save the length of downloaded data.
	 * 
	 * @param path
	 * @param map
	 */
	public void save(ContentValues cv) {

		String name = cv.getAsString("package");
		String value = cv.getAsString("widgetid");

		if (name != null && value != null) {

			Cursor cursor = query(name);

			int size = cursor.getCount();
			cursor.close();

			if (size > 0) {
				update(name, value);
			} else {
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
	public void save(String name, String value) {

		if (name != null && value != null) {

			Cursor cursor = query(name);

			int size = cursor.getCount();
			cursor.close();

			if (size > 0) {
				update(name, value);
			} else {
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
	public void update(String name, String value) {
		SQLiteDatabase db = ddh.getWritableDatabase();

		try {
			db.beginTransaction();

			ContentValues cv = new ContentValues();
			cv.put("widgetid", value);

			db.update("widget", cv, "package='" + name + "'", null);

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
	public void insert(String name, String value) {
		SQLiteDatabase db = ddh.getWritableDatabase();

		try {
			db.beginTransaction();

			ContentValues cv = new ContentValues();
			cv.put("package", name);
			cv.put("widgetid", value);

			db.insert("widget", null, cv);

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		db.close();

	}
}
