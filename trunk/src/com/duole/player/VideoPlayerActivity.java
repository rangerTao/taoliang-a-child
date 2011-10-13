package com.duole.player;

import java.net.URI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.utils.Constants;
import com.duole.widget.DuoleVideoView;

public class VideoPlayerActivity extends PlayerBaseActivity {

	String filename;
	Intent intent;
	DuoleVideoView vvPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.videoplayer);

		vvPlayer = (DuoleVideoView) findViewById(R.id.vvVideoPlayer);

		intent = getIntent();
		filename = intent.getStringExtra("filename");
		
		initViewPlayer();
	}

	private void initViewPlayer(){
		
		if(filename.startsWith("http")){
			Uri uri = Uri.parse(filename);
			vvPlayer.setVideoURI(uri);
		}else{
			vvPlayer.setVideoPath(Constants.CacheDir + Constants.RES_VIDEO + filename);
		}
		MediaController mc = new MediaController(this);

		vvPlayer.setMediaController(mc);

		vvPlayer.start();
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
	
	
	
	
}
