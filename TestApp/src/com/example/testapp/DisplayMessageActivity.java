package com.example.testapp;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/** Display activity. This activity displays text to the user. */
public class DisplayMessageActivity extends Activity {

	private final static int TEXT_SIZE = 20;

	private ClientSocket mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mClient = new ClientSocket();

		// Receive message
		Intent i = getIntent();
		String message = i.getStringExtra(MainActivity.EXTRA_MESSAGE);

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

		DecoratedView dv = new DecoratedView();
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("em");
		dv.appendText("Message: ", tags);
		dv.appendText(message + '\n' + response);
		TextView decoratedTextView = dv.getTextView(this);

		// Make it look prettier
		setContentView(R.layout.activity_display_message);
		TextView tv = (TextView) findViewById(R.id.display_message);
		tv.setText(decoratedTextView.getText());
		tv.setTextSize(TEXT_SIZE);
		tv.setTextColor(Color.CYAN);
		tv.setMovementMethod(new ScrollingMovementMethod());
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

	public void sendMessage(View view) {
		// Allow sending custom text to server
		EditText cmdText = (EditText) findViewById(R.id.cmd_message);
		String cmdString = cmdText.getText().toString();
		Log.d("DisplayMessageActivity", "Command read: " + cmdString);
		mClient.sendData(ServerSocket.CMD_PREFIX + cmdString);
		String response = mClient.getResponse();
		Log.d("DisplayMessageActivity", "Command received: " + response);
		TextView tv = (TextView) findViewById(R.id.display_message);
		tv.setText(response);
	}

}
