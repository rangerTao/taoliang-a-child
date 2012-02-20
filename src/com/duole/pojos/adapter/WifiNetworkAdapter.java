package com.duole.pojos.adapter;

import java.util.ArrayList;
import java.util.List;

import com.duole.R;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WifiNetworkAdapter extends BaseAdapter{

	ArrayList<ScanResult> inputList;
	LayoutInflater inflater;
	WifiConnection wc;
	Context context;
	
	public WifiNetworkAdapter(List<ScanResult> results,Context context){
		inputList = (ArrayList<ScanResult>) results;
		this.context = context;
	}
	
	public int getCount() {
		return inputList.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int arg0, View convertview, ViewGroup arg2) {
		
		ScanResult sr = inputList.get(arg0);
		
		if(convertview == null){
			inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.wificonnection, null);
			wc = new WifiConnection();
			wc.tvSSID = (TextView) view.findViewById(R.id.tvSSID);
			
			wc.tvSSID.setText(sr.SSID);
			
			view.setTag(wc);
			convertview = view;
		}else{
			wc = (WifiConnection) convertview.getTag(); 
		}
		
		wc.tvSSID.setText(sr.SSID);
		
		return convertview;
	}
	
	class WifiConnection{
		TextView tvSSID;
		TextView tvStrength;
	}

}
