package com.duole.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoScrollTextView extends TextView{

	public AutoScrollTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AutoScrollTextView(Context context ,AttributeSet attrs) {
		super(context , attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isFocused() {
		// TODO Auto-generated method stub
		return true;
	}
	
	

}
