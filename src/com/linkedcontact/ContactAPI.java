package com.linkedcontact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;

public abstract class ContactAPI {
	private static ContactAPI api;
 	
	protected Cursor cur;
 	protected ContentResolver cr;
 	
 	public static ContactAPI getAPI() {
 		if (api == null) {
 			String apiClass;
 			if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
 				apiClass = "com.linkedcontact.ContactAPISdk5";
 			} else {
 				apiClass = "com.linkedcontact.ContactAPISdk4";
 			}
 			
 			try {
 				Class<? extends ContactAPI> realClass = Class.forName(apiClass).asSubclass(ContactAPI.class);
 				api = realClass.newInstance();
 			} catch (Exception e) {
 				throw new IllegalStateException(e);
 			}
 			
 		}
 		return api;
 	}
 	public abstract Cursor createCursor(String filter);
 	public abstract Cursor creategroupsCursor();
 	public abstract ContentResolver getCr();
 	public abstract void setCr(ContentResolver cr);
 	public abstract String[] getFrom();
 	public abstract String getPhoneColumnName();
 	public abstract String getGroupIDColumnName();
 	public abstract String[] getgroupFrom();
 	public abstract String[] getnumbersFromgroupID(String groupid);
}
