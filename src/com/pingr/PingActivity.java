package com.pingr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.pingr.PingTarget.STATUS;

public class PingActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "PingActivity";
	public static Button pingButton;
	private static EditText targetEditText;
	// private EditText resultEditText;
	private InetAddress targetAddress;
	private ListView targetListView;
	public static TargetListAdapter adapter;

	/* ping timeout in ms */
	private static int PING_TIMEOUT = 1000;
	private static List<PingTarget> targetList = null;
	private SharedPreferences sharedPref;

	private static String LIST_FILENAME = "pingr_target_list";
	private File listFile;
	private FileOutputStream fos;
	private byte[] byte_buffer;
	private StringBuilder list_buffer;

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
		targetListView.setOnItemClickListener(this);
		targetListView.setOnItemLongClickListener(this);
		targetListView.setAdapter(adapter);

		loadListFromCache();

		// resultEditText = (EditText) findViewById(R.id.editTextPingResult);

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
		PingrApplication.orangeThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_orange), "700"));
		PingrApplication.redThreshold = Integer.valueOf(sharedPref.getString(
				getString(R.string.pref_key_red), "2000"));
	}

	private void loadListFromCache() {
		// read list from cache
		listFile = new File(getCacheDir(), LIST_FILENAME);
		String readHostname = new String();
		try {
			FileInputStream fis = new FileInputStream(listFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			while ((readHostname = br.readLine()) != null) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "adding " + readHostname + " t0 list");
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

			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(targetEditText.getWindowToken(), 0);

			PingTarget mTarget = new PingTarget(targetEditText.getText()
					.toString().toLowerCase().trim());
			adapter.addTarget(mTarget);
			// Pingr.pingAsyncTask(mTarget, PING_TIMEOUT);
			mTarget.ping();
			
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		
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
