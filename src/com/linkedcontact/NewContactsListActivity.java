package com.linkedcontact;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewContactsListActivity extends Activity {
	private ArrayList<String> cards;
	private ArrayList<String> ids;
	private ArrayList<String> phoneNums;
	private ArrayList<String> senderIds;
	private Spinner spinnerCategories;

	public static Hashtable<String, byte[]> images;

	CardListAdapter adp;
	Context context = this;
	ListView lv;

	private Hashtable<String, String> parameters;
	private ProgressDialog mProgressDialog;

	private boolean mIsLogin = false;
	String user_id;
	String hash_code;

	public static final String FETCH_CONTACTS = "http://mgm.funformobile.com/nc/fetchContacts.php";

	private int currentCategoryId = 0;

	public final static HashMap<Integer, String> categories = new HashMap<Integer, String>();
	static {

		categories.put(0, "Unassigned");
		categories.put(1, "Business");
		categories.put(2, "Church");
		categories.put(3, "College");
		categories.put(4, "Community");
		categories.put(5, "Co-workers");
		categories.put(6, "Family");
		categories.put(7, "Friends");
		categories.put(8, "Graduate School");
		categories.put(9, "High School");
		categories.put(10, "Relatives");
	}

	public String[] categoryNums = { "Unassigned", "Business", "Church",
			"Community", "Co-workers", "Family", "Graduate School",
			"High School", "Relatives" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cardslist);

		TextView titleLabel = (TextView) findViewById(R.id.listTitle);
		titleLabel.setText("New Contacts");

		checkLogin();

		Intent intent = getIntent();
		currentCategoryId = intent.getIntExtra("categoryId", 0);

		lv = (ListView) findViewById(R.id.list);

		spinnerCategories = (Spinner) findViewById(R.id.spinner_categories);
		spinnerCategories.setVisibility(View.GONE);

		cards = new ArrayList<String>();
		ids = new ArrayList<String>();
		phoneNums = new ArrayList<String>();
		senderIds = new ArrayList<String>();
		if (images == null)
			images = new Hashtable<String, byte[]>();

		Log.v("REQUEST", "SENT FROM onCreate");
		if (mIsLogin)
			doFetch(user_id);
		else {
			Intent currentIntent = new Intent(NewContactsListActivity.this,
					LoginActivity.class);
			startActivity(currentIntent);
		}

		adp = new CardListAdapter(this, R.layout.cardslistrow, cards);

		lv.setAdapter(adp);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.v("OnClick", "Arrived OnClick to MyCardActivity");
				Intent currentIntent = new Intent(NewContactsListActivity.this,
						NewContactActivity.class);

				String dataId = ids.get(position);
				String senderId = senderIds.get(position);

				currentIntent.putExtra("server_id", dataId);
				currentIntent.putExtra("senderId", senderId);
				currentIntent.putExtra("status", "0");

				String url = "http://mgm.funformobile.com/nc/img/"
						+ senderId.substring(0, 3) + "/" + dataId + ".jpg";
				Log.v("Byte[]", "The byte array is: " + images.get(url));
				currentIntent.putExtra("img", images.get(url));

				startActivityForResult(currentIntent, 0);

			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		TextView noCards = (TextView) findViewById(R.id.nocards_text);
		noCards.setVisibility(View.GONE);
		Log.v("REQUEST", "SENT FROM onActivityResult");
		doFetch(user_id);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class CardListAdapter extends ArrayAdapter<String> {

		public CardListAdapter(Context context, int resourceId,
				ArrayList<String> objects) {
			super(context, resourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.v("GETVIEW", String.valueOf(position));
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.cardslistrow, null);
			}

			if (position >= cards.size())
				return row;
			String item = cards.get(position);
			if (item == null)
				return row;

			TextView name = (TextView) row.findViewById(R.id.listrow_name);
			name.setText(item);

			downloadImg dl = new downloadImg();
			dl.execute(position);

			TextView phoneNum = (TextView) row
					.findViewById(R.id.listrow_phoneNum);
			phoneNum.setText(phoneNums.get(position));

			return row;
		}
	}

	// System Processes
	public void doFetch(String userId) {
		// mProgressDialog = new ProgressDialog(this);
		// mProgressDialog.setTitle("Contacts");
		// mProgressDialog.setMessage("Populating List, Please Wait");
		// mProgressDialog.setIndeterminate(true);
		//
		// mProgressDialog.setCancelable(false);
		//
		// mProgressDialog.show();

		parameters = new Hashtable<String, String>();
		parameters.put("receiver", userId);
		parameters.put("status", "0");
		FetchContacts upl = new FetchContacts();
		Log.v("Execute", "AsyncTask successfully executed.");
		upl.execute(FETCH_CONTACTS);
	}

	public void showUploadSuccess(String json) {
		if (json == null) {
			Toast.makeText(
					this,
					"We have encountered temporary technical difficulties, please try again later",
					Toast.LENGTH_LONG).show();
			return;
		}
		try {
			JSONObject object = new JSONObject(json);
			String status = object.getString("status");
			status = status.trim();
			if (status.equals("OK")) {

				cards.clear();
				ids.clear();
				phoneNums.clear();
				senderIds.clear();
				JSONArray items = object.getJSONArray("list");
				int length = items.length();
				if (length == 0) {
					TextView noCards = (TextView) findViewById(R.id.nocards_text);
					noCards.setText("You have no new contacts.");
					noCards.setVisibility(View.VISIBLE);
				} else {
					for (int i = 0; i < length; i++) {

						TextView noCards = (TextView) findViewById(R.id.nocards_text);
						noCards.setVisibility(View.GONE);

						JSONObject jobj = items.getJSONObject(i);

						String fName = jobj.getString("fName");
						String mInitial = jobj.getString("mInitial");
						String lName = jobj.getString("lName");

						if (mInitial.equals(""))
							cards.add(fName + " " + lName);
						else
							cards.add(fName + " " + mInitial + ". " + lName);

						ids.add(String.valueOf(jobj.getLong("serverId")));
						phoneNums.add(jobj.getString("cellNum"));
						senderIds.add(jobj.getString("uid"));
					}
					adp.notifyDataSetChanged();
				}
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {

		}

	}

	private class FetchContacts extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... searchKey) {

			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				Log.v("Create", "Create successfully executed.");
				return fetch(url, parameters);
				// return downloadImage(url);
			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());
				return null;

			}
		}

		protected void onPostExecute(String result) {
			try {
				// displayMsg();
				// displayImage(resultbm);
				showUploadSuccess(result);
				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

		}

		public String fetch(String serverUrl, Hashtable<String, String> params) {
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

	private class downloadImg extends AsyncTask<Integer, Integer, byte[]> {

		private int pos;

		@Override
		protected byte[] doInBackground(Integer... searchKey) {

			int position = searchKey[0];
			pos = position;

			if (position >= senderIds.size() || position >= ids.size())
				return null;

			URL aURL;
			try {
				Log.v("USERID", "USER ID IS: " + user_id);
				String areaCode = senderIds.get(position).substring(0, 3);
				String url = "http://mgm.funformobile.com/nc/img/" + areaCode
						+ "/" + ids.get(position) + ".jpg";
				aURL = new URL(url);

				Log.v("URL", "http://mgm.funformobile.com/nc/img/" + areaCode
						+ "/" + ids.get(position) + ".jpg");

				byte[] bytes1 = images.get(url);

				if (bytes1 != null)
					return bytes1;

				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);

				int current = 0;

				while ((current = bis.read()) != -1) {

					baf.append((byte) current);

				}

				byte[] bytes = baf.toByteArray();

				if (bytes != null)
					images.put(url, bytes);

				return bytes;
				// adp.notifyDataSetChanged();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(byte[] result) {
			if (result == null)
				return;

			try {
				Integer firstpos = lv.getFirstVisiblePosition();
				View row = lv.getChildAt(pos - firstpos);
				ImageView img = (ImageView) row
						.findViewById(R.id.listrow_image);
				Bitmap bm = BitmapFactory.decodeByteArray(result, 0,
						result.length);
				img.setImageBitmap(bm);
				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

		}
	}

	protected void onDestroy() {
		images.clear();
		super.onDestroy();
	}
}
