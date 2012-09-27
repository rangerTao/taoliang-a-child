package com.duole.service.download.dao;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AssetDao {

	// the instance of musiclistproviderOpenHelper.
	private AssetDBHelper ddh;

	private String db_name = "asset";

	public AssetDao(Context context) {
		ddh = AssetDBHelper.getInstance(context);
	}

	public Cursor queryAll() {

		SQLiteDatabase db = ddh.getReadableDatabase();

		Cursor cursor = db.query(db_name, null, "", null, "", "", "");

		return cursor;

	}

	public void cleanAll() {

		SQLiteDatabase db = ddh.getWritableDatabase();

		db.delete(db_name, "", null);

		db.close();
	}

	public void addToDB(ArrayList<Asset> assets) {
		cleanAll();

		insert(assets);
	}

	/**
	 * update the Asset list.
	 * 
	 * @param path
	 * @param map
	 */
	public void insert(ArrayList<Asset> assets) {
		SQLiteDatabase db = ddh.getWritableDatabase();

		try {

			db.beginTransaction();

			for (Asset asset : assets) {

				ContentValues cv = new ContentValues();

				cv.put("name", asset.getName());
				cv.put("id", asset.getId());
				cv.put("thumbnail", asset.getThumbnail());
				cv.put("size", "");
				cv.put("type", asset.getType());
				cv.put("url", asset.getUrl());

				if (asset.getType().equals(Constants.RES_AUDIO)) {
					cv.put("bg", asset.getBg());
				}

				if (asset.getType().equals(Constants.RES_APK)) {

					PackageManager pm = Duole.appref.getPackageManager();
					File file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

					PackageInfo info;
					info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

					if (info != null) {
						cv.put("packagename", info.packageName);
						cv.put("activity", info.activities[0].name);
					}
				}

				if (asset.getType().equals(Constants.RES_WIDGET)) {
					PackageManager pm = Duole.appref.getPackageManager();
					File file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

					PackageInfo info;
					info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

					if (info != null) {
						cv.put("packagename", info.packageName);
					}
				}

				cv.put("isfront", asset.getIsFront());
				cv.put("frontid", asset.getFrontID());
				cv.put("lastmodified", asset.getLastmodified());
				cv.put("filename", "");
				cv.put("md5", asset.getMd5());
				cv.put("createtime", DuoleUtils.getCurrentTime());

				db.insert(db_name, null, cv);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		db.close();
	}

}
