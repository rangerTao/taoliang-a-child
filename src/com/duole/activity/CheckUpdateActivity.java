package com.duole.activity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.Duole;
import com.duole.R;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;

public class CheckUpdateActivity extends BaseActivity {

	CheckUpdateActivity appref;
	ProgressBar pbUpdate;
	TextView tvLoadingProgress;
	Button btnClose;

	private final static int LOADING = 999;
	private final static int NEW_VERSION_DOWNLOADING = 998;
	private final static int NEW_VERSION_INSTALL = 997;
	private final static int NEW_VERSION_DOWNLOAD_ERROR = 996;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOADING:
				tvLoadingProgress.setText(R.string.check_update_getting_version);
				break;
			case NEW_VERSION_DOWNLOADING:
				tvLoadingProgress.setText(R.string.check_update_downloading);
				break;
			case NEW_VERSION_INSTALL:
				tvLoadingProgress.setText(R.string.check_update_install);
			case NEW_VERSION_DOWNLOAD_ERROR:
				tvLoadingProgress.setText(R.string.download_error);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		appref = this;

		this.setContentView(R.layout.checkupdate);

		this.setTitle(R.string.check_update);

		tvLoadingProgress = (TextView) findViewById(R.id.tvLoadingProgress);
		pbUpdate = (ProgressBar) findViewById(R.id.pbLoad);
		btnClose = (Button) findViewById(R.id.btnClose);

		btnClose.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				finish();
			}
		});

		UpdateTask ut = new UpdateTask();
		ut.execute();
	}

	@SuppressWarnings("rawtypes")
	class UpdateTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {

			Message msg = new Message();
			msg.what = LOADING;
			mHandler.sendMessage(msg);

			String url = Constants.ClientUpdate;

			String version = DuoleUtils.getVersion(Duole.appref);
			String mCode = DuoleUtils.getAndroidId();

			url = url + "?cver=" + version + "&cmcode=" + mCode;

			String result = DuoleNetUtils.connect(url);

			try {
				final JSONObject json = new JSONObject(result);
				final String newVersion = json.getString("ver");
				if (!version.equals(newVersion)) {

					appref.mHandler.post(new Runnable() {

						public void run() {
							tvLoadingProgress.setText("有新版本可以更新，版本号：" + newVersion);
							try {
								update(json);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					});

				} else {
					appref.mHandler.post(new Runnable() {

						public void run() {
							tvLoadingProgress.setText("没有发现新版本。");
							pbUpdate.setVisibility(View.GONE);
						}

					});

				}
			} catch (JSONException e) {
				appref.mHandler.post(new Runnable() {

					public void run() {
						tvLoadingProgress.setText("没有发现新版本。");
						pbUpdate.setVisibility(View.GONE);
					}

				});
				e.printStackTrace();
			}
			return null;
		}

	}

	@SuppressLint({ "ShowToast", "ShowToast" })
	public void update(final JSONObject json) throws JSONException {
		try {
			String path = json.getString("path");
			if (!path.equals("null") || !path.equals("")) {
				final URL url = new URL(Constants.Duole + path);
				final File file = new File(Constants.CacheDir + "client.apk");

				Message msg = new Message();
				msg.what = NEW_VERSION_DOWNLOADING;
				mHandler.sendMessage(msg);

				new Thread() {

					@Override
					public void run() {
						try {
							if (DuoleUtils.downloadSingleFile(url, file, "false")) {

								Message msgDown = new Message();
								msgDown.what = NEW_VERSION_INSTALL;
								mHandler.sendMessage(msgDown);

								Process p = Runtime.getRuntime().exec("pm install -r " + file.getAbsolutePath());

								p.waitFor();
								p.exitValue();
							} else {
								Message msgError = new Message();
								msgError.what = NEW_VERSION_DOWNLOAD_ERROR;
								mHandler.sendMessage(msgError);
							}
						} catch (Exception e) {
							Message msgError = new Message();
							msgError.what = NEW_VERSION_DOWNLOAD_ERROR;
							mHandler.sendMessage(msgError);
						}

						super.run();
					}

				}.start();

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			finish();
			sendBroadcast(new Intent(Constants.Event_AppEnd));
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
