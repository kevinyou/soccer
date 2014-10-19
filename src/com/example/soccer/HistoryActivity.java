package com.example.soccer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import android.app.Activity;
import android.os.Bundle;
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
		try {
			BufferedReader historyLog = new BufferedReader(new FileReader(new File (new URI("/history.log"))));
			TextView temp = new TextView(this);
			temp.setText(historyLog.readLine());
			linearLayout.addView(temp);
		} catch(IOException e){
			
		} catch (Exception e) {
			TextView temp = new TextView(this);
			temp.setText("No history");
			linearLayout.addView(temp);
			e.printStackTrace();
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
