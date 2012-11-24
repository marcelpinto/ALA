package com.hardsoft.alarm.location.adviser;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
//;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AlarmRing extends Activity implements OnSeekBarChangeListener {

	private Vibrator vib;
	private SeekBar sb;
	private MediaPlayer mPlayer;
	private WakeLock wakeLock;
	Window window;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_dialog);
		window = this.getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	/*	((Button)this.findViewById(R.id.sosb)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mPlayer.stop();
				vib.cancel();
				deleteAlarm();
				finish();
			}
			
		});*/
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		sb = (SeekBar)this.findViewById(R.id.stopBar);
		sb.setThumb(resize(this.getResources().getDrawable(R.drawable.thumb_icon),80,60));
		sb.setProgressDrawable(resize(this.getResources().getDrawable(R.drawable.seekbar_forground),100,60));
		sb.setBackgroundDrawable(resize(this.getResources().getDrawable(R.drawable.seekbar_background),width,60));
		sb.setOnSeekBarChangeListener(this);
		TranslateAnimation mAnimation = new TranslateAnimation(
	            TranslateAnimation.RELATIVE_TO_PARENT, 0f,
	            TranslateAnimation.RELATIVE_TO_PARENT, 1.0f,
	            TranslateAnimation.RELATIVE_TO_PARENT, 0f,
	            TranslateAnimation.RELATIVE_TO_PARENT, 0f);
	   mAnimation.setDuration(2000);
	   mAnimation.setRepeatCount(-1);
	   mAnimation.setRepeatMode(Animation.REVERSE);
	   mAnimation.setInterpolator(new LinearInterpolator());
	   ImageView f = (ImageView) findViewById(R.id.finger);
	   f.setAnimation(mAnimation);
        //.i("Ring", "rrrrrr");
        new AlarmTask().execute();
        
	}
	
	private void deleteAlarm() {
		Preference.writeInteger(this, Preference.DEST_LAT, 0);
		Preference.writeInteger(this, Preference.DEST_LON, 0);
		Preference.writeInteger(this, Preference.DEST_METERS, 0); //HARDCODE chage to var
		Preference.writeBoolean(this, Preference.ALARM_ACT, false);
	}

	
	private void ring() {
		String uri = Preference.readString(getApplicationContext(), Preference.RINGTONE, null);
		Uri alert;
	     if(uri == null){
	         // alert is null, using backup
	    	 alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	    	 if (alert == null)
	    		 alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	         if(alert == null)
	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
	     }
	     else 
	    	 alert = Uri.parse(uri);
	     //.i("URI", ""+alert.toString());
	     mPlayer = new MediaPlayer();
	     try {
		     mPlayer.setDataSource(this, alert);
		     mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		     mPlayer.setLooping(true);
		     mPlayer.prepare();
		     mPlayer.start();
	     }
	     catch(Exception e) {
	     //TODO : Implement Error Checking
	    	 e.printStackTrace();
	    	 //.e("MediaPlayer", "Error while playing!");
	     }
	}
	
	private void vibrate() {
		//.i("Vibration", "ON");
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
   	 	long[] pattern = { 0, 500, 200 };
   	 	vib.vibrate(pattern, 0);
   	 	//.i("Vibration", "SET");
	}
	
	private Drawable resize(Drawable image, int wdp, int hdp) {
	    Bitmap d = ((BitmapDrawable)image).getBitmap();
	    Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, wdp, hdp, false);
	    return new BitmapDrawable(bitmapOrig);
	}
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
		if (vib!=null) 
			vib.cancel();
		//wakeLock.release();
	}


	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		//.i("Value seekBar", ""+arg1);
		if (arg1 >= 19) {
			try {
				if (mPlayer != null)
					mPlayer.stop();
				if (vib!=null)
					vib.cancel();
				deleteAlarm();
				finish();
			}
			catch(Exception e) {
				//TODO : Implement Error Checking
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}


	private class AlarmTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			while (!window.isActive()) {}
			if (Preference.readBoolean(getApplicationContext(), Preference.VIBRATION, true))
				vibrate();
			if (Preference.readBoolean(getApplicationContext(), Preference.SOUND, true))
				ring();
			
			return null;
		}
	  
	}
}
