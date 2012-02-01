package com.duole.thread;

import java.util.Vector;

import com.duole.utils.DownloadFileUtils;

public class ThreadQueue {
	
	/**
	 * 
	 */
	java.util.Vector<DownloadFileUtils> eles;
	int p = 0, q = 0;

	public ThreadQueue(int size) {
		init(size);
	}

	private void init(int size) {
		eles = new Vector<DownloadFileUtils>(size);
		eles.setSize(size);
	}

	public ThreadQueue() {
		init(5);
	}

	public boolean empty() {
		return p == q;
	}

	public boolean full() {
		return ((p + 1) % eles.size()) == q;
	}

	public DownloadFileUtils pop_front() {
		assert !empty();
		DownloadFileUtils ret = eles.get(q);
		++q;
		q %= eles.size();
		return ret;
	}

	public void push_back(DownloadFileUtils v) {
		if (!full()) {
			eles.set(p, v);
			++p;
			p %= eles.size();
			return;
		}
		ThreadQueue tmp = new ThreadQueue(eles.size() * 2);
		while (!empty()) {
			tmp.push_back(pop_front());
		}
		eles = tmp.eles;
		p = tmp.p;
		q = tmp.q;
		push_back(v);
	}

}
