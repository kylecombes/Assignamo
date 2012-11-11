package com.acedit.assignamo.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

public final class UiUtils {
	
	public static final short dipToPx(short dip, Resources res) {
		return (short)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
	}
	
	/**
	 * Change the alpha component of a color.
	 * @param color an integer representation of the color to modify.
	 * @param alpha the new alpha component value.
	 * @return the new color with the reduced alpha component.
	 */
	public static final int changeAlpha(int color, int alpha) {
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}
	
}
