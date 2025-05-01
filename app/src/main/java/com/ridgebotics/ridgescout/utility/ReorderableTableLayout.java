package com.ridgebotics.ridgescout.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.view.MotionEvent;
import android.widget.TableLayout;

public class ReorderableTableLayout extends TableLayout {
    private boolean reorderingEnabled = false;
    private int draggedRowIndex = -1;
    private float lastY;
    private List<View> rows;
    private List<Integer> reorderedIndices;
    private int rowHeight;

    public ReorderableTableLayout(Context context) {
        super(context);
        init();
    }

    public ReorderableTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rows = new ArrayList<>();
        reorderedIndices = new ArrayList<>();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!reorderingEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                draggedRowIndex = getRowIndexAtY(lastY);
                if (draggedRowIndex != -1) {
                    View draggedRow = getChildAt(draggedRowIndex);
                    rowHeight = draggedRow.getHeight();
                    saveOriginalOrder();
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        reorderedIndices.add(reorderedIndices.size());
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        reorderedIndices.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!reorderingEnabled || draggedRowIndex == -1) {
            return super.onTouchEvent(event);
        }

        float currentY = event.getY();
        int targetIndex = getRowIndexAtY(currentY);
        View child = getChildAt(targetIndex);
        if(child != null)
            getChildAt(targetIndex).callOnClick();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (targetIndex != -1 && targetIndex != draggedRowIndex) {
                    updateRowOrder(draggedRowIndex, targetIndex);
                    draggedRowIndex = targetIndex;
                }
                lastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                draggedRowIndex = -1;
                break;
        }
        return true;
    }

    private int getRowIndexAtY(float y) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (y >= child.getTop() && y <= child.getBottom()) {
                return i;
            }
        }
        return -1;
    }

    private void saveOriginalOrder() {
        rows.clear();
        for (int i = 0; i < getChildCount(); i++) {
            rows.add(getChildAt(i));
        }
    }

    public void updateRowOrder(int fromIndex, int toIndex) {
        saveOriginalOrder();
        if (fromIndex < toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                Collections.swap(rows, i, i + 1);
                Collections.swap(reorderedIndices, i, i + 1);
            }
        } else {
            for (int i = fromIndex; i > toIndex; i--) {
                Collections.swap(rows, i, i - 1);
                Collections.swap(reorderedIndices, i, i - 1);
            }
        }

        removeAllViewsInLayout();
        for (View view : rows) {
            addViewInLayout(view, -1, view.getLayoutParams(), true);
        }
        requestLayout();
        invalidate();
    }

    public void setReorderingEnabled(boolean enabled) {
        reorderingEnabled = enabled;
    }

    public List<Integer> getReorderedIndexes() {
        return reorderedIndices;
    }

    public void removeElement(int unshuffledindex){
        System.out.println(Arrays.toString(new List[]{reorderedIndices}));

        reorderedIndices.remove(unshuffledindex);

        for (int i = 0; i < reorderedIndices.size(); i++) {
            if(reorderedIndices.get(i) > unshuffledindex)
                reorderedIndices.set(i, reorderedIndices.get(i) - 1);
        }

        System.out.println(Arrays.toString(new List[]{reorderedIndices}));
    }
}