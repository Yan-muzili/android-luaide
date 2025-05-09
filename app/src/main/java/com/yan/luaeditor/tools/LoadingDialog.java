package com.yan.luaeditor.tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.luaide.R;
public class LoadingDialog extends Dialog {

    private TextView messageTextView;
    private ImageView imageView;
    private Animation rotateAnimation;

    public LoadingDialog(Context context) {
        super(context);
        // 移除 requestWindowFeature 的调用，它应该在 onCreate 中调用
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                             WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.dialog_loading);

        messageTextView = findViewById(R.id.messageTextView);
        imageView = findViewById(R.id.imageView);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);

        imageView.startAnimation(rotateAnimation);
    }

    public void setMessage(String message) {
        if (messageTextView != null) {
            messageTextView.setText(message);
        }
    }

    public static class Builder {
        private Context context;
        private String message;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public LoadingDialog create() {
            LoadingDialog dialog = new LoadingDialog(context);
            dialog.setMessage(message);
            return dialog;
        }
    }
}