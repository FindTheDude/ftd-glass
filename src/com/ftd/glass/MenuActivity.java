package com.ftd;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity{

	private static final String TAG = "FTDMenuActivity";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ftf, menu);
		return true;
	}	

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Nothing else to do, closing the Activity.
		finish();
	}	
	
    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "Option Selected.");
		switch (item.getItemId()) {
		case R.id.retake_picture:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
