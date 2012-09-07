package com.duole.widget;

import com.duole.R;
import com.duole.utils.Constants;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class BatteryProgressBar extends RelativeLayout {
	private int m_max = 60;
	private int m_process = 0;
	private ImageView mImageView = null;
	private LayoutParams params;
	private Handler mHandler;
	private Thread mThread = new Thread(new Runnable() {
		public void run() {
			reflashPorcess(m_process);
		}
	});

	public BatteryProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BatteryProgressBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		// mHandler = new Handler(getContext().getMainLooper());
		if (Constants.handler == null) {
			Constants.handler = new Handler(getContext().getMainLooper());
		}
		mHandler = Constants.handler;
		params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	}

	public void setMax(int max) {
		m_max = max;
	}

	public int getMax() {
		return m_max;
	}

	public void setProgress(int process) {
		if (process <= m_max) {
			m_process = process;
			mHandler.post(mThread);
		}
	}

	public void setProgressResource(int resid) {
		mImageView.setImageResource(resid);
	}

	public int getProgress() {
		return m_process;
	}

	private int getCountLength() {
		return (getHeight() - 16) * m_process / m_max;
	}

	private void reflashPorcess(int process) {
		if (mImageView != null)
			removeView(mImageView);
		mImageView = null;
		mImageView = new ImageView(getContext());
		mImageView.setAdjustViewBounds(true);
		mImageView.setScaleType(ScaleType.FIT_XY);
		if (m_process < m_max * 0.15) {
			mImageView.setImageResource(R.drawable.battery_down_progress);
		} else if (m_process < m_max * 0.3) {
			mImageView.setImageResource(R.drawable.battery_low_progress);
		} else {
			mImageView.setImageResource(R.drawable.battery_full_pb);
		}
		params.height = getCountLength();
		addView(mImageView, params);
	}
}