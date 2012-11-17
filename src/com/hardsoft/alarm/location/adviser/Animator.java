package com.hardsoft.alarm.location.adviser;

import android.content.Context;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;

public class Animator {

	private Context mContext;
	private View mView;
	
	public Animator(Context c, View v) {
		this.mContext = c;
		this.mView = v;
	}
	
	public void animate() {
		TranslateAnimation _tAnim = new TranslateAnimation(-100, 0, 0, 0);

	    _tAnim.setInterpolator(new BounceInterpolator());

	    _tAnim.setDuration(1000);
	    mView.startAnimation(_tAnim);
	}
}
