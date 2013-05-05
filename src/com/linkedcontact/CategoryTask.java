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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CategoryTask extends AsyncTask<String, Integer, String> {

	private ProgressDialog mProgressDialog;
	private Hashtable<String, String> parameters;
	Context context;
	public static final String CATEGORY_URL = "http://mgm.funformobile.com/nc/updateContact.php";

	public CategoryTask(Context context1) {
		context = context1;
	}

	public void doSend(String receiverId, int category, long serverId) {
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle("Update Category");
		mProgressDialog.setMessage("Updating Category, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();

		Log.v("SERVERID", "SENDTASK SERVER ID IS: " + serverId);
		parameters = new Hashtable<String, String>();
		parameters.put("category", String.valueOf(category));
		parameters.put("server_id", String.valueOf(serverId));
		parameters.put("receiver", receiverId);
		parameters.put("status", "1");
		this.execute(CATEGORY_URL);
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
				Toast.makeText(context, "Category successfully updated",
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
