package com.hardsoft.alarm.location.adviser;

import java.util.ArrayList;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener {

	public LocationService() {
	}
	
	static final int MSG_REGISTER_CLIENT = 1;
	
	static final int MSG_UNREGISTER_CLIENT = 2;
	
	static final int MSG_SET_VALUE = 3;
	
	static final int LOC_FOUND = 4;
	
	static final int LOC_NOT_FOUND = 5;
	
	static final int MSG_ALARM_SET = 6;
	
	static final int MSG_ALARM_OFF = 7;
	
	static final int MSG_GPS_NOTFOUND = 8;
	
	static final int MSG_IS_GPS_ON = 9;
	
    private static final int TWO_MINUTES = 1000 * 60 * 2;

	
	
	private static final int mId = 1;

	private static final int FREQ_UPDATE_METERS = 10000;

	private static final float FREQ_UPDATE_METERS_BIG = 1000;

	private static final long FREQ_UPDATE_BIG = 1000*60*2;

	private static final long FREQ_UPDATE_SMALL = 1000*30;

	private static final float FREQ_UPDATE_METERS_SMALL = 100;
	
	
	private LocationManager loc_manager;
	private String provider;
	private int meters=0;
	
	
	int alarmDist = 400; //Mes endevant aixo haura de ser una preferencia
	
	// Messenger to comunicate with client (MapAct) \\
	final Messenger inMessenger = new Messenger(new IncomingHandler());
	
	//Last location knew\\
	private Location lastLoc;
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;
	//Array list of the Clients, right now just one\\
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	
	class IncomingHandler extends Handler {
		@Override
	    public void handleMessage(Message msg) {
			switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SET_VALUE:
            	if (lastLoc==null) {
            		enableLocation();
            	}
            	sendLocationToActivity(lastLoc);
            	break;
            case MSG_ALARM_SET:
            	checkDistToDest(lastLoc);
            	createNotification();
            	break;
            case MSG_ALARM_OFF:
            	cancelNotification();
            	break;
            case MSG_IS_GPS_ON:
            	if (!loc_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            		//.i("Service", "GPS OFF");
            		sendNoGPS();
            	}
            	else 
            		//.i("Service", "GPS ON");
            	
            	break;
            default:
				super.handleMessage(msg);
			}
		}
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		enableLocation();
		//createNotification();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cancelNotification();
	}



	private void cancelNotification() {
		if (mBuilder==null || mNotificationManager==null) return;
		mNotificationManager.cancel(mId);
	}
	
	private void updateNotification() {
		if (mBuilder==null || mNotificationManager==null) createNotification();
		Date dt = new Date();
		double m = (meters/(double)1000);
		if (m>5) m = Math.round(m);
		mBuilder.setContentText("Distance to target: "+m+"Km")
				.setWhen(dt.getTime());
		mNotificationManager.notify(mId, mBuilder.build());
	}
	
	private void createNotification() {
		//.i("notify", "yes "+ meters);
		double m = (meters/(double)1000);
		if (m>5) m = Math.round(m);
		mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.alarmicon)
		        .setContentTitle("Alarm Location Adviser ON")
		        .setContentText("Distance to target: "+m+"Km")
		        .setAutoCancel(false);
		Intent resultIntent = new Intent(this, MainActivity.class);
		/*resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// Creates the PendingIntent
		PendingIntent resultPendingIntent =
		        PendingIntent.getActivity(
		        this,
		        0,
		        resultIntent,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);*/
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		stackBuilder.addParentStack(MainActivity.class);

		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_CANCEL_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			mNotificationManager.notify(mId, n);
	}
	
	private void enableLocation() {
		loc_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        provider = loc_manager.getBestProvider(criteria, false);
        Location location = loc_manager.getLastKnownLocation(provider);
        loc_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FREQ_UPDATE_BIG, FREQ_UPDATE_METERS_BIG, this);
        //loc_manager.requestLocationUpdates(provider, 0, 0, this);
        // Initialize the location fields
        
        if (location != null) {
        	//sendLocationToActivity(location);
        	onLocationChanged(location);
        } else {
          //.e("Provider", "No previous location know");
        }
	}

	private void checkDistToDest(Location loc) {
		if (loc==null) return;
		double lat = Preference.readInteger(getApplicationContext(), Preference.DEST_LAT, 0);
		double lon = Preference.readInteger(getApplicationContext(), Preference.DEST_LON, 0);
		if (lat==0 && lon==0) return;
		Location destLoc = new Location("");
		destLoc.setLatitude(lat/1e6);
		destLoc.setLongitude(lon/1e6);
		meters = (int) loc.distanceTo(destLoc);
		if (meters>FREQ_UPDATE_METERS) {
			//loc_manager.removeUpdates(this);
			loc_manager.requestLocationUpdates(provider, FREQ_UPDATE_BIG, FREQ_UPDATE_METERS_BIG, this);
		}
		else {
			//loc_manager.removeUpdates(this);
			loc_manager.requestLocationUpdates(provider, FREQ_UPDATE_SMALL, FREQ_UPDATE_METERS_SMALL, this);
		}
		updateNotification();
		//Toast.makeText(getApplicationContext(), "Hola", Toast.LENGTH_SHORT).show();
		//.v("Meters to target:", ""+meters);
		if (meters<= Preference.readInteger(getApplicationContext(), Preference.DEST_METERS, 300)) {
			Preference.writeBoolean(getApplicationContext(), Preference.ALARM_ACT, false);
			Preference.writeInteger(getApplicationContext(), Preference.DEST_LAT, 0);
			Preference.writeInteger(getApplicationContext(), Preference.DEST_LON, 0);
			Intent intent = new Intent(getBaseContext(), AlarmRing.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags( Intent.FLAG_FROM_BACKGROUND);
			startActivity(intent);
			//this.stopSelf();
			//.v("Alarma", "on");
		}
		
	}

    private void sendLocationToActivity(Location location) {
    	if (mClients.isEmpty()) return;
    	Message msg = Message.obtain();
    	msg.what = MSG_SET_VALUE;
    	Bundle bundle = new Bundle();
    	Date d = new Date();
    	/*//.i("Get time", "date"+d.getTime());
    	//.i("Get time loc", "date"+location.getTime());*/
    	
    	if (location!=null ) {
    		//.i("Location", "FOUND prevloc time= "+location.getTime()+ " Time now = "+ d.getTime());
    		//.i("Location", "FOUND prevloc time= "+(location.getTime()+(1000*60*60)));
    	}
    	if (location == null || (location.getTime()+(1000*60*60)) < d.getTime()) 
    		bundle.putBoolean("isFound", false);
    	else {
    		//.i("Location", "found");
    		bundle.putBoolean("isFound", true);
    		bundle.putDouble("LAT", location.getLatitude());
    		bundle.putDouble("LON", location.getLongitude());
    	}
        msg.setData(bundle);
    	try {
    		//.i("Message", "sending");
			mClients.get(0).send(msg);
			//.i("Message", "sended");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    


    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
	@Override
	public void onLocationChanged(Location location) {
		//.v("Service", "running");
		if (isBetterLocation(location, lastLoc)) 
			lastLoc = location;
		//.i("New Location", "Lat "+lastLoc.getLatitude()+" LON "+lastLoc.getLongitude());
		sendLocationToActivity(lastLoc);
		if (Preference.readBoolean(getApplicationContext(), Preference.ALARM_ACT, false)) {
			//.i("alarm", "True");
			checkDistToDest(location);
		}
	}

	@Override
	public void onProviderDisabled(String disprovider) {
		// TODO Auto-generated method stub
		//.i("Provider", "disabled new provider = "+disprovider);
		if (disprovider.equals(LocationManager.GPS_PROVIDER)) {
			//Toast.makeText(this, "GPS disabled", Toast.LENGTH_SHORT).show();
			if (!Preference.readBoolean(getApplicationContext(), Preference.GPS_INFO, false))
				sendNoGPS();
			Criteria criteria = new Criteria();
	        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	        this.provider = loc_manager.getBestProvider(criteria, false);
	        //.i("Provider", "new provider = "+provider);
	        //Location location = loc_manager.getLastKnownLocation(provider);
	        loc_manager.requestLocationUpdates(this.provider, FREQ_UPDATE_BIG, FREQ_UPDATE_METERS_BIG, this);
			//loc_manager.requestLocationUpdates(this.provider, FREQ_UPDATE_BIG, FREQ_UPDATE_METERS_BIG, this);
			
		}
	}
	
	private void sendNoGPS() {
    	if (mClients.isEmpty()) return;
    	Message msg = Message.obtain();
    	msg.what = MSG_GPS_NOTFOUND;
    	try {
    		//.i("Message", "sending");
			mClients.get(0).send(msg);
			//.i("Message", "NOGPS sended");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void onProviderEnabled(String newprovider) {
		// TODO Auto-generated method stub
		//.i("Provider", "enabled new provider = "+newprovider);
		if (newprovider.equals(LocationManager.GPS_PROVIDER)) {
			this.provider=newprovider;
			loc_manager.requestLocationUpdates(newprovider, FREQ_UPDATE_BIG, FREQ_UPDATE_METERS_BIG, this);
		}
		else {
			//.i("other prov", "other");
			if (!Preference.readBoolean(getApplicationContext(), Preference.GPS_INFO, false))
				sendNoGPS();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		switch (status) {
        case LocationProvider.AVAILABLE:
            Log.v("Service", "available");
            break;
        case LocationProvider.OUT_OF_SERVICE:
        	Log.v("Service", "out_of_service");
            
            break;
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
        	Log.v("Service", "temporarily_unavailable");
            break;
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
	    // Return our messenger to the Activity to get commands
	    return inMessenger.getBinder();
	}
		 
}
