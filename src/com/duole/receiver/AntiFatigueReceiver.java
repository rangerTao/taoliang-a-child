package com.duole.receiver;

import com.duole.Duole;
import com.duole.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AntiFatigueReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Constants.Event_AppStart)){
			Duole.appref.gameCountDown.resume();
		}
		if(intent.getAction().equals(Constants.Event_AppEnd)){
			Duole.appref.gameCountDown.pause();
		}
		
	}

}
