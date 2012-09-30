package com.acedit.assignamo.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.acedit.assignamo.R;

public class ColorStrip extends View {
	
	public ShapeDrawable mDrawable;
	public Resources res;
	private static short hPx;
	private static short wPx;
	
	public ColorStrip(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		res = getResources();
		
		wPx = (short)dipToPx((short)res.getDimension(R.dimen.color_strip_width));
		hPx = (short)dipToPx((short)res.getDimension(R.dimen.color_strip_height));
		
		mDrawable = new ShapeDrawable(new RectShape());
		mDrawable.setBounds(0, 0, wPx, hPx);
		
	}
	
	protected void onDraw(Canvas canvas) {
		mDrawable.draw(canvas);
	}
	
	public void setColor(int color) {
		mDrawable.getPaint().setColor(color);
	}
	
	public void setHeight(short pixels) {
		mDrawable.setBounds(0, 0, wPx, pixels);
	}
	
	public int getColor() {
		return mDrawable.getPaint().getColor();
	}
	
	
	private short dipToPx(short dip) {
		return (short)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
	}
}
