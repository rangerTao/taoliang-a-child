package com.duole.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.duole.R;
import com.duole.utils.Constants;
import com.duole.utils.XmlUtils;

public class PasswordActivity extends BaseActivity {

	PasswordActivity appref;
	Button btnNegative;
	Button btnPositive;
	TextView tvTipOldPass;
	TextView tvNewPass;
	TextView tvNewPassConfirm;
	EditText etOldPass;
	EditText etNewPass;
	EditText etNewPassConfirm;

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

		if (type.equals("0")) {

			doCheckPassword();

		} else {

			doChangePassword();

		}

		setClickListener();

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
				if (pass.equals(Constants.System_Password)) {
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

				if (!oldpass.equals(Constants.System_Password)) {
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
				} else {
					Constants.System_Password = newpass;
					if (XmlUtils.updateSingleNode(Constants.XML_PASSWORD,
							newpass)) {
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
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
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
	

}
