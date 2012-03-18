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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import com.duole.pojos.asset.Asset;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterBC;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
	
	/**
	 * Get the size of used and free space on sdcard.
	 * @param block counts.
	 * @param block size.
	 * @return
	 */
	public static long countUp(int a, int b){
		BigDecimal bc = new BigDecimal(a);
		BigDecimal bs = new BigDecimal(b);
		return Integer.parseInt(bc.multiply(bs).divide(new BigDecimal(1000).multiply(new BigDecimal(1000))).setScale(0,BigDecimal.ROUND_UP).toString());
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
				InputStream inStream = new FileInputStream(oldPathFile); // ����ԭ�ļ�
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
				while (temp != null) {
					String[] tem = temp.split(" ");
					ids.add(tem);
					temp = br.readLine();
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
	 * Swipe the cache dir.
	 */
	public static boolean clearUselessResource() {

		//Clear the temp folder.
		clearTempFolder(Constants.CacheDir + "/temp/");

		try {
			
			Constants.AssetList = XmlUtils.readXML(null, Constants.CacheDir
					+ "itemlist.xml");
			if(Constants.AssetList.size() < 1){
				return false;
			}

			HashMap<String, String> usefulFile = new HashMap<String, String>();

			for (Asset asset : Constants.AssetList) {
				usefulFile.put(
						asset.getThumbnail().substring(
								asset.getThumbnail().lastIndexOf("/") + 1),
						asset.getId());
				usefulFile.put(
						asset.getUrl().substring(
								asset.getUrl().lastIndexOf("/") + 1),
						asset.getId());

				if (asset.getType().equals("front")) {
					usefulFile.put(asset.getId(), asset.getType());
				}
			}

			usefulFile.put(Constants.bgRestUrl.substring(Constants.bgRestUrl
					.lastIndexOf("/") + 1), "bgrest");
			usefulFile.put(Constants.bgurl.substring(Constants.bgurl
					.lastIndexOf("/") + 1), "bg");

			File mainFolder = new File(Constants.CacheDir);
			File[] files = mainFolder.listFiles();

			for (File temp : files) {
				if (temp.getName().equals("picture")
						|| temp.getName().equals("log")) {
					continue;
				}
				if (temp.isDirectory()) {
					File[] dirFiles = temp.listFiles();
					for (File inDir : dirFiles) {
						if (!usefulFile.containsKey(inDir.getName())) {
							if(inDir != null){
								FileUtils.emptyFolder(inDir);
								inDir.delete();
								Log.d("TAG", "Useless file :" + inDir.getAbsolutePath());
							}
							
						}
					}
				}
				if (isPic(temp)) {
					if (!usefulFile.containsKey(temp.getName())) {
						temp.delete();
						Log.d("TAG", "Useless file :" + temp.getAbsolutePath());
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	
	/**
	 * Is a pic file.
	 * @param file
	 * @return
	 */
	public static boolean isPic(File file) {
		if (file.getName().toLowerCase().endsWith(".jpg")
				|| file.getName().toLowerCase().endsWith(".jpeg")
				|| file.getName().toLowerCase().endsWith("png")) {
			return true;
		} else {
			return false;
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
						Log.d("TAG", "file path " + file.getAbsolutePath());
						file.createNewFile();
						zipFile1.extractEntry(entry, file, pass);
					}

				}
			}

		} catch (Exception cwj) {
			cwj.printStackTrace();
			new File(zipFile).delete();
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
		try{
			info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
		}catch (Exception e) {
			e.printStackTrace();
			file.delete();
			return "";
		}

		if(info != null){
			return info.packageName;
		}else{
			return "";
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param asset
	 * @return
	 */
	public static String getPackagenameFromFile(Context context,File file){
		PackageManager pm = context.getPackageManager();

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
	
	
	/**
	 * Change a bitmap to Round corner.
	 * @param bitmap
	 * @param pixels
	 * @return
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) { 

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888); 
		Canvas canvas = new Canvas(output); 

		final int color = 0xff424242; 
		final Paint paint = new Paint(); 
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
		final RectF rectF = new RectF(rect); 
		final float roundPx = pixels; 

		paint.setAntiAlias(true); 
		canvas.drawARGB(0, 0, 0, 0); 
		paint.setColor(color); 
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
		canvas.drawBitmap(bitmap, rect, rect, paint); 

		return output; 
		}
}
