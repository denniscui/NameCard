package com.linkedcontact;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public static final String LOGIN_URL = "http://mgm.funformobile.com/nc/SignIn.php";
	private EditText mPhoneNumText;
	private EditText mPasswordText;
	private Hashtable<String, String> parameters;
	private ProgressDialog mProgressDialog;
	private Integer bgcolor;
	private int previous = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// final SharedPreferences prefs = getSharedPreferences("FunForMobile",
		// MODE_PRIVATE);
		bgcolor = this.getResources().getColor(R.color.mainbg_sky);

		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			if (intent.getExtras().getInt("MyContacts") == 0)
				previous = 0;
			else if (intent.getExtras().getInt("NewContacts") == 1)
				previous = 1;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		TextView mTitleText = (TextView) findViewById(R.id.loginTitle);
		mTitleText.setText("NameCard Login");

		mPhoneNumText = (EditText) findViewById(R.id.login_phonenum);
		mPasswordText = (EditText) findViewById(R.id.login_password);

		TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (tel != null && tel.getLine1Number() != null && !tel.getLine1Number().equals("")) {
			mPhoneNumText.setText(PhoneNumUtil.processNumber(tel
					.getLine1Number()));
		}

		Button go = (Button) findViewById(R.id.login_go);
		go.setOnClickListener(goListener);

		Button cancel = (Button) findViewById(R.id.login_cancel);
		cancel.setOnClickListener(cancelListener);

		Button free = (Button) findViewById(R.id.signup_free);
		free.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));
		free.setOnClickListener(freeListener);
	}

	private View.OnClickListener goListener = new View.OnClickListener() {
		public void onClick(View view) {
			String email = (mPhoneNumText.getText()).toString();
			String password = (mPasswordText.getText()).toString();
			doLogin(email, password);
		}
	};

	private View.OnClickListener cancelListener = new View.OnClickListener() {
		public void onClick(View view) {
			finish();
		}
	};
	
	private View.OnClickListener freeListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent mIntent = new Intent(LoginActivity.this,
					SignupActivity.class);
			startActivityForResult(mIntent, RESULT_OK);
			setResult(RESULT_OK, mIntent);
			finish();
		}
	};

	public void doLogin(String email, String password) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Log In");
		mProgressDialog.setMessage("Logging In, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();
		parameters = new Hashtable<String, String>();
		parameters.put("uid", email);
		parameters.put("passwd", password);
		LoginTask upl = new LoginTask();
		upl.execute(LOGIN_URL);
	}

	public void showUploadSuccess(String json) {
		if (json == null) {
			Toast.makeText(
					this,
					"We have encountered temperary technical difficult, please try again later",
					Toast.LENGTH_LONG).show();
			return;
		}
		try {
			Log.v("JSON", json);
			JSONObject object = new JSONObject(json);
			String status = object.getString("status");
			status = status.trim();
			if (status.equals("OK")) {
				SharedPreferences prefs = getSharedPreferences("NameCard",
						MODE_PRIVATE);
				SharedPreferences.Editor prefsEditor = prefs.edit();
				prefsEditor.putString("h", object.getString("h"));
				prefsEditor.putString("uid", object.getString("uid"));
				Log.v("Name", "Logged in as: " + object.getString("n"));
				prefsEditor.putString("n", object.getString("n"));
				prefsEditor.commit();

				// Bundle bundle = new Bundle();
				// bundle.putString("json", json);
				//
				// Intent mIntent = new Intent();
				// mIntent.putExtras(bundle);
				// setResult(RESULT_OK, mIntent);
				Toast.makeText(this, "You have successfully logged in",
						Toast.LENGTH_LONG).show();

				if (previous == 0) {
					Intent currentIntent = new Intent(LoginActivity.this,
							MyContactsListActivity.class);
					startActivity(currentIntent);
				} else if (previous == 1) {
					Intent currentIntent = new Intent(LoginActivity.this,
							NewContactsListActivity.class);
					startActivity(currentIntent);
				}
				finish();
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {

		}

	}

	private class LoginTask extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... searchKey) {

			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				return login(url, parameters);
				// return downloadImage(url);
			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());
				return null;

			}
		}

		protected void onPostExecute(String result) {
			// Toast.makeText(this.get, "Your Ringtone has been downloaded",
			// Toast.LENGTH_LONG).show();
			try {
				// displayMsg();
				// displayImage(resultbm);
				mProgressDialog.dismiss();
				showUploadSuccess(result);

				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

		}

		public String login(String serverUrl, Hashtable<String, String> params) {
			try {

				Log.v("serverUrl", serverUrl);
				URL url = new URL(serverUrl);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.setRequestMethod("POST");
				con.setRequestProperty("Connection", "Keep-Alive");
				String postString = "";

				Enumeration<String> keys = params.keys();
				String key, val;
				while (keys.hasMoreElements()) {
					key = keys.nextElement().toString();
					// Log.v("KEY",key);
					val = params.get(key);
					// Log.v("VAL",val);
					postString += key + "=" + URLEncoder.encode(val, "UTF-8")
							+ "&";
				}
				postString = postString.substring(0, postString.length() - 1);
				// Log.v("postString",postString);
				con.setRequestProperty("Content-Length",
						"" + Integer.toString(postString.getBytes().length));
				con.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				DataOutputStream dos = new DataOutputStream(
						con.getOutputStream());
				dos.writeBytes(postString);
				dos.flush();
				dos.close();
				// Responses from the server (code and message)
				// int serverResponseCode = con.getResponseCode();
				// Log.v("CODE",String.valueOf(serverResponseCode));
				// String serverResponseMessage = con.getResponseMessage();
				// Log.v("PAGE",serverResponseMessage);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String line;
				StringBuffer response = new StringBuffer();
				while ((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				rd.close();

				String json = response.toString();
				return json;
			} catch (MalformedURLException me) {
				// Log.v("MalformedURLException",me.toString());
				return null;
			} catch (IOException ie) {

				// Log.v("IOException",ie.toString());
				return null;

			} catch (Exception e) {
				// Log.v("Exception",e.toString());
				return null;
				// Log.e("HREQ", "Exception: "+e.toString());
			}

		}
	}
}
