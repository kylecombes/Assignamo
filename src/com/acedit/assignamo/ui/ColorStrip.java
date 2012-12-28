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
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mDrawable.setBounds(0, 0, w, h);
	}
	
	protected void onDraw(Canvas canvas) {
		mDrawable.draw(canvas);
	}
	
	public void setColor(int color) {
		mDrawable.getPaint().setColor(color);
	}
	
	public int getColor() {
		return mDrawable.getPaint().getColor();
	}
}
