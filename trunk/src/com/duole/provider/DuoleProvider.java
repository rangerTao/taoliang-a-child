package com.duole.provider;

import com.duole.Duole;
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
	
	static {
		sURLMatcher.addURI("com.duole.provider", "music", MUSIC_ALL);
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
		
		
		MusicListDao mld = new MusicListDao(Duole.appref);
		
		int match = sURLMatcher.match(uri);
		
		switch (match) {
		case MUSIC_ALL:
			cursor = mld.query();
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
