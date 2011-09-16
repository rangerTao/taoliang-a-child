package com.duole.receiver;

import com.duole.Duole;
import com.duole.service.AntiFatigueService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(ACTION)) {
			// ¿ª»úÏÔÊ¾
			Intent LoadurlIntent = new Intent(context, Duole.class);
			LoadurlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(LoadurlIntent);
			
		}
	}

}
