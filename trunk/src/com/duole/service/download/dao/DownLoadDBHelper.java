package com.duole.service.download.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DownLoadDBHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "downlog.db";
	private static final int VERSION = 1;

	private static DownLoadDBHelper dldb;

	public static DownLoadDBHelper getInstance(Context context) {
		if (dldb == null) {
			return new DownLoadDBHelper(context);
		} else {
			return dldb;
		}
	}

	public DownLoadDBHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	public DownLoadDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(255), threadid INTEGER, downlength INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
	}

}
