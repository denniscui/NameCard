/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedcontact;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class BluetoothListActivity extends Activity {
	// Debugging
	private static final String TAG = "BluetoothListActivity";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final boolean D = true;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private BluetoothArrayAdapter mDevicesArrayAdapter;
	// private BluetoothArrayAdapter mNewDevicesArrayAdapter;

	// Member items
	private ArrayList<String> mDevicesNameArray;
	private ArrayList<String> mDevicesAddressArray;

	// 0 unpaired, 1 paired
	private ArrayList<Integer> mDevicesPairArray;

	private byte[] card;

	// private ArrayList<String> mNewDevicesArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.btdevicelist);

		TextView title = (TextView) this
				.findViewById(R.id.title_paired_devices);
		title.setText("Devices");

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		card = this.getIntent().getByteArrayExtra("bytes");

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setText("Scan");
		scanButton.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.buttongradient));
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		mDevicesNameArray = new ArrayList<String>();
		mDevicesAddressArray = new ArrayList<String>();
		mDevicesPairArray = new ArrayList<Integer>();

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mDevicesArrayAdapter = new BluetoothArrayAdapter(this,
				R.layout.btdevicelist, mDevicesNameArray);
		// mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
		// R.layout.btdevicelist);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// // Find and set up the ListView for newly discovered devices
		// ListView newDevicesListView = (ListView)
		// findViewById(R.id.new_devices);
		// newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		// newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mDevicesNameArray.add(device.getName());
				mDevicesAddressArray.add(device.getAddress());
				mDevicesPairArray.add(1);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle("Scanning");

		// // Turn on sub-title for new devices
		// findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();

	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			if (mBtAdapter.isDiscovering())
				mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v.findViewById(R.id.bt_num)).getText()
					.toString();
			String address = info.substring(info.length() - 17);
			Log.v("info", info);
			Log.v("address", address);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// Set result and finish this Activity
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mDevicesNameArray.add(device.getName());
					mDevicesAddressArray.add(device.getAddress());
					mDevicesPairArray.add(0);
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle("Select Device");
			}

			mDevicesArrayAdapter.notifyDataSetChanged();
		}
	};

	public class BluetoothArrayAdapter extends ArrayAdapter<String> {

		public BluetoothArrayAdapter(Context context, int resourceId,
				ArrayList<String> objects) {
			super(context, resourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.bluetoothrow, null);
			}

			if (position >= mDevicesNameArray.size())
				return row;
			String item = mDevicesNameArray.get(position);
			if (item == null)
				return row;

			TextView name = (TextView) row.findViewById(R.id.bt_name);
			name.setText(item);

			TextView address = (TextView) row.findViewById(R.id.bt_num);
			address.setText(mDevicesAddressArray.get(position));

			return row;
		}
	}
}