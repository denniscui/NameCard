package com.linkedcontact;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.Context;
import android.content.Intent;
import android.database.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class MyCardsListActivity extends Activity {

	private ArrayList<String> cards;
	private ArrayList<String> ids;
	private Spinner spinnerCategories;
	private DbAdapter db;
	private ImgUtil imageUtilities;
	CardListAdapter adp;
	Context context = this;
	ListView lv;

	private int currentCategoryId = 0;

	public final static HashMap<Integer, String> categories = new HashMap<Integer, String>();
	static {

		categories.put(0, "All");
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

	public String[] categoryNums = { "All", "Business", "Church", "Community",
			"Co-workers", "Family", "High School", "Personal", "Relatives" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cardslist);

		TextView titleLabel = (TextView) findViewById(R.id.listTitle);
		titleLabel.setText("My Cards");

		Intent intent = getIntent();
		currentCategoryId = intent.getIntExtra("categoryId", 0);

		lv = (ListView) findViewById(R.id.list);

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
		imageUtilities = new ImgUtil(this);

		cards = new ArrayList<String>();
		ids = new ArrayList<String>();
		final Cursor mCursor;
		if (currentCategoryId != 0)
			mCursor = db.fetchByCategory(currentCategoryId);
		else
			mCursor = db.fetchAllContacts();

		if (mCursor == null) {
			TextView noCards = (TextView) this.findViewById(R.id.nocards_text);
			noCards.setVisibility(0x00000000);
		} else {
			TextView noCards = (TextView) this.findViewById(R.id.nocards_text);
			noCards.setVisibility(0x00000008);
			mCursor.moveToFirst();

			while (!mCursor.isAfterLast()) {

				String name = "";
				int dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_FNAME);
				name = name + mCursor.getString(dataIndex);

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_MINITIAL);

				if (!mCursor.getString(dataIndex).equals(""))
					name = name + " " + mCursor.getString(dataIndex) + ". ";
				else
					name = name + " ";

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_LNAME);

				name = name + mCursor.getString(dataIndex);

				cards.add(name);
				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_ID);

				ids.add(mCursor.getString(dataIndex));

				mCursor.moveToNext();
			}
		}

		adp = new CardListAdapter(this, R.layout.cardslistrow, cards);

		lv.setAdapter(adp);

		// lv.setOnItemLongClickListener(new OnItemLongClickListener() {
		//
		// public boolean onItemLongClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// serverId = Long.parseLong(ids.get(position));
		// // categoryId = serverCategories.get(position);
		// _qact = new CategoryQuickAction(view, user_id, categoryId,
		// serverId);
		// ActionItem item = new ActionItem();
		// item.setOnClickListener(confirmListener);
		// ActionItem item1 = new ActionItem();
		// item1.setOnItemSelectedListener(categoriesPopupListener);
		// _qact.addActionItem(item);
		// _qact.addActionItem(item1);
		// _qact.show();
		//
		// return true;
		// }
		//
		// });

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.v("OnClick", "Arrived OnClick to MyCardActivity");
				Intent currentIntent = new Intent(MyCardsListActivity.this,
						MyCardActivity.class);

				String dataId = ids.get(position);

				int contactId = Integer.parseInt(dataId);

				currentIntent.putExtra("contactId", contactId);
				currentIntent.putExtra("categoryId", currentCategoryId);

				startActivity(currentIntent);

				mCursor.close();
				db.close();

				finish();
			}
		});

		mCursor.close();
		db.close();
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
					R.array.category_array1)[pos];
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

			Cursor mCursor;
			if (currentCategoryId != 0)
				mCursor = db.fetchByCategory(currentCategoryId);
			else
				mCursor = db.fetchAllContacts();

			if (mCursor == null) {
				TextView noCards = (TextView) findViewById(R.id.nocards_text);
				noCards.setText("There are no contacts in this category.");
				noCards.setVisibility(0x00000000);
				adp.notifyDataSetChanged();
				return;
			}

			TextView noCards = (TextView) findViewById(R.id.nocards_text);
			noCards.setVisibility(0x00000008);

			mCursor.moveToFirst();

			while (!mCursor.isAfterLast()) {

				String name = "";

				int dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_FNAME);
				name = name + mCursor.getString(dataIndex);

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_MINITIAL);

				if (!mCursor.getString(dataIndex).equals(""))
					name = name + " " + mCursor.getString(dataIndex) + ". ";
				else
					name = name + " ";

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_LNAME);

				name = name + mCursor.getString(dataIndex);

				cards.add(name);

				dataIndex = mCursor
						.getColumnIndexOrThrow(DbAdapter.KEY_CONTACT_ID);

				ids.add(mCursor.getString(dataIndex));

				mCursor.moveToNext();
			}

			mCursor.close();
			db.close();
			Log.v("Cards", cards.toString());
			adp.notifyDataSetChanged();
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

			ImageView img = (ImageView) row.findViewById(R.id.listrow_image);
			int tempId = Integer.parseInt(ids.get(position));

			Cursor cursor = db.fetchContact(tempId);

			int dataIndice = cursor.getColumnIndexOrThrow(DbAdapter.KEY_IMAGE);
			String imgURI = cursor.getString(dataIndice);

			// String lastDigits = imgURI.substring(imgURI.lastIndexOf("/") +
			// 1);
			// Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(
			// context.getContentResolver(), Integer.parseInt(lastDigits),
			// MediaStore.Images.Thumbnails.MICRO_KIND, null);
			byte[] bm = imageUtilities.getResizedImageData(Uri.parse(imgURI),
					100, 100);
			Bitmap bmp = BitmapFactory.decodeByteArray(bm, 0, bm.length);
			img.setImageBitmap(bmp);

			TextView phoneNum = (TextView) row
					.findViewById(R.id.listrow_phoneNum);

			dataIndice = cursor.getColumnIndexOrThrow(DbAdapter.KEY_CELL_PHONE);
			String cellNum = cursor.getString(dataIndice);
			phoneNum.setText(cellNum);

			cursor.close();
			db.close();

			return row;
		}
	}

	public void onDestroy() {
		db.close();
		super.onDestroy();
	}
}