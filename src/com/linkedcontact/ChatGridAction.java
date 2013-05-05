package com.linkedcontact;

import android.content.Context;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ChatGridAction extends CustomPopupWindow {
	private final View root;
	private final LayoutInflater inflater;
	private final Context context;

	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;

	private int displayX = -1, displayY = -1;
	private GridView options;
	private ArrayList<ActionItem> actionLst;
	private ChatGridAdapter actlist;
	private Integer viewheight;
	private String mainTitle = null;
	private TextView mainTitleTV;
	Boolean flingTag = false;

	public class MessageObject {
		Integer _pos;
		String _key;
	}

	public void setViewheight(Integer height) {
		viewheight = height;
	}

	public View getRoot() {
		return root;
	}

	public ChatGridAction(View anchor, int columnwidth) {
		super(anchor);

		actionLst = new ArrayList<ActionItem>();
		context = anchor.getContext();
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (ViewGroup) inflater.inflate(R.layout.chatgridoptions, null);
		options = (GridView) root.findViewById(R.id.gridopts);
		if (columnwidth != 0)
			options.setColumnWidth(columnwidth);
		setContentView(root);
		((ContainedGridView) root).setViewObj(this);
		mainTitleTV = (TextView) root.findViewById(R.id.mainTitle);
	}

	public void setMainTitle(String mTitle) {
		this.mainTitle = mTitle;
	}

	public void setDisplayPosX(int posX) {
		displayX = posX;
	}

	public void setDisplayPosY(int posY) {
		displayY = posY;
	}

	public void addActionItem(ActionItem action) {
		actionLst.add(action);
	}

	public void show() {
		preShow();

		int xPos, yPos;
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		if (!TextUtils.isEmpty(mainTitle)) {
			mainTitleTV.setVisibility(View.VISIBLE);
			mainTitleTV.setText(mainTitle);
		}

		createActionList();

		if (displayY != -1) {
			int pleft, pright, pbottom;
			pleft = root.getPaddingLeft();
			pright = root.getPaddingRight();
			pbottom = root.getPaddingBottom();
			root.setPadding(pleft, 0, pright, pbottom);
		}

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootHeight = root.getMeasuredHeight();
		if (viewheight != null)
			rootHeight = viewheight;
		int rootWidth = root.getMeasuredWidth();

		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		
		if (displayX != -1)
			xPos = displayX;
		else
			xPos = ((screenWidth - rootWidth) / 2);
		if (displayY != -1)
			yPos = displayY;
		else
			yPos = anchorRect.top - rootHeight;

		Log.v("anchor", anchor.toString());
		window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	public class ChatGridAdapter extends ArrayAdapter<ActionItem> {
		final float scale;
		int width, height;

		public ChatGridAdapter(Context context, int textViewResourceId,
				ArrayList<ActionItem> objects) {
			super(context, textViewResourceId, objects);
			scale = context.getResources().getDisplayMetrics().density;
			width = (int) (75 * scale + 0.5f);
			height = (int) (75 * scale + 0.5f);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			// Log.v("get chat view ", String.valueOf(position));

			OnClickListener listener = actionLst.get(position).getListener();
			Integer bgcolor = actionLst.get(position).getBackgroundcolor();
			Drawable imgIcon = actionLst.get(position).getIcon();
			String title = actionLst.get(position).getTitle();
			;

			if (convertView == null) {
				if (bgcolor != null)
					view = inflater.inflate(R.layout.chatoption_fixed_item,
							null);
				else
					view = inflater.inflate(R.layout.chatoption_item, null);
			} else {
				view = convertView;
			}

			Boolean selected = actionLst.get(position).getSelected();

			if (selected != null && selected)
				view.setBackgroundResource(R.drawable.action_item_btn_sel);
			else
				view.setBackgroundResource(R.drawable.action_item_btn);

			ImageView img = (ImageView) view.findViewById(R.id.icon);
			img.setImageDrawable(imgIcon);
			TextView text = (TextView) view.findViewById(R.id.title);

			if (bgcolor != null) {
				img.setBackgroundColor(bgcolor);
				FrameLayout imgborder = (FrameLayout) img.getParent();
				if (selected != null && selected) {
					imgborder.setPadding(3, 3, 4, 0);
					imgborder.setBackgroundColor(0x00000000);
				} else {
					imgborder.setPadding(1, 1, 1, 1);
					imgborder.setBackgroundColor(0xff707070);
				}
			}
			if (title != null) {
				text.setVisibility(View.VISIBLE);
				text.setText(title);
				text.setTypeface(Typeface.DEFAULT);
			} else {
				text.setVisibility(View.GONE);
				title = actionLst.get(position).getHiddenTitle();
				;
				if (title != null)
					text.setText(title);
			}
			if (listener != null)
				view.setOnClickListener(listener);

			return view;
		}
	}

	private void createActionList() {
		try {
			actlist = new ChatGridAdapter(context, R.layout.chatoption_item,
					actionLst);
			options.setAdapter(actlist);
		} catch (Exception e) {
			Log.v("creation gridview exception", e.toString());
		}
	}
}