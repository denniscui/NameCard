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

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SendTask extends AsyncTask<String, Integer, String> {

	private ProgressDialog mProgressDialog;
	private Hashtable<String, String> parameters;
	Context context;
	public static final String SEND_URL = "http://mgm.funformobile.com/nc/sendCards.php";

	String receiver;
	String user_id;
	String server_id;

	public SendTask(Context context1) {
		context = context1;
	}

	public void doSend(String receiverId, String userId, int category,
			long serverId) {
		receiver = receiverId;
		user_id = userId;
		server_id = String.valueOf(serverId);
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle("Send Card");
		mProgressDialog.setMessage("Sending Card, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();

		Log.v("SERVERID", "SENDTASK SERVER ID IS: " + serverId);
		parameters = new Hashtable<String, String>();
		parameters.put("sender", userId);
		parameters.put("category", String.valueOf(category));
		parameters.put("server_id", String.valueOf(serverId));
		parameters.put("receiver", receiverId);

		DbAdapter db = new DbAdapter(context);
		Cursor mCursor = db.fetchContact(Long.parseLong(server_id));

		String[] receivers = receiver.split(",");

		try {

			SmsManager sms = SmsManager.getDefault();

			if (sms != null) {
				String text = "";

				int dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_FNAME);

				text += mCursor.getString(dataIndex) + " ";

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_MINITIAL);

				if (!mCursor.getString(dataIndex).equals(""))
					text += mCursor.getString(dataIndex) + ". ";

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_LNAME);

				text += mCursor.getString(dataIndex) + " \n";

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CELL_PHONE);

				text += mCursor.getString(dataIndex) + " \n";

				dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_EMAIL);

				if (!mCursor.getString(dataIndex).equals(""))
					text += mCursor.getString(dataIndex) + " \n";

				text += "<NameCard> http://linkedcontact.com/card.php?s="
						+ server_id;

				for (String s : receivers) {
					try {
						sms.sendTextMessage(s.trim(), null, text, null, null);
					} catch (IllegalArgumentException e) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			// All exceptions should be caught.
		}

		this.execute(SEND_URL);
	}

	@Override
	protected String doInBackground(String... searchKey) {
		String url = searchKey[0];

		try {
			// Log.v("gsearch","gsearch result with AsyncTask");
			Log.v("Create", "Create successfully executed.");
			return send(url, parameters);
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
			Log.v("Dismiss", "mProcessDialog successfully dismissed.");
			mProgressDialog.dismiss();
			showUploadSuccess(result);
			// Log.v("Ringtone","Ringtone Path:"+resultbm);

		} catch (Exception e) {
			// Log.v("Exception google search","Exception:"+e.getMessage());

		}

	}

	public void showUploadSuccess(String json) {
		if (json == null) {
			Toast.makeText(
					context,
					"We have encountered temporary technical difficulties, please try again later",
					Toast.LENGTH_LONG).show();
			return;
		}
		try {
			JSONObject object = new JSONObject(json);
			String status = object.getString("status");
			status = status.trim();
			if (status.equals("OK")) {

				// Bundle bundle = new Bundle();
				// bundle.putString("json", json);
				//
				// Intent mIntent = new Intent();
				// mIntent.putExtras(bundle);
				// setResult(RESULT_OK, mIntent);
				Toast.makeText(context, "Card successfully sent",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {

		}

	}

	public String send(String serverUrl, Hashtable<String, String> params) {
		try {
			Log.v("serverUrl", serverUrl);
			URL url = new URL(serverUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
				postString += key + "=" + URLEncoder.encode(val, "UTF-8") + "&";
			}
			postString = postString.substring(0, postString.length() - 1);
			// Log.v("postString",postString);
			con.setRequestProperty("Content-Length",
					"" + Integer.toString(postString.getBytes().length));
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			DataOutputStream dos = new DataOutputStream(con.getOutputStream());
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
