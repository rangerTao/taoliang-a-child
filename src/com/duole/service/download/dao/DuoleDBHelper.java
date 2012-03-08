package com.duole.service.download.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DuoleDBHelper extends SQLiteOpenHelper{
	
	private static final String DBNAME = "duole.db";
	private static final int VERSION = 3;
	
	private static DuoleDBHelper fmdd;
	
	public static DuoleDBHelper getInstance(Context context){
		if(fmdd == null){
			return new DuoleDBHelper(context);
		}else{
			return fmdd;
		}
	}

	public DuoleDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DuoleDBHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS musiclist (id integer primary key autoincrement, name varchar(255), thumb varchar(255),path varchar(255), modify_time varchar(255))");
		db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(255), threadid INTEGER, downlength INTEGER)");
		db.execSQL("CREATE TABLE IF NOT EXISTS configure (id integer primary key autoincrement, name varchar(255), value varchar(255))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
		db.execSQL("DROP TABLE IF EXISTS musiclist");
		onCreate(db);
	}

}
