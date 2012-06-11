package com.duole.thread;

import java.util.ArrayList;
import java.util.Vector;

import com.duole.pojos.asset.Asset;

public class DownloadTaskQueue {
	
	/**
	 * 
	 */
	java.util.Vector<Asset> eles;
	int p = 0, q = 0;

	public DownloadTaskQueue(int size) {
		init(size);
	}

	private void init(int size) {
		eles = new Vector<Asset>(size);
		eles.setSize(size);
	}

	public DownloadTaskQueue() {
		init(1);
	}

	public boolean empty() {
		return p == q;
	}
	
	public int size(){
		return eles.size();
	}
	
	public boolean remove(Asset asset){
		return eles.remove(asset);
	}

	public boolean full() {
		return ((p + 1) % eles.size()) == q;
	}
	
	public void trim(){
		
		ArrayList<Asset> altmp = new ArrayList<Asset>();
		while(!empty()){
			Asset tmp = pop_front();
			if(tmp != null){
				altmp.add(tmp);
			}
		}
		
		DownloadTaskQueue tmp = new DownloadTaskQueue(altmp.size() == 0 ? 1 : altmp.size());
		
		for(Asset asset : altmp){
			tmp.push_back(asset);
		}
		eles = tmp.eles;
		p = tmp.p;
		q = tmp.q;
	}

	public Asset pop_front() {
		assert !empty();
		Asset ret = eles.get(q);
		eles.set(q, null);
		++q;
		q %= eles.size();
		return ret;
	}

	public void push_back(Asset v) {
		if (!full()) {
			eles.set(p, v);
			++p;
			p %= eles.size();
			return;
		}
		DownloadTaskQueue tmp = new DownloadTaskQueue(eles.size() * 2);
		while (!empty()) {
			tmp.push_back(pop_front());
		}
		eles = tmp.eles;
		p = tmp.p;
		q = tmp.q;
		push_back(v);
	}

}
