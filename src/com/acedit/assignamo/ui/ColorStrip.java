package com.acedit.assignamo.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import com.acedit.assignamo.R;

public class ColorStrip extends View {
	
	public ShapeDrawable mDrawable;
	private short hPx = 0;
	private short wPx = 0;
	
	public ColorStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDrawable = new ShapeDrawable(new RectShape());
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ColorStrip, 0, 0);
		try {
			int color = a.getInt(R.styleable.ColorStrip_color, 0);
			if (color != 0)
				setColor(color);
		} finally {
			a.recycle();
		}
		
	}
	
	protected void onDraw(Canvas canvas) {
		if (wPx == 0)
			wPx = (short) getWidth();
		if (hPx == 0)
			hPx = (short) getHeight();
		mDrawable.setBounds(0, 0, wPx, hPx);
		mDrawable.draw(canvas);
	}
	
	public void setColor(int color) {
		mDrawable.getPaint().setColor(color);
	}
	
	public void setHeight(short pixels) {
		hPx = pixels;
	}
	
	public void setWidth(short pixels) {
		wPx = pixels;
	}
	
	public int getColor() {
		return mDrawable.getPaint().getColor();
	}
}
