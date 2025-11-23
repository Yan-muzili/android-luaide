package com.yan.luaeditor.tools.dep;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.yan.luaide.R;

import java.util.Locale;

public class Material3ProgressDialog implements IProgressUpdater {

    private final Dialog dialog;
    private final TextView tvTitle, tvMsg;
    private final LinearProgressIndicator pbCurrent, pbFiles;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public Material3ProgressDialog(Context context) {
        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        View root = LayoutInflater.from(context).inflate(R.layout.download_dialog, null);
        tvTitle     = root.findViewById(R.id.title);
        tvMsg       = root.findViewById(R.id.message);
        pbCurrent   = root.findViewById(R.id.progress_current);
        pbFiles     = root.findViewById(R.id.progress_files);

        builder.setView(root)
               .setPositiveButton("后台", (d, w) -> d.dismiss())
               .setNegativeButton("取消", (d, w) -> d.dismiss());

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
    }

    public void show() { dialog.show(); }

    @Override
    public void dismiss() { mainHandler.post(dialog::dismiss); }

    @Override
    public void setTitle(String title) {
        mainHandler.post(() -> tvTitle.setText(title));
    }

    @Override
    public void setMessage(String msg) {
        mainHandler.post(() -> tvMsg.setText(msg));
    }
    @Override
    public void updateProgress1(int percent) {
        mainHandler.post(() -> pbCurrent.setProgress(percent));
    }


    @Override
    public void updateTotalProgress(String string,int finished, int total) {
        mainHandler.post(() -> {
            pbFiles.setProgress((int) (finished * 100L / total));
            setMessage(String.format(Locale.getDefault(),
                    string+"\n文件 %d/%d  平均进度 %d%%", finished, total,
                    (finished * 100 / total)));
        });
    }
}