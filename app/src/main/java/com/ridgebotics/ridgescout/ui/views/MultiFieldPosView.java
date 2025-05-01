package com.ridgebotics.ridgescout.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ridgebotics.ridgescout.R;

import java.util.ArrayList;
import java.util.List;

public class MultiFieldPosView extends FrameLayout {
    private Paint paint;
    private static final float POINT_RADIUS = 10f;
    private ImageView imageView;
    private List<Integer[]> points = new ArrayList<>();

    public MultiFieldPosView(Context context) {
        super(context);
        init(context);
    }

    public MultiFieldPosView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        // Initialize paint
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);


        // Create and add ImageView
        imageView = new ImageView(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        addView(imageView);

        setImageResource(R.drawable.field_2025);

    }

    public void addPos(int[] pos){
        if(pos.length != 2) return;
        points.add(new Integer[]{
            pos[0],
            pos[1]
        });
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for(int i = 0; i < points.size(); i++){
            int x = points.get(i)[0];
            int y = points.get(i)[1];
            if (x >= 0 && y >= 0) {
                canvas.drawCircle(
                        ((float) x / 255) * getWidth(),
                        ((float) y / 255) * getHeight(),
                        POINT_RADIUS, paint);
                }
        }
    }

    public void setImageResource(int resId) {
        imageView.setImageResource(resId);
    }
}