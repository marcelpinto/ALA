package com.hardsoft.alarm.location.adviser;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.tapfortap.AppWall;
import com.tapfortap.AppWall.AppWallListener;
import com.tapfortap.TapForTap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MoreApps extends SherlockActivity implements OnClickListener, AppWallListener  {


	public static final String TAG = "AdMobActivity";
    private AdView adView;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		
		if (item.getItemId()==android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.more_view);
		ActionBar aBar = getSupportActionBar();
        aBar.setDisplayShowTitleEnabled(false);
        aBar.setHomeButtonEnabled(true);
        aBar.setDisplayHomeAsUpEnabled(true);
        configureLogo();
        configureAds();
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void configureAds() {
		// TODO Auto-generated method stub
		TapForTap.initialize(MoreApps.this, "3c2b05f3e9409e591e4ef24559cf0fba");
		AppWall.prepare(MoreApps.this);
		AppWall.setListener(this);
		((Button) findViewById(R.id.moreappb)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				// Later when you want to display the app wall
				//setProgressBarIndeterminateVisibility(true|false);
				AppWall.show(MoreApps.this);
			}
			
		});

	}

	private void configureLogo() {
		// TODO Auto-generated method stub
		ImageView v1 = (ImageView) findViewById(R.id.ala_plus_img);
		ImageView v2 = (ImageView) findViewById(R.id.doctorkey_img);
		ImageView v3 = (ImageView) findViewById(R.id.poker_img);
		
		v1.setTag(1);
		v2.setTag(2);
		v3.setTag(3);
		v1.setOnClickListener(this);
		v2.setOnClickListener(this);
		v3.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int rol = (Integer) arg0.getTag();
		setProgressBarIndeterminateVisibility(Boolean.TRUE);
		switch(rol) {
			case 1:
				final String appName = "com.hardsoft.alarm.location.adviser.plus";
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
				}
				break;
			case 2:
				final String appName2 = "com.hardsoft.doctorkey";
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName2)));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName2)));
				}
				break;
			case 3:
				final String appName3 = "com.hardsoft.pokerfriends";
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName3)));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName3)));
				}
				break;
		}
		
	}
	
	@Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		//setProgressBarIndeterminateVisibility(Boolean.FALSE);
	}


}
