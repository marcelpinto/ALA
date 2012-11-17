package com.hardsoft.alarm.location.adviser;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
//;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapItems extends ItemizedOverlay<OverlayItem> {

	Context mContext;
	
	public MapItems(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	public void setDrawable(Drawable d) {
		boundCenterBottom(d);
	}

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void removeOverlay(OverlayItem overlay) {
		mOverlays.remove(overlay);
		populate();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}
	
	/*@Override
	protected boolean onTap(int index) {
		//.i("entra", "si");
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return false;
	}*/
	
	
	

}
