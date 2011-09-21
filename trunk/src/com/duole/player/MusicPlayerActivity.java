package com.duole.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.RelativeLayout;

import com.duole.Duole;
import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.adapter.MusicItemAdapter;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

public class MusicPlayerActivity extends PlayerBaseActivity implements OnFocusChangeListener, OnItemSelectedListener, OnClickListener{

	MediaPlayer mp;
	RelativeLayout llMain;
	String url = "";
	int index;
	Gallery gallery;
	MusicPlayerActivity appref;
	
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
		
		mp = new MediaPlayer();
		
		index = Integer.parseInt(intent.getStringExtra("index"))  - 1;
		gallery.setSelection(index);
		
		setMusicData(index);
		
		btnPlay.setOnClickListener(this);
		
		registerReceiver();

	}
	
	private void registerReceiver(){
		
		IntentFilter intentFilter = new IntentFilter(
		"com.duole.restime.out");
		this.registerReceiver(timeOutReceiver, intentFilter);
		
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
					// TODO Auto-generated catch block
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
//				sendBroadcast(new Intent(Constants.Event_AppEnd));
			}
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		mp.stop();
		super.onDestroy();
	}

	public void onFocusChange(View view, boolean value) {
		
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		index = arg2;

		this.mHandler.post(new Runnable(){

			public void run() {
				mp.stop();
				mp.release();
				mp = new MediaPlayer();

				setMusicData(index);
				mp.start();
				btnPlay.setBackgroundResource(R.drawable.pause);
			}
			
		});
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	BroadcastReceiver timeOutReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			appref.finish();
		}
		
	};

	public void onClick(View v) {
		if(mp.isPlaying()){
			btnPlay.setBackgroundResource(R.drawable.play);
			mp.pause();
		}else{
			btnPlay.setBackgroundResource(R.drawable.pause);
			mp.start();
		}
	}
	
//
//	public void onCompletion(MediaPlayer mp) {
//
//		mp.stop();
//		mp.release();
//		mp = new MediaPlayer();
//		
//		if(index < Constants.MusicList.size()){
//			setMusicData(index + 1);
//			gallery.setSelection(index + 1);
//		}else{
//			setMusicData(0);
//			gallery.setSelection(0);
//		}
//		
//	}

}
