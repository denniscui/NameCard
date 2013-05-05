package com.linkedcontact;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends Activity {

	private String fName = "";
	private String lName = "";
	private String mInitial = "";
	private String cellNum = "";
	private String homeNum = "";
	private String faxNum = "";
	private String company = "";
	private String title = "";
	private String address = "";
	private String email = "";
	private String website = "";
	private String note = "";
	private String textFont = "sans";
	private String textColor = "0";
	private String bgColor = "16777215";
	private LinearLayout layout;
	private long serverId;
	private String senderId;
	private Button accept;
	private Button update;

	private ImageView caller;
	private ImageView smser;
	private ImageView emailer;
	private ImageView moreStuff;

	private boolean isFromNewContacts = false;

	private boolean mIsLogin = false;
	private boolean isChecked = false;
	public static String cookieTime = "";

	private int listPosition;

	public static final int SEND_CODE = 1;
	private int categoryId;
	private byte[] icon;

	private Spinner categorySpinner;
	private Button save;
	private LinearLayout updateLayout;

	private String imageUri;
	private byte[] imageBytes;

	public static final String FETCH_CONTACT = "http://mgm.funformobile.com/nc/fetchContact.php";

	private Hashtable<String, String> parameters;
	private String user_id;
	private String hash_code;
	private RemoveTask removeTask;
	private static AcceptTask acceptTask;
	private static CategoryTask categoryTask;

	public final static String[] textFonts = { "sans", "monospace", "serif" };

	public final static HashMap<String, Typeface> fontMap = new HashMap<String, Typeface>();
	static {
		fontMap.put("sans", Typeface.SANS_SERIF);
		fontMap.put("monospace", Typeface.MONOSPACE);
		fontMap.put("serif", Typeface.SERIF);
	}

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
		categories.put(9, "Personal");
		categories.put(8, "High School");
		categories.put(10, "Relatives");
	}

	private ImageView mImage;
	private TextView mName;
	private TextView mCellPhoneNum;
	private TextView mHomePhoneNum;
	private TextView mFaxNum;
	private TextView mCompanyName;
	private TextView mTitle;
	private TextView mAddress;
	private TextView mEmail;
	private TextView mWebURL;
	private TextView mNote;

	private Intent intent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mycontact);

		checkLogin();
		removeTask = new RemoveTask(this);
		acceptTask = new AcceptTask(this);
		categoryTask = new CategoryTask(this);

		intent = getIntent();

		Bundle bundle = intent.getExtras();

		serverId = Long.parseLong(bundle.getString("server_id"));
		senderId = bundle.getString("senderId");
		listPosition = bundle.getInt("pos");
		// doFetch(user_id, serverId);

		parameters = new Hashtable<String, String>();
		parameters.put("receiver", user_id);
		parameters.put("server_id", String.valueOf(serverId));

		String json = null;
		try {
			Log.v("serverUrl", FETCH_CONTACT);
			URL url = new URL(FETCH_CONTACT);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.setRequestProperty("Connection", "Keep-Alive");

			String postString = "";

			Enumeration<String> keys = parameters.keys();
			String key, val;
			while (keys.hasMoreElements()) {
				key = keys.nextElement().toString();
				// Log.v("KEY",key);
				val = parameters.get(key);
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
			json = response.toString();
		} catch (Exception e) {

		}

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
					fName = jobj.getString("fName");
					lName = jobj.getString("lName");
					mInitial = jobj.getString("mInitial");
					cellNum = jobj.getString("cellNum");
					homeNum = jobj.getString("homeNum");
					faxNum = jobj.getString("faxNum");
					company = jobj.getString("company");
					title = jobj.getString("title");
					address = jobj.getString("address");
					email = jobj.getString("email");
					website = jobj.getString("webUrl");
					note = jobj.getString("note");
					textFont = jobj.getString("textFont");
					textColor = jobj.getString("textColor");
					bgColor = jobj.getString("bgColor");
					categoryId = jobj.getInt("category");
					imageUri = jobj.getString("imageUri");
				}
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {

		}

		Log.v("ServerID", "The server ID in MyCardActivity is: " + serverId);

		mImage = (ImageView) findViewById(R.id.mycard_image);
		mName = (TextView) findViewById(R.id.mycard_name);
		mCellPhoneNum = (TextView) findViewById(R.id.mycard_cellphonenumber);
		mHomePhoneNum = (TextView) findViewById(R.id.mycard_homephonenumber);
		mFaxNum = (TextView) findViewById(R.id.mycard_faxnumber);
		mCompanyName = (TextView) findViewById(R.id.mycard_companyname);
		mTitle = (TextView) findViewById(R.id.mycard_title);
		mAddress = (TextView) findViewById(R.id.mycard_address1);
		mEmail = (TextView) findViewById(R.id.mycard_email);
		mWebURL = (TextView) findViewById(R.id.mycard_websiteURL);
		mNote = (TextView) findViewById(R.id.mycard_note);
		layout = (LinearLayout) findViewById(R.id.mycard_layout);

		mName.setTypeface(fontMap.get(textFont));
		mCellPhoneNum.setTypeface(fontMap.get(textFont));
		mHomePhoneNum.setTypeface(fontMap.get(textFont));
		mFaxNum.setTypeface(fontMap.get(textFont));
		mCompanyName.setTypeface(fontMap.get(textFont));
		mTitle.setTypeface(fontMap.get(textFont));
		mAddress.setTypeface(fontMap.get(textFont));
		mEmail.setTypeface(fontMap.get(textFont));
		mWebURL.setTypeface(fontMap.get(textFont));
		mNote.setTypeface(fontMap.get(textFont));
		icon = bundle.getByteArray("img");
		if (icon != null) {
			Bitmap bmp = BitmapFactory.decodeByteArray(icon, 0, icon.length);
			imageBytes = icon;
			mImage.setImageBitmap(bmp);
		} else if (!imageUri.equals("")) {
			String areaCode = senderId.substring(0, 3);
			URL aURL;
			try {
				aURL = new URL("http://mgm.funformobile.com/nc/img/" + areaCode
						+ "/" + serverId + ".jpg");

				Log.v("IMAGEURI", imageUri);
				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);

				int current = 0;

				while ((current = bis.read()) != -1) {

					baf.append((byte) current);

				}

				byte[] bm = baf.toByteArray();
				imageBytes = bm;
				Bitmap bmp = BitmapFactory.decodeByteArray(bm, 0, bm.length);
				mImage.setImageBitmap(bmp);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (mInitial.equals("")) {
			mName.setText(fName + " " + lName);
		} else {
			mName.setText(fName + " " + mInitial + ". " + lName);
		}

		if (homeNum.equals(""))
			mHomePhoneNum.setVisibility(View.GONE);
		if (faxNum.equals(""))
			mFaxNum.setVisibility(View.GONE);
		if (company.equals(""))
			mCompanyName.setVisibility(View.GONE);
		if (title.equals(""))
			mTitle.setVisibility(View.GONE);
		if (address.equals(""))
			mAddress.setVisibility(View.GONE);
		if (email.equals(""))
			mEmail.setVisibility(View.GONE);
		if (website.equals(""))
			mWebURL.setVisibility(View.GONE);
		if (note.equals(""))
			mNote.setVisibility(View.GONE);

		mCellPhoneNum.setText(PhoneNumberUtils.formatNumber(cellNum) + " (C)");
		mHomePhoneNum.setText(PhoneNumberUtils.formatNumber(homeNum) + " (T)");
		mFaxNum.setText(PhoneNumberUtils.formatNumber(faxNum) + " (F)");
		mCompanyName.setText(company);
		mTitle.setText(title);
		mAddress.setText(address);
		mEmail.setText(email);
		mWebURL.setText(website);
		mNote.setText(note);

		layout.setBackgroundColor(Integer.parseInt(bgColor));

		mName.setTextColor(Integer.parseInt(textColor));
		mCellPhoneNum.setTextColor(Integer.parseInt(textColor));
		mHomePhoneNum.setTextColor(Integer.parseInt(textColor));
		mFaxNum.setTextColor(Integer.parseInt(textColor));
		mCompanyName.setTextColor(Integer.parseInt(textColor));
		mTitle.setTextColor(Integer.parseInt(textColor));
		mAddress.setTextColor(Integer.parseInt(textColor));
		mEmail.setTextColor(Integer.parseInt(textColor));
		mWebURL.setTextColor(Integer.parseInt(textColor));
		mNote.setTextColor(Integer.parseInt(textColor));

		mName.setTypeface(Typeface.DEFAULT_BOLD);
		mCompanyName.setTypeface(Typeface.DEFAULT_BOLD);

		GradientDrawable shape = (GradientDrawable) getResources().getDrawable(
				R.drawable.image_border);
		shape.setColor(Integer.parseInt(textColor));
		mImage.setBackgroundDrawable(shape);

		caller = (ImageView) findViewById(R.id.mycontact_call);
		caller.setOnClickListener(callListener);

		smser = (ImageView) findViewById(R.id.mycontact_sms);
		smser.setOnClickListener(smsListener);

		emailer = (ImageView) findViewById(R.id.mycontact_email);
		emailer.setOnClickListener(emailListener);

		moreStuff = (ImageView) findViewById(R.id.mycontact_more);
		moreStuff.setOnClickListener(moreListener);

		// Button delete = (Button) findViewById(R.id.mycard_delete);
		// delete.setOnClickListener(deleteListener);
		// delete.setBackgroundDrawable(this.getResources().getDrawable(
		// R.drawable.buttongradient));
		// delete.setText("Delete");
		//
		// accept = (Button) findViewById(R.id.mycard_accept);
		updateLayout = (LinearLayout) findViewById(R.id.change_category);
		// update = (Button) findViewById(R.id.mycard_update);
		// update.setBackgroundDrawable(this.getResources().getDrawable(
		// R.drawable.buttongradient));
		save = (Button) findViewById(R.id.update_confirm);

		save.setOnClickListener(saveListener);
		//
		categorySpinner = (Spinner) findViewById(R.id.spinner_categories);
		ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter
				.createFromResource(this, R.array.category_array,
						android.R.layout.simple_spinner_item);
		adapterCategory
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapterCategory);
		categorySpinner.setSelection(adapterCategory.getPosition(categories
				.get(categoryId)));
		categorySpinner.setOnItemSelectedListener(categoriesListener);
		//
		// update.setOnClickListener(updateListener);

		// if(mCursor == null)
		// this.setTitle("Not saved");
	}

	private View.OnClickListener updateListener = new View.OnClickListener() {

		public void onClick(View v) {

			categorySpinner.setVisibility(View.VISIBLE);
			save.setVisibility(View.VISIBLE);
			updateLayout.setVisibility(View.VISIBLE);

		}
	};

	private View.OnClickListener callListener = new View.OnClickListener() {

		public void onClick(View v) {

			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + cellNum));
			startActivity(callIntent);

		}
	};

	private View.OnClickListener smsListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setData(Uri.parse("smsto:" + cellNum));
			startActivity(smsIntent);

		}
	};

	private View.OnClickListener emailListener = new View.OnClickListener() {

		public void onClick(View v) {

			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));

		}
	};

	private View.OnClickListener moreListener = new View.OnClickListener() {

		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ContactActivity.this);
			builder.setCancelable(true);
			builder.setTitle("Options");
			builder.setItems(R.array.contact_more_array,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								categorySpinner.setVisibility(View.VISIBLE);
								save.setVisibility(View.VISIBLE);
								updateLayout.setVisibility(View.VISIBLE);
							} else if (which == 1) {

								showFinalAlert("Delete contact?");

							} else if (which == 2) {
								ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.RawContacts.CONTENT_URI)
										.withValue(
												ContactsContract.RawContacts.ACCOUNT_TYPE,
												null)
										.withValue(
												ContactsContract.RawContacts.ACCOUNT_NAME,
												null).build());

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
												fName).build());

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
												mInitial).build());

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
												lName).build());

								if (mInitial.equals("")) {
									ops.add(ContentProviderOperation
											.newInsert(
													ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(
													ContactsContract.Data.RAW_CONTACT_ID,
													0)
											.withValue(
													ContactsContract.Data.MIMETYPE,
													ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
											.withValue(
													ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
													fName + " " + lName)
											.build());
								} else {
									ops.add(ContentProviderOperation
											.newInsert(
													ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(
													ContactsContract.Data.RAW_CONTACT_ID,
													0)
											.withValue(
													ContactsContract.Data.MIMETYPE,
													ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
											.withValue(
													ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
													fName + " " + mInitial
															+ ". " + lName)
											.build());
								}

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												cellNum)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
										.build());

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												homeNum)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
										.build());

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
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
											.newInsert(
													ContactsContract.Data.CONTENT_URI)
											.withValueBackReference(
													ContactsContract.Data.RAW_CONTACT_ID,
													0)
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

								String filePath = ImgUtil.makePixFilename(
										String.valueOf(serverId), ".jpg");

								ImgUtil.saveBufferToPhone(ContactActivity.this,
										imageBytes, filePath);

								ContentValues values = new ContentValues();
								values.put(MediaStore.Images.Media.MIME_TYPE,
										"image/jpeg");
								values.put(MediaStore.Images.Media.SIZE,
										imageBytes.length);
								values.put(MediaStore.Images.Media.TITLE,
										serverId);
								values.put(MediaStore.Images.Media.DATA,
										filePath);
								values.put(MediaStore.Images.Media.DESCRIPTION,
										"Image downloaded by NameCard");
								Date now = new Date();
								long time = now.getTime() / 1000;
								values.put(
										MediaStore.MediaColumns.DATE_MODIFIED,
										time);
								Uri mImgUri = getContentResolver()
										.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
												values);
								if (filePath != null) {
									new SingleMediaScanner(
											ContactActivity.this, filePath);
								}

								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Photo.PHOTO,
												imageBytes).build());

								try {
									ContactActivity.this.getContentResolver()
											.applyBatch(
													ContactsContract.AUTHORITY,
													ops);
									Toast.makeText(ContactActivity.this,
											"Contact successfully added",
											Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(ContactActivity.this,
											"Exception: " + e.getMessage(),
											Toast.LENGTH_SHORT).show();
								}
							} else {
								VCard card = new VCard(fName, "VCard", lName,
										email, cellNum, homeNum,
										ContactActivity.this, serverId);

								File f = null;

								try {
									f = card.writeVCard();
									Toast.makeText(ContactActivity.this,
											"Saved to SD Card",
											Toast.LENGTH_SHORT).show();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					});
			builder.create();
			builder.show();

		}
	};

	private View.OnClickListener deleteListener = new View.OnClickListener() {

		public void onClick(View v) {
			showFinalAlert("Delete contact?");
		}

	};

	public OnClickListener saveListener = new OnClickListener() {
		public void onClick(View view) {
			Log.v("MOVING CONTACT", "CATEGORY ID SET TO: " + categoryId);
			categoryTask.doSend(user_id, categoryId, serverId);
			categoryTask = new CategoryTask(ContactActivity.this);
			save.setVisibility(View.GONE);
			categorySpinner.setVisibility(View.GONE);
			updateLayout.setVisibility(View.GONE);
		}
	};

	// public OnClickListener confirmListener = new OnClickListener() {
	// public void onClick(View view) {
	// Log.v("MOVING CONTACT", "CATEGORY ID SET TO: " + categoryId);
	// categoryTask.doSend(user_id, categoryId, serverId);
	// categoryTask = new CategoryTask(ContactActivity.this);
	// acceptTask.doAccept(user_id, serverId);
	// if (isChecked && isNotPhoneContact()) {
	// Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
	// // Intent intent = new Intent(Intent.ACTION_INSERT);
	// intent.setType("vnd.android.cursor.item/contact");
	// intent.putExtra(ContactsContract.Intents.Insert.PHONE, cellNum);
	// intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
	// ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
	// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	// startActivity(intent);
	// }
	// setResult(1, null);
	// finish();
	// }
	// };

	private AdapterView.OnItemSelectedListener categoriesListener = new AdapterView.OnItemSelectedListener() {

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

	// private View.OnClickListener checkBoxListener = new
	// View.OnClickListener() {
	//
	// public void onClick(View v) {
	// isChecked = !isChecked;
	// }
	// };

	private View.OnClickListener acceptListener = new View.OnClickListener() {

		public void onClick(View v) {
			// categorySpinner.setVisibility(View.VISIBLE);
			// save.setVisibility(View.VISIBLE);
			// updateLayout.setVisibility(View.VISIBLE);
			acceptTask.doAccept(user_id, serverId);
			acceptTask = new AcceptTask(ContactActivity.this);

			if (isFromNewContacts)
				finish();
			// new AlertDialog.Builder(ContactActivity.this)
			// .setTitle("Notification")
			// .setMessage("Contact will be added to your contact list?")
			// .setPositiveButton(R.string.alert_ok_button,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// }
			// })
			// .setNegativeButton("Cancel",
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			//
			// }
			// }).setCancelable(true).show();
		}

	};

	/**
	 * Displays an alert message.
	 * 
	 * @param message
	 */
	private void showFinalAlert(CharSequence message) {
		new AlertDialog.Builder(ContactActivity.this)
				.setTitle("Confirmation")
				.setMessage(message)
				.setPositiveButton(R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								removeTask.doRemove(user_id, serverId);
								Intent intent = new Intent();
								intent.putExtra("position", listPosition);
								setResult(1, intent);
								finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).setCancelable(true).show();
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

}
