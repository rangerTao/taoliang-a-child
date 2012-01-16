package com.duole.receiver;

import com.duole.Duole;
import com.duole.utils.DuoleUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateInstallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		DuoleUtils.instalUpdateApk(Duole.appref);
	
	}

}
