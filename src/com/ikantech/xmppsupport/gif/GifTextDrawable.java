package com.ikantech.xmppsupport.gif;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class GifTextDrawable extends AnimationDrawable {

	private TextView tv = null;
	private int curFrame = -1;

	private boolean Stop = false;
	
	public GifTextDrawable(TextView tv) {
		setTextView(tv);
	}

	public void setTextView(TextView tv) {
		this.tv = tv;
	}

	@Override
	public void invalidateDrawable(Drawable who) {
		// TODO Auto-generated method stub
		super.invalidateDrawable(who);
	}

	@Override
	public boolean selectDrawable(int idx) {
		// TODO Auto-generated method stub
		curFrame = idx;
		return super.selectDrawable(idx);
	}

	@Override
	public void scheduleSelf(Runnable what, long when) {
		// TODO Auto-generated method stub

		if (!Stop) {
			if (tv != null) {
				tv.postInvalidate();
				tv.postDelayed(this, this.getDuration(curFrame));
			}
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
		Stop = true;
	}
}
