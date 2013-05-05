package com.linkedcontact;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ContainedGridView extends LinearLayout {
	private Object _viewobj=null;
	public Object getViewObj() {
		return _viewobj;
	}
	public void setViewObj(Object obj) {
		_viewobj = obj;
		return;
	}
	
	public ContainedGridView(Context context) {
		super(context);
		
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (_viewobj!=null && event.getAction() == MotionEvent.ACTION_DOWN) {
					((CustomPopupWindow)_viewobj).dismiss();
					return true;
				}
				return false;
			}
		});
	}
	public ContainedGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (_viewobj!=null && event.getAction() == MotionEvent.ACTION_DOWN) {
					((CustomPopupWindow)_viewobj).dismiss();
					return true;
				}
				return false;
			}
		});
	}
}