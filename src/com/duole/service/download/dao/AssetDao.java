package com.duole.service.download.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AssetDao {

	//the instance of musiclistproviderOpenHelper.
	private DuoleDBHelper ddh;
	
	public AssetDao(Context context){
		ddh = DuoleDBHelper.getInstance(context);
	}
	
	public Cursor queryAll() {

		SQLiteDatabase db = ddh.getReadableDatabase();
		
		Cursor cursor = db.query("asset", null, "", null, "", "", "");
		
		return cursor;

	}

}
