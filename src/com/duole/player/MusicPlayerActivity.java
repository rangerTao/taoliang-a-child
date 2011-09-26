package com.duole.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.layout.MusicGallery;
import com.duole.pojos.adapter.MusicItemAdapter;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

public class MusicPlayerActivity extends PlayerBaseActivity implements OnFocusChangeListener, OnItemSelectedListener, OnClickListener, OnItemClickListener, OnCompletionListener{

	MediaPlayer mp;
	RelativeLayout llMain;
	String url = "";
	int index;
	Gallery gallery;
	MusicPlayerActivity appref;
	private AnimationSet manimationSet; 
	
	public boolean clicked = false;
	
	Button btnPlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.SetFullScreen();
		setContentView(R.layout.musicplayer);
		appref = this;
		btnPlay = (Button) findViewById(R.id.btnPlay);
		setBackground();
		
		Intent intent = getIntent();

		gallery = (Gallery)findViewById(R.id.musicGallery);
		
		gallery.setOnFocusChangeListener(this);

		gallery.setOnItemSelectedListener(this);
		
		gallery.setAdapter(new MusicItemAdapter(Constants.MusicList));
		
		gallery.setCallbackDuringFling(false);

		gallery.setOnItemClickListener(this);
		mp = new MediaPlayer();
		
		index = Integer.parseInt(intent.getStringExtra("index"))  - 1;
		gallery.setSelection(index);
		
		setMusicData(index);
		
		btnPlay.setOnClickListener(this);
		mp.setOnCompletionListener(this);
		
		registerReceiver();

	}
	
	private void registerReceiver(){
		
		IntentFilter intentFilter = new IntentFilter(
		"com.duole.restime.out");
		registerReceiver(timeOutReceiver, intentFilter);
		
	}
	
	public void setBackground() {

		llMain = (RelativeLayout) findViewById(R.id.llMusicPlayer);

		if (!Constants.bgRestUrl.equals("")) {
			File bg = new File(Constants.CacheDir
					+ Constants.bgRestUrl.substring(Constants.bgRestUrl
							.lastIndexOf("/")));
			if (bg.exists()) {
				llMain.setBackgroundDrawable(Drawable.createFromPath(bg
						.getAbsolutePath()));
			} else {
				try {
					DuoleUtils.downloadSingleFile(new URL(Constants.Duole
							+ Constants.bgRestUrl), bg);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
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

		RelativeLayout llMusicPlayer = (RelativeLayout) findViewById(R.id.llMusicPlayer);

		if (!Constants.bgRestUrl.equals("")) {
			File file = new File(Constants.CacheDir
					+ Constants.bgRestUrl.substring(Constants.bgRestUrl
							.lastIndexOf("/")));
			if (file.exists()) {
				llMusicPlayer.setBackgroundDrawable(Drawable
						.createFromPath(file.getAbsolutePath()));
			}
		}

		try {
			
			mp.setDataSource(this, Uri.fromFile(new File(url)));
			mp.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			
			musicControl();
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		mp.stop();
		unregisterReceiver(timeOutReceiver);

		super.onDestroy();
	}

	public void onFocusChange(View view, boolean value) {
		
	}

	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		index = position;
		if (position != Constants.MusicList.size()) {
			AnimationSet animationSet = new AnimationSet(true);
			if (manimationSet != null && manimationSet != animationSet) {
				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.85f, 1.0f,
						0.85f, Animation.RELATIVE_TO_SELF, 1f, 
						Animation.RELATIVE_TO_SELF, 1f);
				manimationSet.addAnimation(scaleAnimation);
				manimationSet.setFillAfter(true);
				view.startAnimation(manimationSet);
			}
				ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.2f, 1, 1.2f,
						Animation.RELATIVE_TO_SELF, 1f,
						Animation.RELATIVE_TO_SELF, 1f);
				scaleAnimation.setDuration(1000);
				animationSet.addAnimation(scaleAnimation);
				animationSet.setFillAfter(true);
				view.startAnimation(animationSet);
				manimationSet = animationSet;

		} else {
			if (null != manimationSet)
				manimationSet.setFillAfter(false);
		}	
		
		this.mHandler.post(new Runnable(){

			public void run() {
				mp.stop();
				mp.release();
				mp = new MediaPlayer();

				setMusicData(index);

				if(clicked){
					playMusic();
				}
			}
			
		});
	}
	
	

	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	BroadcastReceiver timeOutReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			appref.finish();
		}
		
	};

	public void onClick(View v) {
		musicControl();
	}

	private void musicControl(){
		
		if(mp.isPlaying()){
			pauseMusic();
		}else{
			playMusic();
		}
		
	}
	private void playMusic(){
		btnPlay.setBackgroundResource(R.drawable.pause);
		mp.start();
	}
	
	private void pauseMusic(){
		btnPlay.setBackgroundResource(R.drawable.play);
		mp.pause();
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		clicked = true;
	}

	public void onCompletion(MediaPlayer arg0) {
		btnPlay.setBackgroundResource(R.drawable.play);
		mp.seekTo(0);
	}
	
	
	
}
