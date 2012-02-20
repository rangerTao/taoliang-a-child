package com.duole.player;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.MediaController;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.utils.Constants;
import com.duole.widget.DuoleVideoView;

public class VideoPlayerActivity extends PlayerBaseActivity {

	String filename;
	Intent intent;
	DuoleVideoView vvPlayer;
	
	WakeLock mWakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.videoplayer);
		
		acquireWakeLock();

		vvPlayer = (DuoleVideoView) findViewById(R.id.vvVideoPlayer);

		intent = getIntent();
		filename = intent.getStringExtra("filename");
		
		initViewPlayer();
	}

	private void initViewPlayer(){
		
		vvPlayer.videoAutoPlay();
		
		if(filename.startsWith("http")){
			Uri uri = Uri.parse(filename);
			vvPlayer.setVideoURI(uri);
		}else{
			vvPlayer.setVideoPath(Constants.CacheDir + Constants.RES_VIDEO + filename);
		}
		MediaController mc = new MediaController(this);

		vvPlayer.setMediaController(mc);

		vvPlayer.start();
		
		vvPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			public void onCompletion(MediaPlayer mp) {
				mWakeLock.release();
			}
		});
	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		vvPlayer.destroyDrawingCache();
		uploadGamePeriod();
		super.onDestroy();
	}
	
	private void acquireWakeLock() {

		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "TAG");

			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}
	
	
}
