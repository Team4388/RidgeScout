package com.ridgebotics.ridgescout.ui.views;

import android.view.View;

public interface RecyclerHolderFactory<T> {
    RecyclerHolder<T> createViewHolder(View itemView);
}
