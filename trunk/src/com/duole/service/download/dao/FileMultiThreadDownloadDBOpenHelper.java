package com.duole.service.download.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FileMultiThreadDownloadDBOpenHelper extends SQLiteOpenHelper{
	
	private static final String DBNAME = "duole.db";
	private static final int VERSION = 1;
	
	private static FileMultiThreadDownloadDBOpenHelper fmdd;
	
	public static FileMultiThreadDownloadDBOpenHelper getInstance(Context context){
		if(fmdd == null){
			return new FileMultiThreadDownloadDBOpenHelper(context);
		}else{
			return fmdd;
		}
	}

	public FileMultiThreadDownloadDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public FileMultiThreadDownloadDBOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(255), threadid INTEGER, downlength INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
		onCreate(db);
	}

}
