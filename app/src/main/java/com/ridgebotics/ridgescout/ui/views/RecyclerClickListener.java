package com.ridgebotics.ridgescout.ui.views;

public interface RecyclerClickListener<T> {
    void onItemClick(T item, int position);
}