package com.linkedcontact;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MyContactsListActivity extends Activity {
	private ArrayList<String> cards;
	public ArrayList<String> ids;
	private ArrayList<String> phoneNums;
	private ArrayList<String> senderIds;
	private ArrayList<String> urls;
	public static Hashtable<String, byte[]> images;

	// private ArrayList<Integer> serverCategories;
	private Spinner spinnerCategories;
	private DbAdapter db;
	CardListAdapter adp;
	Context context = this;
	public ListView lv;
	private RemoveTask removeTask;

	private Hashtable<String, String> parameters;
	private ProgressDialog mProgressDialog;

	private boolean mIsLogin = false;
	String user_id;
	String hash_code;
	long serverId;
	int categoryId;

	private CategoryTask categoryTask;

	private int increment;

	public static final String FETCH_CONTACTS = "http://mgm.funformobile.com/nc/fetchContacts.php";
	public static final String DOWNLOAD_IMAGE = "http://mgm.funformobile.com/nc/img/";

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
		categories.put(8, "High School");
		categories.put(9, "Personal");
		categories.put(10, "Relatives");
	}

	public String[] categoryNums = { "Unassigned", "Business", "Church",
			"Community", "Co-workers", "Family", "High School", "Personal",
			"Relatives" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cardslist);

		TextView titleLabel = (TextView) findViewById(R.id.listTitle);
		titleLabel.setText("My Contacts");

		checkLogin();

		categoryTask = new CategoryTask(this);
		Intent intent = getIntent();
		currentCategoryId = intent.getIntExtra("categoryId", 0);
		categoryId = currentCategoryId;
		removeTask = new RemoveTask(this);

		lv = (ListView) findViewById(R.id.list);

		increment = 0;

		spinnerCategories = (Spinner) findViewById(R.id.spinner_categories);
		ArrayAdapter<CharSequence> adapterCategories = ArrayAdapter
				.createFromResource(this, R.array.category_array1,
						android.R.layout.simple_spinner_item);
		adapterCategories
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCategories.setAdapter(adapterCategories);
		spinnerCategories.setSelection(adapterCategories.getPosition(categories
				.get(currentCategoryId)));
		spinnerCategories.setOnItemSelectedListener(categoriesListener);

		db = new DbAdapter(this);

		cards = new ArrayList<String>();
		ids = new ArrayList<String>();
		phoneNums = new ArrayList<String>();
		senderIds = new ArrayList<String>();
		urls = new ArrayList<String>();

		if (images == null)
			images = new Hashtable<String, byte[]>();

		// Log.v("REQUEST", "Sent from onCreate");
		if (mIsLogin)
			doFetch(user_id, currentCategoryId);
		else {
			Intent currentIntent = new Intent(MyContactsListActivity.this,
					LoginActivity.class);
			startActivity(currentIntent);
		}

		adp = new CardListAdapter(this, R.layout.cardslistrow, cards);

		lv.setAdapter(adp);

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final int pos = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MyContactsListActivity.this);
				builder.setCancelable(true);
				builder.setTitle("Options");
				builder.setItems(R.array.contacts_longclick_array,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									Intent currentIntent = new Intent(
											MyContactsListActivity.this,
											ContactActivity.class);

									String dataId = ids.get(pos);
									String senderId = senderIds.get(pos);

									currentIntent.putExtra("server_id", dataId);
									currentIntent
											.putExtra("senderId", senderId);
									String url = "http://mgm.funformobile.com/nc/img/"
											+ senderId.substring(0, 3)
											+ "/"
											+ dataId + ".jpg";
									// Log.v("Byte[]", "The byte array is: " +
									// images.get(url));
									currentIntent.putExtra("img",
											images.get(url));
									currentIntent.putExtra("pos", pos);

									startActivityForResult(currentIntent, 0);
								} else if (which == 1) {
									showFinalAlert("Delete contact?", pos);
								} else {
									if (isNotPhoneContact(phoneNums.get(pos))) {
										FetchContact fetch1 = new FetchContact();
										fetch1.doFetchContact(user_id,
												ids.get(pos));
									}
								}
							}
						});
				builder.create();
				builder.show();

				return true;
			}

		});

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Log.v("OnClick", "Arrived OnClick to MyCardActivity");
				Intent currentIntent = new Intent(MyContactsListActivity.this,
						ContactActivity.class);

				String dataId = ids.get(position);
				String senderId = senderIds.get(position);

				currentIntent.putExtra("server_id", dataId);
				currentIntent.putExtra("senderId", senderId);
				String url = "http://mgm.funformobile.com/nc/img/"
						+ senderId.substring(0, 3) + "/" + dataId + ".jpg";
				// Log.v("Byte[]", "The byte array is: " + images.get(url));
				currentIntent.putExtra("img", images.get(url));

				startActivityForResult(currentIntent, 0);

			}
		});

		db.close();
	}

	/**
	 * Displays an alert message.
	 * 
	 * @param message
	 */
	private void showFinalAlert(CharSequence message, final int position) {
		new AlertDialog.Builder(MyContactsListActivity.this)
				.setTitle("Confirmation")
				.setMessage(message)
				.setPositiveButton(R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String dataId = ids.get(position);
								String senderId = senderIds.get(position);

								String url = "http://mgm.funformobile.com/nc/img/"
										+ senderId.substring(0, 3)
										+ "/"
										+ dataId + ".jpg";

								removeTask.doRemove(user_id,
										Long.parseLong(ids.remove(position)));
								cards.remove(position);
								phoneNums.remove(position);
								senderIds.remove(position);
								images.remove(url);
								adp.notifyDataSetChanged();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).setCancelable(true).show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			int listPos = data.getIntExtra("position", 0);
			cards.remove(listPos);
			ids.remove(listPos);
			phoneNums.remove(listPos);
			senderIds.remove(listPos);
			urls.remove(listPos);

			adp.notifyDataSetChanged();

		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private AdapterView.OnItemSelectedListener categoriesListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String categoryName = getResources().getStringArray(
					R.array.category_array)[pos];
			for (Integer s : categories.keySet()) {
				if (categories.get(s).equals(categoryName)) {
					currentCategoryId = s;
					break;
				}
			}

			Log.v("Category", "The current category ID is: "
					+ currentCategoryId);

			cards.clear();
			ids.clear();

			if (mIsLogin && increment != 0) {
				// Log.v("REQUEST", "Sent from categoriesListener");
				doFetch(user_id, currentCategoryId);
			}

			increment++;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	private AdapterView.OnItemSelectedListener categoriesPopupListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String category = getResources().getStringArray(
					R.array.category_array)[pos];
			for (Integer s : categories.keySet()) {
				if (categories.get(s).equals(category)) {
					categoryId = s;
				}
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

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
	public void doFetch(String userId, int category) {
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
		parameters.put("category", String.valueOf(category));
		parameters.put("status", "1");
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
				urls.clear();
				JSONArray items = object.getJSONArray("list");
				int length = items.length();
				if (length == 0) {
					TextView noCards = (TextView) findViewById(R.id.nocards_text);
					noCards.setText("There are no contacts in this category.");
					noCards.setVisibility(View.VISIBLE);
				} else {
					for (int i = 0; i < length; i++) {
						JSONObject jobj = items.getJSONObject(i);
						String id = String.valueOf(jobj.getLong("serverId"));

						TextView noCards = (TextView) findViewById(R.id.nocards_text);
						noCards.setVisibility(View.GONE);

						String fName = jobj.getString("fName");
						String mInitial = jobj.getString("mInitial");
						String lName = jobj.getString("lName");

						if (mInitial.equals(""))
							cards.add(fName + " " + lName);
						else
							cards.add(fName + " " + mInitial + ". " + lName);

						ids.add(id);
						Log.v("CELLNUM", jobj.getString("cellNum"));
						phoneNums.add(jobj.getString("cellNum"));
						Log.v("SENDERID", jobj.getString("uid"));
						senderIds.add(jobj.getString("uid"));
						urls.add(jobj.getString("imageUri"));
						// serverCategories.add(jobj.getInt("category"));
					}
					
					adp.notifyDataSetChanged();
				}
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (NullPointerException e) {

		} catch (Exception e) {

		}

	}

	private class downloadImg extends AsyncTask<Integer, Integer, byte[]> {

		private int pos;

		@Override
		protected byte[] doInBackground(Integer... searchKey) {

			int position = searchKey[0];
			pos = position;

			if (urls.get(pos).equals(""))
				return null;

			if (position >= cards.size() || position >= senderIds.size()
					|| position >= ids.size())
				return null;

			URL aURL;
			try {
				Log.v("USERID", "USER ID IS: " + user_id);
				Log.v("PoSITION", String.valueOf(pos));
				String areaCode = senderIds.get(position).substring(0, 3);
				String url = "http://mgm.funformobile.com/nc/img/" + areaCode
						+ "/" + ids.get(position) + ".jpg";
				aURL = new URL(url);

				Log.v("URL", "http://mgm.funformobile.com/nc/img/" + areaCode
						+ "/" + ids.get(position) + ".jpg");

				byte[] bytes1 = images.get(url);

				if (bytes1 != null) {
					Log.v("FROMCACHE", url);
					return bytes1;
				}

				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);

				int current = 0;

				while ((current = bis.read()) != -1) {

					baf.append((byte) current);

				}
				is.close();
				bis.close();

				byte[] bytes = baf.toByteArray();
				baf.clear();
				if (bytes != null) {
					Log.v("FROMSERVER", url);
					images.put(url, bytes);
				}

				return bytes;
				// adp.notifyDataSetChanged();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(byte[] result) {

			Log.v("CAME HERE", "CAME HERE");

			try {
				Integer firstpos = lv.getFirstVisiblePosition();
				Log.v("SETPOSITION",
						"Setting row at: " + String.valueOf(pos - firstpos));
				View row = lv.getChildAt(pos - firstpos);
				if (result == null) {
					ImageView img = (ImageView) row
							.findViewById(R.id.listrow_image);

					img.setImageDrawable(getResources().getDrawable(
							R.drawable.defaulticon));
				} else {
					ImageView img = (ImageView) row
							.findViewById(R.id.listrow_image);
					Bitmap bm = BitmapFactory.decodeByteArray(result, 0,
							result.length);
					img.setImageBitmap(bm);
				}
				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

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

	protected void onDestroy() {
		images.clear();
		super.onDestroy();
	}

	private boolean isNotPhoneContact(String cellNum) {
		Cursor cursor = getContentResolver().query(
				Uri.withAppendedPath(getPhoneLookupContentFilterUri(),
						Uri.encode(cellNum)), getPhoneLookupProjection(), null,
				null, null);

		if (cursor != null) {
			return true;
		} else
			return false;
	}

	public static String[] getPhoneLookupProjection() {
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
			return new String[] { Contacts.Phones.PERSON_ID,
					Contacts.Phones.DISPLAY_NAME };
			// new String[] {
			// ContactWrapper.getColumn(ContactWrapper.COL_CONTACT_PERSON_ID) },
		}

		/*
		 * ContactsContract.PhoneLookup._ID
		 * ContactsContract.PhoneLookup.DISPLAY_NAME
		 * ContactsContract.PhoneLookup.LOOKUP_KEY
		 */
		return new String[] { "_id", "display_name", "lookup" };
	}

	public static Uri getPhoneLookupContentFilterUri() {
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
			return Contacts.Phones.CONTENT_FILTER_URL;
		}

		// ContactsContract.PhoneLookup.CONTENT_FILTER_URI
		return Uri.parse("content://com.android.contacts/phone_lookup");
	}

	private class FetchContact extends AsyncTask<String, Integer, String> {

		private Hashtable<String, String> params;
		public static final String FETCH_CONTACT = "http://mgm.funformobile.com/nc/fetchContact.php";

		protected String doInBackground(String... searchKey) {

			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				Log.v("Create", "Create successfully executed.");
				return fetch(url, params);
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
				showUploadSuccessContact(result);
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

		public void doFetchContact(String receiverId, String cardId) {
			// mProgressDialog = new ProgressDialog(this);
			// mProgressDialog.setTitle("Contacts");
			// mProgressDialog.setMessage("Populating List, Please Wait");
			// mProgressDialog.setIndeterminate(true);
			//
			// mProgressDialog.setCancelable(false);
			//
			// mProgressDialog.show();

			params = new Hashtable<String, String>();
			params.put("receiver", receiverId);
			params.put("server_id", cardId);
			Log.v("Execute", "AsyncTask successfully executed.");
			execute(FETCH_CONTACT);
		}
	}

	private void showUploadSuccessContact(String json) {
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
				JSONArray items = object.getJSONArray("list");
				int length = items.length();
				for (int i = 0; i < length; i++) {
					JSONObject jobj = items.getJSONObject(i);

					String fName = jobj.getString("fName");
					String mInitial = jobj.getString("mInitial");
					String lName = jobj.getString("lName");
					String email = jobj.getString("email");
					String cellPhone = jobj.getString("cellNum");
					String homePhone = jobj.getString("homeNum");
					String company = jobj.getString("company");
					String title = jobj.getString("title");

					Log.v("Save contact", "About to save contact");

					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.RawContacts.CONTENT_URI)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_TYPE,
									null)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_NAME,
									null).build());

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
									fName).build());

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
									mInitial).build());

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
									lName).build());

					if (mInitial.equals("")) {
						ops.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(
										ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(
										ContactsContract.Data.MIMETYPE,
										ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
								.withValue(
										ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
										fName + " " + lName).build());
					} else {
						ops.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(
										ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(
										ContactsContract.Data.MIMETYPE,
										ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
								.withValue(
										ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
										fName + " " + mInitial + ". " + lName)
								.build());
					}

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									cellPhone)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
							.build());

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									homePhone)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
							.build());

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Email.DATA,
									email)
							.withValue(
									ContactsContract.CommonDataKinds.Email.TYPE,
									ContactsContract.CommonDataKinds.Email.TYPE_WORK)
							.build());

					if (!company.equals("") && !title.equals("")) {
						ops.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(
										ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(
										ContactsContract.Data.MIMETYPE,
										ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
								.withValue(
										ContactsContract.CommonDataKinds.Organization.COMPANY,
										company)
								.withValue(
										ContactsContract.CommonDataKinds.Organization.TYPE,
										ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
								.withValue(
										ContactsContract.CommonDataKinds.Organization.TITLE,
										title)
								.withValue(
										ContactsContract.CommonDataKinds.Organization.TYPE,
										ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
								.build());
					}

					try {
						getContentResolver().applyBatch(
								ContactsContract.AUTHORITY, ops);
						Toast.makeText(this, "Contact successfully added",
								Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(this, "Exception: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}

					// Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
					// // Intent intent = new
					// // Intent(Intent.ACTION_INSERT);
					// intent.setType("vnd.android.cursor.item/contact");
					// intent.putExtra(ContactsContract.Intents.Insert.PHONE,
					// cellPhone);
					// intent.putExtra(ContactsContract.Intents.Insert.NAME,
					// fName);
					// intent.putExtra(ContactsContract.Intents.Insert.EMAIL,
					// email);
					// intent.putExtra(ContactsContract.Intents.Insert.COMPANY,
					// company);
					// intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE,
					// title);
					// intent.putExtra(
					// ContactsContract.Intents.Insert.SECONDARY_PHONE,
					// homePhone);
					// intent.putExtra(
					// ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
					// ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
					// intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
					// ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
					// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					// startActivity(intent);
				}
			}

		} catch (NullPointerException e) {

		} catch (Exception e) {

		}

	}

}
