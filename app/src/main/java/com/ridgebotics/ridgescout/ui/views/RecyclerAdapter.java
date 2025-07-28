package com.ridgebotics.ridgescout.ui.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerHolder<T>> {
    private List<T> items;
    private final int layoutResId;
    private final RecyclerHolderFactory<T> viewHolderFactory;
    private RecyclerClickListener<T> onItemClickListener;

    public RecyclerAdapter(int layoutResId, RecyclerHolderFactory<T> viewHolderFactory) {
        this.items = new ArrayList<>();
        this.layoutResId = layoutResId;
        this.viewHolderFactory = viewHolderFactory;
    }

    @Override
    public RecyclerHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return viewHolderFactory.createViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder<T> holder, int position) {
        T item = items.get(position);
        holder.bind(item, position);
        holder.setOnItemClickListener(item, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // List management methods
    public void setItems(List<T> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void addItem(T item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void addItem(int position, T item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeItem(T item) {
        int position = items.indexOf(item);
        if (position != -1) {
            removeItem(position);
        }
    }

    public void updateItem(int position, T item) {
        if (position >= 0 && position < items.size()) {
            items.set(position, item);
            notifyItemChanged(position);
        }
    }

    public void clear() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    public T getItem(int position) {
        return items.get(position);
    }

    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    public void setOnItemClickListener(RecyclerClickListener<T> listener) {
        this.onItemClickListener = listener;
    }
}
