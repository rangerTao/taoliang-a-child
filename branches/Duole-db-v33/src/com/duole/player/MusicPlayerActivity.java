package com.duole.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.duole.Duole;
import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.DuoleCountDownTimer;
import com.duole.pojos.adapter.MusicItemAdapter;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.widget.MusicGallery;

public class MusicPlayerActivity extends PlayerBaseActivity implements OnFocusChangeListener, OnItemSelectedListener, OnClickListener, OnItemClickListener, OnCompletionListener{

	MediaPlayer mp;
	MediaPlayer mp2;
	RelativeLayout llMain;
	String url = "";
	int index;
	String type = "";
	Gallery gallery;
	public MusicPlayerActivity appref;
	private AnimationSet manimationSet; 
	
	int homeCount = 0;
	
	ProgressBar pbCountDown;
	
	public boolean clicked = false;
	public boolean isTopOfStack = false;
	
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
		type = intent.getStringExtra("type");
		
		gallery.setSelection(index);
		
		if(Constants.MusicList.size() > 0){
			setMusicData(index);
		}
				
		btnPlay.setOnClickListener(this);
		mp.setOnCompletionListener(this);
		
		registerReceiver();
		
		initProgressBar();

	}
	
	private void playTipSound() {

		try {
			if(Constants.SCREEN_ON){
				File file = new File(Constants.CacheDir + Constants.TIPSTARTNAME);
				if(file.exists()){

					mp2 = new MediaPlayer();
					
					mp2.setDataSource(this,
							Uri.fromFile(file));

					mp2.prepare();
					mp2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

						public void onPrepared(MediaPlayer mp) {
							mp2.start();
						}
					});
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}
	
	/**
	 * Init the progress of progress bar.
	 */
	private void initProgressBar(){
		
		Duole.appref.gameCountDown.pause();
		
		if(type != null && type.equals("rest")){
			pbCountDown = (ProgressBar) findViewById(R.id.pbRestTime);
			pbCountDown.setVisibility(View.VISIBLE);
			pbCountDown.setMax(Duole.appref.restCountDown.getTotalTime());
			pbCountDown.setBackgroundColor(Color.RED);
			
			Duole.appref.restCountDown.setPb(pbCountDown);
			
			Duole.appref.restCountDown.start();
			
			playTipSound();
		}
		
	}

	private void registerReceiver(){
		
		IntentFilter intentFilter = new IntentFilter("com.duole.restime.out");
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
			if(homeCount < 3 && mp2 != null){
				mp2.seekTo(0);
				mp2.start();
				homeCount ++;
			}
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
			if(!Constants.SLEEP_TIME){
				appref.finish();
				Constants.ENTIME_OUT = false;
			}
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

	@Override
	protected void onPause() {
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		if(pm.isScreenOn()){
			Constants.musicPlayerIsRunning = false;
		}

		super.onPause();
	}
	
	@Override
	protected void onResume() {
		
		KeyguardManager km = (KeyguardManager) Duole.appref.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock kl = km.newKeyguardLock("com.duole");
		kl.reenableKeyguard();
		super.onResume();
	}

	public void topOfStack(){
		isTopOfStack = true;
	}
	
	public void notTopOfStack(){
		isTopOfStack = false;
	}
	
	public boolean IsTopOfStack(){
		return isTopOfStack;
	}
	
}
