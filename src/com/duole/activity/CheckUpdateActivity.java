package com.duole.activity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.R;
import com.duole.utils.Constants;
import com.duole.utils.DuoleNetUtils;
import com.duole.utils.DuoleUtils;

public class CheckUpdateActivity extends BaseActivity {

	CheckUpdateActivity appref;
	ProgressBar pbUpdate;
	RelativeLayout rlContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		appref = this;

		this.setContentView(R.layout.checkupdate);

		this.setTitle(R.string.check_update);

		pbUpdate = (ProgressBar) findViewById(R.id.pbLoad);
		rlContent = (RelativeLayout) findViewById(R.id.rlContent);

		UpdateTask ut = new UpdateTask();
		ut.execute();
	}

	class UpdateTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			String url = "http://www.duoleyuan.com/e/member/child/ancJver.php";

			String result = DuoleNetUtils.connect(url);
			String version = DuoleUtils.getVersion(appref);

			try {
				final JSONObject json = new JSONObject(result);
				String newVersion = json.getString("ver");
				if (!version.equals(newVersion)) {
					appref.mHandler.post(new Runnable() {

						public void run() {
							pbUpdate.setVisibility(View.GONE);
							rlContent.setVisibility(View.VISIBLE);
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
							pbUpdate.setVisibility(View.GONE);
							rlContent.setVisibility(View.VISIBLE);
							noUpdate();
						}

					});

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	public void update(final JSONObject json) throws JSONException {
		TextView tvContent = (TextView) findViewById(R.id.tvUpdate);
		StringBuffer sb = new StringBuffer();
		sb.append(appref.getString(R.string.version_new) + "\n");
		sb.append(appref.getString(R.string.version) + " : "
				+ json.getString("ver") + "\n");
		sb.append(appref.getString(R.string.change_log) + "\n");
		sb.append("        " + json.getString("detail") + "\n");
		sb.append(appref.getString(R.string.date));
		tvContent.setText(sb.toString());

		Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				
				try {
					String path = json.getString("path");
					if(!path.equals("null") || !path.equals("")){
						URL url = new URL(Constants.Duole + path);
						File file = new File(Constants.CacheDir + "client.apk");
						if(DuoleUtils.downloadSingleFile(url, file)){
							Process p = Runtime.getRuntime().exec("pm install -r " + file.getAbsolutePath());
//							Process p = Runtime.getRuntime().exec("pm install -r /sdcard/DuoleCache/Duole.apk");
							p.waitFor();
							int result = p.exitValue();
						}else{
							Toast.makeText(appref, R.string.download_error, 2000);
						}
						
						
					
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
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				appref.finish();
			}
		});

	}

	public void noUpdate() {
		TextView tvContent = (TextView) findViewById(R.id.tvUpdate);
		tvContent.setText(appref.getString(R.string.client_uptodate));
		Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnConfirm.setVisibility(View.GONE);
		LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.CENTER_HORIZONTAL);
		btnCancel.setLayoutParams(param);
		btnCancel.setText(appref.getString(R.string.btnClose));
		btnCancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				appref.finish();
			}
		});
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
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
