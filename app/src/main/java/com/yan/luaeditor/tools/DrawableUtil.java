package com.yan.luaeditor.tools;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;

public class DrawableUtil {
  public static void setDrawableColor(View view, int color) {
    Drawable drawable = view.getBackground();
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    view.setBackground(drawable);
  }
}
