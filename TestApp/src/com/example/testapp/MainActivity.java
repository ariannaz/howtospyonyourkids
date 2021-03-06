package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

/** Main activity. This is the initial window that gives the user a submit form. */
public class MainActivity extends Activity {

	protected final static String EXTRA_MESSAGE = "com.example.testapp.EXTRA_MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Launch server
		ServerSocket server = new ServerSocket();
		server.launch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void sendMessage(View view) {
		// On button click
		Intent i = new Intent(this, DisplayMessageActivity.class);
		EditText editText = (EditText) findViewById(R.id.edit_message);
		i.putExtra(EXTRA_MESSAGE, editText.getText().toString());
		startActivity(i);
	}
}
