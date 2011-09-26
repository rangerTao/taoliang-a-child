package com.duole.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

	
	//whether tmep file exists
	public static boolean isCacheFileExists(String filename){
		
		File file = new File(Constants.CacheDir + "/temp/" + filename);
		
		if(file.exists()){
			return true;
		}
		
		return false;
		
	}
	
	//move files
	public static void moveFile(File source,File target){
		try{
			Runtime.getRuntime().exec("mv " + source.getAbsolutePath() + " " + target.getAbsolutePath());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(String oldPathFile, String newPathFile) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPathFile); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[512];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
