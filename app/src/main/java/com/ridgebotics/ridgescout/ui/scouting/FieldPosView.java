package com.ridgebotics.ridgescout.ui.scouting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ridgebotics.ridgescout.R;

public class FieldPosView extends FrameLayout {
    private int x = -1;
    private int y = -1;
    private Paint paint;
    private static final float POINT_RADIUS = 10f;
    private ImageView imageView;
    private boolean enabled = true;

    public interface onTapListener {
        void onUpdate(int[] pos);
    };

    public FieldPosView(Context context) {
        super(context);
        init(context, pos -> {});
    }

    public FieldPosView(Context context, onTapListener tapListener) {
        super(context);
        init(context, tapListener);
    }

    public FieldPosView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, pos -> {});
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context, onTapListener tl) {
        // Initialize paint
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);


        // Create and add ImageView
        imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        addView(imageView);

        // Set touch listener
        setOnTouchListener((v, event) -> {
            if (enabled && event.getAction() == MotionEvent.ACTION_DOWN) {
                x = (int) ((event.getX()/getWidth())*255);
                y = (int) ((event.getY()/getHeight())*255);
                tl.onUpdate(getPos());
                invalidate();
                return true;
            }
            return false;
        });

        setImageResource(R.drawable.field_2025);

    }

    public void setPos(int[] pos){
        if(pos.length == 0) return;
        x = pos[0];
        y = pos[1];
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (x >= 0 && y >= 0) {
            canvas.drawCircle(
                    ((float) x /255)*getWidth(),
                    ((float) y /255)*getHeight(),
                POINT_RADIUS, paint);
        }
    }

    public void setImageResource(int resId) {
        imageView.setImageResource(resId);
    }

    public void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public int[] getPos() {
        return new int[]{
                x,
                y
        };
    }
}