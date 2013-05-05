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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TitlePageActivity extends Activity {

	private DbAdapter db = new DbAdapter(this);
	private ImageView logStatus;
	private boolean mIsLogin = false;

	private Hashtable<String, String> parameters;
	public static final String COUNT_URL = "http://mgm.funformobile.com/nc/NumberofNewContacts.php";

	private String user_id;
	private String hash_code;

	private int count = 0;
	private Button showCount;

	private LinearLayout masterRow;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.titlepage);

		checkLogin();
		checkAppVersion();

		if (mIsLogin)
			doCount(user_id);

		logStatus = (ImageView) findViewById(R.id.title_login_image);
		if (mIsLogin)
			logStatus.setImageResource(R.drawable.login);
		else
			logStatus.setImageResource(R.drawable.logout);

		logStatus.setOnClickListener(logStatusListener);

		TextView mTitleText = (TextView) findViewById(R.id.titleTitle);
		mTitleText.setText("NameCard");

		TextView mBottomText = (TextView) findViewById(R.id.welcomeTitle);
		if (mIsLogin) {
			final SharedPreferences prefs = getSharedPreferences("NameCard",
					MODE_PRIVATE);
			mBottomText.setText("Hi, " + prefs.getString("n", "") + ".");
		} else
			mBottomText.setText("Not logged in.");

		ImageView createNewCard = (ImageView) findViewById(R.id.title_createnewcard);
		createNewCard.setOnClickListener(createNewCardListener);
		ImageView myCards = (ImageView) findViewById(R.id.title_viewmycards);
		myCards.setOnClickListener(myCardsListener);
		ImageView contacts = (ImageView) findViewById(R.id.title_contacts);
		contacts.setOnClickListener(contactsListener);
		ImageView newContacts = (ImageView) findViewById(R.id.title_newcontacts);
		newContacts.setOnClickListener(newContactsListener);

		masterRow = (LinearLayout) findViewById(R.id.master_row);
		showCount = (Button) findViewById(R.id.count_new);
		// Button recoverCards = (Button) findViewById(R.id.title_recovercards);
		// recoverCards.setOnClickListener(recoverCardsListener);
	}

	private View.OnClickListener createNewCardListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent currentIntent = new Intent(TitlePageActivity.this,
					Form1Activity.class);
			startActivity(currentIntent);
		}
	};

	private View.OnClickListener myCardsListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent currentIntent = new Intent(TitlePageActivity.this,
					MyCardsListActivity.class);
			Cursor mCursor = db.fetchAllContacts();
			if (mCursor == null) {
				showFinalAlert("Please create a card");

			} else {
				mCursor.close();
				startActivity(currentIntent);
			}

		}

	};

	private View.OnClickListener contactsListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (mIsLogin) {
				Intent currentIntent = new Intent(TitlePageActivity.this,
						MyContactsListActivity.class);
				startActivity(currentIntent);
			} else {
				Intent currentIntent = new Intent(TitlePageActivity.this,
						LoginActivity.class);
				currentIntent.putExtra("MyContacts", 0);
				startActivity(currentIntent);
			}
		}

	};

	private View.OnClickListener newContactsListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (mIsLogin) {
				Intent currentIntent = new Intent(TitlePageActivity.this,
						NewContactsListActivity.class);
				startActivity(currentIntent);
			} else {
				Intent currentIntent = new Intent(TitlePageActivity.this,
						LoginActivity.class);
				currentIntent.putExtra("NewContacts", 1);
				startActivity(currentIntent);
			}
		}

	};

	// private View.OnClickListener recoverCardsListener = new
	// View.OnClickListener() {
	//
	// public void onClick(View v) {
	//
	// }
	//
	// };

	private View.OnClickListener logStatusListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (mIsLogin) {
				new AlertDialog.Builder(TitlePageActivity.this)
						.setTitle("Log Out")
						.setMessage("Would you like to log out?")
						.setPositiveButton("Okay",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										final SharedPreferences prefs = getSharedPreferences(
												"NameCard", MODE_PRIVATE);
										SharedPreferences.Editor prefsEditor = prefs
												.edit();
										prefsEditor.clear();
										prefsEditor.commit();

										mIsLogin = false;
										logStatus
												.setImageResource(R.drawable.logout);

										TextView mBottomText = (TextView) findViewById(R.id.welcomeTitle);
										mBottomText.setText("Not logged in.");
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).setCancelable(true).show();
			} else {
				Intent currentIntent = new Intent(TitlePageActivity.this,
						LoginActivity.class);
				startActivity(currentIntent);
			}
		}

	};

	private void showFinalAlert(CharSequence message) {
		new AlertDialog.Builder(TitlePageActivity.this)
				.setTitle("No Cards")
				.setMessage(message)
				.setPositiveButton(R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								startActivity(new Intent(
										TitlePageActivity.this,
										Form1Activity.class));
							}
						}).setCancelable(false).show();
	}

	private boolean checkLogin() {
		final SharedPreferences prefs = getSharedPreferences("NameCard",
				MODE_PRIVATE);
		user_id = prefs.getString("uid", null);
		hash_code = prefs.getString("h", null);
		if (user_id == null || hash_code == null) {
			return false;
		} else {
			mIsLogin = true;
			return true;
		}
	}

	@Override
	public void onResume() {
		checkLogin();
		if (mIsLogin)
			logStatus.setImageResource(R.drawable.login);
		else
			logStatus.setImageResource(R.drawable.logout);

		if (mIsLogin)
			doCount(user_id);
		else {
			count = 0;
			showCount.setVisibility(View.GONE);
		}

		TextView mBottomText = (TextView) findViewById(R.id.welcomeTitle);
		if (mIsLogin) {
			final SharedPreferences prefs = getSharedPreferences("NameCard",
					MODE_PRIVATE);
			mBottomText.setText("Hi, " + prefs.getString("n", "") + ".");
		} else
			mBottomText.setText("Not logged in.");
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			checkLogin();
			if (mIsLogin)
				logStatus.setImageResource(R.drawable.login);
			else
				logStatus.setImageResource(R.drawable.logout);

			TextView mBottomText = (TextView) findViewById(R.id.welcomeTitle);
			if (mIsLogin) {
				final SharedPreferences prefs = getSharedPreferences(
						"NameCard", MODE_PRIVATE);
				mBottomText.setText("Logged in as " + prefs.getString("n", "")
						+ ".");
			} else
				mBottomText.setText("Not logged in.");
		}
		// TODO handle here.
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			masterRow.setOrientation(LinearLayout.HORIZONTAL);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			masterRow.setOrientation(LinearLayout.VERTICAL);
		}
	}

	// Fetch count
	public void doCount(String userId) {
		parameters = new Hashtable<String, String>();
		parameters.put("receiver", userId);
		NewContactsCountTask countTask = new NewContactsCountTask();
		countTask.execute(COUNT_URL);
	}

	public class NewContactsCountTask extends
			AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... searchKey) {
			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				Log.v("Create", "Create successfully executed.");
				return count(url, parameters);
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
				showUploadSuccess(result);
				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

		}

		public void showUploadSuccess(String json) {
			if (json == null) {
				return;
			}
			try {
				JSONObject object = new JSONObject(json);
				String status = object.getString("status");
				status = status.trim();
				if (status.equals("OK")) {

					count = object.getInt("count");
					if (count != 0) {
						showCount.setVisibility(View.VISIBLE);
						showCount.setText(String.valueOf(count));
					} else
						showCount.setVisibility(View.GONE);

					// Bundle bundle = new Bundle();
					// bundle.putString("json", json);
					//
					// Intent mIntent = new Intent();
					// mIntent.putExtras(bundle);
					// setResult(RESULT_OK, mIntent);
				} else {
				}
			} catch (Exception e) {

			}

		}

		public String count(String serverUrl, Hashtable<String, String> params) {
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
					Log.v("RESPONSE", response.toString());
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

			} catch (StringIndexOutOfBoundsException e) {
				Log.v("Exception", e.toString());
				return null;
			} catch (Exception e) {
				Log.v("Exception", e.toString());
				return null;
				// Log.e("HREQ", "Exception: "+e.toString());
			}

		}

	}

	// Version Check

	private void doVersionCheck(String version, CharSequence message) {
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			int currentVersionCode = packageInfo.versionCode;
			int availableVersionCode = Integer.parseInt(version.trim());
			if (currentVersionCode < availableVersionCode) {

				if (message == null || message == "")
					message = "A new version is available with bug fixes and new features.";

				new AlertDialog.Builder(TitlePageActivity.this)
						.setTitle("New Version Available")
						.setMessage(message)
						.setPositiveButton("Get It Now",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										onNewVersion();
									}
								})
						.setNegativeButton("Later",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).setCancelable(true).show();
			}

		} catch (NameNotFoundException e) {
			Log.v("TitlePageActivity:doVersionCheck:Exception", e.toString());
		}
	}

	private void onNewVersion() {
		String pageUrl = "market://details?id=com.linkedcontact";
		Uri uri = Uri.parse(pageUrl);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
		finish();
	}

	private void checkAppVersion() {
		VersionChecker checker = new VersionChecker();
		checker.execute("");
	}

	@SuppressWarnings("finally")
	public String doCheck(String serverUrl) {
		String json = "";
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Connection", "Keep-Alive");

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				json += line + "\r";
			}

		} catch (MalformedURLException me) {
			Log.v("MalformedURLException", me.toString());
		} catch (IOException ie) {
			Log.v("IOException", ie.toString());
		} catch (Exception e) {
			Log.v("Exception", e.toString());
		} finally {
			// Log.v("ffmlist:doCheck:json", json);
			return json;
		}
	}

	private class VersionChecker extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... searchKey) {
			String url = "http://mgm.funformobile.com/nc/getAppVersion.php";
			try {
				return doCheck(url);
			} catch (Exception e) {
				Log.v("ffmList:NewMsgCountChecker",
						"Exception:" + e.getMessage());
				return null;
			}
		}

		protected void onPostExecute(String json) {
			processVersionCheck(json);
		}
	}

	public void processVersionCheck(String json) {
		try {
			JSONObject object = new JSONObject(json);
			String status = object.getString("status");
			status = status.trim();
			if (status.equals("OK")) {
				String version = object.getString("v");
				String msg = "";
				if (object.has("msg"))
					msg = object.getString("msg");
				doVersionCheck(version, msg);
			}
		} catch (Exception e) {
			Log.v("ffmList, processVersionCheck:Exception", e.toString());
		}
	}// Java Document
	
	public void onDestroy()
	{
		db.close();
		super.onDestroy();
	}

}
