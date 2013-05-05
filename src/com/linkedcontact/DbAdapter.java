package com.linkedcontact;

import android.content.ContentValues;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DbAdapter {

	public static final String CONTACTS_DB_TABLE = "cards";

	public static final String KEY_CONTACT_ID = "local_id";
	public static final int KEY_CONTACT_ID_NUM = 0;
	public static final String KEY_CONTACT_FNAME = "firstname";
	public static final int KEY_CONTACT_FNAME_NUM = 1;
	public static final String KEY_CONTACT_LNAME = "lastname";
	public static final int KEY_CONTACT_LNAME_NUM = 2;
	public static final String KEY_CONTACT_MINITIAL = "middleinitial";
	public static final int KEY_CONTACT_MINITIAL_NUM = 3;
	public static final String KEY_CELL_PHONE = "cellnumber";
	public static final int KEY_CELL_PHONE_NUM = 4;
	public static final String KEY_HOME_PHONE = "homenumber";
	public static final int KEY_HOME_PHONE_NUM = 5;
	public static final String KEY_FAX = "faxnumber";
	public static final int KEY_FAX_NUM = 6;
	public static final String KEY_ORGANIZATION = "organization";
	public static final int KEY_COMPANY_NUM = 7;
	public static final String KEY_TITLE = "title";
	public static final int KEY_JOB_TITLE_NUM = 8;
	public static final String KEY_ADDRESS = "address";
	public static final int KEY_ADDRESS_NUM = 9;
	public static final String KEY_EMAIL = "email";
	public static final int KEY_EMAIL_NUM = 10;
	public static final String KEY_WEB_URL = "weburl";
	public static final int KEY_WEB_URL_NUM = 11;
	public static final String KEY_IMAGE = "image";
	public static final int KEY_IMAGE_NUM = 12;
	public static final String KEY_CATEGORY = "category";
	public static final int KEY_CATEGORY_NUM = 13;
	public static final String KEY_LAYOUT_ID = "layoutid";
	public static final int KEY_LAYOUT_ID_NUM = 14;
	public static final String KEY_TEXT_FONT = "textfont";
	public static final int KEY_TEXT_FONT_NUM = 15;
	public static final String KEY_TEXT_COLOR = "textcolor";
	public static final int KEY_TEXT_COLOR_NUM = 16;
	public static final String KEY_BGCOLOR = "bgcolor";
	public static final int KEY_BGCOLOR_NUM = 17;
	public static final String KEY_NOTE = "note";
	public static final int KEY_NOTE_NUM = 18;
	public static final String KEY_SERVER_ID = "server_id";
	public static final int KEY_SERVER_ID_NUM = 19;
	public static final String KEY_SOCIAL_TYPE1 = "social_type1";
	public static final int KEY_SOCIAL_TYPE1_NUM = 20;
	public static final String KEY_SOCIAL_URL1 = "social_url1";
	public static final int KEY_SOCIAL_URL1_NUM = 21;
	public static final String FLAG_1 = "flag1";
	public static final int KEY_FLAG_1 = 22;
	public static final String FLAG_2 = "flag2";
	public static final int KEY_FLAG_2 = 23;
	public static final String FLAG_3 = "flag3";
	public static final int KEY_FLAG_3 = 24;
	public static final String FLAG_4 = "flag4";
	public static final int KEY_FLAG_4 = 25;
	public static final String FLAG_5 = "flag5";
	public static final int KEY_FLAG_5 = 26;
	public static final String KEY_SOCIAL_TYPE2 = "social_type2";
	public static final int KEY_SOCIAL_TYPE2_NUM = 27;
	public static final String KEY_SOCIAL_URL2 = "social_url2";
	public static final int KEY_SOCIAL_URL2_NUM = 28;
	public static final String KEY_SOCIAL_TYPE3 = "social_type3";
	public static final int KEY_SOCIAL_TYPE3_NUM = 29;
	public static final String KEY_SOCIAL_URL3 = "social_url3";
	public static final int KEY_SOCIAL_URL3_NUM = 30;
	public static final String KEY_SOCIAL_TYPE4 = "social_type4";
	public static final int KEY_SOCIAL_TYPE4_NUM = 31;
	public static final String KEY_SOCIAL_URL4 = "social_url4";
	public static final int KEY_SOCIAL_URL4_NUM = 32;

	private static final String DATABASE_NAME = "namecards";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper mDbHelper;
	public SQLiteDatabase mDb;

	private final Context context;

	public DbAdapter(Context _context) {
		this.context = _context;
		mDbHelper = null;
		mDb = null;
	}

	/**
	 * Table creation sql statement
	 */
	private static final String CONTACTS_DB_CREATE = "create table "
			+ CONTACTS_DB_TABLE + " (" + KEY_CONTACT_ID
			+ " integer primary key autoincrement, " + KEY_CONTACT_FNAME
			+ " text default 'Unknown', " + KEY_CONTACT_LNAME
			+ " text default 'Unknown', " + KEY_CONTACT_MINITIAL
			+ " text default 'Unknown', " + KEY_CELL_PHONE
			+ " text default '0000000000', " + KEY_HOME_PHONE
			+ " text default '0000000000', " + KEY_FAX
			+ " text default '0000000', " + KEY_ORGANIZATION
			+ " text default 'Unknown', " + KEY_TITLE
			+ " text default 'Unknown', " + KEY_ADDRESS
			+ " text default 'Unknown', " + KEY_EMAIL
			+ " text default 'Unknown', " + KEY_WEB_URL
			+ " text default 'Unknown', " + KEY_IMAGE
			+ " text default 'Unknown', " + KEY_LAYOUT_ID
			+ " integer default '0', " + KEY_CATEGORY
			+ " integer default '0', " + KEY_TEXT_FONT
			+ " text default 'sans', " + KEY_TEXT_COLOR
			+ " text default '0xFF000000', " + KEY_BGCOLOR
			+ " text default '0xFFFFFFFF', " + KEY_NOTE
			+ " text default 'Unknown', " + KEY_SERVER_ID + " long, "
			+ KEY_SOCIAL_TYPE1 + " integer default '0', " + KEY_SOCIAL_URL1
			+ " text default 'Unknown', " + KEY_SOCIAL_TYPE2
			+ " integer default '0', " + KEY_SOCIAL_URL2
			+ " text default 'Unknown', " + KEY_SOCIAL_TYPE3
			+ " integer default '0', " + KEY_SOCIAL_URL3
			+ " text default 'Unknown', " + KEY_SOCIAL_TYPE4
			+ " integer default '0', " + KEY_SOCIAL_URL4
			+ " text default 'Unknown', " + FLAG_1
			+ " text default 'Unknown', " + FLAG_2
			+ " text default 'Unknown', " + FLAG_3
			+ " text default 'Unknown', " + FLAG_4
			+ " text default 'Unknown', " + FLAG_5
			+ " text default 'Unknown'" + ");";

	public static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CONTACTS_DB_CREATE);
			Log.v("Database", CONTACTS_DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	/**
	 * Open the contacts database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @param readOnly
	 *            if the database should be opened read only
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public void open(boolean readOnly) throws SQLException {
		if (mDbHelper == null) {
			mDbHelper = new DatabaseHelper(context);
			if (readOnly) {
				mDb = mDbHelper.getReadableDatabase();
			} else {
				mDb = mDbHelper.getWritableDatabase();
			}
		}

	}

	/**
	 * Close the database
	 */
	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}

	public boolean deleteContact(int contactId) {
		return deleteContact(contactId, true);
	}

	public boolean deleteContact(int contactId, boolean showToast) {
		open(true);
		if (mDb.delete(CONTACTS_DB_TABLE,
				KEY_CONTACT_ID + "=" + String.valueOf(contactId), null) > 0) {

			if (showToast) {
				Toast.makeText(context,
						R.string.contact_customization_removed_toast,
						Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}

	public int createContact(String fName, String lName, String mInitial,
			String cellNum, String homeNum, String faxNum, String companyName,
			String jobTitle, String address1, String emailAddress,
			String websiteURL, String imageUri, int category, int layoutId,
			String textFont, String textColor, String bgColor, String note,
			int socialType1, String socialUrl1, int socialType2,
			String socialUrl2, int socialType3, String socialUrl3,
			int socialType4, String socialUrl4) {

		open(true);
		String image = "";
		if (!imageUri.equals(""))
			image = imageUri;

		ContentValues vals = new ContentValues();
		// vals.put(KEY_CONTACT_ID, contactId);
		vals.put(KEY_CONTACT_FNAME, fName.trim());
		vals.put(KEY_CONTACT_LNAME, lName.trim());
		vals.put(KEY_CONTACT_MINITIAL, mInitial.trim());
		vals.put(KEY_CELL_PHONE, cellNum.trim());
		vals.put(KEY_HOME_PHONE, homeNum.trim());
		vals.put(KEY_FAX, faxNum.trim());
		vals.put(KEY_ORGANIZATION, companyName.trim());
		vals.put(KEY_TITLE, jobTitle.trim());
		vals.put(KEY_ADDRESS, address1.trim());
		vals.put(KEY_EMAIL, emailAddress.trim());
		vals.put(KEY_WEB_URL, websiteURL.trim());
		vals.put(KEY_IMAGE, image.toString());
		vals.put(KEY_CATEGORY, category);
		vals.put(KEY_LAYOUT_ID, layoutId);
		vals.put(KEY_TEXT_FONT, textFont.trim());
		vals.put(KEY_TEXT_COLOR, textColor.trim());
		vals.put(KEY_BGCOLOR, bgColor.trim());
		vals.put(KEY_NOTE, note.trim());
		vals.put(KEY_SOCIAL_TYPE1, socialType1);
		vals.put(KEY_SOCIAL_URL1, socialUrl1.trim());
		vals.put(KEY_SOCIAL_TYPE2, socialType2);
		vals.put(KEY_SOCIAL_URL2, socialUrl2.trim());
		vals.put(KEY_SOCIAL_TYPE3, socialType3);
		vals.put(KEY_SOCIAL_URL3, socialUrl3.trim());
		vals.put(KEY_SOCIAL_TYPE4, socialType4);
		vals.put(KEY_SOCIAL_URL4, socialUrl4.trim());
		return (int) mDb.insert(CONTACTS_DB_TABLE, null, vals);
	}

	public int updateContact(int id, String fName, String lName,
			String mInitial, String cellNum, String homeNum, String faxNum,
			String companyName, String jobTitle, String address1,
			String emailAddress, String websiteURL, String imageUri,
			int category, int layoutId, String textFont, String textColor,
			String bgColor, String note, long serverId, int socialType1,
			String socialUrl1, int socialType2, String socialUrl2,
			int socialType3, String socialUrl3, int socialType4,
			String socialUrl4) {
		open(true);

		String image = "";
		if (!imageUri.equals(""))
			image = imageUri;

		Log.v("UPDATE SOCIALURL", socialUrl1.trim());
		Log.v("UPDATE SOCIALTYPE", socialType1 + "");
		ContentValues vals = new ContentValues();
		// vals.put(KEY_CONTACT_ID, contactId);
		vals.put(KEY_CONTACT_FNAME, fName.trim());
		vals.put(KEY_CONTACT_LNAME, lName.trim());
		vals.put(KEY_CONTACT_MINITIAL, mInitial.trim());
		vals.put(KEY_CELL_PHONE, cellNum.trim());
		vals.put(KEY_HOME_PHONE, homeNum.trim());
		vals.put(KEY_FAX, faxNum.trim());
		vals.put(KEY_ORGANIZATION, companyName.trim());
		vals.put(KEY_TITLE, jobTitle.trim());
		vals.put(KEY_ADDRESS, address1.trim());
		vals.put(KEY_EMAIL, emailAddress.trim());
		vals.put(KEY_WEB_URL, websiteURL.trim());
		vals.put(KEY_IMAGE, image.toString());
		vals.put(KEY_CATEGORY, category);
		vals.put(KEY_LAYOUT_ID, layoutId);
		vals.put(KEY_TEXT_FONT, textFont.trim());
		vals.put(KEY_TEXT_COLOR, textColor.trim());
		vals.put(KEY_BGCOLOR, bgColor.trim());
		vals.put(KEY_NOTE, note.trim());
		vals.put(KEY_SERVER_ID, serverId);
		vals.put(KEY_SOCIAL_TYPE1, socialType1);
		vals.put(KEY_SOCIAL_URL1, socialUrl1.trim());
		vals.put(KEY_SOCIAL_TYPE2, socialType2);
		vals.put(KEY_SOCIAL_URL2, socialUrl2.trim());
		vals.put(KEY_SOCIAL_TYPE3, socialType3);
		vals.put(KEY_SOCIAL_URL3, socialUrl3.trim());
		vals.put(KEY_SOCIAL_TYPE4, socialType4);
		vals.put(KEY_SOCIAL_URL4, socialUrl4.trim());;

		return mDb.update(CONTACTS_DB_TABLE, vals, KEY_CONTACT_ID + "=" + id,
				null);

	}

	public Cursor fetchContact(int id) throws SQLException {
		boolean found = false;
		open(true);
		Cursor mCursor = mDb.query(true, CONTACTS_DB_TABLE, null,
				KEY_CONTACT_ID + "=" + id, null, null, null, null, null);
		if (mCursor != null) {
			found = mCursor.moveToFirst();
		}
		if (!found) {
			if (mCursor != null) {
				mCursor.close();
			}
			return null;
		}
		return mCursor;
	}

	public Cursor fetchContact(long id) throws SQLException {
		boolean found = false;
		open(true);
		Cursor mCursor = mDb.query(true, CONTACTS_DB_TABLE, null, KEY_SERVER_ID
				+ "=" + id, null, null, null, null, null);
		if (mCursor != null) {
			found = mCursor.moveToFirst();
		}
		if (!found) {
			if (mCursor != null) {
				mCursor.close();
			}
			return null;
		}
		return mCursor;
	}

	/**
	 * Fetch a list of all contacts in the database
	 * 
	 * @return Db cursor
	 */
	public Cursor fetchAllContacts() {
		boolean found = false;
		open(true);
		Cursor mCursor = mDb.query(CONTACTS_DB_TABLE, null, null, null, null,
				null, null);

		if (mCursor != null) {
			found = mCursor.moveToFirst();
		}
		if (!found) {
			if (mCursor != null) {
				mCursor.close();
			}
			return null;
		}
		return mCursor;
	}

	public Cursor fetchByCategory(int category) {
		boolean found = false;
		open(true);
		Cursor mCursor = mDb.query(CONTACTS_DB_TABLE, null, KEY_CATEGORY + "="
				+ category, null, null, null, null);

		if (mCursor != null) {
			found = mCursor.moveToFirst();
		}
		if (!found) {
			if (mCursor != null) {
				mCursor.close();
			}
			return null;
		}
		return mCursor;
	}

	public int updateServerId(int contactId, long server_id) {
		open(true);

		ContentValues vals = new ContentValues();
		vals.put(KEY_SERVER_ID, server_id);
		return mDb.update(CONTACTS_DB_TABLE, vals, KEY_CONTACT_ID + "="
				+ contactId, null);
	}
}
