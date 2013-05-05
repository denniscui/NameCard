package com.linkedcontact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Contacts.People;
import android.util.Log;


public class ContactAPISdk5 extends ContactAPI {

	@Override
	public Cursor createCursor(String infilter) {
	   	String[] projection = new String[]{
    				ContactsContract.CommonDataKinds.Phone._ID,
    				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
    				ContactsContract.CommonDataKinds.Phone.NUMBER
    	};
    	
        Cursor cur = null;
        try{
        	String whereClause;
        	String filter;
        		
        	whereClause= ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " != \"\"";
        	filter = ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " AND " + whereClause;
        	if (infilter != null && infilter.length() > 0) {
        		filter += " AND ("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" LIKE \"%"+ infilter + "%\")";
        	}
        	
        	String orderBy;
          		
        	orderBy = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        	
            cur = this.cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, filter, null, orderBy);
           
        }catch(Exception e){
        	//Log.e("TAG", "Exception getting mobile contacts " + e.getMessage());
        	//FlurryAgent.onError("SEND_TO_CONTACT_LIST_ERROR", e.getMessage(), "SentToContact");;
        }
        return cur;
	}

	@Override
	public ContentResolver getCr() {
		// TODO Auto-generated method stub
		return cr;
	}

	@Override
	public void setCr(ContentResolver cr) {
		this.cr=cr;

	}

	@Override
	public String[] getFrom() {
		// TODO Auto-generated method stub
		return  new String[]{
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER
		};
	}
	
	public  String getPhoneColumnName(){
		return ContactsContract.CommonDataKinds.Phone.NUMBER;
	}

	@Override
	public Cursor creategroupsCursor() {
	   	String[] projection = new String[]{
	   			ContactsContract.Groups._ID,
				ContactsContract.Groups.TITLE 
	   	};
	
	   	Cursor cur = null;
	   	try{
	   		cur = this.cr.query(ContactsContract.Groups.CONTENT_URI, projection, null, null, null);	
	   	}catch(Exception e){
        	//Log.e("TAG", "Exception getting mobile contacts " + e.getMessage());
        	//FlurryAgent.onError("SEND_TO_CONTACT_LIST_ERROR", e.getMessage(), "SentToContact");;
        }
        return cur;
	}

	@Override
	public String getGroupIDColumnName() {
		// TODO Auto-generated method stub
		return ContactsContract.Groups._ID;
	}

	@Override
	public String[] getgroupFrom() {
		return  new String[]{
				ContactsContract.Groups.TITLE
		};
	}

	@Override
	public String[] getnumbersFromgroupID(String groupid) {
	   	String[] projection = new String[]{
				ContactsContract.CommonDataKinds.Phone.NUMBER
	   	};
	
	   	Cursor cur = null;
	   	try{
	   		String whereClause;
	   		whereClause= ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" +groupid;
	   		String filter = ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " AND " + whereClause;
	   		cur = this.cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, filter, null, null);
	   		int count= cur.getCount();
	   		String[] numbers =new String[count];
	   		int _columnIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		
	   		cur.moveToFirst(); 
	   		for(int i=0;i<count;i++){
	   			numbers[i]= cur.getString(_columnIndex);
	   			cur.moveToNext();
	   		}
	   		return numbers;
	   	}catch(Exception e){
	   		return null;
	   	}
	
	}

}
