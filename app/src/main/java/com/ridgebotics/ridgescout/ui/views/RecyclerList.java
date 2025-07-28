package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.List;

public class RecyclerList<T> extends RecyclerView {
    private RecyclerAdapter<T> adapter;

    public RecyclerList(Context context) {
        super(context);
        init();
    }

    public RecyclerList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecyclerList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // Set default layout manager
        setLayoutManager(new LinearLayoutManager(getContext()));

        // Enable optimizations
        setHasFixedSize(true);
        setItemViewCacheSize(20);
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    // Setup method to configure the RecyclerView
    public RecyclerList<T> setup(int layoutResId, RecyclerHolderFactory<T> RecyclerHolderFactory) {
        adapter = new RecyclerAdapter<>(layoutResId, RecyclerHolderFactory);
        setAdapter(adapter);
        return this;
    }

    // Layout manager convenience methods
    public RecyclerList<T> withLinearLayout() {
        setLayoutManager(new LinearLayoutManager(getContext()));
        return this;
    }

    public RecyclerList<T> withLinearLayout(int orientation) {
        setLayoutManager(new LinearLayoutManager(getContext(), orientation, false));
        return this;
    }

    public RecyclerList<T> withGridLayout(int spanCount) {
        setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        return this;
    }

    public RecyclerList<T> withDivider() {
        DividerItemDecoration divider = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        addItemDecoration(divider);
        return this;
    }

    public RecyclerList<T> withItemClickListener(RecyclerClickListener<T> listener) {
        if (adapter != null) {
            adapter.setOnItemClickListener(listener);
        }
        return this;
    }

    // Data management methods
    public void setItems(List<T> items) {
        if (adapter != null) {
            adapter.setItems(items);
        }
    }

    public void addItem(T item) {
        if (adapter != null) {
            adapter.addItem(item);
        }
    }

    public void addItem(int position, T item) {
        if (adapter != null) {
            adapter.addItem(position, item);
        }
    }

    public void removeItem(int position) {
        if (adapter != null) {
            adapter.removeItem(position);
        }
    }

    public void removeItem(T item) {
        if (adapter != null) {
            adapter.removeItem(item);
        }
    }

    public void updateItem(int position, T item) {
        if (adapter != null) {
            adapter.updateItem(position, item);
        }
    }

    public void clear() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    public T getItem(int position) {
        return adapter != null ? adapter.getItem(position) : null;
    }

    public List<T> getItems() {
        return adapter != null ? adapter.getItems() : null;
    }

    public RecyclerAdapter<T> getGenericAdapter() {
        return adapter;
    }
}