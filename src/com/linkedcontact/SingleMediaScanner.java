package com.linkedcontact;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class SingleMediaScanner implements MediaScannerConnectionClient {

	private MediaScannerConnection mMs;
	private String mPath;
	private Uri mUri;
	private String mName;
	private Context mContext;
	private String mColumname;
	private String lengthkey;
	private long length;
	private String mMimeType;
	private boolean callBack = false;

	public SingleMediaScanner(Context context, String path) {
		mPath = path;
		mContext = context;
		mMs = new MediaScannerConnection(context, this);
		mMs.connect();
	}

	public void setCallBack() {
		callBack = true;
	}

	public SingleMediaScanner setMimeType(String mime) {
		mMimeType = mime;
		return this;
	}

	public void setValues(String title, Uri toneuri, String columname) {
		mName = title;
		mUri = toneuri;
		mColumname = columname;
	}

	public void setLength(String lkey, long lvalue) {
		lengthkey = lkey;
		length = lvalue;
	}

	public void onMediaScannerConnected() {
		mMs.scanFile(mPath, mMimeType);
	}

	public void onScanCompleted(String path, Uri uri) {
		mMs.disconnect();
		// mUri = uri;
		if (mUri != null) {
			ContentValues newvalues = new ContentValues();
			if (mName != null)
				newvalues.put(mColumname, mName);
			Date now = new Date();
			long time = now.getTime() / 1000;
			// newvalues.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
			if (lengthkey != null)
				newvalues.put(lengthkey, length);
			// Log.v("UPDATE URI", mUri.toString()+
			// " time since 1970 "+String.valueOf(time) );
			// if( mUri.toString().indexOf("internal")>0)
			// mContext.getContentResolver().delete(mUri, null, null);
			mContext.getContentResolver().update(mUri, newvalues, null, null);
			// mContext.getContentResolver().update(uri, newvalues, null, null);
		}

	}

}
