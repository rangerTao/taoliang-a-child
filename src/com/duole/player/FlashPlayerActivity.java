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
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

public class FlashPlayerActivity extends PlayerBaseActivity {

	WebView wvPlay;
	FrameLayout llContainer;

	FrameLayout llFullScreen;

	int mOriention;
	View mFullScreenView;
	CustomViewCallback mCustomViewCallback;

	int mOriginalOrientation;

	PopupWindow mFullPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.SetFullScreen();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.flashplayer);

		llContainer = (FrameLayout) findViewById(R.id.llWebViewContainer);
		llFullScreen = (FrameLayout) findViewById(R.id.llWebViewFullScreen);

		wvPlay = (WebView) findViewById(R.id.wvFlash);

		Intent intent = getIntent();

		String filename = intent.getStringExtra("filename");
		String url = "";
		if (filename.startsWith("http")) {
			url = filename;
			if (url.contains("youku.com")) {
				String id = filename.substring(filename.indexOf("sid/") + 4, filename.indexOf("/v.swf"));
				url = "http://static.youku.com/v1.0.0134/v/swf/qplayer.swf?VideoIDS=" + id
						+ "&embedid=&isAutoPlay=true&MMControl=false&MMout=false&embedid";
			}
		} else {
			url = "file://" + Constants.CacheDir + "/game/" + filename;
		}

		File file = new File(Constants.CacheDir + "/game/" + filename);

		Log.d("TAG", url);
		if (file.exists() || url.startsWith("http")) {

			wvPlay.getSettings().setPluginState(PluginState.ON);

			wvPlay.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

			wvPlay.loadUrl(url);
		} else {
			wvPlay.setBackgroundResource(R.drawable.bg86);
		}

		wvPlay.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				Log.d("TAG", "url");
				if (url.indexOf("duoleyuan.com") != -1) {
					view.loadUrl(url);
					return true;
				} else {
					return true;
				}
			}

		});

		wvPlay.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onHideCustomView() {
				llFullScreen.setVisibility(View.INVISIBLE);

				if (mFullScreenView == null) {
					return;
				}

				llContainer.setVisibility(View.VISIBLE);
				llContainer.bringToFront();

				llFullScreen.removeView(mFullScreenView);

				try {
					mCustomViewCallback.onCustomViewHidden();

					mFullScreenView = null;
					mCustomViewCallback = null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				setRequestedOrientation(mOriginalOrientation);
			}

			public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {

				if (mFullScreenView != null) {
					callback.onCustomViewHidden();
					return;
				}

				if (android.os.Build.VERSION.SDK_INT >= 14) {
					mFullScreenView = view;
					mCustomViewCallback = callback;

					mOriginalOrientation = getRequestedOrientation();

					llFullScreen.addView(view);

					llFullScreen.setVisibility(View.VISIBLE);

					llContainer.setVisibility(View.INVISIBLE);

					mFullScreenView = view;

					llFullScreen.bringToFront();

					setRequestedOrientation(mOriginalOrientation);

				}

			}

		});
	}

	@Override
	protected void onPause() {
		finish();
		if (mFullScreenView != null) {
			mFullScreenView = null;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		wvPlay.destroy();

		uploadGamePeriod();
		super.onDestroy();
	}

}
