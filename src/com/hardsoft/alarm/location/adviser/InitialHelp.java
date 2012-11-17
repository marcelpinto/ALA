package com.hardsoft.alarm.location.adviser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
//;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InitialHelp extends Activity {


	private boolean isOver = false;
	private Handler mHandler = new Handler();
	private int counter = 0;
	private Button tap;
	private Runnable button_animation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ini_view);
		if (!Preference.readBoolean(getApplicationContext(), Preference.ISFIRST, true)) {
        	startActivity(new Intent(this, MainActivity.class));
        	finish();
        }
		Preference.writeBoolean(getApplicationContext(), Preference.ISFIRST, false);
		tap = (Button) findViewById(R.id.tapButton);
		button_animation = new Runnable() {
		  @Override
		  public void run() {
			  switch (counter) {
			  case 0:
				  tap.setText(">           ");
				  break;
			  case 1:
				  tap.setText(">>          ");
				  break;
			  case 2:
				  tap.setText(">>  T       ");
				  break;
			  case 3:
				  tap.setText(">>  TA      ");
				  break;
			  case 4:
				  tap.setText(">>  TAP     ");
				  break;
			  case 5:
				  tap.setText(">>  TAP  >  ");
				  break;
			  case 6:
				  tap.setText(">>  TAP  >>");
				  break;
			  }
			  counter++;
			  if (counter>6)counter = 0;
			  mHandler.postDelayed(button_animation, 100);
		  }
		};
		mHandler.postDelayed(button_animation, 100);
		tap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//.i("Click", "go to help");
				tap.setText("LOADING");
				startActivity(new Intent(InitialHelp.this,MainActivity.class));
				finish();
			}
			
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}



}
