package com.linkedcontact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.People;

public class ContactAPISdk4 extends ContactAPI {

	@Override
	public Cursor createCursor(String infilter) {
	   	String[] projection = new String[]{
    				People._ID,
        			People.NAME,
        			People.NUMBER,
        			
        };
    	
        Cursor cur = null;
        try{
        	String whereClause;
        	String filter;
        		
        	whereClause= People.NAME + " != \"\"";
        	filter = People.TYPE + " = " + People.TYPE_MOBILE + " AND " + whereClause;
        	if (infilter != null && infilter.length() > 0) {
        		filter += " AND ("+People.NAME +" LIKE \"%"+ infilter + "%\")";
        	}	
        	String orderBy;
          		
        	orderBy = People.NAME + " ASC";
       
            cur = this.cr.query(Contacts.People.CONTENT_URI, projection, filter, null, orderBy);
            	
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
		return new String[]{
    			
    			People.NAME,
    			People.NUMBER,
    			
    		};
	}
	
	@Override
	public  String getPhoneColumnName(){
		return People.NUMBER;
	}

	@Override
	public Cursor creategroupsCursor() {
	   	String[] projection = new String[]{
	   			Contacts.Groups._ID,
	   			Contacts.Groups.NAME  
	   	};
	
	   	Cursor cur = null;
	   	try{
	   		cur = this.cr.query(Contacts.Groups.CONTENT_URI, projection, null, null, null);	
	   	}catch(Exception e){
        	//Log.e("TAG", "Exception getting mobile contacts " + e.getMessage());
        	//FlurryAgent.onError("SEND_TO_CONTACT_LIST_ERROR", e.getMessage(), "SentToContact");;
        }
        return cur;
	}

	@Override
	public String getGroupIDColumnName() {
		// TODO Auto-generated method stub
		return Contacts.Groups._ID;
	}

	@Override
	public String[] getgroupFrom() {
			return new String[]{
					Contacts.Groups.NAME 
    		};
	}

	@Override
	public String[] getnumbersFromgroupID(String groupid) {
	   	String[] projection = new String[]{
	   			People.NUMBER
	   	};
	
	   	Cursor cur = null;
	   	try{
	   		String whereClause;
	   		whereClause= Contacts.GroupMembership.GROUP_ID + "=" +groupid;
	   		String filter = People.TYPE + " = " + People.TYPE_MOBILE + " AND " + whereClause;
	   		cur = this.cr.query(Contacts.People.CONTENT_URI, projection, filter, null, null);
	   		int count= cur.getCount();
	   		String[] numbers =new String[count];
	   		int _columnIndex = cur.getColumnIndex(People.NUMBER);
		
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
