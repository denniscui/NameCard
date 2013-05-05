package com.linkedcontact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyCardActivity extends Activity {

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_address";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_CONTACT_PICKER = 3;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBtAdapter = null;
	// Member object for the chat services
	private BluetoothService mBService = null;

	private String fName;
	private String lName;
	private String mInitial;
	private String cellNum;
	private String homeNum;
	private String faxNum;
	private String company;
	private String title;
	private String address;
	private String email;
	private String website;
	private String note;
	private Uri image = null;
	private String textFont = "sans";
	private String textColor = String.valueOf(-16777216);
	private String bgColor = String.valueOf(-65794);
	private LinearLayout layout;
	private long serverId;

	private int socialType1;
	private String socialUrl1;
	private int socialType2;
	private String socialUrl2;
	private int socialType3;
	private String socialUrl3;
	private int socialType4;
	private String socialUrl4;

	private boolean mIsLogin = false;
	public static String cookieTime = "";

	public static final int SEND_CODE = 1;
	private int categoryId;

	private ImgUtil imageUtilities;
	private String user_id;
	private String hash_code;
	private DeleteTask deleteTask;
	private LinearLayout buttonLayout;
	private int lastCategoryList;

	public final static String[] textFonts = { "sans", "monospace", "serif" };

	public final static HashMap<String, Typeface> fontMap = new HashMap<String, Typeface>();
	static {
		fontMap.put("sans", Typeface.SANS_SERIF);
		fontMap.put("monospace", Typeface.MONOSPACE);
		fontMap.put("serif", Typeface.SERIF);
	}

	private ImageView socialImage1;
	private ImageView socialImage2;
	private ImageView socialImage3;
	private ImageView socialImage4;

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
	private GradientDrawable shape;

	private EditText receiver;
	private Button sendConfirm;
	private RelativeLayout sendPopup;
	private ImageView img;

	private Button send;

	private DbAdapter db;
	private Intent intent;

	private int contactId;
	private SendTask sendTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mycard);

		checkLogin();
		deleteTask = new DeleteTask(this);

		db = new DbAdapter(this);
		imageUtilities = new ImgUtil(this);

		intent = getIntent();
		Bundle bundle = intent.getExtras();

		Cursor mCursor;
		contactId = bundle.getInt("contactId");
		lastCategoryList = bundle.getInt("categoryId");
		mCursor = db.fetchContact(contactId);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		sendTask = new SendTask(this);

		buttonLayout = (LinearLayout) findViewById(R.id.mycard_button_layout);

		receiver = (EditText) findViewById(R.id.receiver_number);
		sendConfirm = (Button) findViewById(R.id.receiver_confirm);
		sendPopup = (RelativeLayout) findViewById(R.id.send_popup);
		img = (ImageView) findViewById(R.id.contact_image);

		img.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MyCardActivity.this,
						ContactPicker.class);
				startActivityForResult(intent, REQUEST_CONTACT_PICKER);
			}
		});

		sendConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String[] receiverNumbers = receiver.getText().toString()
						.split(",");

				for (int i = 0; i < receiverNumbers.length; i++) {
					String receiverNumber = PhoneNumUtil
							.processNumber(receiverNumbers[i]);
					receiverNumbers[i] = receiverNumber;

					if (!PhoneNumberUtils.isGlobalPhoneNumber(receiverNumber)
							|| !isValidPhoneNum(receiverNumber)) {
						String message = "Invalid Number. Please double-check. Separate numbers with commas. \n Ex: 5553332211";
						showAlert(message);
						return;
					}
				}

				String receiverNums = "";
				for (String s : receiverNumbers) {
					receiverNums = receiverNums + s.trim() + ",";
				}

				sendTask.doSend(
						receiverNums.substring(0, receiverNums.length() - 1),
						user_id, categoryId, serverId);
				sendTask = new SendTask(MyCardActivity.this);
				receiver.setVisibility(View.GONE);
				sendConfirm.setVisibility(View.GONE);
				sendPopup.setVisibility(View.GONE);
				img.setVisibility(View.GONE);
			}

		});

		int dataIndex = mCursor
				.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_FNAME);
		fName = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_LNAME);
		lName = mCursor.getString(dataIndex);

		dataIndex = mCursor
				.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_MINITIAL);
		mInitial = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_CELL_PHONE);
		cellNum = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_HOME_PHONE);
		homeNum = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_FAX);
		faxNum = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_ORGANIZATION);
		company = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_TITLE);
		title = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_ADDRESS);
		address = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_EMAIL);
		email = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_WEB_URL);
		website = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_NOTE);
		note = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_IMAGE);
		if (!mCursor.getString(dataIndex).equals(""))
			image = Uri.parse(mCursor.getString(dataIndex));

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_CATEGORY);
		categoryId = Integer.parseInt(mCursor.getString(dataIndex));

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_TEXT_FONT);
		textFont = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_TEXT_COLOR);
		textColor = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_BGCOLOR);
		bgColor = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SERVER_ID);
		serverId = mCursor.getLong(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE1);
		socialType1 = mCursor.getInt(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL2);
		socialUrl2 = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE2);
		socialType2 = mCursor.getInt(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL2);
		socialUrl2 = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE3);
		socialType3 = mCursor.getInt(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL3);
		socialUrl3 = mCursor.getString(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE4);
		socialType4 = mCursor.getInt(dataIndex);

		dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL4);
		socialUrl4 = mCursor.getString(dataIndex);

		socialImage1 = (ImageView) findViewById(R.id.social_image1);
		socialImage1.setOnClickListener(socialImageListener1);
		switch (socialType1) {
		case 0:
			socialImage1.setVisibility(View.GONE);
			break;
		case 1:
			socialImage1.setBackgroundResource(R.drawable.facebook);
			break;
		case 2:
			socialImage1.setBackgroundResource(R.drawable.twitter);
			break;
		case 3:
			socialImage1.setBackgroundResource(R.drawable.gplus);
			break;
		case 4:
			socialImage1.setBackgroundResource(R.drawable.linkedin);
			break;
		}

		socialImage2 = (ImageView) findViewById(R.id.social_image2);
		socialImage2.setOnClickListener(socialImageListener2);
		switch (socialType2) {
		case 0:
			socialImage2.setVisibility(View.GONE);
			break;
		case 1:
			socialImage2.setBackgroundResource(R.drawable.facebook);
			break;
		case 2:
			socialImage2.setBackgroundResource(R.drawable.twitter);
			break;
		case 3:
			socialImage2.setBackgroundResource(R.drawable.gplus);
			break;
		case 4:
			socialImage2.setBackgroundResource(R.drawable.linkedin);
			break;
		}

		socialImage3 = (ImageView) findViewById(R.id.social_image3);
		socialImage3.setOnClickListener(socialImageListener3);
		switch (socialType3) {
		case 0:
			socialImage3.setVisibility(View.GONE);
			break;
		case 1:
			socialImage3.setBackgroundResource(R.drawable.facebook);
			break;
		case 2:
			socialImage3.setBackgroundResource(R.drawable.twitter);
			break;
		case 3:
			socialImage3.setBackgroundResource(R.drawable.gplus);
			break;
		case 4:
			socialImage3.setBackgroundResource(R.drawable.linkedin);
			break;
		}

		socialImage4 = (ImageView) findViewById(R.id.social_image4);
		socialImage4.setOnClickListener(socialImageListener4);
		switch (socialType4) {
		case 0:
			socialImage4.setVisibility(View.GONE);
			break;
		case 1:
			socialImage4.setBackgroundResource(R.drawable.facebook);
			break;
		case 2:
			socialImage4.setBackgroundResource(R.drawable.twitter);
			break;
		case 3:
			socialImage4.setBackgroundResource(R.drawable.gplus);
			break;
		case 4:
			socialImage4.setBackgroundResource(R.drawable.linkedin);
			break;
		}

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

		if (image == null)
			mImage.setImageResource(R.drawable.defaulticon);
		else {
			byte[] bm = imageUtilities.getResizedImageData(image, 320, 320);
			Bitmap bmp = BitmapFactory.decodeByteArray(bm, 0, bm.length);
			mImage.setImageBitmap(bmp);
		}

		if (mInitial.equals(""))
			mName.setText(fName + " " + lName);
		else
			mName.setText(fName + " " + mInitial + ". " + lName);

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
		shape = (GradientDrawable) getResources().getDrawable(
				R.drawable.image_border);
		shape.setColor(Integer.parseInt(textColor));
		mImage.setBackgroundDrawable(shape);

		mName.setTypeface(Typeface.DEFAULT_BOLD);
		mCompanyName.setTypeface(Typeface.DEFAULT_BOLD);

		mCursor.close();
		db.close();

		Button edit = (Button) findViewById(R.id.mycard_edit);
		edit.setOnClickListener(editListener);
		edit.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));

		Button delete = (Button) findViewById(R.id.mycard_delete);
		delete.setOnClickListener(deleteListener);
		delete.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));

		send = (Button) findViewById(R.id.mycard_send);
		send.setOnClickListener(sendListener);
		send.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));

		// if(mCursor == null)
		// this.setTitle("Not saved");
	}

	public static boolean isValidPhoneNum(String phoneNumber) {
		boolean isValid = false;

		if (phoneNumber.length() < 10)
			return false;

		String expression = "[0-9]*";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * This runs after startActivityForResult - changes the image.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				Intent bIntent = new Intent(MyCardActivity.this,
						BluetoothListActivity.class);// Send
														// it
														// to
														// PC
				VCard card = new VCard(fName, "VCard", lName, email, cellNum,
						homeNum, MyCardActivity.this, contactId);

				bIntent.putExtra("bytes", card.toByteArray());

				startActivityForResult(bIntent, 5);
			} else {
				new AlertDialog.Builder(MyCardActivity.this)
						.setTitle("Error")
						.setMessage("Unable to start Bluetooth.")
						.setPositiveButton(R.string.alert_ok_button,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// finish();
									}
								}).setCancelable(false).show();
			}
		} else if (requestCode == REQUEST_CONNECT_DEVICE) {
			if (resultCode == RESULT_OK && data != null) {
				String address = data.getStringExtra(DEVICE_NAME);
				BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
				mBService.connect(device);
				Log.v("State", "" + mBService.getState());

				// // Check that we're actually connected before trying anything
				// if (mBService.getState() != BluetoothService.STATE_CONNECTED)
				// {
				// Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT)
				// .show();
				// return;
				// }

				// VCard card = new VCard(fName, "VCard", lName, email, cellNum,
				// homeNum, MyCardActivity.this, contactId);
				//
				// sendVcf(card);
			}
		} else if (requestCode == REQUEST_CONTACT_PICKER
				&& resultCode == RESULT_OK && data != null) {
			Bundle bundle = data.getExtras();
			String str = bundle.getString("numbers");
			receiver.setText(str);
		}
	}

	private View.OnClickListener editListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent currentIntent = new Intent(MyCardActivity.this,
					Form1Activity.class);
			currentIntent.putExtra("contactId", contactId);
			startActivity(currentIntent);
			finish();
		}

	};

	private View.OnClickListener socialImageListener1 = new View.OnClickListener() {

		public void onClick(View v) {
			if (!socialUrl1.startsWith("https://")
					&& !socialUrl1.startsWith("http://")) {
				socialUrl1 = "http://" + socialUrl1;
			}
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(socialUrl1));
			startActivity(browserIntent);
		}

	};

	private View.OnClickListener socialImageListener2 = new View.OnClickListener() {

		public void onClick(View v) {
			if (!socialUrl2.startsWith("https://")
					&& !socialUrl2.startsWith("http://")) {
				socialUrl2 = "http://" + socialUrl2;
			}
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(socialUrl2));
			startActivity(browserIntent);
		}

	};

	private View.OnClickListener socialImageListener3 = new View.OnClickListener() {

		public void onClick(View v) {
			if (!socialUrl3.startsWith("https://")
					&& !socialUrl3.startsWith("http://")) {
				socialUrl3 = "http://" + socialUrl3;
			}
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(socialUrl3));
			startActivity(browserIntent);
		}

	};

	private View.OnClickListener socialImageListener4 = new View.OnClickListener() {

		public void onClick(View v) {
			if (!socialUrl4.startsWith("https://")
					&& !socialUrl4.startsWith("http://")) {
				socialUrl4 = "http://" + socialUrl4;
			}
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(socialUrl4));
			startActivity(browserIntent);
		}

	};

	private View.OnClickListener sendListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (mIsLogin && serverId != 0) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						MyCardActivity.this);
				builder.setCancelable(true);
				builder.setTitle("Options");
				builder.setItems(R.array.share_array,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									receiver.setVisibility(View.VISIBLE);
									sendConfirm.setVisibility(View.VISIBLE);
									sendPopup.setVisibility(View.VISIBLE);
									img.setVisibility(View.VISIBLE);
								} else if (which == 1) {
									VCard card = new VCard(fName, "VCard",
											lName, email, cellNum, homeNum,
											MyCardActivity.this, contactId);

									File f = null;

									try {
										f = card.writeVCard();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									Intent emailIntent = new Intent(
											android.content.Intent.ACTION_SEND);
									emailIntent.setType("*/*");
									emailIntent.putExtra(Intent.EXTRA_STREAM,
											Uri.fromFile(f));

									String body = "";
									body += fName;
									if (mInitial.equals(""))
										body += " " + mInitial + ". " + lName
												+ "\n";
									else
										body += " " + lName + "\n";

									body += cellNum + "\n";
									if (!email.equals(""))
										body += email + "\n";

									body += "<NameCard> http://linkedcontact.com/card.php?s="
											+ serverId + "\n\n";

									body += "Card created using NameCard: linkedcontact.com";
									emailIntent.putExtra(Intent.EXTRA_TEXT,
											body);

									startActivity(emailIntent);
								} else {

									// If the adapter is null, then Bluetooth is
									// not supported
									if (mBtAdapter == null) {
										Toast.makeText(MyCardActivity.this,
												"Bluetooth is not available",
												Toast.LENGTH_LONG).show();
										return;
									}

									if (!mBtAdapter.isEnabled()) {
										Intent enableBtIntent = new Intent(
												BluetoothAdapter.ACTION_REQUEST_ENABLE);
										startActivityForResult(enableBtIntent,
												REQUEST_ENABLE_BT);
									} else {
										mBService = new BluetoothService(
												MyCardActivity.this, mHandler);

										Log.v("State",
												"" + mBService.getState());

										Intent bIntent = new Intent(
												MyCardActivity.this,
												BluetoothListActivity.class);// Send
																				// it
																				// to
																				// PC
										startActivityForResult(bIntent,
												REQUEST_CONNECT_DEVICE);
									}
								}
							}
						});
				builder.create();
				builder.show();

				// Log.v("SERVERID", "SENT SERVER ID IS: " + serverId);
				// SendQuickAction _qact = new SendQuickAction(send, user_id,
				// categoryId, serverId);
				// _qact.show();

			} else {
				if (mIsLogin && serverId == 0) {
					new AlertDialog.Builder(MyCardActivity.this)
							.setTitle("Sending Error")
							.setMessage(
									"Please re-save this card before sending.")
							.setPositiveButton(R.string.alert_ok_button,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// finish();
										}
									}).setCancelable(false).show();
				} else {
					Intent currentIntent = new Intent(MyCardActivity.this,
							LoginActivity.class);
					startActivity(currentIntent);
				}
			}

		}
	};

	private View.OnClickListener deleteListener = new View.OnClickListener() {

		public void onClick(View v) {
			showFinalAlert("Delete card?");
		}

	};

	private void showAlert(CharSequence message) {
		new AlertDialog.Builder(MyCardActivity.this)
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

	/**
	 * Displays an alert message.
	 * 
	 * @param message
	 */
	private void showFinalAlert(CharSequence message) {
		new AlertDialog.Builder(MyCardActivity.this)
				.setTitle("Confirmation")
				.setMessage(message)
				.setPositiveButton(R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								db.deleteContact(contactId);
								if (mIsLogin && serverId != 0) {
									deleteTask.doDelete(user_id, serverId);
								}
								if (db.fetchAllContacts() == null) {
									Intent currentIntent = new Intent(
											MyCardActivity.this,
											TitlePageActivity.class);
									startActivity(currentIntent);
									finish();
								} else {
									Intent currentIntent = new Intent(
											MyCardActivity.this,
											MyCardsListActivity.class);
									startActivity(currentIntent);
									finish();
								}
								db.close();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).setCancelable(true).show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent currentIntent = new Intent(MyCardActivity.this,
					MyCardsListActivity.class);
			currentIntent.putExtra("categoryId", lastCategoryList);
			startActivity(currentIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
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

		shape = (GradientDrawable) getResources().getDrawable(
				R.drawable.image_border);
		shape.setColor(Integer.parseInt(textColor));

		if (mBService != null) {
			// Only if the state is STATE_NONE,
			// do we know that we haven't
			// started already
			if (mBService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat
				// services
				int i = mBService.getState();
				mBService.start();
				Log.v("State onResume", "" + mBService.getState());
			}
		}

		super.onResume();
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					Toast.makeText(getApplicationContext(), "Connected",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothService.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(), "Connecting",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothService.STATE_LISTEN:
					Toast.makeText(getApplicationContext(),
							"Listening for connection", Toast.LENGTH_SHORT)
							.show();
					break;
				case BluetoothService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendVcf(VCard card) {
		// Check that we're actually connected before trying anything
		if (mBService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		// if (message.length() > 0) {
		// // Get the message bytes and tell the BluetoothChatService to write
		// byte[] send = message.getBytes();
		// mBService.write(send);
		//
		// // Reset out string buffer to zero and clear the edit text field
		// // mOutStringBuffer.setLength(0);
		// // mOutEditText.setText(mOutStringBuffer);
		// }
	}
}
