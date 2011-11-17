package com.duole.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.duole.pojos.asset.Asset;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterBC;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;

public class FileUtils {

	// whether tmep file exists
	public static boolean isCacheFileExists(String filename) {

		File file = new File(Constants.CacheDir + "/temp/" + filename);

		if (file.exists()) {
			return true;
		}

		return false;

	}

	// move files
	public static void moveFile(File source, File target) {
		try {
			Runtime.getRuntime().exec(
					"mv " + source.getAbsolutePath() + " "
							+ target.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(String oldPathFile, String newPathFile) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPathFile); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[512];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readTxt(ArrayList<String[]> ids, String filepath) {

		FileInputStream fis;
		try {
			File file = new File(filepath);
			if (file.exists()) {
				fis = new FileInputStream(file);

				InputStreamReader isr = new InputStreamReader(fis);

				BufferedReader br = new BufferedReader(isr);

				String temp = br.readLine();
				int index = 0;
				while (temp != null) {
					String[] tem = temp.split(" ");
					ids.add(tem);
					temp = br.readLine();
					index++;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void saveTxt(String[] ids, String filepath) {

		try {
			File file = new File(filepath);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file, true);

			if (ids == null) {
				fw.write("");
			} else {
				fw.write(ids[0] + " " + ids[1] + " " + ids[2] + "\n");
			}

			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Bitmap decodeBMPFromURL(String bmpath) {

		return null;
	}

	// When all download task done.clear the temp folder
	public static void clearTempFolder(String path) {

		File tempFolder = new File(path);
		if (tempFolder.isDirectory()) {
			for (File temp : tempFolder.listFiles()) {
				temp.delete();
			}
		}

	}

	/**
	 * Unzip a file
	 * 
	 * @param zipFile
	 *            path
	 * @param targetDir
	 *            path
	 * @param pass
	 */
	public static void Unzip(String zipFile, String targetDir, String pass) {

		Log.v("TAG", "Unzip file:" + zipFile + " to:" + " " + targetDir);
		try {
			AesZipFileDecrypter zipFile1;

			AESDecrypterBC aesd = new AESDecrypterBC();

			zipFile1 = new AesZipFileDecrypter(new File(zipFile), aesd);

			File foldertemp = new File(targetDir);
			if(foldertemp.exists()){
				emptyFolder(foldertemp);
				foldertemp.delete();
			}
			
			for (ExtZipEntry entry : zipFile1.getEntryList()) {
				File file = new File(targetDir + "/" + entry.getName());
				if (!file.exists()) {
					if (entry.isDirectory()) {
						file.mkdirs();
					} else {
						File folder = new File(file.getAbsolutePath()
								.substring(
										0,
										file.getAbsolutePath()
												.lastIndexOf("/")));
						if (!folder.exists()) {
							folder.mkdirs();
						}
						file.createNewFile();
						zipFile1.extractEntry(entry, file, pass);
					}

				}
			}

		} catch (Exception cwj) {
			cwj.printStackTrace();
			File file = new File(targetDir);
			if(emptyFolder(file)){
				file.delete();
			}
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param asset
	 * @return
	 */
	public static String getPackagenameFromAPK(Context context,Asset asset){
		PackageManager pm = context.getPackageManager();
		File file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

		PackageInfo info;
		info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

		if(info != null){
			return info.packageName;
		}else{
			return "";
		}
	}
	
	
	public static boolean emptyFolder(File file){
		
		for(File temp : file.listFiles()){
			if(temp.isDirectory()){
				emptyFolder(temp);
				temp.delete();
			}else{
				temp.delete();
			}
		}
		
		return true;
	}
}
