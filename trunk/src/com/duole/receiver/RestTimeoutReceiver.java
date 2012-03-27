package com.duole.receiver;

import com.duole.Duole;
import com.duole.utils.Constants;
import com.duole.utils.DuoleUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestTimeoutReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {

		Log.d("TAG", "rest time out receiver");
		if (!Constants.SLEEP_TIME) {
			Constants.ENTIME_OUT = false;
		}
	}

}
