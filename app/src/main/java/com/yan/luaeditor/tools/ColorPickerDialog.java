package com.yan.luaeditor.tools;

import androidx.appcompat.app.AlertDialog;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import android.content.Context;
import com.yan.luaide.R;

public class ColorPickerDialog extends AlertDialog {

  private ColorPickerView colorPickerView;
  private ColorPanelView colorPickerPanel;
  private OnColorSelectedListener onColorSelectedListener;

  public ColorPickerDialog(Context context, int initialColor, OnColorSelectedListener listener) {
    super(context);
    this.onColorSelectedListener = listener;
    init(initialColor);
  }

  private void init(int initialColor) {
    setContentView(R.layout.dialog_color_picker);
    colorPickerView = findViewById(R.id.color_picker_view);
    colorPickerPanel = findViewById(R.id.color_picker_panel);

    colorPickerView.setColor(initialColor);
    colorPickerView.setOnColorChangedListener(
        new ColorPickerView.OnColorChangedListener() {
          @Override
          public void onColorChanged(int color) {
            colorPickerPanel.setColor(color);
            if (onColorSelectedListener != null) {
              onColorSelectedListener.onColorSelected(color);
            }
          }
        });
  }

  public interface OnColorSelectedListener {
    void onColorSelected(int color);
  }
}
