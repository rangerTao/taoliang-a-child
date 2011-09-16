package com.duole.pojos.asset;

import org.json.JSONObject;

public class Asset {

	private String name;
	private String thumbnail;
	private String size;
	private String url;
	private String type;
	private String id;
	private String lastmodified;
	private String filename;
	private String packag;
	private String activity;

	public Asset(JSONObject json) {
		try {
			String title = json.getString("title");
			String pic = json.getString("pic");
			String url = json.getString("url");
			String type = json.getString("type");
			String id = json.getString("id");
			String lastmodifi = json.getString("lastmodified");
			setName(title == null ? "" : title);
			setThumbnail(pic == null ? "" : pic);
			setUrl(url == null ? "" : url);
			setType(type == null ? "" : type);
			setId(id == null ? "" : id);
			setLastmodified(lastmodifi == null ? "" : lastmodifi);
			setFilename(url.substring(url.lastIndexOf("/")));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Asset() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getPackag() {
		return packag;
	}

	public void setPackag(String packag) {
		this.packag = packag;
	}
	
	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String toString(){
		return "name: " + this.getName() +" "
	+ "thumbnail: " + this.getThumbnail() + " "
	+ "url: "+ this.getUrl() + " "
	+ "type: " + this.getType() + " "
	+ "id: " + this.getId() + " "
	+ "lastmodified: " + this.getLastmodified() + " "
	+ "filename: "+ this.getFilename();
	}

}
