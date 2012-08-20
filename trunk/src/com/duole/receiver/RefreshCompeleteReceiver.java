package com.duole.receiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.duole.Duole;
import com.duole.R;
import com.duole.Duole.PageDiv;
import com.duole.pojos.adapter.AssetItemAdapter;
import com.duole.pojos.asset.Asset;
import com.duole.service.BackgroundRefreshService;
import com.duole.service.RefreshMainViewService;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;
import com.duole.utils.FileUtils;
import com.duole.utils.XmlUtils;
import com.duole.widget.ScrollLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;

public class RefreshCompeleteReceiver extends BroadcastReceiver {

	private static long refreshStartTime = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.Refresh_Complete)) {

			Log.v("TAG", "refresh complete : " + new SimpleDateFormat("yyyy MM dd HH mm ss").format(new Date(System.currentTimeMillis())));

			Log.d("TAG", "is new item exists : " + Constants.newItemExists);

			if (Constants.newItemExists) {

				Log.d("TAG", "is refresh enable " + Constants.viewrefreshenable);

				Duole.appref.mHandler.post(new Runnable() {

					public void run() {

						if (Constants.viewrefreshenable) {

							if ((System.currentTimeMillis() - refreshStartTime) > 15 * 1000) {
								refreshView();
							}

						}
					}

				});
			}

			Constants.newItemExists = false;

		}
	}

	/**
	 * if there is anything changed,refresh the view.
	 */
	public synchronized static boolean refreshView() {

		refreshStartTime = System.currentTimeMillis();

		Intent refreshService = new Intent(Duole.appref, RefreshMainViewService.class);
		Duole.appref.startService(refreshService);

		return true;

	}

}
