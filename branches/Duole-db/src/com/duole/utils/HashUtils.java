package com.duole.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtils {

	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/**
	 * Get the md5.
	 * @param filepath
	 * @return
	 */
	public static String getMD5(String filepath){
		try {
			return getHash(filepath, "MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Get hash of current type.
	 * @param fileName the name of file to verify.
	 * @param hashType.type:md5...
	 * @return
	 * @throws Exception
	 */
	private static String getHash(String filepath, String hashType)
			throws Exception {
		InputStream fis;
		fis = new FileInputStream(filepath);
		byte[] buffer = new byte[1024];
		MessageDigest md5 = MessageDigest.getInstance(hashType);
		int numRead = 0;
		while ((numRead = fis.read(buffer)) > 0) {
			md5.update(buffer, 0, numRead);
		}
		fis.close();
		return toHexString(md5.digest());
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}
}
