package com.hardsoft.alarm.location.adviser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class MainActivity extends SherlockMapActivity implements OnClickListener, OnSeekBarChangeListener, OnEditorActionListener, Runnable
 {
	
	private static final int BUTTON_YES 		= 0;
	private static final int BUTTON_NO 			= 1;
	private static final int BUTTON_X 			= 2;
	private static final int BUTTON_REMOVE_YES 	= 3;
	private static final int BUTTON_REMOVE_NO 	= 4;
	private static final int BUTTON_FINDME 		= 5;
	
	private static final String TXT_POINT = "Point on the map";
	
	
	private static final int REMOVE_ALARM  = 10;
	private static final int GPS_NOT_FOUND =11;
	
	private static final String AD_CODE = "a1509ab9b1adae4";
	

	ActionBar aBar;
	private MapView mapView;
	private MapController mapControl;
	private MapItems sourceItem;
	private MapItems destItem;
	private List<Overlay> mapOverlays;
	private OverlayItem posItem;
	private OverlayItem touchedItem;
	private MapOverlay mapO;
	private static LinearLayout infoP;
	private static LinearLayout textInfo;
	private static LinearLayout dialP;
	private static AutoCompleteTextView addrsBox;
	private TextView mTxt;
	private SeekBar mBar;
	private Button yesB;
	private TextView loadTxt;
	private FrameLayout loadFrame;
	
	private Thread thread;  
    //Create a Thread handler to queue code execution on a thread  
    private Handler handler;
	private int typePannel; 
	
	
	private static boolean isAddressBoxModify=false;
	
	private AdView adView;
	
	private boolean isLoading = true;
	
 
	
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle data = msg.getData();
		      //.i("Handling", "message");
		      switch (msg.what) {
		      case LocationService.MSG_SET_VALUE:
		    	  //Stop loading position
		    	  if (!data.getBoolean("isFound")) {
		    		  isLoading = true;
		    		  return;
		    	  }
		    	  
		    	  removeItem();
		    	  setIconOverlay(data.getDouble("LAT"),data.getDouble("LON"));
		    	  Preference.writeString(MainActivity.this, Preference.POS_LAT, Double.toString(data.getDouble("LAT")));
		    	  Preference.writeString(MainActivity.this, Preference.POS_LON, Double.toString(data.getDouble("LON")));
		    	  //.i("Handling", "message, location found "+Double.toString(data.getDouble("LAT")));
		    	  if (mapO.getPoint()!=null && Preference.readBoolean(getApplicationContext(), Preference.ALARM_ACT, false)) {
		    		  //.i("msg...", "centering map");
		    		  int lat = mapO.getPoint().getLatitudeE6();
		    		  int lon = mapO.getPoint().getLongitudeE6();
		  			  GeoPoint point = new GeoPoint(lat,lon);
		  			  centerMap(point);
		    	  }
		    		  
		    	  isLoading = false;
		    	  break;
		      case LocationService.MSG_GPS_NOTFOUND:
		    	  //.i("Message NOGPS", "RECIVED");
		    	  if (!Preference.readBoolean(getApplicationContext(), Preference.GPS_INFO, false)) {
		    		  setDialogPannel(GPS_NOT_FOUND);
		    	  }
		    	  break;
		      default:
				  super.handleMessage(msg);
		      }
		}
		
	}
	  
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = new Messenger(binder);
			//.i("Service", "Conected");
			try {
	            Message msg = Message.obtain(null,
	                    LocationService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            mService.send(msg);
	            
	            requestLocation();
	        } catch (RemoteException e) {
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			//.i("Service", "Disconected");
		}
	};
	
	private void requestLocation() {
		//.i("Service", "requesting conection");
		try {
            Message msg = Message.obtain(null,
                    LocationService.MSG_SET_VALUE);
            mService.send(msg);
        } catch (RemoteException e) {
        	//.e("Error while sending msg", ""+e.getMessage());
        }
	}
	
	private void sendAlarmActivated() {
		//.i("Service", "Alarm Conected");
		try {
            Message msg = Message.obtain(null,
                    LocationService.MSG_ALARM_SET);
            mService.send(msg);
        } catch (RemoteException e) {
        	//.e("Error while sending msg", ""+e.getMessage());
        }
	}
	
	private void sendAlarmDesactivated() {
		//.i("Service", "Alarm Desconected");
		try {
            Message msg = Message.obtain(null,
                    LocationService.MSG_ALARM_OFF);
            mService.send(msg);
        } catch (RemoteException e) {
        	//.e("Error while sending msg", ""+e.getMessage());
        }
	}
	
	private void requestGPS() {
		//.i("Service", "GPS conected?");
		try {
			if (mService==null) return;
            Message msg = Message.obtain(null,
                    LocationService.MSG_IS_GPS_ON);
            mService.send(msg);
        } catch (RemoteException e) {
        	//.e("Error while sending msg", ""+e.getMessage());
        }
	}
	
	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
		Intent intent = new Intent(this.getApplicationContext(), LocationService.class);
		startService(intent);
	    bindService(intent, conn, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}
	
	void doUnbindService() {
	    if (mIsBound) {
	        // If we have received the service, and hence registered with
	        // it, then now is the time to unregister.
	        if (mService != null) {
	            try {
	                Message msg = Message.obtain(null,
	                        LocationService.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e) {
	                // There is nothing special we need to do if the service
	                // has crashed.
	            }
	        }

	        // Detach our existing connection.
	        unbindService(conn);
	        mIsBound = false;
	    }
	}
	
	
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		doUnbindService();
		if (!Preference.readBoolean(this, Preference.ALARM_ACT, false)) stopService(new Intent(this, LocationService.class)); 
		
		//alarm.SetAlarm(this);
	}

    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aBar = getSupportActionBar();
        aBar.setDisplayShowTitleEnabled(false);
        aBar.setHomeButtonEnabled(true);
        final Map<String, Object> data = (Map<String, Object>) getLastNonConfigurationInstance();
        
        //.i("onCreate", "in");
        loadFrame = (FrameLayout) findViewById(R.id.loadingFrame);
        loadTxt = (TextView) findViewById(R.id.loadingText);
        if (data == null) {
	      //Initialize the handler  
	        handler = new Handler();  
	        //Initialize the thread  
	        thread = new Thread(this, "ProgressDialogThread");  
	        //start the thread  
	        thread.start();
        }
        configureUI();
        configureAdMob();
        configureMapView();
        configureMapItems();
        if (data != null) {
        	mapView.setVisibility(View.VISIBLE);
        	loadFrame.setVisibility(View.INVISIBLE);
        	if (((Boolean) data.get("Tap"))) {
        		//.v("Recovering", "Point");
        		GeoPoint p = (GeoPoint) data.get("TapPoint");
        		if (p!= null) {
        			//.i("Set Point", "after config");
        			mapO.setPoint(p);
        			mapO.setVisible(true);
        			mapO.drawAim(mapView);
        			setInfoPannel(true, true, null);
        		}
        		else 
        			setInfoPannel(true, false, null);
        	}
        }
        
    }
	
	
	private void configureAdMob() {
		// TODO Auto-generated method stub
		adView = new AdView(this, AdSize.BANNER , AD_CODE);
		LinearLayout layout = (LinearLayout)findViewById(R.id.admobFrame);
	    layout.addView(adView);
	    layout.setGravity(Gravity.BOTTOM);
	    adView.loadAd(new AdRequest());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId()==android.R.id.home) {
			//.i("in", "in");
			Builder builder = new AlertDialog.Builder(this);
		      builder.setMessage("HardSoft Studios\n" +
		      		"Follow us on @HardSoftStudios\n");
		      builder.setCancelable(true);
		      builder.setPositiveButton("Follow", new OkOnClickListener());
		      builder.setNegativeButton("Not now", new CancelOnClickListener());
		      AlertDialog dialog = builder.create();
		      dialog.show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}

	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//Start twitter
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mobile.twitter.com/HardSoftstudios"));
			startActivity(i);
		}
	}
	
	

	private void configureUI() {
		Button findme = (Button) findViewById(R.id.findme);
		infoP = (LinearLayout) this.findViewById(R.id.infoPannel);
		dialP = (LinearLayout) this.findViewById(R.id.dialogPannel);
		textInfo = ((LinearLayout) findViewById(R.id.textlayout));
		addrsBox = (AutoCompleteTextView)this.findViewById(R.id.adressbox);
		addrsBox.setOnEditorActionListener(this);
		AddressPredictor ap = new AddressPredictor(this);
		AddressPredictor.PlacesAutoCompleteAdapter auto = ap.new PlacesAutoCompleteAdapter(this, R.layout.list_item);
		addrsBox.setAdapter(auto);
		addrsBox.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (inputManager.isActive(addrsBox)) 
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				addrsBox.selectAll();
				if (!isAddressBoxModify) {
					mapO.setVisible(false);
					mapO.removeItem();
					mapView.postInvalidate();
				}
				isAddressBoxModify=true;
			}
			
		});
		addrsBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//.i("Click on box", "is modify? "+isAddressBoxModify);
				if (!isAddressBoxModify)
					addrsBox.selectAll();
			}
			
		});
		mTxt = (TextView)this.findViewById(R.id.meterbox);
		mBar = (SeekBar)this.findViewById(R.id.meterbar);
		mTxt.setText(""+mBar.getProgress());
		yesB = (Button)this.findViewById(R.id.yesb);
		Button noB = (Button)this.findViewById(R.id.nob);
		Button remYes = (Button)this.findViewById(R.id.removeYes);
		Button remNo = (Button)this.findViewById(R.id.removeNo);
		yesB.setTag(BUTTON_YES);
		noB.setTag(BUTTON_NO);
		remYes.setTag(BUTTON_REMOVE_YES);
		remNo.setTag(BUTTON_REMOVE_NO);
		findme.setTag(BUTTON_FINDME);
		yesB.setOnClickListener(this);
		noB.setOnClickListener(this);
		remYes.setOnClickListener(this);
		remNo.setOnClickListener(this);
		findme.setOnClickListener(this);
		mBar.setOnSeekBarChangeListener(this);
		//setBackground();
		((CheckBox) findViewById(R.id.checkBoxGPS)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				//.i("GPS checkbox", "is: "+ arg1);
				Preference.writeBoolean(getApplicationContext(), Preference.GPS_INFO, arg1);
			}
			
		});
		infoP.setVisibility(View.GONE);
		
	}
	
    private void configureMapView() {
    	mapView  = (MapView) findViewById(R.id.mapview);
    	mapView.setVisibility(View.INVISIBLE);
        mapView.setBuiltInZoomControls(false);
        mapView.setSatellite(false);
        mapView.setKeepScreenOn(true);
        int maxZoom = mapView.getMaxZoomLevel();
        int initZoom = maxZoom-5;
        mapControl = mapView.getController();
        mapControl.setZoom(initZoom);
        
    }
    
    private void configureMapItems() {
    	//mapView.getOverlays().clear();
    	mapOverlays = mapView.getOverlays();
    	mapO = new MapOverlay();
    	if (Preference.readBoolean(MainActivity.this, Preference.ALARM_ACT, false)) {
      		GeoPoint p = new GeoPoint(Preference.readInteger(MainActivity.this, Preference.DEST_LAT, 0),
      				Preference.readInteger(MainActivity.this, Preference.DEST_LON, 0));
      		mapO.setVisible(true);
      		mapO.setPoint(p);
      		mapO.drawAim(mapView);
      	}
    	if (sourceItem!=null) {
    		//.i("Previous guy Item", "removing");
    		removeItem();
    	}
    	Drawable drawable = this.getResources().getDrawable(R.drawable.guyicon);
    	sourceItem = new MapItems(drawable, this);
    	mapOverlays.add(mapO);
    }
    
    public List<Address> inversGeoLocation(double lat, double lon) {
    	Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses=null;
		try {
			addresses = gcd.getFromLocation(lat, lon, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			addresses=null;
			e.printStackTrace();
		}
		return addresses;
    }
    
    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (infoP.getVisibility()==View.VISIBLE) {
			mapO.isVisible = false;
			mapO.removeItem();
			mapView.postInvalidate();
			MainActivity.setInfoPannel(false, false, null);
			//.v("Button:", "infopannel off");
		}
		else if (dialP.getVisibility()==View.VISIBLE) {
			dialP.setVisibility(View.INVISIBLE);
		}
		else 
			super.onBackPressed();
	}

	private void setIconOverlay(double lat, double lon) {
    	//.i("set home", "lat"+lat+" lon"+lon);
    	int latE6 =  (int) (lat*1e6);
        int lonE6 = (int) (lon*1e6);
        GeoPoint gp = new GeoPoint(latE6, lonE6);
        /*List<Address> addresses = inversGeoLocation(lat,lon);
        String loc="";
        if (addresses!=null && addresses.size() > 0) 
            loc = addresses.get(0).getLocality();*/
    	posItem = new OverlayItem(gp, "You are here:", "");
    	sourceItem.addOverlay(posItem);
    	mapOverlays.add(sourceItem);
    	mapControl.animateTo(gp);
    }
    

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	private boolean isLocationServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.hardsoft.alarm.location.adviser.LocationService".equals(service.service.getClassName())) {
	        	//.i("Running", "yes");
	            return true;
	        }
	    }
	    return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.menu_new_alarm:
			if (!Preference.readBoolean(MainActivity.this, Preference.ALARM_ACT, false)) {
				setInfoPannel(true, false, null);
			}
			else 
				setDialogPannel(REMOVE_ALARM);
			break;
		case R.id.menu_stop_alarm:
			if (Preference.readBoolean(MainActivity.this, Preference.ALARM_ACT, false))
				setDialogPannel(REMOVE_ALARM);
			else 
				Toast.makeText(getApplicationContext(), "Set an Alarm first", Toast.LENGTH_SHORT).show();
	    	break;
		case R.id.menu_settings:
			//Start new fragment project settings
			item.getSubMenu().findItem(R.id.set_sound).setChecked(Preference.readBoolean(getApplicationContext(), Preference.SOUND, true));
			item.getSubMenu().findItem(R.id.set_vibration).setChecked(Preference.readBoolean(getApplicationContext(), Preference.VIBRATION, true));
			break;
		case R.id.set_ringtoon:
			showRingtonePicker();
			break;
		case R.id.set_exit:
			//First display dialog with info, like:
			// If you press OK the location service and the Alarm will turn off
			/*Preference.writeBoolean(MainActivity.this, Preference.ALARM_ACT, false);
			Preference.writeInteger(getApplicationContext(), Preference.DEST_LAT, 0);
			Preference.writeInteger(getApplicationContext(), Preference.DEST_LON, 0);*/
			if (Preference.readBoolean(getApplicationContext(), Preference.ALARM_ACT, false)) {
				doUnbindService();
				stopService(new Intent(this, LocationService.class));
			}
			finish();
			break;
		case R.id.set_sound:
			Preference.writeBoolean(MainActivity.this, Preference.SOUND, !item.isChecked());
			item.setChecked(!item.isChecked());
			return true;
		case R.id.set_vibration:
			Preference.writeBoolean(MainActivity.this, Preference.VIBRATION, !item.isChecked());
			item.setChecked(!item.isChecked());
			return true;
		case R.id.set_tips:
			startActivity(new Intent(getApplicationContext(), HelpManager.class));
			break;
		case R.id.set_plus:
			/*final String appName = "com.hardsoft.alarm.location.adviser";
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
			}*/
			Toast.makeText(getApplicationContext(), "Comming soon", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void setDialogPannel(int type) {
		if (infoP.getVisibility()==View.VISIBLE)
			infoP.setVisibility(View.INVISIBLE);
		TranslateAnimation _tAnim = new TranslateAnimation(-100, 0, 0, 0);

	    _tAnim.setInterpolator(new BounceInterpolator());

	    _tAnim.setDuration(1000);
	    dialP.setAnimation(_tAnim);
		dialP.setVisibility(View.VISIBLE);
		typePannel = type;
		TextView txt = ((TextView) findViewById(R.id.infoTextP));
		switch (type) {
		case REMOVE_ALARM:
			txt.setText(R.string.remove);
			((CheckBox) findViewById(R.id.checkBoxGPS)).setVisibility(View.GONE);
			break;
		case GPS_NOT_FOUND:
			txt.setText(R.string.gps);
			((CheckBox) findViewById(R.id.checkBoxGPS)).setVisibility(View.VISIBLE);
			break;
		}
		
		
	}
	
	private void removeAlarm() {
		
		Preference.writeBoolean(MainActivity.this, Preference.ALARM_ACT, false);
		Preference.writeInteger(getApplicationContext(), Preference.DEST_LAT, 0);
		Preference.writeInteger(getApplicationContext(), Preference.DEST_LON, 0);
		sendAlarmDesactivated();
    	mapO.setVisible(false);
		mapO.removeItem();
		mapView.postInvalidate();
    	/*double lat = Double.parseDouble(Preference.readString(getApplicationContext(), Preference.POS_LAT, "0"));
    	double lon = Double.parseDouble(Preference.readString(getApplicationContext(), Preference.POS_LON, "0"));
    	setIconOverlay(lat,lon);*/
	}
	
	private void showRingtonePicker() {
		Intent Mringtone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

		//specifies what type of tone we want, in this case "ringtone", can be notification if you want
		Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);

		//gives the title of the RingtoneManager picker title
		Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose the Alert ringtone");

		//returns true shows the rest of the songs on the device in the default location
		Mringtone.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, true);
		startActivityForResult(Mringtone, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent Mringtone) {
		switch (resultCode) {
		case RESULT_OK:
		//sents the ringtone that is picked in the Ringtone Picker Dialog
			Uri uri = Mringtone.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (uri==null) {
				Preference.writeBoolean(getApplicationContext(), Preference.SOUND, false);
				return;
			}
			Preference.writeString(this, Preference.RINGTONE, uri.toString());
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (adView != null) {
		      adView.destroy();
		}
		if (isLocationServiceRunning()) {
			Log.v("Service", "is still running");
			if (!Preference.readBoolean(getApplicationContext(), Preference.ALARM_ACT, false))
				stopService(new Intent(MainActivity.this, LocationService.class));
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//.i("Onresume", "in");
		if (!Preference.readBoolean(getApplicationContext(), Preference.ALARM_ACT, false) &
				Preference.readInteger(getApplicationContext(), Preference.DEST_METERS, 300)==0) {
			if (mapO!=null && mapO.isVisible()) {
				//.i("Onresume", "remove AIM");
				mapO.setVisible(false);
				mapO.removeItem();
				mapView.postInvalidate();
			}
		}
		/*if (isLocationServiceRunning()) {
			Message msg = Message.obtain(null, LocationService.MSG_SET_VALUE);
            try {
				mService.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				//.e("Message:", "not send");
				e.printStackTrace();
			}
		}*/
		doBindService();
	}
	
	
	public void removeItem() {
		//.i("Removing", "item");
		if (posItem==null) return;
		//.i("Removing", "item removed");
		mapOverlays.remove(sourceItem);
		sourceItem.removeOverlay(posItem);
		mapView.invalidate();
	}
	
	private Drawable resize(Drawable image, int h, int w) {
	    Bitmap d = ((BitmapDrawable)image).getBitmap();
	    Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, w, h, false);
	    return new BitmapDrawable(bitmapOrig);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public static void setInfoPannel(boolean b, boolean mapPress, String loc) {
		if (b) {
			if (dialP.getVisibility()==View.VISIBLE)
				dialP.setVisibility(View.INVISIBLE);
			infoP.setVisibility(View.VISIBLE);
			TranslateAnimation _tAnim = new TranslateAnimation(-100, 0, 0, 0);

		    _tAnim.setInterpolator(new BounceInterpolator());

		    _tAnim.setDuration(1000);
		    addrsBox.startAnimation(_tAnim);
		    infoP.startAnimation(_tAnim);
		    textInfo.startAnimation(_tAnim);
			//inversGeoLocation(p.getLatitudeE6()/1e6, p.getLongitudeE6()/1e6);
			if (mapPress) {
				//.i("MapPressed", "select all "+isAddressBoxModify);
				addrsBox.setText(TXT_POINT);
				addrsBox.setTextColor(Color.BLUE);
				addrsBox.selectAll();
				addrsBox.dismissDropDown();
				isAddressBoxModify=false;
			}
			else {
				isAddressBoxModify=true;
				addrsBox.setTextColor(Color.GRAY);
				addrsBox.setText("");
			}
				
		}
		else {
			infoP.setVisibility(View.GONE);
		}
	}

	
	private void centerMap(GeoPoint point) {
		int lat0 = (int) (Double.parseDouble(Preference.readString(getApplicationContext(), Preference.POS_LAT, "0"))*1e6);
    	int lon0 = (int) (Double.parseDouble(Preference.readString(getApplicationContext(), Preference.POS_LON, "0"))*1e6);
    	int lat1 = point.getLatitudeE6();
    	int lon1 = point.getLongitudeE6();
    	//.d("SIZE", ""+lat1+" "+lat0);
    	int maxLat = Math.max(lat0, lat1);
    	int minLat = Math.min(lat0, lat1);
    	int maxLon = Math.max(lon0, lon1);
    	int minLon = Math.min(lon0, lon1);
		mapControl.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
		mapControl.animateTo(new GeoPoint( (maxLat + minLat)/2, 
		(maxLon + minLon)/2 )); 
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		//.v("ButtonClick:", "Click");
		int id = (Integer) arg0.getTag();
		//InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		/*if (inputManager.isActive(addrsBox)) 
			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*/
		switch(id) {
			case BUTTON_YES:
				//.i("Button:", "YES");
				yesB.setVisibility(View.INVISIBLE);
				setAlarm();
				yesB.setVisibility(View.VISIBLE);
				break;
			case BUTTON_NO:
				//.v("Button:", "NO");
				mapO.setVisible(false);
				mapO.removeItem();
				mapView.postInvalidate();
				MainActivity.setInfoPannel(false, false, null);
				//.v("Button:", "infopannel off");
				break;
			case BUTTON_X:
				//.v("Button:", "infopannel clean");
				addrsBox.setText("");
				break;
			case BUTTON_REMOVE_YES:
				if (typePannel == REMOVE_ALARM) removeAlarm();
				else 
					showGPSConfiguration();
				dialP.setVisibility(View.GONE);
				break;
			case BUTTON_REMOVE_NO:
				dialP.setVisibility(View.GONE);
				break;
			case BUTTON_FINDME:
				requestLocation();
				break;
			default:
				return;
		}
	}
	
	private void showGPSConfiguration() {
		// TODO Auto-generated method stub
		startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}

	private void setAlarm() {
		int lat, lon;
		lat = lon = 0;
		if (MainActivity.isAddressBoxModify) {
			Geocoder gcd = new Geocoder(this, Locale.getDefault());
			List<Address> addresses=null;
			try {
				addresses = gcd.getFromLocationName(MainActivity.addrsBox.getText().toString(), 5);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				addresses=null;
				e.printStackTrace();
			}
			////.i("Get @", "from "+this.addrsBox.getText().toString()+" found "+addresses.size());
			if (addresses != null && addresses.size()>0) {
				lat = (int) (addresses.get(0).getLatitude()*1e6);
				lon = (int) (addresses.get(0).getLongitude()*1e6);
				GeoPoint point = new GeoPoint(lat,lon);
				//.i("Point: ", ""+point.getLatitudeE6()+" "+point.getLongitudeE6());
				mapO.setPoint(point);
				mapO.drawAim(mapView);
				centerMap(point);
				mapView.postInvalidate();
			}
			else {
				//.i("found?", "false");
				return;
			}
			//.i("found?", "true");	
			
		}
		else {
			lat = mapO.getPoint().getLatitudeE6();
			lon = mapO.getPoint().getLongitudeE6();
			GeoPoint point = new GeoPoint(lat,lon);
			centerMap(point);
			mapView.postInvalidate();
		}
		Preference.writeInteger(MainActivity.this, Preference.DEST_LAT, lat);
		Preference.writeInteger(MainActivity.this, Preference.DEST_LON, lon);
		Preference.writeInteger(MainActivity.this, Preference.DEST_METERS, Integer.parseInt(mTxt.getText().toString())); //HARDCODE chage to var
		Preference.writeBoolean(MainActivity.this, Preference.ALARM_ACT, true);
		sendAlarmActivated();
		MainActivity.setInfoPannel(false, false, null);
		//displayRoute(lat,lon);
	}
	

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		//offset = arg1;
		mTxt.setText(""+(arg1+300));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		//arg0.setProgress(0);
		/*arg0.setProgress(offset);
		final Drawable d = getResources().getDrawable(R.drawable.thumbmbar_incl);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()));
		arg0.setThumb(d);
		//.i("Offset", ""+offset);
		arg0.setProgress(offset);*/
	}

	
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		//arg0.setProgress(0);
		/*arg0.setProgress(offset);
		final Drawable d = getResources().getDrawable(R.drawable.thumbmbar);
		d.setBounds(new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()));
		arg0.setThumb(d);
		//.i("Offset", ""+offset);
		arg0.setProgress(offset);*/
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		if (!isAddressBoxModify) {
			mapO.setVisible(false);
			mapO.removeItem();
			mapView.postInvalidate();
		}
		//.i("EditorAction", "in");
		if (MainActivity.addrsBox.getText().equals(TXT_POINT))
			isAddressBoxModify = false;
		else 
			isAddressBoxModify = true;
		return false;
	}
	
	
 
	
	private int counter = 0;
	
	@Override  
    public void run()  
    {  
        try  
        {  
        	
            //Obtain the thread's token  
            synchronized (thread)  
            {  
                //While the counter is smaller than four  
                while(counter <= 6 && isLoading)  
                {  
                    //Wait 850 milliseconds   
                    thread.wait(1500);  
                    //Increment the counter  
                    counter++;
                    if (counter>=6) thread.wait(1000);
                    //update the changes to the UI thread  
                    handler.post(new Runnable()  
                    {  
                        @Override  
                        public void run()  
                        {  
                            //Set the current progress.  
                        	switch (counter) {
                        	case 1:
                        		loadTxt.setText(R.string.mapLoad);
                        		break;
                        	case 2:
                        		loadTxt.setText(R.string.posLoad);
                        		break;
                        	case 3:
                        		loadTxt.setText(R.string.relaxLoad);
                        		break;
                        	case 4:
                        		loadTxt.setText(R.string.posLoad);
                        		break;
                        	case 5:
                        		
                        		loadTxt.setText(R.string.noLocation);
                        		break;
                        	}
                        	TranslateAnimation _tAnim = new TranslateAnimation(-100, 0, 0, 0);

                		    _tAnim.setInterpolator(new BounceInterpolator());

                		    _tAnim.setDuration(900);
                        	loadTxt.setAnimation(_tAnim);
                        	
                        }  
                    });  
                }  
            }  
        }  
        catch (InterruptedException e)  
        {  
            e.printStackTrace();  
        }  
  
        //This works just like the onPostExecute method from the AsyncTask class  
        handler.post(new Runnable()  
        {  
            @Override  
            public void run()  
            {  
            	//set invisible
            	loadFrame.setVisibility(View.GONE);
            	if (mapView!=null) mapView.setVisibility(View.VISIBLE);
            	if (isLoading) requestLocation();
            	requestGPS();
            	
            }  
        });  
  
        //Try to "kill" the thread, by interrupting its execution  
        synchronized (thread)  
        {  
            thread.interrupt();  
        }  
    }
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final Map<String, Object> data = collectData();
	    return data;
	}
	
	private Map<String,Object> collectData() {
		Map<String, Object> tmp = new HashMap<String, Object>();
		if (infoP.getVisibility()==View.VISIBLE) {
			//.v("collecting data", "map");
			tmp.put("Tap", true);
			tmp.put("TapText", addrsBox.getText().toString());
			if (mapO.isVisible()) {
				//.v("collecting data", "point");
				int lat = mapO.getPoint().getLatitudeE6();
				int lon = mapO.getPoint().getLongitudeE6();
				GeoPoint point = new GeoPoint(lat,lon);
				tmp.put("TapPoint", point);
			}
			else
				tmp.put("TapPoint", null);
		}
		else 
			tmp.put("Tap", false);
		tmp.put("NoLoad", true);
		return tmp;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		//loadFrame.setVisibility(View.GONE);
	}  
	
	
	
}
