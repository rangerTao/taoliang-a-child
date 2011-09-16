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

import android.util.Log;

import com.duole.pojos.asset.Asset;

public class parseXML {
	public static String filePath = Constants.CacheDir + "/itemlist.xml";

	// public static FileInputStream iStream = null;

	public static void initFile(InputStream is, DocumentBuilder dBuilder,
			File file) throws SAXException, IOException, TransformerException {
		Document document;
		document = dBuilder.parse(is);
		DOMSource domSource = new DOMSource(document);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer;
		transformer = transformerFactory.newTransformer();
		StreamResult streamResult = new StreamResult(file);
		transformer.transform(domSource, streamResult);

	}

	public static ArrayList<Asset> readFile(String filePath,
			DocumentBuilder dBuilder) throws SAXException, IOException {

		FileInputStream iStream = new FileInputStream(new File(
				parseXML.filePath));

		Document document = dBuilder.parse(iStream);

		NodeList nList = document.getElementsByTagName("item");
		ArrayList<Asset> result = new ArrayList<Asset>();

		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			NodeList nodeList = node.getChildNodes();
			Asset asset = new Asset();
			for (int j = 0; j < nodeList.getLength(); j++) {

				Node node2 = nodeList.item(j);
				if (node2.getNodeName().equalsIgnoreCase("filename")) {
					asset.setName(node2.getFirstChild().getNodeValue());
				} else if (node2.getNodeName().equalsIgnoreCase("type")) {
					asset.setType(node2.getFirstChild().getNodeValue());
				} else if (node2.getNodeName().equalsIgnoreCase("thumbnail")) {
					asset.setThumbnail(node2.getFirstChild().getNodeValue());
				} else if (node2.getNodeName().equalsIgnoreCase("size")) {
					if(node2.getFirstChild() != null){
						asset.setSize(node2.getFirstChild().getNodeValue());
					}					
				} else if (node2.getNodeName().equalsIgnoreCase("url")) {
					if(node2.getFirstChild() != null){
						asset.setUrl(node2.getFirstChild().getNodeValue());
					}
				}
			}

			result.add(asset);
		}
		return result;
	}

	public static ArrayList<Asset> readXML(InputStream is, String filePath)
			throws IOException, TransformerException, SAXException {

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
			initFile(is, dBuilder, file);
			result = readFile(filePath, dBuilder);
		} catch (SAXException e) {
			file.delete();
			initFile(is, dBuilder, file);
			result = readFile(filePath, dBuilder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(is != null){
			is.close();
		}
		return result;
	}
}
