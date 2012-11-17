package com.hardsoft.alarm.location.adviser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
//;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


class MapOverlay extends Overlay {

	GeoPoint gp;
	boolean isVisible=false;
	boolean isAlarmFixed=false;
	OverlayItem posItem;
	ProgressBar pg;
	private boolean isPinch = false;
	
	private MapItems sourceItem;
	
	public void setPoint(GeoPoint g) {
		this.gp=g;
	}
	
	public GeoPoint getPoint() {
		return this.gp;
	}
	
	public boolean isVisible() {
		return this.isVisible;
	}
	
	public void setVisible(boolean b) {
		this.isVisible=b;
	}
	
	public void removeItem() {
		if (sourceItem==null || posItem==null) return;
		sourceItem.removeOverlay(posItem);
	}
	
	private Drawable resize(Drawable image) {
	    Bitmap d = ((BitmapDrawable)image).getBitmap();
	    Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, 50, 50, false);
	    return new BitmapDrawable(bitmapOrig);
	}

	//PRE: gp != null previous call to setGeoPoint()
	public void drawAim(MapView mapView) {
		//.i("Drawing", "aim");
		Drawable d = resize(mapView.getResources().getDrawable(R.drawable.aim));
		sourceItem = new MapItems(d, mapView.getContext());
		mapView.getOverlays().add(sourceItem);
		posItem = new OverlayItem(this.gp, "You are here:", "");
    	sourceItem.addOverlay(posItem);
    	mapView.getOverlays().add(sourceItem);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		// TODO Auto-generated method stub
		//.i("TAP", "TAP");
		if ( isPinch ){
	        return false;
	    }else{
	    	if (this.isVisible) return false;
			this.isVisible = true;
			if (Preference.readBoolean(mapView.getContext(), Preference.ALARM_ACT, false)) return true;
			this.gp=p;
			drawAim(mapView);
			//.i("Loading Bar", "start");
			MainActivity.setInfoPannel(true, true, null);
			//new findTapPoint(mapView).execute(mapView.getContext());
	        return false;
	    }
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView)
	{
	    int fingers = e.getPointerCount();
	    if( e.getAction()==MotionEvent.ACTION_DOWN ){
	        isPinch=false;  // Touch DOWN, don't know if it's a pinch yet
	    }
	    if( e.getAction()==MotionEvent.ACTION_MOVE && fingers==2 ){
	        isPinch=true;   // Two fingers, def a pinch
	    }
	    return super.onTouchEvent(e,mapView);
	}
	
	private class findTapPoint extends AsyncTask<Context, Void, String> {
		
		private MapView mapView;
		
		public findTapPoint(MapView mV) {
			this.mapView = mV;
		}

		@Override
		protected String doInBackground(Context... params) {
			// TODO Auto-generated method stub
	    	Geocoder gcd = new Geocoder(mapView.getContext(), Locale.getDefault());
	        List<Address> addresses=null;
			try {
				addresses = gcd.getFromLocation(gp.getLatitudeE6()/1e6, gp.getLongitudeE6()/1e6, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				addresses=null;
				e.printStackTrace();
			}
			String loc="";
	        if (addresses!=null && addresses.size() > 0)
	        	//loc = (addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
	            loc = addresses.get(0).getLocality();
			return loc;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			//pg.setActivated(false);
			MainActivity.setInfoPannel(true, true, result);
		}	
	}
	
/*	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sourceItem.removeOverlay(posItem);
		}
	}

	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Preference.writeInteger(parent, Preference.DEST_LAT, gp.getLatitudeE6());
			Preference.writeInteger(parent, Preference.DEST_LON, gp.getLongitudeE6());
			Preference.writeInteger(parent, Preference.DEST_METERS, 300); //HARDCODE chage to var
			Preference.writeBoolean(parent, Preference.ALARM_ACT, true);
	  	}
	}*/
	
	
}
