package com.yan.luaeditor.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yan.luaide.R;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;

/**
 * Default adapter to display results
 *
 * @author Rose
 */
public final class MyCompletionAdapter extends EditorCompletionAdapter {

    // 定义点击事件接口
    public interface OnItemClickListener {
        void onItemClick(String label, String desc, int pos);
    }

    // 定义一个成员变量来保存点击事件的回调
    private OnItemClickListener mListener;

    // 提供一个方法来设置点击事件的回调
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemHeight() {
        // 45 dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getContext().getResources().getDisplayMetrics());
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.completion_item_adapter, parent, false);
        }
        var item = getItem(pos);

        TextView tv = view.findViewById(R.id.result_item_label);
        tv.setText(item.label);
        tv.setTextColor(getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_PRIMARY));

        tv = view.findViewById(R.id.result_item_desc);
        tv.setText(item.desc);
        tv.setTextColor(getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY));

        view.setTag(pos);
        if (isCurrentCursorPosition) {
            view.setBackgroundColor(getThemeColor(EditorColorScheme.COMPLETION_WND_ITEM_CURRENT));
        } else {
            view.setBackgroundColor(0);
        }
        ImageView iv = view.findViewById(R.id.result_item_image);
        iv.setImageDrawable(item.icon);

        // 设置点击事件
        view.setOnClickListener((v) -> {
            TextView tvDesc = v.findViewById(R.id.result_item_desc);
            TextView tvLabel = v.findViewById(R.id.result_item_label);
            String label = tvLabel.getText().toString();
            String desc = tvDesc.getText().toString();
            if (mListener != null) {
                mListener.onItemClick(label, desc, pos);
            }
        });

        return view;
    }
}