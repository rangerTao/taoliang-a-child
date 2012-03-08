package com.duole.provider;

import com.duole.Duole;
import com.duole.service.download.dao.ConfigDao;
import com.duole.service.download.dao.MusicListDao;

import android.R.integer;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DuoleProvider extends ContentProvider {

	private static final UriMatcher sURLMatcher =new UriMatcher(UriMatcher.NO_MATCH);
	
	private static final int MUSIC_ALL = 1;
	private static final int CONFIG = 2;
	
	static {
		sURLMatcher.addURI("com.duole.provider", "music", MUSIC_ALL);
		sURLMatcher.addURI("com.duole.provider", "config", CONFIG);
		}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		int match = sURLMatcher.match(uri);
		
		switch (match) {
		case CONFIG:
			ConfigDao cd = new ConfigDao(Duole.appref);
			cd.save(values);
			break;

		default:
			break;
		}
		
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Cursor cursor = null;
		
		int match = sURLMatcher.match(uri);
		
		switch (match) {
		
		case MUSIC_ALL:
			MusicListDao mld = new MusicListDao(Duole.appref);
			cursor = mld.query();
			break;
		case CONFIG:
			ConfigDao cd = new ConfigDao(Duole.appref);
			cursor = cd.queryAll();
			break;
			
		}
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
