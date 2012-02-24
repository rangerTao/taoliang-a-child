package com.duole.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.util.Xml;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;

public class XmlUtils {
	public static String filePath = Constants.CacheDir + "/itemlist.xml";
	
	public static int errorcount = 0;

	// public static FileInputStream iStream = null;

	public static void initFile(InputStream is, DocumentBuilder dBuilder,
			File file) throws SAXException, IOException, TransformerException {
		try{
			Document document;
			File itemfile = new File(Constants.ItemList);
			if(!itemfile.exists()){
				itemfile.createNewFile();
			}else{
				itemfile.delete();
				itemfile.createNewFile();
			}
			FileInputStream iStream = new FileInputStream(itemfile);
			document = dBuilder.newDocument();
			DOMSource domSource = new DOMSource(document);
			
			Text value = document.createTextNode("");
			// new elements
			Element newElement = document.createElement("items");

			newElement.appendChild(value);
			
			document.appendChild(newElement);
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			StreamResult streamResult = new StreamResult(file);
			transformer.transform(domSource, streamResult);
			
			iStream.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void initConfiguration(String path,DocumentBuilder dBuilder,
			File file){

		try {
			File itemlist = new File(Constants.SystemConfigFile);
			if(!itemlist.exists()){
				itemlist.createNewFile();
			}
			FileInputStream iStream = new FileInputStream(itemlist);
			Document document = dBuilder.newDocument();

			Text value = document.createTextNode("");
			// new elements
			Element newElement = document.createElement("items");

			newElement.appendChild(value);
			
			document.appendChild(newElement);
//			document.getDocumentElement().appendChild(newElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(itemlist);
			transformer.transform(domSource, streamResult);
			iStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String readNodeValue(String file,String nodename){
		try {
			FileInputStream iStream = new FileInputStream(file);
			
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(iStream, "UTF-8");
			int event = parser.getEventType();
			
			while(event!=XmlPullParser.END_DOCUMENT){
				switch(event){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (nodename.equals(parser.getName())) {
						return parser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					if(Constants.XML_ITEM.equals(parser.getName())){
					}
					break;
					
				}
				event = parser.next();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}

	public static ArrayList<Asset> readFile(String filePath,
			DocumentBuilder dBuilder,boolean update) throws SAXException, IOException, XmlPullParserException, TransformerException, ParserConfigurationException {

		ArrayList<Asset> result = new ArrayList<Asset>();
		
		FileInputStream iStream = new FileInputStream(new File(
				filePath));

		XmlPullParser parser = Xml.newPullParser();
		
		parser.setInput(iStream, "UTF-8");
		
		int event = parser.getEventType();
		Asset asset = null;
		try{
			while(event!=XmlPullParser.END_DOCUMENT){
				switch(event){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if(Constants.XML_ITEM.equals(parser.getName())){
						asset = new Asset();
					}
					
					if(asset != null){
						if(Constants.XML_ID.equals(parser.getName())){
							asset.setId(parser.nextText());
						}
						if(Constants.XML_TITLE.equals(parser.getName())){
							asset.setName(parser.nextText());
						}
						if(Constants.XML_THUMBNAIL.equals(parser.getName())){
							asset.setThumbnail(parser.nextText());
						}
						if(Constants.XML_URL.equals(parser.getName())){
							asset.setUrl(parser.nextText());
						}
						if(Constants.XML_PACKAGE.equals(parser.getName())){
							asset.setPackag(parser.nextText());
						}
						if(Constants.XML_ACTIVITY.equals(parser.getName())){
							asset.setActivity(parser.nextText());
						}
						if(Constants.XML_LASTMODIFIED.equals(parser.getName())){
							asset.setLastmodified(parser.nextText());
						}
						if(Constants.XML_MD5.equals(parser.getName())){
							asset.setMd5(parser.nextText());
						}
						if(Constants.XML_TYPE.equals(parser.getName())){
							asset.setType(parser.nextText());
						}
						if(Constants.XML_BG.equals(parser.getName())){
							asset.setBg(parser.nextText());
						}
						if(Constants.XML_FRONTID.equals(parser.getName())){
							asset.setFrontID(parser.nextText());
						}
						if(Constants.XML_ISFRONT.equals(parser.getName())){
							asset.setIsFront(parser.nextText());
						}
					}
					
					if (Constants.XML_BGURL.equals(parser.getName())) {
						Constants.bgurl = parser.nextText();
					}
					if (Constants.XML_RESTURL.equals(parser.getName())) {
						Constants.bgRestUrl = parser.nextText();
					}
					if (Constants.XML_TIPSTART.equals(parser.getName())) {
						Constants.restart = parser.nextText();
					}
					if (Constants.XML_ENTIME.equals(parser.getName())) {
						String entime = parser.nextText();
						if(entime.equals("")){
							Constants.entime = "25";
						}else{
							Constants.entime = entime;
						}
						
					}
					if (Constants.XML_RESTIME.equals(parser.getName())) {
						String entime = parser.nextText();
						if(entime.equals("")){
							Constants.restime = "120";
						}else{
							Constants.restime = entime;
						}
					}
					if (Constants.XML_SLEEPSTART.equals(parser.getName())) {
						String entime = parser.nextText();
						if(entime.equals("")){
							Constants.sleepstart = "22:00";
						}else{
							Constants.sleepstart = entime;
						}
					}
					if (Constants.XML_SLEEPEND.equals(parser.getName())) {
						String entime = parser.nextText();
						if(entime.equals("")){
							Constants.sleepend = "07:00";
						}else{
							Constants.sleepend = entime;
						}
					}
					if (Constants.XML_KE.equals(parser.getName())){
						Constants.ke = parser.nextText();
					}
					if(Constants.XML_PASSWORD.equals(parser.getName())){
						Constants.System_Password = parser.nextText();
					}
					if(Constants.XML_VER.equals(parser.getName())){
						Constants.System_ver = parser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					if(Constants.XML_ITEM.equals(parser.getName())){
						result.add(asset);
						asset = null;
					}
					break;
					
				}
				event = parser.next();
			}
		}catch (Exception e){
			e.printStackTrace();
			Constants.entime = "25";
			Constants.restime = "120";
			Constants.sleepstart = "22:00";
			Constants.sleepend = "07:00";

			if(errorcount < 1){
				errorcount ++;
				Log.d("TAG", "reading xml error");
				Log.d("TAG", "Fix the item list with backup file.");
				new File(filePath).delete();
				FileUtils.copyFile(filePath  + ".bak", filePath);
				
				return readFile(filePath+".bak", dBuilder,false);
				
			}else {
				return result;
			}
		}
		if(update){
			new File(filePath + ".bak").delete();
			FileUtils.copyFile(filePath, filePath + ".bak");
		}
		return result;
	}
	
	/**
	 * Get the configuration from config.xml
	 * @param filePath
	 * @param dBuilder
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 */
	public static void readConfiguration() throws SAXException, IOException, XmlPullParserException, TransformerException, ParserConfigurationException {
		
		File file = new File(Constants.SystemConfigFile);

		try{
			
			FileInputStream iStream = new FileInputStream(file);

			XmlPullParser parser = Xml.newPullParser();
			
			parser.setInput(iStream, "UTF-8");
			
			int event = parser.getEventType();
			Asset asset = null;
			
			while(event!=XmlPullParser.END_DOCUMENT){
				switch(event){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if(Constants.XML_PASSWORD.equals(parser.getName())){
						Constants.System_Password = parser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}
		}catch (Exception e){
			e.printStackTrace();
			file.delete();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			initConfiguration(Constants.SystemConfigFile,dBuilder,file);
		}
	}

	public static ArrayList<Asset> readXML(InputStream is, String filePath)
			throws IOException, TransformerException, SAXException, XmlPullParserException, ParserConfigurationException {

		ArrayList<Asset> result = null;
		StringBuffer sbresult = new StringBuffer();
		File file = new File(filePath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbf.newDocumentBuilder();
			if (!file.exists()) {
				initFile(is, dBuilder, file);
				
				errorcount = 0;
				result = readFile(filePath+".bak", dBuilder, true);
			}else{
				errorcount = 0;
				result = readFile(filePath, dBuilder,true);
			}

		} catch (ParserConfigurationException e) {
			file.delete();
			createItemList();
			result = readFile(filePath, dBuilder,true);
		} catch (SAXException e) {
			file.delete();
			createItemList();
			result = readFile(filePath, dBuilder,true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (is != null) {
			is.close();
		}
		return result;
	}

	public static void deleteAllItemNodes() throws Exception {
		String[] result = null;
		StringBuffer sbresult = new StringBuffer();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		FileInputStream iStream = null;
		File file = new File(
				Constants.ItemList);
		try {
			dBuilder = dbf.newDocumentBuilder();
			iStream = new FileInputStream(file);
			Document document = dBuilder.parse(iStream);

			NodeList nList = document.getElementsByTagName("item");
			result = new String[nList.getLength()];

			for (int i = 0; i < nList.getLength(); i++) {
				sbresult = new StringBuffer();
				Node node = nList.item(i);
				node.getParentNode().removeChild(node);

				result[i] = sbresult.toString();
			}
			
			//bgurl bgRestUrl  entime  restime sleepstart sleepend  ke tipstart
			String[] names = {"bgurl", "bgRestUrl", "entime", "restime", "sleepstart", "sleepend", "ke", "tipstart"};
			removeNode(document, names);
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			transformer.transform(domSource, streamResult);

			iStream.close();
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
			initFile(iStream, dBuilder, file);
			throw e;
		}
	}
	
	public static void removeNode(Document document , String[] name){
		
		NodeList nl;
		
		for( int j = 0 ; j < name.length ; j ++){
			//bgurl bgRestUrl  entime  restime sleepstart sleepend  ke tipstart
			nl = document.getElementsByTagName(name[j]);
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				node.getParentNode().removeChild(node);
			}
		}
		
		
	}

	public static void addNode(ArrayList<Asset> assetList) throws IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			FileInputStream iStream = new FileInputStream(new File(
					Constants.ItemList));
			Document document = dBuilder.parse(iStream);

			for (int i = 0; i < assetList.size(); i++) {

				Asset asset = assetList.get(i);

				// the values
				Text id = document.createTextNode(asset.getId().trim());
				Text title = document.createTextNode(asset.getName().trim());
				Text thumbnail = document.createTextNode(asset.getThumbnail().trim());
				Text url = document.createTextNode(asset.getUrl().trim());
				Text lastmodified = document.createTextNode(asset
						.getLastmodified().trim());
				Text type = document.createTextNode(asset.getType().trim());
				Text frontId = document.createTextNode(asset.getFrontID().trim());
				Text isFront = document.createTextNode(asset.getIsFront().trim());
				Text md5 = document.createTextNode(asset.getMd5().trim());

				// new elements
				Element newElement = document.createElement("item");
				Element newIdElement = document.createElement("id");
				Element newTitleElement = document.createElement("title");
				Element newThumbElement = document.createElement("thumbnail");
				Element newUrlElement = document.createElement("url");
				Element newLastModifiedElement = document
						.createElement("lastmodified");
				Element newTypeElement = document.createElement("type");
				Element newFrontID = document.createElement("frontid");
				Element newIsFront = document.createElement("isfront");
				Element newMd5 = document.createElement("md5");
				
				
				newIdElement.appendChild(id);
				newTitleElement.appendChild(title);
				newThumbElement.appendChild(thumbnail);
				newUrlElement.appendChild(url);
				newLastModifiedElement.appendChild(lastmodified);
				newTypeElement.appendChild(type);
				newFrontID.appendChild(frontId);
				newIsFront.appendChild(isFront);
				newMd5.appendChild(md5);

				newElement.appendChild(newIdElement);
				newElement.appendChild(newTitleElement);
				newElement.appendChild(newThumbElement);
				newElement.appendChild(newUrlElement);
				newElement.appendChild(newLastModifiedElement);
				newElement.appendChild(newTypeElement);
				newElement.appendChild(newFrontID);
				newElement.appendChild(newIsFront);
				newElement.appendChild(newMd5);
				
				if(asset.getType().equals(Constants.RES_AUDIO)){
					Text bg = document.createTextNode(asset.getBg().trim());
					Element newBGElement = document.createElement("bg");
					newBGElement.appendChild(bg);
					newElement.appendChild(newBGElement);
				}
				
				if(asset.getType().equals(Constants.RES_APK)){
					
					PackageManager pm = Duole.appref.getPackageManager();
					File file = new File(Constants.CacheDir + Constants.RES_APK + asset.getUrl().substring(asset.getUrl().lastIndexOf("/")));

					PackageInfo info;
					info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

					if(info != null){
						Text packag = document.createTextNode(info.packageName);
						Element newPackage = document.createElement("package");
						newPackage.appendChild(packag);
						newElement.appendChild(newPackage);
						
						Text mainActivity = document.createTextNode(info.activities[0].name);
						Element newActivity = document.createElement("activity");
						newActivity.appendChild(mainActivity);
						newElement.appendChild(newActivity);
						
					}
				}
				
				document.getDocumentElement().appendChild(newElement);

			}

			updateNode(document);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			
			transformer.transform(domSource, streamResult);
			iStream.close();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addSingleNode(String name,String value) throws IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			FileInputStream iStream = new FileInputStream(new File(
					Constants.ItemList));
			Document document = dBuilder.parse(iStream);

			// the values
			Text newText = document.createTextNode(value);

			// new elements
			Element newElement = document.createElement(name);
			
			newElement.appendChild(newText);
			
			document.getDocumentElement().appendChild(newElement);

			updateNode(document);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			transformer.transform(domSource, streamResult);
			iStream.close();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Update the confiration.
	 * @param document
	 */
	public static void updateNode(Document document) {
		
		NodeList nl;
		//bg url
		if (!Constants.bgurl.equals("")) {
			nl = document.getElementsByTagName("bgurl"); 
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild()
				.setNodeValue(Constants.bgurl);
			}else{
				createNode(document,"bgurl",Constants.bgurl);
			}
		}

		//Bg of rest
		if (!Constants.bgRestUrl.equals("")) {
			nl = document.getElementsByTagName("bgRestUrl");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.bgRestUrl);
			}else{
				createNode(document,"bgRestUrl",Constants.bgRestUrl);
			}
		}

		//entainment time.
		if (!Constants.entime.equals("")) {
			nl = document.getElementsByTagName("entime");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.entime);
			}else{
				createNode(document,"entime",Constants.entime);
			}
		}

		//rest tiem
		if (!Constants.restime.equals("")) {
			nl = document.getElementsByTagName("restime");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.restime);
			}else{
				createNode(document,"restime",Constants.restime);
			}
		}

		//sleepstart
		if (!Constants.sleepstart.equals("")) {
			nl = document.getElementsByTagName("sleepstart");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.sleepstart);
				
			}else{
				createNode(document,"sleepstart",Constants.sleepstart);
			}
		}

		//sleepend
		if (!Constants.sleepend.equals("")) {
			nl = document.getElementsByTagName("sleepend");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.sleepend);
			}else{
				createNode(document,"sleepend",Constants.sleepend);
			}
		}
		
		//ke
		if (!Constants.ke.equals("")) {
			nl = document.getElementsByTagName("ke");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.ke);
			}else{
				createNode(document,"ke",Constants.ke);
			}
		}
		
		//tipstart
		if (!Constants.restart.equals("")) {
			nl = document.getElementsByTagName("tipstart");
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.restart);
			}else{
				createNode(document,"tipstart",Constants.restart);
			}
		}
		
		if (!Constants.System_Password.equals("")){
			nl = document.getElementsByTagName(Constants.XML_PASSWORD);
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.System_Password);
			}else{
				createNode(document,Constants.XML_PASSWORD,Constants.System_Password);
			}
		}

	}
	
	/**
	 * update a node in file itemlist.xml
	 * @param name
	 * @param value
	 * @return
	 */
	public static boolean updateSingleNode(String name,String value){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			FileInputStream iStream = new FileInputStream(new File(
					Constants.ItemList));
			Document document = dBuilder.parse(iStream);
			
			NodeList nl;
			if (!value.equals("")){
				nl = document.getElementsByTagName(name);
				if(nl.getLength() > 0){
					nl.item(0).getFirstChild().setNodeValue(value);
				}else{
					createNode(document,name,value);
				}
			}

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			transformer.transform(domSource, streamResult);
			iStream.close();
		}catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Update a node in file.
	 * @param file
	 * @param name
	 * @param value
	 * @return
	 */
	public static boolean updateSingleNode(String file,String name,String value){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			FileInputStream iStream = new FileInputStream(new File(file));
			Document document = dBuilder.parse(iStream);
			
			NodeList nl;
			if (!value.equals("")){
				nl = document.getElementsByTagName(name);
				if(nl.getLength() > 0){
					if(nl.item(0).getFirstChild() != null){
						nl.item(0).getFirstChild().setNodeValue(value);
					}else{
						Text nodevalue = document.createTextNode(value);
						nl.item(0).appendChild(nodevalue);
					}
					nl.item(0).getFirstChild().setNodeValue(value);
				}else{
					createNode(document,name,value);
				}
			}   

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(file));
			transformer.transform(domSource, streamResult);
			iStream.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Create a new xml node.
	 * @param document
	 * @param name
	 * @param value
	 */
	public static void createNode(Document document,String name,String value){
		
		//Create a new node.
		Text nodevalue = document.createTextNode(value);
		Element node = document.createElement(name);
		
		node.appendChild(nodevalue);
		
		//Append it.
		document.getDocumentElement().appendChild(node);
		
	}

	public static void createItemList(){


		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			File itemlist = new File(Constants.ItemList);
			if(!itemlist.exists()){
				itemlist.createNewFile();
			}
			FileInputStream iStream = new FileInputStream(itemlist);
			Document document = dBuilder.newDocument();

			Text value = document.createTextNode("");
			// new elements
			Element newElement = document.createElement("items");

			newElement.appendChild(value);
			
			document.appendChild(newElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			transformer.transform(domSource, streamResult);
			iStream.close();
			
			if(Constants.AssetList.size() > 0 ){
				addNode(Constants.AssetList);
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
