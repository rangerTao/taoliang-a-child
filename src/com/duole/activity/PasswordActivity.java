package com.duole.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.duole.R;
import com.duole.utils.Constants;
import com.duole.utils.XmlUtils;

public class PasswordActivity extends BaseActivity implements OnEditorActionListener {

	PasswordActivity appref;
	Button btnNegative;
	Button btnPositive;
	TextView tvTipOldPass;
	TextView tvNewPass;
	TextView tvNewPassConfirm;
	EditText etOldPass;
	EditText etNewPass;
	EditText etNewPassConfirm;
	String passwd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		appref = this;
		this.setContentView(R.layout.passwordinput);

		btnNegative = (Button) findViewById(R.id.btnCancel);
		btnPositive = (Button) findViewById(R.id.btnConfirm);
		Intent intent = this.getIntent();
		String type = intent.getStringExtra("type");

		passwd = XmlUtils.readNodeValue(Constants.SystemConfigFile, Constants.XML_PASSWORD);
		if(passwd.equals("")){
			passwd = Constants.defaultPasswd;
		}
		
		if (type.equals("0")) {

			doCheckPassword();

		} else {

			doChangePassword();

		}

		setClickListener();
		
		setOnEditorListener();

	}
	
	private void setOnEditorListener(){
		if(etOldPass!=null){
			etOldPass.setOnEditorActionListener(this);
		}
		
		if(etNewPass!=null){
			etNewPass.setOnEditorActionListener(this);
		}

		if(etNewPassConfirm!=null){
			etNewPassConfirm.setOnEditorActionListener(this);
		}
		
	}

	/**
	 * when check password
	 */
	private void doCheckPassword() {

		this.setTitle(this.getString(R.string.input_password));

		etOldPass = (EditText) findViewById(R.id.etOldPass);

		btnPositive.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				String pass = etOldPass.getText().toString();
				if (pass.equals(passwd)) {
					Intent intent = new Intent(appref,
							SystemConfigActivity.class);
					appref.startActivity(intent);
					finish();
				} else {
					new AlertDialog.Builder(appref)
							.setTitle(appref.getString(R.string.password_wrong))
							.setMessage(R.string.password_retype)
							.setPositiveButton("ok",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub

										}
									}).show();
					
					etOldPass.setText("");
				}
			}

		});

	}

	/**
	 * when change password
	 */
	private void doChangePassword() {

		this.setTitle(this.getString(R.string.security_changePasswd));

		tvTipOldPass = (TextView) findViewById(R.id.tvTipOldPass);
		tvNewPass = (TextView) findViewById(R.id.tvNewPass);
		tvNewPassConfirm = (TextView) findViewById(R.id.tvNewPassConfirm);
		etOldPass = (EditText) findViewById(R.id.etOldPass);
		etNewPass = (EditText) findViewById(R.id.etNewPass);
		etNewPassConfirm = (EditText) findViewById(R.id.etNewPassConfirm);

		tvTipOldPass.setText(R.string.password_old);
		tvNewPass.setText(R.string.password_new);
		tvNewPassConfirm.setText(R.string.password_confirm);

		tvNewPass.setVisibility(View.VISIBLE);
		tvNewPassConfirm.setVisibility(View.VISIBLE);
		etNewPass.setVisibility(View.VISIBLE);
		etNewPassConfirm.setVisibility(View.VISIBLE);

		btnPositive.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				String oldpass = etOldPass.getText().toString();
				String newpass = etNewPass.getText().toString();
				String newpassconfirm = etNewPassConfirm.getText().toString();

				if (!oldpass.equals(passwd)) {
					new AlertDialog.Builder(appref)
							.setTitle(R.string.password_wrong)
							.setMessage(R.string.password_old_wrong)
							.setNegativeButton(
									appref.getString(R.string.btnPositive),
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub

										}
									}).show();
					etOldPass.setText("");
				} else if (newpass.equals("")) {
					new AlertDialog.Builder(appref)
							.setTitle(R.string.password_wrong)
							.setMessage(R.string.password_cannot_null)
							.setNegativeButton(
									appref.getString(R.string.btnPositive),
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub

										}
									}).show();
				} else if (!newpass.equals(newpassconfirm)) {
					new AlertDialog.Builder(appref)
							.setTitle(R.string.password_wrong)
							.setMessage(R.string.password_not_same)
							.setNegativeButton(
									appref.getString(R.string.btnPositive),
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub

										}
									}).show();
					etNewPass.setText("");
					etNewPassConfirm.setText("");
				} else {
					
					if (XmlUtils.updateSingleNode(Constants.SystemConfigFile ,Constants.XML_PASSWORD,
							newpass)) {
						Constants.System_Password = newpass;
						Toast.makeText(appref, R.string.password_set_success,
								2000).show();
						finish();
					}
				}

			}

		});
	}

	private void setClickListener() {

		btnNegative.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				appref.setResult(0);
				finish();
			}

		});
	}

	@Override
	public void onAttachedToWindow() {
		if (android.os.Build.VERSION.SDK_INT < 12) {
			this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		}
		super.onAttachedToWindow();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch(keyCode){
		case KeyEvent.KEYCODE_HOME:
			finish();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		InputMethodManager imm = (InputMethodManager) appref.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(appref.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return true;
	}
	

}
