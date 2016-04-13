package com.example.assignment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private final static String sUsername = "username";
	private final static String sPassword = "password";
	private EditText mUserName;
	private EditText mPassword;
	private Button mLoginBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		mUserName = (EditText) findViewById(R.id.login_txt_username);
		mPassword = (EditText) findViewById(R.id.login_txt_password);
		mLoginBtn = (Button) findViewById(R.id.login_btn_login);
		mLoginBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//if (sUsername.equals(mUserName.getText().toString())
		// && sPassword.equals(mPassword.getText().toString())) {
		if (true) {
			Intent intent = new Intent(this, NewsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showToast(R.string.login_fail_toast);
		}
	}
	
	private Toast mToast;
    public void showToast(int resId) {
        showToast(getString(resId));
    }
    
    public void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }
}
