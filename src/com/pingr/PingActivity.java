/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.pingr;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PingActivity extends Activity implements OnClickListener {

	public static Button pingButton;
	private static EditText targetEditText;
	// private EditText resultEditText;
	private InetAddress targetAddress;
	private ListView targetListView;
	public static TargetListAdapter adapter;

	/* ping timeout in ms */
	private static int PING_TIMEOUT = 1000;
	public static int greenThreshold, orangeThreshold, redThreshold;
	private static ArrayList<PingTarget> targetList = null;
	private SharedPreferences sharedPref;

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
		targetListView = (ListView) findViewById(R.id.list_target);
		targetListView.setAdapter(adapter);

		// resultEditText = (EditText) findViewById(R.id.editTextPingResult);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// read settings
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		greenThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_green), "200"));
		orangeThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_orange), "700"));
		redThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_red), "2000"));
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
			startActivity(new Intent(this, SettingsActivity.class));
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
			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(targetEditText.getWindowToken(),
					0);
			PingTarget mTarget = Pingr.pingAsyncTask(targetEditText.getText()
					.toString().trim(), PING_TIMEOUT);
			adapter.addTarget(mTarget);
			break;

		default:
			break;
		}
	}
}
