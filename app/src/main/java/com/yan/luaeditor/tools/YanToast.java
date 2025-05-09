package com.yan.luaeditor.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yan.luaide.R;

import java.util.Random;
public class YanToast extends Toast {
  @SuppressLint({"StaticFieldLeak"})
  public static YanToast toast;

  @SuppressLint({"StaticFieldLeak"})
  public static TextView toast_tv;

  public YanToast(Context context) {
    super(context);
  }

  public static void cancelToast() {
    YanToast apocalypse_Toast = toast;
    if (apocalypse_Toast != null) {
      apocalypse_Toast.cancel();
    }
  }

  public static void initToast(Context context, CharSequence charSequence) {
    cancelToast();
    toast = new YanToast(context);
    LayoutInflater inflater = LayoutInflater.from(context);
    View customView = inflater.inflate(R.layout.mtoast, null);
    TextView text = customView.findViewById(R.id.text);
    ImageView image = customView.findViewById(R.id.image);
    Random random = new Random();
    int rd = random.nextInt(6);
    switch (rd) {
      case 0:
        image.setBackgroundResource(R.drawable.icon);
        break;
      case 1:
        image.setBackgroundResource(R.drawable.ic_111);
        break;
      case 2:
        image.setBackgroundResource(R.drawable.ic_222);
        break;
      case 3:
        image.setBackgroundResource(R.drawable.ic_333);
        break;
      case 4:
        image.setBackgroundResource(R.drawable.ic_444);
        break;
      case 5:
        image.setBackgroundResource(R.drawable.ic_555);
        break;
      case 6:
        image.setBackgroundResource(R.drawable.ic_666);
        break;
    }
    text.setText(charSequence);
    toast.setView(customView);
  }

  public static void show(final Context context, final CharSequence charSequence) {
    if (charSequence != null) {
      ThreadManager.runOnMainThread(
          new Runnable() {

            @Override
            public void run() {
              initToast(context, charSequence);
              toast.show();
            }
          });
    }
  }
}
