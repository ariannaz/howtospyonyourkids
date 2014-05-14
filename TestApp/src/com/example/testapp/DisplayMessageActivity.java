package com.example.testapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/** Display activity. This activity displays text to the user. */
public class DisplayMessageActivity extends Activity {

	private final static int TEXT_SIZE = 40;

	private ClientSocket mClient;

	private TextView mTextView;

	public TextView getTextView() {
		return mTextView;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mClient = new ClientSocket();

		// Receive message
		Intent i = getIntent();
		String message = i.getStringExtra(MainActivity.EXTRA_MESSAGE);

		// Create the text view
		mTextView = new TextView(this);
		mTextView.setTextSize(TEXT_SIZE);
		mTextView.setText(message);
		mTextView.setMovementMethod(new ScrollingMovementMethod());
		mTextView.setBackgroundColor(Color.BLACK);
		mTextView.setTextColor(Color.CYAN);
		setContentView(mTextView);
		
		// Show the Up button in the action bar.
		setupActionBar();

		// Send data to server
		mClient.setServerIpAddress(message);

		String androidId = Secure.getString(getBaseContext()
				.getContentResolver(), Secure.ANDROID_ID);
		String deviceIdentifier = android.os.Build.MODEL + " (" + androidId
				+ ") connected\n";
		mClient.sendData(deviceIdentifier);
		String response = mClient.getResponse();

		// prints on the next line
		mTextView.setText(message + '\n' + response);
		setContentView(mTextView);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Do nothing
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
