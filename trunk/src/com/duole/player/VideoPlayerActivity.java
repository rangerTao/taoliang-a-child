package com.duole.player;

import android.content.Intent;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;

public class VideoPlayerActivity extends PlayerBaseActivity {

	int index;
	Intent intent;
	VideoView vvPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.videoplayer);

		vvPlayer = (VideoView) findViewById(R.id.vvVideoPlayer);

		intent = getIntent();
		index = Integer.parseInt(intent.getStringExtra("index"))  - 1;
		
		initViewPlayer();
	}

	private void initViewPlayer(){

		Asset asset = Constants.AssetList.get(index);
		String path = asset.getUrl();
		
		vvPlayer.setVideoPath(Constants.CacheDir + Constants.RES_VIDEO + path.substring(path.lastIndexOf("/")));

		MediaController mc = new MediaController(this);

		vvPlayer.setMediaController(mc);

		vvPlayer.start();
	}
}
