package com.duole.service.download.dao;

import java.util.ArrayList;
import java.util.HashMap;

import com.duole.R.string;
import com.duole.pojos.asset.BaseApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BaseAppDao {

	private String table_name = "baseapp";

	// the instance of musiclistproviderOpenHelper.
	private DuoleDBHelper bad;

	public BaseAppDao(Context context) {
		bad = DuoleDBHelper.getInstance(context);
	}

	public Cursor query() {

		SQLiteDatabase db = bad.getReadableDatabase();

		Cursor cursor = db.query(table_name, null, "", null, "", "", "");

		return cursor;

	}

	public Cursor query(String bid) {

		SQLiteDatabase db = bad.getReadableDatabase();

		Cursor cursor = db.query(table_name, null, "bid='" + bid + "'", null,
				"", "", "");

		return cursor;

	}

	public void save(ArrayList<BaseApp> alba) {

		HashMap<String, String> hmba = new HashMap<String, String>();
		Cursor all = query();
		all.moveToFirst();
		int i = 0;
		for (all.moveToFirst(); !all.isAfterLast(); all.moveToNext()) {
			hmba.put(i + "", all.getString(all.getColumnIndex("bid")));
			i++;
		}

		SQLiteDatabase db = bad.getWritableDatabase();

		try {

			for (BaseApp ba : alba) {

				if (hmba.containsValue(ba.getBid())) {
					update(db, ba);
				} else {
					insert(db, ba);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		SQLiteDatabase db = bad.getWritableDatabase();

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

	public void update(SQLiteDatabase db, BaseApp ba) {

		db.beginTransaction();

		ContentValues cv = new ContentValues();
		cv.put("bid", ba.getBid());
		cv.put("bname", ba.getBname());
		cv.put("bpath", ba.getbPath());
		cv.put("filemd5", ba.getFilemd5());
		cv.put("uptime", ba.getUptime());

		db.update(table_name, cv, "bid='" + ba.getBid() + "'", null);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void insert(SQLiteDatabase db, BaseApp ba) {

		db.beginTransaction();

		ContentValues cv = new ContentValues();
		cv.put("bid", ba.getBid());
		cv.put("bname", ba.getBname());
		cv.put("bpath", ba.getbPath());
		cv.put("filemd5", ba.getFilemd5());
		cv.put("uptime", ba.getUptime());

		db.insert(table_name, null, cv);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/**
	 * update the Configuration.
	 * 
	 * @param path
	 * @param map
	 */
	public void insert(String name, String value) {
		SQLiteDatabase db = bad.getWritableDatabase();

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

	/**
	 * when download successful finished.delete the current log.
	 * 
	 * @param path
	 */
	public void delete(ArrayList<String> ids) {
		SQLiteDatabase db = bad.getWritableDatabase();
		if (!db.isDbLockedByOtherThreads() || !db.isDbLockedByCurrentThread()) {
			for (String id : ids) {
				db.execSQL("delete from musiclist where id ='" + id + "'",
						new Object[] {});
			}
		}
		db.close();
	}

	public void delete(String id) {
		try {
			SQLiteDatabase db = bad.getWritableDatabase();
			if (!db.isDbLockedByOtherThreads()
					|| !db.isDbLockedByCurrentThread()) {
				db.execSQL("delete from " + table_name + " where bid ='" + id + "'",
						new Object[] {});
			}
			db.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
