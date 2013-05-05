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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends Activity {
	public static final String SIGNUP_URL = "http://mgm.funformobile.com/nc/SignUp.php";
	private EditText mEmailText;
	private EditText mPasswordText;
	private EditText mUserNameText;
	private EditText mPhoneNumberText;
	private EditText mPasswordTextConfirm;
	private Hashtable<String, String> parameters;
	private ProgressDialog mProgressDialog;

	boolean isConfirmedPhoneNumber = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.signup);

		TextView mTitleText = (TextView) findViewById(R.id.titleTitle);
		mTitleText.setText("Signup");

		mEmailText = (EditText) findViewById(R.id.signup_email);
		mPasswordText = (EditText) findViewById(R.id.signup_password);
		mPasswordTextConfirm = (EditText) findViewById(R.id.signup_password_confirm);
		mUserNameText = (EditText) findViewById(R.id.signup_username);
		mPhoneNumberText = (EditText) findViewById(R.id.signup_phone_number);

		TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (tel != null && tel.getLine1Number() != null
				&& !tel.getLine1Number().equals("")) {
			mPhoneNumberText.setText(PhoneNumUtil.processNumber(tel
					.getLine1Number()));
			mPhoneNumberText.setFocusable(false);
			isConfirmedPhoneNumber = true;
		}
		Button go = (Button) findViewById(R.id.signup_submit);
		go.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));
		go.setOnClickListener(goListener);
	}

	private View.OnClickListener goListener = new View.OnClickListener() {
		public void onClick(View view) {
			String email = (mEmailText.getText()).toString().trim();
			if (!isEmailValid(email)) {
				String message = "Please enter a valid email address";
				showFinalAlert(message);
				return;
			}
			String password = (mPasswordText.getText()).toString().trim();
			String confPassword = (mPasswordTextConfirm.getText()).toString()
					.trim();
			if (password.length() < 4) {
				String message = "Password must be 4 characters or longer.";
				showFinalAlert(message);
				return;
			} else if (!confPassword.equalsIgnoreCase(password)) {
				String message = "Password do not match.";
				showFinalAlert(message);
				return;
			}
			String username = (mUserNameText.getText()).toString();
			if (username.length() < 4) {
				String message = "User name must be 4 characters or longer.";
				showFinalAlert(message);
				return;
			}

			String phoneNum = mPhoneNumberText.getText().toString().trim();
			if (phoneNum.length() < 10
					|| !PhoneNumUtil.isValidPhoneNum(phoneNum)) {
				String message = "Please enter a valid phone number.";
				showFinalAlert(message);
				return;
			}

			// Log.v("NEWSTRING", StringUtils.removeReturnTrim(username));

			doSignUp(email, password.trim(),
					StringUtils.removeReturnTrim(username), mPhoneNumberText
							.getText().toString().trim());
		}
	};

	/**
	 * isEmailValid: Validate email address using Java reg ex. This method
	 * checks if the input string is a valid email address.
	 * 
	 * @param email
	 *            String. Email address to validate
	 * @return boolean: true if email address is valid, false otherwise.
	 */

	public static boolean isEmailValid(String email) {
		boolean isValid = false;

		/*
		 * Email format: A valid email address will have following format:
		 * [\\w\\.-]+: Begins with word characters, (may include periods and
		 * hypens).
		 * 
		 * @: It must have a '@' symbol after initial characters.
		 * ([\\w\\-]+\\.)+: '@' must follow by more alphanumeric characters (may
		 * include hypens.). This part must also have a "." to separate domain
		 * and subdomain names. [A-Z]{2,4}$ : Must end with two to four
		 * alaphabets. (This will allow domain names with 2, 3 and 4 characters
		 * e.g pa, com, net, wxyz)
		 * 
		 * Examples: Following email addresses will pass validation abc@xyz.net;
		 * ab.c@tx.gov
		 */

		// Initialize reg ex for email.
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		// Make the comparison case-insensitive.
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	private void showFinalAlert(CharSequence message) {
		new AlertDialog.Builder(SignupActivity.this)
				.setTitle("Input Error")
				.setMessage(message)
				.setPositiveButton(R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// finish();
							}
						}).setCancelable(false).show();
	}

	public void doSignUp(String email, String password, String username,
			String phoneNum) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Sign Up");
		mProgressDialog.setMessage("Signing Up, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();
		parameters = new Hashtable<String, String>();

		if (isConfirmedPhoneNumber)
			parameters.put("valid", "Y");
		else
			parameters.put("valid", "N");

		parameters.put("email", email.trim());
		parameters.put("passwd", password);

		String usrnm = username.replace("\n", " ");
		parameters.put("name", usrnm.trim());
		parameters.put("uid", phoneNum.trim());
		SignupTask upl = new SignupTask();
		upl.execute(SIGNUP_URL);
	}

	public void showUploadSuccess(String json) {
		Log.v("JSON", json);
		if (json == null) {
			String message = "An network error has occured. Please try again later";
			showFinalAlert(message);
			return;
		}
		try {
			JSONObject object = new JSONObject(json);
			String status = object.getString("status");
			status = status.trim();
			Log.v("STATUS", "Status is: " + status);
			if (status.equals("OK")) {

				SharedPreferences prefs = getSharedPreferences("NameCard",
						MODE_PRIVATE);
				SharedPreferences.Editor prefsEditor = prefs.edit();
				Log.v("CHECK", "h = " + object.getString("h") + " | "
						+ "uid = " + object.getString("uid") + " | " + "n = "
						+ object.getString("n"));
				prefsEditor.putString("h", object.getString("h"));
				prefsEditor.putString("uid", object.getString("uid"));
				prefsEditor.putString("n", object.getString("n"));
				prefsEditor.commit();

				// Bundle bundle = new Bundle();
				// bundle.putString("json", json);
				//
				// Intent mIntent = new Intent();
				// mIntent.putExtras(bundle);
				// setResult(RESULT_OK, mIntent);

				Toast.makeText(this, "You have successfully Signed Up",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				Log.v("STATUS", status);
				String message = status;
				showFinalAlert(message);
			}
		} catch (Exception e) {

		}

	}

	private class SignupTask extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... searchKey) {

			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				return login(url, parameters);
				// return "SUCCESS";
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
					Log.v("KEY", key);
					val = params.get(key);
					Log.v("VAL", val);
					postString += key + "=" + URLEncoder.encode(val, "UTF-8")
							+ "&";
				}
				postString = postString.substring(0, postString.length() - 1);
				Log.v("postString", postString);
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
				int serverResponseCode = con.getResponseCode();
				Log.v("CODE", String.valueOf(serverResponseCode));
				String serverResponseMessage = con.getResponseMessage();
				Log.v("PAGE", serverResponseMessage);

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
				Log.v("MalformedURLException", me.toString());
				return null;
			} catch (IOException ie) {
				Log.v("IOException", ie.toString());
				return null;

			} catch (Exception e) {
				Log.v("Exception", e.toString());
				return null;
				// Log.e("HREQ", "Exception: "+e.toString());
			}
		}

	};
}
