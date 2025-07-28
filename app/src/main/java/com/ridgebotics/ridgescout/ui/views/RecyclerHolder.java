package com.ridgebotics.ridgescout.ui.views;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerHolder<T> extends RecyclerView.ViewHolder {
    public RecyclerHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T item, int position);

    // Optional method for handling item clicks
    public void setOnItemClickListener(T item, RecyclerClickListener<T> listener) {
        if (listener != null) {
            itemView.setOnClickListener(v -> listener.onItemClick(item, getAdapterPosition()));
        }
    }
}