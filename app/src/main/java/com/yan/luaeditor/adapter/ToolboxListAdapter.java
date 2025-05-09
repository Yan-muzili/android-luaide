package com.yan.luaeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yan.luaeditor.ToolboxListItem;
import com.yan.luaide.R;

import java.util.List;

public class ToolboxListAdapter extends RecyclerView.Adapter<ToolboxListAdapter.ListViewHolder> {

    private List<ToolboxListItem> itemList;
    private OnItemClickListener mListener;
    public ToolboxListAdapter(List<ToolboxListItem> itemList) {
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toolbox_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ToolboxListItem item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.toolicon.setImageResource(item.getId());
        final int finalposition=position;
        holder.boxitemroot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(finalposition);
            }
        });
        //holder.descriptionTextView.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView toolicon;
        LinearLayout boxitemroot;
        //TextView descriptionTextView;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            toolicon=itemView.findViewById(R.id.tool_icon);
            boxitemroot=itemView.findViewById(R.id.box_item_root);
            //descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}