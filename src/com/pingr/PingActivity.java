package com.pingr;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PingActivity extends Activity implements OnClickListener {

	private Button pingButton;
	private EditText targetEditText;
	// private EditText resultEditText;
	private InetAddress targetAddress;
	private ListView targetListView;
	public static TargetListAdapter adapter;

	/* ping timeout in ms */
	private static int PING_TIMEOUT = 1000;
	private static ArrayList<PingTarget> targetList = null;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ping, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.buttonPing :
				PingTarget mTarget = Pingr.pingAsyncTask(targetEditText
						.getText().toString().trim(), PING_TIMEOUT);
				adapter.addTarget(mTarget);				
				break;

			default :
				break;
		}
	}
}
