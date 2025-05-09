package com.yan.luaeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yan.luaide.R;

import java.util.List;

public class SymbolAdapter extends RecyclerView.Adapter<SymbolAdapter.SymbolViewHolder> {

    private Context context;
    private String[] symbolList;
    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public SymbolAdapter(Context context, String[] symbolList) {
        this.context = context;
        this.symbolList = symbolList;
    }

    @NonNull
    @Override
    public SymbolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.symbol_item, parent, false);
        return new SymbolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymbolViewHolder holder, int position) {
        String symbol = symbolList[position];
        holder.textViewSymbol.setText(symbol);
        final int finalposition=position;
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(finalposition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return symbolList.length;
    }

    public static class SymbolViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSymbol;
        LinearLayout layout;
        public SymbolViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSymbol = itemView.findViewById(R.id.symbol_text);
            layout=itemView.findViewById(R.id.symbol_item_layout);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}