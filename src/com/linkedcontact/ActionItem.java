package com.linkedcontact;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ActionItem {
	private Drawable icon;
	private int iconid;
	private String logo;
	private String title;
	private String hiddentitle;
	private OnClickListener listener;
	private OnItemSelectedListener listener1;
	private Boolean selected;
	private Integer backgroundcolor;
	private Object itemObject;

	public Object getItemObject() {
		return itemObject;
	}

	public void setItemObject(Object object) {
		this.itemObject = object;
	}

	public ActionItem() {
	}

	public ActionItem(Drawable icon) {
		this.icon = icon;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setHiddenTitle(String title) {
		this.hiddentitle = title;
	}

	public String getLogo() {
		return this.logo;
	}

	public String getTitle() {
		return this.title;
	}

	public String getHiddenTitle() {
		return this.hiddentitle;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public void setIconId(int iconid) {
		this.iconid = iconid;
	}

	public void setSelected(Boolean sel) {
		this.selected = sel;
	}

	public void setBackgroundcolor(Integer color) {
		this.backgroundcolor = color;
	}

	public Boolean getSelected() {
		return this.selected;
	}

	public Drawable getIcon() {
		return this.icon;
	}

	public int getIconId() {
		return this.iconid;
	}

	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener1) {
		this.listener1 = listener1;
	}

	public OnClickListener getListener() {
		return this.listener;
	}

	public OnItemSelectedListener getListener1() {
		return this.listener1;
	}

	public Integer getBackgroundcolor() {
		return this.backgroundcolor;
	}
}