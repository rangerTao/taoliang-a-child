package com.duole.activity;

import com.duole.utils.Constants;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.WindowManager;

public class PlayerBaseActivity extends BaseActivity{

	public void initSystemParams(){
		
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
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
	
}
