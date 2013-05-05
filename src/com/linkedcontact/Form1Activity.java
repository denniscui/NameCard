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
import java.util.HashMap;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Form1Activity extends Activity {

	public static final String CREATE_URL = "http://mgm.funformobile.com/nc/createCard.php";
	public static final String UPDATE_URL = "http://mgm.funformobile.com/nc/updateCard.php";

	private Intent intent;
	private int contactId = -1;

	private UploadTask uploadTask;

	private Drawable oldBackground;

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
	private String socialUrl1;
	private String socialUrl2;
	private String socialUrl3;
	private String socialUrl4;
	private int categoryId = 0;
	private Uri image = null;
	private long serverId;

	private EditText mFName;
	private EditText mLName;
	private EditText mMInitial;
	private EditText mCellPhoneNum;
	private EditText mHomePhoneNum;
	private EditText mCompanyName;
	private EditText mTitle;
	private EditText mAddress;
	private EditText mFaxNum;
	private EditText mEmail;
	private EditText mWebURL;
	private EditText mNote;
	private EditText mSocial1;
	private EditText mSocial2;
	private EditText mSocial3;
	private EditText mSocial4;
	private FrameLayout bgColorFrame;
	private FrameLayout textColorFrame;

	private LinearLayout socialLayout1;
	private RelativeLayout socialLayout2;
	private RelativeLayout socialLayout3;
	private RelativeLayout socialLayout4;

	private RelativeLayout addSocialLayout;

	private ImageView mImage;

	private Button social1;
	private Button social2;
	private Button social3;
	private Button social4;
	private Button socialPlus;

	private Button socialMinus1;
	private Button socialMinus2;
	private Button socialMinus3;
	private Button socialMinus4;

	Spinner spinnerCategory;
	Spinner spinnerTextFont;

	private int socialType1 = 0;
	private int socialType2 = 0;
	private int socialType3 = 0;
	private int socialType4 = 0;
	private int mLayoutId;
	private String textFont = "sans";
	private String textColor = String.valueOf(-16777216);
	private String bgColor = String.valueOf(-65794);

	private static int textColorSelected = 0;
	private static int bgColorSelected = 0;

	private DbAdapter db;
	private ImgUtil imageUtilities;
	private Hashtable<String, String> parameters;
	private Hashtable<String, String> updateParams;
	private ProgressDialog mProgressDialog;

	private boolean mIsLogin = false;
	String user_id;
	String hash_code;

	public static final int PICKPIC_FROM_ALBUM = 1;

	public final static String[] textFonts = { "sans", "monospace", "serif" };

	public final static HashMap<String, Typeface> fontMap = new HashMap<String, Typeface>();
	static {
		fontMap.put("sans", Typeface.SANS_SERIF);
		fontMap.put("monospace", Typeface.MONOSPACE);
		fontMap.put("serif", Typeface.SERIF);
	}

	public final static String[] socialNetworks = { "facebook", "twitter",
			"gplus", "linkedin" };

	public HashMap<String, Drawable> socialMap = new HashMap<String, Drawable>();

	public final static String[] bgcolornames = { "snow", "aqua", "lime",
			"pine", "sky", "cream", "gold", "olive", "silver", "orchid",
			"barbie", "coral", };

	public final static HashMap<String, Integer> bgMainColors = new HashMap<String, Integer>();
	static {
		bgMainColors.put("sky", 0xFFC0F0F0);
		bgMainColors.put("aqua", 0xFF9DD5FD);
		bgMainColors.put("lime", 0xFFB8F8A8);
		bgMainColors.put("pine", 0xFF90CF88);

		bgMainColors.put("snow", 0xFFFEFEFE);
		bgMainColors.put("cream", 0xFFF8FDD0);
		bgMainColors.put("gold", 0xFFF3EDAF);
		bgMainColors.put("olive", 0xFFDDC88D);

		bgMainColors.put("silver", 0xFFE0E0E0);
		bgMainColors.put("orchid", 0xFFFBEAF2);
		bgMainColors.put("barbie", 0xFFE4D6E3);
		bgMainColors.put("coral", 0xFFF8C0B3);
	}

	public final static String[] textcolornames = { "night", "sea", "slate",
			"fire", "plum", "cyan", "envy", "khaki", "choco", "royal", "brick",
			"dawn", };

	public final static HashMap<String, Integer> textMainColors = new HashMap<String, Integer>();
	static {
		textMainColors.put("night", 0xFF000000);
		textMainColors.put("sea", 0xFF151B54);
		textMainColors.put("slate", 0xFF342826);
		textMainColors.put("brick", 0xFF800517);

		textMainColors.put("plum", 0xFF571B7e);
		textMainColors.put("cyan", 0xFF307D7E);
		textMainColors.put("envy", 0xFF254117);
		textMainColors.put("khaki", 0xFF827839);

		textMainColors.put("choco", 0xFF7E3517);
		textMainColors.put("royal", 0xFF151B8D);
		textMainColors.put("fire", 0xFFC11B17);
		textMainColors.put("dawn", 0xFFC35617);
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
		categories.put(8, "High School");
		categories.put(9, "Personal");
		categories.put(10, "Relatives");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.form1);

		TextView titleLabel = (TextView) findViewById(R.id.listTitle);
		titleLabel.setText("Create/Update a Card");

		checkLogin();

		db = new DbAdapter(this);
		imageUtilities = new ImgUtil(this);
		uploadTask = new UploadTask(this);

		socialPlus = (Button) findViewById(R.id.add_social);
		socialPlus.setOnClickListener(addSocialListener);

		social1 = (Button) findViewById(R.id.social1);
		social1.setOnClickListener(socialSpinner);

		social2 = (Button) findViewById(R.id.social2);
		social2.setOnClickListener(socialSpinner);

		social3 = (Button) findViewById(R.id.social3);
		social3.setOnClickListener(socialSpinner);

		social4 = (Button) findViewById(R.id.social4);
		social4.setOnClickListener(socialSpinner);

		oldBackground = social1.getBackground();
		socialMap.put("facebook",
				getResources().getDrawable(R.drawable.facebook));
		socialMap
				.put("twitter", getResources().getDrawable(R.drawable.twitter));
		socialMap.put("gplus", getResources().getDrawable(R.drawable.gplus));
		socialMap.put("linkedin",
				getResources().getDrawable(R.drawable.linkedin));

		mSocial1 = (EditText) findViewById(R.id.social_url1);
		mSocial2 = (EditText) findViewById(R.id.social_url2);
		mSocial3 = (EditText) findViewById(R.id.social_url3);
		mSocial4 = (EditText) findViewById(R.id.social_url4);

		socialMinus2 = (Button) findViewById(R.id.minus_social2);
		socialMinus3 = (Button) findViewById(R.id.minus_social3);
		socialMinus4 = (Button) findViewById(R.id.minus_social4);

		socialMinus2.setOnClickListener(minusListener);
		socialMinus3.setOnClickListener(minusListener);
		socialMinus4.setOnClickListener(minusListener);

		socialLayout1 = (LinearLayout) findViewById(R.id.social_layout1);
		socialLayout2 = (RelativeLayout) findViewById(R.id.social_layout2);
		socialLayout3 = (RelativeLayout) findViewById(R.id.social_layout3);
		socialLayout4 = (RelativeLayout) findViewById(R.id.social_layout4);
		addSocialLayout = (RelativeLayout) findViewById(R.id.add_social_layout);

		mImage = (ImageView) findViewById(R.id.createform1_image);
		mFName = (EditText) findViewById(R.id.createform1_fName);
		mLName = (EditText) findViewById(R.id.createform1_lName);
		mMInitial = (EditText) findViewById(R.id.createform1_mInitial);
		mCellPhoneNum = (EditText) findViewById(R.id.createform1_cellphonenumber);
		mHomePhoneNum = (EditText) findViewById(R.id.createform1_homephonenumber);
		mFaxNum = (EditText) findViewById(R.id.createform1_faxnumber);
		mCompanyName = (EditText) findViewById(R.id.createform1_companyname);
		mTitle = (EditText) findViewById(R.id.createform1_title);
		mAddress = (EditText) findViewById(R.id.createform1_address1);
		mEmail = (EditText) findViewById(R.id.createform1_email);
		mWebURL = (EditText) findViewById(R.id.createform1_websiteURL);
		mNote = (EditText) findViewById(R.id.createform1_note);
		bgColorFrame = (FrameLayout) findViewById(R.id.bgcolor_layout);
		textColorFrame = (FrameLayout) findViewById(R.id.textcolor_layout);

		intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			titleLabel.setText("Update Card");
			contactId = bundle.getInt("contactId");

			Cursor mCursor = db.fetchContact(contactId);

			int dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_FNAME);
			fName = mCursor.getString(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_LNAME);
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

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_ORGANIZATION);
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
			categoryId = mCursor.getInt(dataIndex);

			Log.v("categoryId", "The contactId in DB is: " + contactId);

			dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_TEXT_FONT);
			textFont = mCursor.getString(dataIndex);

			dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_TEXT_COLOR);
			textColor = mCursor.getString(dataIndex);

			for (int i = 0; i < textcolornames.length; i++) {
				if (textColor
						.equals(textMainColors.get(textcolornames[i]) + "")) {
					textColorSelected = i;
					break;
				}
			}

			dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_BGCOLOR);
			bgColor = mCursor.getString(dataIndex);

			for (int i = 0; i < bgcolornames.length; i++) {
				if (bgColor.equals(bgMainColors.get(bgcolornames[i]) + "")) {
					bgColorSelected = i;
					break;
				}
			}

			dataIndex = mCursor.getColumnIndexOrThrow(DbAdapter.KEY_SERVER_ID);
			serverId = mCursor.getLong(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE1);
			socialType1 = mCursor.getInt(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL1);
			socialUrl1 = mCursor.getString(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE2);
			socialType2 = mCursor.getInt(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL2);
			socialUrl2 = mCursor.getString(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE3);
			socialType3 = mCursor.getInt(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL3);
			socialUrl3 = mCursor.getString(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_TYPE4);
			socialType4 = mCursor.getInt(dataIndex);

			dataIndex = mCursor
					.getColumnIndexOrThrow(DbAdapter.KEY_SOCIAL_URL4);
			socialUrl4 = mCursor.getString(dataIndex);

			mCursor.close();
			db.close();

			for (String s : bgMainColors.keySet()) {
				if (bgMainColors.get(s) == (Integer.parseInt(bgColor))) {
					bgColorFrame.setBackgroundColor(Integer.parseInt(bgColor));
					break;
				}
			}

			for (String s : textMainColors.keySet()) {
				if (textMainColors.get(s) == (Integer.parseInt(textColor))) {
					textColorFrame.setBackgroundColor(Integer
							.parseInt(textColor));
					break;
				}
			}

			mFName.setTypeface(fontMap.get(textFont));
			mLName.setTypeface(fontMap.get(textFont));
			mMInitial.setTypeface(fontMap.get(textFont));
			mCellPhoneNum.setTypeface(fontMap.get(textFont));
			mHomePhoneNum.setTypeface(fontMap.get(textFont));
			mCompanyName.setTypeface(fontMap.get(textFont));
			mTitle.setTypeface(fontMap.get(textFont));
			mAddress.setTypeface(fontMap.get(textFont));
			mFaxNum.setTypeface(fontMap.get(textFont));
			mEmail.setTypeface(fontMap.get(textFont));
			mWebURL.setTypeface(fontMap.get(textFont));
			mNote.setTypeface(fontMap.get(textFont));
			mSocial1.setTypeface(fontMap.get(textFont));
			mSocial2.setTypeface(fontMap.get(textFont));
			mSocial3.setTypeface(fontMap.get(textFont));
			mSocial4.setTypeface(fontMap.get(textFont));

			if (image != null) {
				byte[] bm = imageUtilities.getResizedImageData(image, 320, 320);
				Bitmap bmp = BitmapFactory.decodeByteArray(bm, 0, bm.length);
				mImage.setImageBitmap(bmp);
			}

			switch (socialType1) {
			case 0:
				break;
			case 1:
				social1.setBackgroundResource(R.drawable.facebook);
				social1.setText("");
				break;
			case 2:
				social1.setBackgroundResource(R.drawable.twitter);
				social1.setText("");
				break;
			case 3:
				social1.setBackgroundResource(R.drawable.gplus);
				social1.setText("");
				break;
			case 4:
				social1.setBackgroundResource(R.drawable.linkedin);
				social1.setText("");
				break;
			}

			if (socialType1 != 0 && !socialUrl1.equals(""))
				mSocial1.setText(socialUrl1);

			switch (socialType2) {
			case 0:
				break;
			case 1:
				socialLayout2.setVisibility(View.VISIBLE);
				social2.setBackgroundResource(R.drawable.facebook);
				social2.setText("");
				break;
			case 2:
				socialLayout2.setVisibility(View.VISIBLE);
				social2.setBackgroundResource(R.drawable.twitter);
				social2.setText("");
				break;
			case 3:
				socialLayout2.setVisibility(View.VISIBLE);
				social2.setBackgroundResource(R.drawable.gplus);
				social2.setText("");
				break;
			case 4:
				socialLayout2.setVisibility(View.VISIBLE);
				social2.setBackgroundResource(R.drawable.linkedin);
				social2.setText("");
				break;
			}

			if (socialType2 != 0 && !socialUrl2.equals(""))
				mSocial2.setText(socialUrl2);

			switch (socialType3) {
			case 0:
				break;
			case 1:
				socialLayout3.setVisibility(View.VISIBLE);
				social3.setBackgroundResource(R.drawable.facebook);
				social3.setText("");
				break;
			case 2:
				socialLayout3.setVisibility(View.VISIBLE);
				social3.setBackgroundResource(R.drawable.twitter);
				social3.setText("");
				break;
			case 3:
				socialLayout3.setVisibility(View.VISIBLE);
				social3.setBackgroundResource(R.drawable.gplus);
				social3.setText("");
				break;
			case 4:
				socialLayout2.setVisibility(View.VISIBLE);
				social3.setBackgroundResource(R.drawable.linkedin);
				social3.setText("");
				break;
			}

			if (socialType3 != 0 && !socialUrl3.equals(""))
				mSocial3.setText(socialUrl3);

			switch (socialType4) {
			case 0:
				break;
			case 1:
				socialLayout4.setVisibility(View.VISIBLE);
				social4.setBackgroundResource(R.drawable.facebook);
				social4.setText("");
				break;
			case 2:
				socialLayout4.setVisibility(View.VISIBLE);
				social4.setBackgroundResource(R.drawable.twitter);
				social4.setText("");
				break;
			case 3:
				socialLayout4.setVisibility(View.VISIBLE);
				social4.setBackgroundResource(R.drawable.gplus);
				social4.setText("");
				break;
			case 4:
				socialLayout4.setVisibility(View.VISIBLE);
				social4.setBackgroundResource(R.drawable.linkedin);
				social4.setText("");
				break;
			}

			if (socialType4 != 0 && !socialUrl4.equals(""))
				mSocial4.setText(socialUrl4);

			if (socialLayout1.isShown() && socialLayout2.isShown()
					&& socialLayout3.isShown() && socialLayout4.isShown())
				addSocialLayout.setVisibility(View.GONE);

			mFName.setText(fName);

			if (!lName.equals(""))
				mLName.setText(lName);
			if (!mInitial.equals(""))
				mMInitial.setText(mInitial);

			mCellPhoneNum.setText(cellNum);

			if (!homeNum.equals(""))
				mHomePhoneNum.setText(homeNum);
			if (!faxNum.equals(""))
				mFaxNum.setText(faxNum);
			if (!company.equals(""))
				mCompanyName.setText(company);
			if (!title.equals(""))
				mTitle.setText(title);
			if (!address.equals(""))
				mAddress.setText(address);
			if (!email.equals(""))
				mEmail.setText(email);
			if (!website.equals(""))
				mWebURL.setText(website);
			if (!note.equals(""))
				mNote.setText(note);
		} else {
			titleLabel.setText("Create Card");
			bgColorFrame.setBackgroundColor(bgMainColors.get(bgcolornames[0]));
			textColorFrame.setBackgroundColor(textMainColors
					.get(textcolornames[0]));
		}

		Button clearAll = (Button) findViewById(R.id.createform1_clearFields);
		clearAll.setOnClickListener(clearListener);
		clearAll.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));
		Button save = (Button) findViewById(R.id.createform1_save);
		save.setOnClickListener(saveListener);
		save.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));

		spinnerCategory = (Spinner) findViewById(R.id.spinner_choosecategory);
		ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter
				.createFromResource(this, R.array.category_array,
						android.R.layout.simple_spinner_item);
		adapterCategory
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCategory.setAdapter(adapterCategory);
		spinnerCategory.setSelection(adapterCategory.getPosition(categories
				.get(categoryId)));
		spinnerCategory.setOnItemSelectedListener(categoriesListener);

		spinnerTextFont = (Spinner) findViewById(R.id.spinner_textfonts);
		ArrayAdapter<CharSequence> adapterTextFont = ArrayAdapter
				.createFromResource(this, R.array.textfont_array,
						android.R.layout.simple_spinner_item);
		adapterTextFont
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerTextFont.setAdapter(adapterTextFont);
		if (textFont.equals("sans"))
			spinnerTextFont.setSelection(adapterTextFont
					.getPosition("Droid Sans"));
		else if (textFont.equals("monospace"))
			spinnerTextFont.setSelection(adapterTextFont
					.getPosition("Droid Mono"));
		else
			spinnerTextFont.setSelection(adapterTextFont
					.getPosition("Droid Serif"));
		spinnerTextFont.setOnItemSelectedListener(textFontListener);

		bgColorFrame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				chooseBackgroundColor();
			}
		});

		textColorFrame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				chooseTextColor();
			}
		});

		/**
		 * Creates listener for the icon.
		 */
		mImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				innerIntent.setType("image/*");
				startActivityForResult(innerIntent, PICKPIC_FROM_ALBUM);
			}
		});
	}

	private View.OnClickListener clearListener = new View.OnClickListener() {

		public void onClick(View v) {
			mFName.setText("");
			mLName.setText("");
			mMInitial.setText("");
			mCellPhoneNum.setText("");
			mHomePhoneNum.setText("");
			mFaxNum.setText("");
			mCompanyName.setText("");
			mTitle.setText("");
			mAddress.setText("");
			mEmail.setText("");
			mWebURL.setText("");
			mNote.setText("");
			mImage.setImageResource(R.drawable.chooseanimage);
			categoryId = 0;
			textFont = "sans";
			textColor = String.valueOf(-16777216);
			bgColor = String.valueOf(-65794);
			image = null;
			social1.setBackgroundDrawable(oldBackground);
			social2.setBackgroundDrawable(oldBackground);
			social3.setBackgroundDrawable(oldBackground);
			social4.setBackgroundDrawable(oldBackground);
			social1.setText("Social");
			social2.setText("Social");
			social3.setText("Social");
			social4.setText("Social");
			socialType1 = 0;
			socialType2 = 0;
			socialType3 = 0;
			socialType4 = 0;
			mSocial1.setText("");
			mSocial2.setText("");
			mSocial3.setText("");
			mSocial4.setText("");
			socialLayout2.setVisibility(View.GONE);
			socialLayout3.setVisibility(View.GONE);
			socialLayout4.setVisibility(View.GONE);
			textFont = "sans";
			textColor = String.valueOf(-16777216);
			bgColor = String.valueOf(-65794);

			bgColorFrame.setBackgroundColor(bgMainColors.get("snow"));
			textColorFrame.setBackgroundColor(textMainColors.get("night"));
			textColorSelected = 0;
			bgColorSelected = 0;

			spinnerTextFont.setSelection(textColorSelected);
			spinnerCategory.setSelection(0);

			addSocialLayout.setVisibility(View.VISIBLE);
		}

	};

	private View.OnClickListener socialSpinner = new View.OnClickListener() {

		public void onClick(View v) {
			if (v.equals(social1))
				chooseSocialNetwork(1);
			else if (v.equals(social2))
				chooseSocialNetwork(2);
			else if (v.equals(social3))
				chooseSocialNetwork(3);
			else
				chooseSocialNetwork(4);
		}

	};

	private View.OnClickListener minusListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (v.equals(socialMinus2)) {
				socialLayout2.setVisibility(View.GONE);
				social2.setBackgroundDrawable(oldBackground);
				social2.setText("Social");
				mSocial2.setText("");
				socialType2 = 0;
			} else if (v.equals(socialMinus3)) {
				socialLayout3.setVisibility(View.GONE);
				social3.setBackgroundDrawable(oldBackground);
				social3.setText("Social");
				mSocial3.setText("");
				socialType3 = 0;
			} else if (v.equals(socialMinus4)) {
				socialLayout4.setVisibility(View.GONE);
				social4.setBackgroundDrawable(oldBackground);
				social4.setText("Social");
				mSocial4.setText("");
				socialType4 = 0;
			}

			addSocialLayout.setVisibility(View.VISIBLE);
		}

	};

	private View.OnClickListener addSocialListener = new View.OnClickListener() {

		public void onClick(View v) {
			if (!socialLayout2.isShown())
				socialLayout2.setVisibility(View.VISIBLE);
			else if (!socialLayout3.isShown())
				socialLayout3.setVisibility(View.VISIBLE);
			else if (!socialLayout4.isShown())
				socialLayout4.setVisibility(View.VISIBLE);

			if (socialLayout2.isShown() && socialLayout3.isShown()
					&& socialLayout4.isShown()) {
				addSocialLayout.setVisibility(View.GONE);
			}
		}

	};

	private View.OnClickListener saveListener = new View.OnClickListener() {

		public void onClick(View v) {
			String fName = mFName.getText().toString();
			if (fName.equals("")) {
				String message = "Please enter your first name";
				showFinalAlert(message);
				return;
			}
			String lName = mLName.getText().toString();
			if (lName.equals("")) {
				String message = "Please enter your last name";
				showFinalAlert(message);
				return;
			}
			String cellNum = (mCellPhoneNum.getText()).toString().trim();
			if (cellNum.equals("")) {
				String message = "Please enter a cell phone number";
				showFinalAlert(message);
				return;
			}
			String email = (mEmail.getText()).toString().trim();
			if (!email.equals("") && !isEmailValid(email)) {
				String message = "Please enter a valid email address";
				showFinalAlert(message);
				return;
			}
			Log.v("categoryId", "The category ID is: " + categoryId);
			db.open(true);

			String mImgUriString;
			if (image == null)
				mImgUriString = "";
			else
				mImgUriString = image.toString();

			if (contactId == -1) {

				contactId = db.createContact(
						mFName.getText().toString().trim(), mLName.getText()
								.toString().trim(), mMInitial.getText()
								.toString().trim(), mCellPhoneNum.getText()
								.toString().trim(), mHomePhoneNum.getText()
								.toString().trim(), mFaxNum.getText()
								.toString().trim(), mCompanyName.getText()
								.toString().trim(), mTitle.getText().toString()
								.trim(), mAddress.getText().toString().trim(),
						mEmail.getText().toString().trim(), mWebURL.getText()
								.toString().trim(), mImgUriString.trim(),
						categoryId, 0, textFont, textColor, bgColor, mNote
								.getText().toString(), socialType1, mSocial1
								.getText().toString().trim(), socialType2,
						mSocial2.getText().toString().trim(), socialType3,
						mSocial3.getText().toString().trim(), socialType4,
						mSocial4.getText().toString().trim());
			} else {
				db.updateContact(contactId, mFName.getText().toString().trim(),
						mLName.getText().toString().trim(), mMInitial.getText()
								.toString().trim(), mCellPhoneNum.getText()
								.toString().trim(), mHomePhoneNum.getText()
								.toString().trim(), mFaxNum.getText()
								.toString().trim(), mCompanyName.getText()
								.toString().trim(), mTitle.getText().toString()
								.trim(), mAddress.getText().toString().trim(),
						mEmail.getText().toString().trim(), mWebURL.getText()
								.toString().trim(), mImgUriString.trim(),
						categoryId, 0, textFont, textColor, bgColor, mNote
								.getText().toString().trim(), serverId,
						socialType1, mSocial1.getText().toString().trim(),
						socialType2, mSocial2.getText().toString().trim(),
						socialType3, mSocial3.getText().toString().trim(),
						socialType4, mSocial4.getText().toString().trim());
			}
			db.close();

			if (mIsLogin) {
				if (serverId == 0) {
					doCreate(contactId, user_id, mFName.getText().toString()
							.trim(), mLName.getText().toString().trim(),
							mMInitial.getText().toString().trim(),
							mCellPhoneNum.getText().toString().trim(),
							mHomePhoneNum.getText().toString().trim(), mFaxNum
									.getText().toString().trim(), mCompanyName
									.getText().toString().trim(), mTitle
									.getText().toString().trim(), mAddress
									.getText().toString().trim(), mEmail
									.getText().toString().trim(), mWebURL
									.getText().toString().trim(),
							mImgUriString.trim(), categoryId, 0, textFont,
							textColor, bgColor, mNote.getText().toString()
									.trim());
				} else {
					doUpdate(contactId, user_id, mFName.getText().toString()
							.trim(), mLName.getText().toString().trim(),
							mMInitial.getText().toString().trim(),
							mCellPhoneNum.getText().toString().trim(),
							mHomePhoneNum.getText().toString().trim(), mFaxNum
									.getText().toString().trim(), mCompanyName
									.getText().toString().trim(), mTitle
									.getText().toString().trim(), mAddress
									.getText().toString().trim(), mEmail
									.getText().toString().trim(), mWebURL
									.getText().toString().trim(),
							mImgUriString.trim(), categoryId, 0, textFont,
							textColor, bgColor, mNote.getText().toString()
									.trim(), serverId);
				}
			} else {
				Intent currentIntent = new Intent(Form1Activity.this,
						MyCardActivity.class);
				currentIntent.putExtra("contactId", contactId);
				startActivity(currentIntent);
				finish();
			}
		}

	};

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

	private AdapterView.OnItemSelectedListener textFontListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			textFont = getResources().getStringArray(R.array.textfont_array)[pos];
			if (textFont.equals("Droid Sans"))
				textFont = "sans";
			else if (textFont.equals("Droid Mono"))
				textFont = "monospace";
			else
				textFont = "serif";

			mFName.setTypeface(fontMap.get(textFont));
			mLName.setTypeface(fontMap.get(textFont));
			mMInitial.setTypeface(fontMap.get(textFont));
			mCellPhoneNum.setTypeface(fontMap.get(textFont));
			mHomePhoneNum.setTypeface(fontMap.get(textFont));
			mCompanyName.setTypeface(fontMap.get(textFont));
			mTitle.setTypeface(fontMap.get(textFont));
			mAddress.setTypeface(fontMap.get(textFont));
			mFaxNum.setTypeface(fontMap.get(textFont));
			mEmail.setTypeface(fontMap.get(textFont));
			mWebURL.setTypeface(fontMap.get(textFont));
			mNote.setTypeface(fontMap.get(textFont));

		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	/**
	 * Displays an alert message.
	 * 
	 * @param message
	 */
	private void showFinalAlert(CharSequence message) {
		new AlertDialog.Builder(Form1Activity.this)
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
	 * Overrides the original change of configuration setup.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * This runs after startActivityForResult - changes the image.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKPIC_FROM_ALBUM && data != null) {
			image = data.getData();
			byte[] bm = imageUtilities.getResizedImageData(image, 320, 320);
			Bitmap bmp = BitmapFactory.decodeByteArray(bm, 0, bm.length);
			mImage.setImageBitmap(bmp);
		}
	}

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

	public void chooseBackgroundColor() {
		final ChatGridAction _qact = new ChatGridAction(mFName, 0);
		_qact.setMainTitle("Background Color");

		View.OnClickListener pickcolor = new View.OnClickListener() {
			public void onClick(View v) {
				String txt = (String) ((TextView) v.findViewById(R.id.title))
						.getText();
				for (int j = 0; j < bgcolornames.length; j++) {
					if (bgcolornames[j].equals(txt)) {
						bgColorSelected = j;
						bgColor = ""
								+ bgMainColors
										.get(bgcolornames[bgColorSelected]);
						for (String s : bgMainColors.keySet()) {
							if (bgMainColors.get(s).equals(
									Integer.parseInt(bgColor))) {
								bgColorFrame.setBackgroundColor(bgMainColors
										.get(s));
								break;
							}
						}
						break;
					}
				}
				_qact.dismiss();
			}
		};

		ActionItem item;
		for (int i = 0; i < bgcolornames.length; i++) {
			item = new ActionItem();
			item.setTitle(bgcolornames[i]);
			item.setOnClickListener(pickcolor);
			item.setBackgroundcolor(bgMainColors.get(bgcolornames[i]));
			if (i == bgColorSelected) {
				item.setSelected(true);
			}
			_qact.addActionItem(item);
		}
		// _qact.setAnimStyle(ChatGridAction.ANIM_GROW_FROM_CENTER);
		// _qact.setDisplayPosY(100);
		_qact.show();
	}

	public void chooseSocialNetwork(final int socialNumber) {
		final ChatGridAction _qact = new ChatGridAction(mFName, 0);
		_qact.setMainTitle("Social");

		View.OnClickListener pickSocialNetwork = new View.OnClickListener() {
			public void onClick(View v) {
				String txt = (String) ((TextView) v.findViewById(R.id.title))
						.getText();
				for (int j = 0; j < socialNetworks.length; j++) {
					if (socialNetworks[j].equals(txt)) {
						switch (socialNumber) {
						case 1:
							socialType1 = j + 1;
							social1.setBackgroundDrawable(socialMap
									.get(socialNetworks[j]));
							social1.setText("");
							break;
						case 2:
							socialType2 = j + 1;
							social2.setBackgroundDrawable(socialMap
									.get(socialNetworks[j]));
							social2.setText("");
							break;
						case 3:
							socialType3 = j + 1;
							social3.setBackgroundDrawable(socialMap
									.get(socialNetworks[j]));
							social3.setText("");
							break;
						case 4:
							socialType4 = j + 1;
							social4.setBackgroundDrawable(socialMap
									.get(socialNetworks[j]));
							social4.setText("");
							break;

						}
					}
				}
				_qact.dismiss();
			}
		};

		ActionItem item;
		for (int i = 0; i < socialNetworks.length; i++) {
			item = new ActionItem();
			item.setOnClickListener(pickSocialNetwork);
			item.setHiddenTitle(socialNetworks[i]);
			item.setIcon(socialMap.get(socialNetworks[i]));
			switch (socialNumber) {
			case 1:
				if (i == socialType1 - 1) {
					item.setSelected(true);
				}
			case 2:
				if (i == socialType2 - 1) {
					item.setSelected(true);
				}
			case 3:
				if (i == socialType3 - 1) {
					item.setSelected(true);
				}
			case 4:
				if (i == socialType4 - 1) {
					item.setSelected(true);
				}
			}
			_qact.addActionItem(item);
		}
		// _qact.setAnimStyle(ChatGridAction.ANIM_GROW_FROM_CENTER);
		// _qact.setDisplayPosY(100);
		_qact.show();
	}

	public void chooseTextColor() {
		final ChatGridAction _qact = new ChatGridAction(mFName, 0);
		_qact.setMainTitle("Text Color");

		View.OnClickListener pickcolor = new View.OnClickListener() {
			public void onClick(View v) {
				String txt = (String) ((TextView) v.findViewById(R.id.title))
						.getText();
				for (int j = 0; j < textcolornames.length; j++) {
					if (textcolornames[j].equals(txt)) {
						textColorSelected = j;
						textColor = ""
								+ textMainColors
										.get(textcolornames[textColorSelected]);
						Log.v("Color", "Color string is: " + textColor);
						for (String s : textMainColors.keySet()) {
							if (textMainColors.get(s).equals(
									Integer.parseInt(textColor))) {
								textColorFrame
										.setBackgroundColor(textMainColors
												.get(s));
								break;
							}
						}
						break;
					}
				}
				_qact.dismiss();
			}
		};

		ActionItem item;
		for (int i = 0; i < textcolornames.length; i++) {
			item = new ActionItem();
			item.setTitle(textcolornames[i]);
			item.setOnClickListener(pickcolor);
			item.setBackgroundcolor(textMainColors.get(textcolornames[i]));
			if (i == textColorSelected) {
				item.setSelected(true);
			}
			_qact.addActionItem(item);
		}
		// _qact.setAnimStyle(ChatGridAction.ANIM_GROW_FROM_CENTER);
		// _qact.setDisplayPosY(100);
		_qact.show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (contactId == -1) {
				new AlertDialog.Builder(Form1Activity.this)
						.setTitle("Warning")
						.setMessage(
								"Your progress will not be saved if you leave this page")
						.setPositiveButton("Continue",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										finish();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).setCancelable(true).show();
				return true;
			} else {
				new AlertDialog.Builder(Form1Activity.this)
						.setTitle("Warning")
						.setMessage(
								"Your progress will not be saved if you leave this page")
						.setPositiveButton("Continue",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										Intent currentIntent = new Intent(
												Form1Activity.this,
												MyCardActivity.class);
										currentIntent.putExtra("contactId",
												contactId);
										startActivity(currentIntent);
										finish();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).setCancelable(true).show();

				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// Save to server activities
	public void doCreate(int localId, String userId, String fName,
			String lName, String mInitial, String cellPhoneNumber,
			String homePhoneNumber, String faxNumber, String companyName,
			String title, String address, String email, String websiteURL,
			String imageURI, int category, int layoutId, String textFont,
			String textColor, String bgColor, String note) {

		mProgressDialog = new ProgressDialog(Form1Activity.this);
		mProgressDialog.setTitle("Save Card");
		mProgressDialog.setMessage("Saving Card, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();

		parameters = new Hashtable<String, String>();
		parameters.put("localId", String.valueOf(localId));
		parameters.put("uid", userId);
		parameters.put("fName", fName);
		parameters.put("lName", lName);
		parameters.put("mInitial", mInitial);
		parameters.put("cellNum", cellPhoneNumber);
		parameters.put("homeNum", homePhoneNumber);
		parameters.put("faxNum", faxNumber);
		parameters.put("company", companyName);
		parameters.put("title", title);
		parameters.put("address", address);
		parameters.put("email", email);
		parameters.put("webUrl", websiteURL);
		parameters.put("imageUri", imageURI);
		parameters.put("category", String.valueOf(category));
		parameters.put("layoutId", String.valueOf(layoutId));
		parameters.put("textFont", textFont);
		parameters.put("textColor", textColor);
		parameters.put("bgColor", bgColor);
		parameters.put("note", note);
		CreateTask upl = new CreateTask();
		Log.v("Execute", "AsyncTask successfully executed.");
		upl.execute(CREATE_URL);
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
				db.open(true);
				serverId = object.getLong("id");
				db.updateServerId(contactId, serverId);
				db.close();

				uploadTask.doUpload(user_id, serverId, image);

				Intent currentIntent = new Intent(Form1Activity.this,
						MyCardActivity.class);
				currentIntent.putExtra("contactId", contactId);
				startActivity(currentIntent);
				// Bundle bundle = new Bundle();
				// bundle.putString("json", json);
				//
				// Intent mIntent = new Intent();
				// mIntent.putExtras(bundle);
				// setResult(RESULT_OK, mIntent);
				Toast.makeText(this, "Card successfully saved",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {

		}

	}

	private class CreateTask extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... searchKey) {

			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				Log.v("Create", "Create successfully executed.");
				return create(url, parameters);
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

	// Update

	public void doUpdate(int localId, String userId, String fName,
			String lName, String mInitial, String cellPhoneNumber,
			String homePhoneNumber, String faxNumber, String companyName,
			String title, String address, String email, String websiteURL,
			String imageURI, int category, int layoutId, String textFont,
			String textColor, String bgColor, String note, long serverId) {
		mProgressDialog = new ProgressDialog(Form1Activity.this);
		mProgressDialog.setTitle("Update Card");
		mProgressDialog.setMessage("Updating Card, Please Wait");
		mProgressDialog.setIndeterminate(true);

		mProgressDialog.setCancelable(false);

		mProgressDialog.show();

		updateParams = new Hashtable<String, String>();
		updateParams.put("localId", String.valueOf(localId));
		updateParams.put("uid", userId);
		updateParams.put("fName", fName);
		updateParams.put("lName", lName);
		updateParams.put("mInitial", mInitial);
		updateParams.put("cellNum", cellPhoneNumber);
		updateParams.put("homeNum", homePhoneNumber);
		updateParams.put("faxNum", faxNumber);
		updateParams.put("company", companyName);
		updateParams.put("title", title);
		updateParams.put("address", address);
		updateParams.put("email", email);
		updateParams.put("webUrl", websiteURL);
		updateParams.put("imageUri", imageURI);
		updateParams.put("category", String.valueOf(category));
		updateParams.put("layoutId", String.valueOf(layoutId));
		updateParams.put("textFont", textFont);
		updateParams.put("textColor", textColor);
		updateParams.put("bgColor", bgColor);
		updateParams.put("note", note);
		updateParams.put("server_id", String.valueOf(serverId));
		UpdateTask updateTask = new UpdateTask();
		updateTask.execute(UPDATE_URL);
	}

	public void showUpdateSuccess(String json) {
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

				uploadTask.doUpload(user_id, serverId, image);

				Intent currentIntent = new Intent(Form1Activity.this,
						MyCardActivity.class);
				currentIntent.putExtra("contactId", contactId);
				startActivity(currentIntent);
				// if (mProgressDialog != null && mProgressDialog.isShowing())
				// mProgressDialog.dismiss();

				// Bundle bundle = new Bundle();
				// bundle.putString("json", json);
				//
				// Intent mIntent = new Intent();
				// mIntent.putExtras(bundle);
				// setResult(RESULT_OK, mIntent);
				Toast.makeText(this, "Card successfully updated",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(this, status, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.v("Exception", e.toString());
		}

	}

	public class UpdateTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... searchKey) {
			String url = searchKey[0];

			try {
				// Log.v("gsearch","gsearch result with AsyncTask");
				Log.v("Create", "Create successfully executed.");
				Log.v("Parameters", updateParams.keys().toString());
				return create(url, updateParams);
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
				showUpdateSuccess(result);
				// Log.v("Ringtone","Ringtone Path:"+resultbm);

			} catch (Exception e) {
				// Log.v("Exception google search","Exception:"+e.getMessage());

			}

		}

	}

	public String create(String serverUrl, Hashtable<String, String> params) {
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
			Log.v("Exception", "Exception caught in create");
			Log.v("Exception", e.toString());
			return null;
			// Log.e("HREQ", "Exception: "+e.toString());
		}

	}
}