/*
 *	Copyright 2014 Deepankar Bhardwaj 
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

package org.zerogravity.pingr;

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

import org.zerogravity.pingr.PingTarget.STATUS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.Undoable;

public class PingActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {
	
	public static Button pingButton;
	public static TargetListAdapter adapter;
	private static final String TAG = "PingActivity";
	private static EditText targetEditText;
	private static EditText portEditText;
	private static List<PingTarget> targetList = null;
	private static String LIST_FILENAME = "pingr_target_list";
	private EnhancedListView targetListView;
	private SharedPreferences sharedPref;
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
		targetEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		targetEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		targetEditText.requestFocus();

		// targetEditText.setOnEditorActionListener(new
		// TextView.OnEditorActionListener() {
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		//
		//
		// return false;
		// }
		// });

		portEditText = (EditText) findViewById(R.id.editTextPort);
		targetListView = (EnhancedListView) findViewById(R.id.list_target);
		targetListView.setOnItemClickListener(this);
		targetListView.setOnItemLongClickListener(this);
		targetListView.setAdapter(adapter);
	
		//targetListView.setSwipingLayout(R.id.s)
		targetListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
			
			@Override
			public Undoable onDismiss(EnhancedListView listView, final int position) {
				final PingTarget item = adapter.getItem(position);
				adapter.remove(position);
				return new EnhancedListView.Undoable() {
					
					@Override
					public void undo() {
					 adapter.insert(item, position);
					}
				};
			}
		});

		targetListView.setUndoStyle(EnhancedListView.UndoStyle.MULTILEVEL_POPUP);
		targetListView.setRequireTouchBeforeDismiss(false);
		targetListView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);
		targetListView.enableSwipeToDismiss();
		
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
		// clear the current list
		// adapter.clear();
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
				if (adapter.isHostInList(readHostname)) {
					continue;
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
			
//			//Cause NPE to test ACRA
//			String NPE = null;
//			Log.v(NPE,NPE);
						
			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(targetEditText.getWindowToken(), 0);

			String host = targetEditText.getText().toString().toLowerCase()
					.trim();

			if (!isValidHost(host)) {
				Toast.makeText(this, "Invalid hostname!", Toast.LENGTH_SHORT)
						.show();
				break;
			}

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

	private boolean isValidHost(String host) {
		if (host.isEmpty()) {
			return false;
		}
		return true;
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

		if ( p.getStatus() != STATUS.PING_IN_PROGRESS) {
			// Pingr.pingAsyncTask(p, PING_TIMEOUT);
			p.ping();
		}
	}
}
