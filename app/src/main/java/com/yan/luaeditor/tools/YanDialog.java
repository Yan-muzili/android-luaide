package com.yan.luaeditor.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yan.luaide.R;

public class YanDialog extends AlertDialog{

  private Context mContext;
  private String mTitle;
  private String mMessage;
  public static YanDialog dialog;

  public YanDialog(@NonNull Context context) {
    super(context);
    this.mContext = context;
  }

  public static void cancelDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  public static void show(
      @NonNull Context context, @NonNull String title, @NonNull String message) {
    cancelDialog();
    dialog = new YanDialog(context);

    // 使用 MaterialAlertDialogBuilder 创建 AlertDialog
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

    // 加载自定义布局
    LayoutInflater inflater = LayoutInflater.from(context);
    View customView = inflater.inflate(R.layout.yan_dialog_layout, null);

    // 设置标题和消息
    TextView tit = customView.findViewById(R.id.dialog_title);
    TextView mess = customView.findViewById(R.id.dialog_message);
    tit.setText(title);
    mess.setText(message);

    // 设置自定义视图为布局
    builder.setView(customView);

    // 创建 AlertDialog
    final AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }
}
