package com.hardsoft.alarm.location.adviser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Tips extends Activity {

	private int page = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tips_view);
		((ImageButton) findViewById(R.id.go_right)).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						nextPage();
					}
					
				});
	}
	
	private void nextPage() {
		page++;
		MainActivity.setInfoPannel(true, false, null);
		setContentView(R.layout.tips_view2);
		((ImageButton) findViewById(R.id.go_left)).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						previousePage();
					}
					
				});
	}
	
	private void previousePage() {
		// TODO Auto-generated method stub
		setContentView(R.layout.tips_view);
		page--;
		MainActivity.setInfoPannel(false, false, null);
		((ImageButton) findViewById(R.id.go_right)).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						nextPage();
					}
					
				});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (page>0)
			MainActivity.setInfoPannel(false, false, null);
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		startActivity(new Intent(getApplicationContext(), HelpManager.class));
	}
	
	
	

}
