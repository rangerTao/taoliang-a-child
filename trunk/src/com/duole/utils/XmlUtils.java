package com.duole.utils;

import java.io.File;
import java.io.FileInputStream;
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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;

public class XmlUtils {
	public static String filePath = Constants.CacheDir + "/itemlist.xml";

	// public static FileInputStream iStream = null;

	public static void initFile(InputStream is, DocumentBuilder dBuilder,
			File file) throws SAXException, IOException, TransformerException {
		Document document;
		File itemfile = new File(Constants.ItemList);
		if(!itemfile.exists()){
			itemfile.createNewFile();
		}
		FileInputStream iStream = new FileInputStream(itemfile);
		document = dBuilder.parse(iStream);
		DOMSource domSource = new DOMSource(document);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer;
		transformer = transformerFactory.newTransformer();
		StreamResult streamResult = new StreamResult(file);
		transformer.transform(domSource, streamResult);

	}

	public static ArrayList<Asset> readFile(String filePath,
			DocumentBuilder dBuilder) throws SAXException, IOException, XmlPullParserException {

		ArrayList<Asset> result = new ArrayList<Asset>();
		
		FileInputStream iStream = new FileInputStream(new File(
				XmlUtils.filePath));

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
							asset.setFilename(parser.nextText());
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
						if(Constants.XML_TYPE.equals(parser.getName())){
							asset.setType(parser.nextText());
						}
					}
					
					if (Constants.XML_BGURL.equals(parser.getName())) {
						Constants.bgurl = parser.nextText();
					}
					if (Constants.XML_RESTURL.equals(parser.getName())) {
						Constants.bgRestUrl = parser.nextText();
					}
					if (Constants.XML_ENTIME.equals(parser.getName())) {
						Constants.entime = parser.nextText();
					}
					if (Constants.XML_RESTIME.equals(parser.getName())) {
						Constants.restime = parser.nextText();
					}
					if (Constants.XML_SLEEPSTART.equals(parser.getName())) {
						Constants.sleepstart = parser.nextText();
					}
					if (Constants.XML_SLEEPEND.equals(parser.getName())) {
						Constants.sleepend = parser.nextText();
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
			createItemList();
			Constants.AssetList = readFile(filePath,
					dBuilder);
		}
		 
		
		return result;
	}

	public static ArrayList<Asset> readXML(InputStream is, String filePath)
			throws IOException, TransformerException, SAXException, XmlPullParserException {

		ArrayList<Asset> result = null;
		StringBuffer sbresult = new StringBuffer();
		File file = new File(filePath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbf.newDocumentBuilder();
			if (!file.exists()) {
				initFile(is, dBuilder, file);
			}

			result = readFile(filePath, dBuilder);

		} catch (ParserConfigurationException e) {
			file.delete();
			createItemList();
			result = readFile(filePath, dBuilder);
		} catch (SAXException e) {
			file.delete();
			createItemList();
			result = readFile(filePath, dBuilder);
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

	public static void deleteAllItemNodes() throws IOException {
		String[] result = null;
		StringBuffer sbresult = new StringBuffer();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			FileInputStream iStream = new FileInputStream(new File(
					Constants.ItemList));
			Document document = dBuilder.parse(iStream);

			NodeList nList = document.getElementsByTagName("item");
			result = new String[nList.getLength()];

			for (int i = 0; i < nList.getLength(); i++) {
				sbresult = new StringBuffer();
				Node node = nList.item(i);
				node.getParentNode().removeChild(node);

				result[i] = sbresult.toString();
			}

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
				Text id = document.createTextNode(asset.getId());
				Text title = document.createTextNode(asset.getName());
				Text thumbnail = document.createTextNode(asset.getThumbnail());
				Text url = document.createTextNode(asset.getUrl());
				Text lastmodified = document.createTextNode(asset
						.getLastmodified());
				Text type = document.createTextNode(asset.getType());

				// new elements
				Element newElement = document.createElement("item");
				Element newIdElement = document.createElement("id");
				Element newTitleElement = document.createElement("title");
				Element newThumbElement = document.createElement("thumbnail");
				Element newUrlElement = document.createElement("url");
				Element newLastModifiedElement = document
						.createElement("lastmodified");
				Element newTypeElement = document.createElement("type");
				
				newIdElement.appendChild(id);
				newTitleElement.appendChild(title);
				newThumbElement.appendChild(thumbnail);
				newUrlElement.appendChild(url);
				newLastModifiedElement.appendChild(lastmodified);
				newTypeElement.appendChild(type);
				newElement.appendChild(newIdElement);
				newElement.appendChild(newTitleElement);
				newElement.appendChild(newThumbElement);
				newElement.appendChild(newUrlElement);
				newElement.appendChild(newLastModifiedElement);
				newElement.appendChild(newTypeElement);
				
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
		
		if (!Constants.System_Password.equals("")){
			nl = document.getElementsByTagName(Constants.XML_PASSWORD);
			if(nl.getLength() > 0){
				nl.item(0).getFirstChild().setNodeValue(Constants.System_Password);
			}else{
				createNode(document,Constants.XML_PASSWORD,Constants.System_Password);
			}
		}

	}
	
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
//			document.getDocumentElement().appendChild(newElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(filePath));
			transformer.transform(domSource, streamResult);
			iStream.close();
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
