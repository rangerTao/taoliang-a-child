package com.duole.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

public class SingleMusicPlayerActivity extends PlayerBaseActivity{

	
	MediaPlayer mp;
	View llMain;
	View rl;
	String url = "";
	TextView tvMusicTitle;
	
	int screen_off_timeout = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.singlemusicplayer);
		
		Intent intent = getIntent();
		
		int index = Integer.parseInt(intent.getStringExtra("index"));
		
		setBackground(index);
		try {
			screen_off_timeout = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 1000 * 60 * 4);
		
		mp = new MediaPlayer();
		
		setMusicData(index);
	}
	
	
	public void setBackground(int index) {

		llMain = findViewById(R.id.llMusicPlayer);
		rl = findViewById(R.id.relativeLayout1);
		ImageButton ivMusicThumb = (ImageButton)findViewById(R.id.ivMusicThumb);
		
		tvMusicTitle = (TextView)findViewById(R.id.tvMusicTitle);
		tvMusicTitle.setVisibility(View.INVISIBLE);
		Asset asset = Constants.MusicList.get(index);
		
		String mpbg = asset.getBg();
		
		if( mpbg != null && !mpbg.trim().equals("")){

			File bg = new File(Constants.CacheDir
					+ Constants.RES_THUMB + mpbg.substring(mpbg
							.lastIndexOf("/")));
			
			if (bg.exists()) {
				try{
					llMain.setBackgroundDrawable(Drawable.createFromPath(bg
							.getAbsolutePath()));
				}catch(Exception e){
					e.printStackTrace();
				}
				
			} else {
				try {
					DuoleUtils.downloadPicSingle(new URL(Constants.Duole
							+ mpbg), bg);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		
		}
		
		
		String ivUrl = asset.getThumbnail();
		String ivThumb = Constants.CacheDir + Constants.RES_THUMB + ivUrl.substring(ivUrl.lastIndexOf("/"));
		ivMusicThumb.setImageDrawable(Drawable.createFromPath(ivThumb));
		tvMusicTitle.setText(asset.getName());
		
		if (!Constants.bgRestUrl.equals("")) {}
		
		ivMusicThumb.setOnTouchListener(new OnTouchListener() {
			
			int lastX, lastY;

			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					// int dy = 0;//y方向不需要

					int left = v.getLeft() + dx;
					int top = v.getTop() + dy;
					int right = v.getRight() + dx;
					int bottom = v.getBottom() + dy;
					
					if (left < 0) {
						left = 0;
						right = left + v.getWidth();
					}

					if (right > rl.getMeasuredWidth()) {
						right = rl.getMeasuredWidth();
						left = right - v.getWidth();
					}

					if (top < 0) {
						top = 0;
						bottom = top + v.getHeight();
					}

					if (bottom > rl.getMeasuredHeight()) {
						bottom = rl.getMeasuredHeight();
						top = bottom - v.getHeight();
					}
					v.layout(left, top, right, bottom);
					
					int tvLeft = (v.getWidth() - tvMusicTitle.getWidth()) / 2 + left;
					int tvTop = top + v.getHeight();
					tvMusicTitle.layout(tvLeft, tvTop, tvLeft + tvMusicTitle.getWidth(), tvTop + tvMusicTitle.getHeight());

					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					v.postInvalidate();
					tvMusicTitle.postInvalidate();
					break;
				case MotionEvent.ACTION_UP:
					break;

				default:
					break;
				}
				return false;
			}
		});
		
	}
	
	public void setMusicData(int index) {
		
		String filename = Constants.MusicList.get(index).getUrl();

		if (filename.startsWith("http:")) {
		} else {
			filename = filename.substring(filename.lastIndexOf("/"));
		}

		if (filename.startsWith("http")) {
			url = filename;
		} else {
			url = Constants.CacheDir + Constants.RES_AUDIO + filename;
		}

		try {
			
			mp.setDataSource(this, Uri.fromFile(new File(url)));
			mp.prepare();
			mp.setOnPreparedListener(new OnPreparedListener() {
				
				public void onPrepared(MediaPlayer mp) {
					mp.start();
				}
			});
//			mp.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			
			if (!Constants.SLEEP_TIME && !Constants.ENTIME_OUT) {
				finish();
			}
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		mp.stop();
		android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, screen_off_timeout);
		uploadGamePeriod();
		super.onDestroy();
	}


	@Override
	protected void onPause() {
		mp.pause();
		finish();
		super.onPause();
	}


	@Override
	protected void onResume() {
		mp.start();
		super.onResume();
	}
	
	
}
