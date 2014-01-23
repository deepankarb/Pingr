/*
 *	Copyright 2013 Deepankar Bhardwaj 
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License. 
 *	You may obtain a copy of the License at 
 * 	
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 * 
 */

package com.pingr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pingr.PingTarget.STATUS;

public class PingActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "PingActivity";
	public static Button pingButton;
	private static EditText targetEditText;
	private static EditText portEditText;
	private ListView targetListView;
	public static TargetListAdapter adapter;

	private static List<PingTarget> targetList = null;
	private SharedPreferences sharedPref;

	private static String LIST_FILENAME = "pingr_target_list";
	private File listFile;
	private FileOutputStream fos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ping);
		

		if (targetList == null) {
			targetList = new ArrayList<PingTarget>();
		}

		adapter = new TargetListAdapter(this, R.layout.list_target, targetList);
		pingButton = (Button) findViewById(R.id.buttonPing);
		pingButton.setOnClickListener(this);
		targetEditText = (EditText) findViewById(R.id.editTextTarget);
		portEditText = (EditText) findViewById(R.id.editTextPort);
		targetListView = (ListView) findViewById(R.id.list_target);
		targetListView.setOnItemClickListener(this);
		targetListView.setOnItemLongClickListener(this);
		targetListView.setAdapter(adapter);

		loadListFromCache();

	}

	@Override
	protected void onPause() {

		super.onPause();
		saveListToCache();
	}

	private void saveListToCache() {

		// open a file in cache dir
		listFile = new File(getCacheDir(), LIST_FILENAME);

		// write hostnames to the file
		try {
			fos = new FileOutputStream(listFile);
			PrintWriter pw = new PrintWriter(fos);
			for (PingTarget pt : adapter.getTargetList()) {
				pw.println(pt.getHostname());
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// read settings
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		PingrApplication.greenThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_green), "200"));
		PingrApplication.orangeThreshold = Integer.valueOf(sharedPref
				.getString(getString(R.string.pref_key_orange), "700"));
		PingrApplication.redThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_red), "2000"));
	}

	private void loadListFromCache() {
		//  clear the current list
		adapter.clear();
		// read list from cache
		listFile = new File(getCacheDir(), LIST_FILENAME);
		String readHostname = new String();
		try {
			FileInputStream fis = new FileInputStream(listFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			while ((readHostname = br.readLine()) != null) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "adding " + readHostname + " to list");
				}
				adapter.add(new PingTarget(readHostname));
			}

			br.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private void clearList() {
		adapter.clear();
		listFile = new File(getCacheDir(), LIST_FILENAME);
		listFile.delete();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ping, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
				startActivity(new Intent(this, SettingsActivity.class));
			} else {
				Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.action_clear_list:			
			clearList();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.buttonPing:

			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(targetEditText.getWindowToken(), 0);

			String host = targetEditText.getText().toString().toLowerCase()
					.trim();

			int port = 80;
			try {
				port = Integer
						.valueOf(portEditText.getText().toString().trim());
			} catch (NumberFormatException e) {

				e.printStackTrace();
			}

			PingTarget mTarget = new PingTarget(host, port);
			adapter.addTarget(mTarget);
			mTarget.ping();

			break;

		default:
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, parent.toString() + " " + view.toString() + " "
					+ position + " " + id);
		}

		PingTarget p = (PingTarget) parent.getItemAtPosition(position);

		if (BuildConfig.DEBUG) {
			Log.d(TAG, p.getHostname());
		}

		if (p.getStatus() != STATUS.PING_IN_PROGRESS)
			// Pingr.pingAsyncTask(p, PING_TIMEOUT);
			p.ping();
	}
}
