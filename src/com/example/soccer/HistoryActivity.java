package com.example.soccer;

import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.info);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		SharedPreferences sp = getSharedPreferences("com.example.soccer.DATA", MODE_PRIVATE);
		Map<String, ?> spTree = sp.getAll();
		for (Map.Entry<String, ?> entry : spTree.entrySet()){
			Log.v("ADDING STUFF", entry.toString());
			TextView text = new TextView(this);
			text.setText(entry.getKey() + " " + entry.getValue());
			linearLayout.addView(text);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
