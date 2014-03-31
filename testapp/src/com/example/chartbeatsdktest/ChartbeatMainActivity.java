package com.example.chartbeatsdktest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.chartbeat.androidsdk.Tracker;

/**
 * This is a sample Application for demonstrating and testing
 * the Chartbeat SDK. This minimal implementation
 * shows how to track views, user interaction, and a user entering/leaving the app.
 * 
 * 
 * @author bjorn
 *
 */
public class ChartbeatMainActivity extends Activity {
	private static final String TAG = "ChartbeatMainActivity";
	private static final String VIEW_ID = "A_VIEW_ID";
	private static final String VIEW_TITLE = "Test View";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chartbeat_main);
		Tracker.startTrackerWithAccountId("54876", this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chartbeat_main, menu);
		return true;
	}
	
	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		Tracker.userInteracted();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Tracker.trackView(VIEW_ID, VIEW_TITLE);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Tracker.userLeftView(VIEW_ID);
	}
	
	public void simulateTyping(View view) {
		Log.d(TAG, "Typing");
		Tracker.userTyped();
	}
	
	public void switchViews(View view) {
		Log.d(TAG, "Switching Views");
		Intent intent = new Intent(this,AltActivity.class);
		startActivity(intent);
	}
}
