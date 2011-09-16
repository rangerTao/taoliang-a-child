package com.duole.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.asset.Asset;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

public class SingleMusicPlayerActivity extends PlayerBaseActivity{

	
	MediaPlayer mp;
	LinearLayout llMain;
	String url = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.singlemusicplayer);
		
		Intent intent = getIntent();
		
		int index = Integer.parseInt(intent.getStringExtra("index"));
		
		setBackground(index);
		
		mp = new MediaPlayer();
		
		setMusicData(index);
	}
	
	
	public void setBackground(int index) {

		llMain = (LinearLayout) findViewById(R.id.llMusicPlayer);
		ImageView ivMusicThumb = (ImageView)findViewById(R.id.ivMusicThumb);
		TextView tvMusicTitle = (TextView)findViewById(R.id.tvMusicTitle);
		Asset asset = Constants.MusicList.get(index);
		
		String ivUrl = asset.getThumbnail();
		String ivThumb = Constants.CacheDir + Constants.RES_THUMB + ivUrl.substring(ivUrl.lastIndexOf("/"));
		ivMusicThumb.setImageDrawable(Drawable.createFromPath(ivThumb));
		tvMusicTitle.setText(asset.getFilename());
		
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

		LinearLayout llMusicPlayer = (LinearLayout) findViewById(R.id.llMusicPlayer);

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
			mp.start();
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
				sendBroadcast(new Intent(Constants.Event_AppEnd));
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

}
