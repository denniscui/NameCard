package com.linkedcontact;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ContactPicker extends Activity {

	protected static final String TAG = null;
	private ArrayList<String> names;
	private ArrayList<String> phoneNums;
	private ArrayList<String> choices;
	public String[] Contacts = {};
	public int[] to = {};
	public ListView list;
	private static ContactAPI api;
	private SimpleCursorAdapter mAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_list);

		final Button done_Button =  (Button) findViewById(R.id.done_Button);
		final Button clear_Button = (Button) findViewById(R.id.clear_Button);

		done_Button.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));
		clear_Button.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));

		list = (ListView) findViewById(R.id.contact_listview);
		try {
			api = ContactAPI.getAPI();
			api.setCr(getContentResolver());
			mAdapter = new SimpleCursorAdapter(this,
			// Use a template that displays a text view
					R.layout.multiple_choice_row,
					// Give the cursor to the list adatper
					api.createCursor(""), api.getFrom(),
					// To widget ids in the row layout...
					new int[] { R.id.row_name, R.id.row_number });

			list.setAdapter(mAdapter);
			list.setItemsCanFocus(false);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Log.e("list", "onItemClick CALLED");
					ViewGroup row = (ViewGroup) view;
					com.linkedcontact.widget.ListCheckBox check = (com.linkedcontact.widget.ListCheckBox) row
							.findViewById(R.id.checkbox);
					
					if(check.isChecked())
					{
						Log.v("IsChecked", "TRUE. Unchecking now.");
						list.setItemChecked(position, false);
					}
					
					check.toggle();
				}
			});

		} catch (SecurityException e) {
			// No permission to retrieve contacts?
			Log.e("SecurityException", e.toString());
		} catch (IllegalStateException e) {
			Log.e("IllegalStateException", e.toString());
		}

		clear_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Selections Cleared",
						Toast.LENGTH_SHORT).show();
				ClearSelections();
			}
		});

		/** When 'Done' Button Pushed: **/
		done_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				final SparseBooleanArray checkedItems = list
						.getCheckedItemPositions();
				if (checkedItems == null) {
					finish();
					return;
				}

				Cursor c = mAdapter.getCursor();
				String columnName;
				columnName = api.getPhoneColumnName();
				String AsImplodedString = "";
				String mobileNumber;
				StringBuffer sb = new StringBuffer();
				final int checkedItemsCount = checkedItems.size();
				for (int i = 0; i < checkedItemsCount; ++i) {
					final int position = checkedItems.keyAt(i);

					c.moveToPosition(position);
					int dataIndex = c.getColumnIndexOrThrow(columnName);
					mobileNumber = PhoneNumUtil.processNumber(c
							.getString(dataIndex));

					if (mobileNumber.length() < 10
							|| !PhoneNumUtil.isValidPhoneNum(mobileNumber))
						continue;
					Log.v(columnName, mobileNumber);
					if (i == 0) {
						sb.append(mobileNumber);
					} else {
						sb.append(",");
						sb.append(mobileNumber);
					}
				}
				AsImplodedString = sb.toString().trim();
				Bundle bundle = new Bundle();
				bundle.putString("numbers", AsImplodedString);
				setResult(RESULT_OK, new Intent().putExtras(bundle));
				finish();
			}
		}); // <-- End of Done_Button

	} // <-- end of onCreate();

	private void ClearSelections() {

		int count = this.list.getAdapter().getCount();

		if (count == 0)
			return;

		for (int i = 0; i < count; i++) {
			this.list.setItemChecked(i, false);
		}

	}
}
