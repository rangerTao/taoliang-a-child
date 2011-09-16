package com.duole.player;

import java.io.File;

import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.utils.Constants;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;

public class FlashPlayerActivity extends PlayerBaseActivity{

	WebView wvPlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.SetFullScreen();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.flashplayer);
		
		wvPlay = (WebView) findViewById(R.id.wvFlash);
		
		Intent intent = getIntent();
		
		String filename = intent.getStringExtra("filename");
		String url = "";
		if(filename.startsWith("http")){
			url = filename;
			String id = filename.substring(filename.indexOf("sid/") + 4,filename.indexOf("/v.swf"));
			url = "http://static.youku.com/v1.0.0134/v/swf/qplayer.swf?VideoIDS=" + id + "&embedid=&isAutoPlay=true&MMControl=false&MMout=false&embedid";
		}else{
			url = "file://" + Constants.CacheDir + "/game/" + filename;
		}
		
		File file = new File(Constants.CacheDir + "/game/" + filename);
		
		if(file.exists() || url.startsWith("http")){
			wvPlay.getSettings().setPluginsEnabled(true);
			wvPlay.getSettings().setPluginState(PluginState.ON);

			wvPlay.loadUrl(url);
		}else{
			wvPlay.setBackgroundResource(R.drawable.bg86);
		}
		
		wvPlay.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.indexOf("duoleyuan.com") != -1){
            		view.loadUrl(url);
            		return true;
            	}else{
            		return true;
            	}
			}
			
		});
		
		wvPlay.setLongClickable(false);
		wvPlay.setOnLongClickListener(new View.OnLongClickListener(){

			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				return false;
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
		wvPlay.destroy();
		super.onDestroy();
	}
	
}
