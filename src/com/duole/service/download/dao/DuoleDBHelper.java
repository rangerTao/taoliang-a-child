package com.duole.service.download.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DuoleDBHelper extends SQLiteOpenHelper{
	
	private static final String DBNAME = "duole.db";
	private static final int VERSION = 6;
	
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
		db.execSQL("CREATE TABLE IF NOT EXISTS asset (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
				"name VARCHAR(255) NOT NULL," +
				"id varchar(20) NOT NULL," +
				"thumbnail VARCHAR(255)," +
				"size VARCHAR(32)," +
				"type VARCHAR(10)," +
				"url VARCHAR(255)," +
				"bg VARCHAR(255)," +
				"isfront VARCHAR(5)," +
				"frontid VARCHAR(20)," +
				"lastmodified VARCHAR(40)," +
				"filename VARCHAR(255)," +
				"packagename VARCHAR(255)," +
				"activity VARCHAR(255)," +
				"md5 VARCHAR(35)," +
				"createtime VARCHAR(50)," +
				"modifytime VARCHAR(50))");
		db.execSQL("CREATE TABLE IF NOT EXISTS widget (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,package VARCHAR(255) NOT NULL,widgetid VARCHAR(10) NULL)");
		db.execSQL("CREATE TABLE IF NOT EXISTS baseapp (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,bid VARCHAR(50) UNIQUE NOT NULL,bname VARCHAR(255) NULL,bpath VARCHAR(255) NULL,filemd5 VARCHAR(40) NULL,uptime VARCHAR(40) NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
		db.execSQL("DROP TABLE IF EXISTS asset");
//		db.execSQL("DROP TABLE IF EXISTS musiclist");
		onCreate(db);
	}

}
