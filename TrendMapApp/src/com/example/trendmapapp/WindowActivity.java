package com.example.trendmapapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class WindowActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_window);
		Bundle bun =  WindowActivity.this.getIntent().getExtras();
		
		TextView t = (TextView)findViewById(R.id.textView1);
		t.setText(bun.getString("search"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.window, menu);
		return true;
	}

}
