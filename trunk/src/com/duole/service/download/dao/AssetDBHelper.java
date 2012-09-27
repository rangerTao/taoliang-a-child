package com.duole.service.download.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AssetDBHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "duoleasset.db";
	private static final int VERSION = 6;
	
	private static AssetDBHelper adbh;
	
	public static AssetDBHelper getInstance(Context context){
		if(adbh == null){
			return new AssetDBHelper(context);
		}else{
			return adbh;
		}
	}
	
	public AssetDBHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}
	
	public AssetDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
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
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS asset");
	}

}
