package com.duole.pojos.asset;

import java.util.jar.Attributes.Name;

import org.json.JSONObject;

public class BaseApp {

	/*
	 * item0: {
      bid: "6",
      bname: "ProiorityRes",
      path: "/upc/baseapp/PriorityRes.apk",
      filemd5: "891659d0120f04932c637af35529b329",
      uptime: "2012-03-22"
    }
	 * 
	 * */
	
	
	private String bid;
	private String bname;
	private String bPath;
	private String filemd5;
	private String uptime;
	
	public BaseApp(JSONObject json){
		
		try{
			String id = json.getString("bid");
			String name = json.getString("bname");
			String path = json.getString("path");
			String md5 = json.getString("filemd5");
			String time = json.getString("uptime");
			
			bid = id == null?"":id;
			bname = name == null ? "" : name;
			bPath = path == null ? "" : path;
			filemd5 = md5 == null ? "" : md5;
			uptime = time == null ? "" : time;
			
		}catch (Exception e) {
		}
		
	}
	
	
	public String getBid() {
		
		return bid == null?"":bid;
	}
	public void setBid(String bid) {
		this.bid = bid;
	}
	public String getBname() {
		return bname  == null?"":bname;
	}
	public void setBname(String bname) {
		this.bname = bname;
	}
	public String getbPath() {
		return bPath == null?"":bPath;
	}
	public void setbPath(String bPath) {
		this.bPath = bPath;
	}
	public String getFilemd5() {
		return filemd5 == null?"":filemd5;
	}
	public void setFilemd5(String filemd5) {
		this.filemd5 = filemd5;
	}
	public String getUptime() {
		return uptime == null?"":uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	
	
}
