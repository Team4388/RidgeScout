package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CandlestickHeader extends View {

    private float absoluteMin = 0;
    private float absoluteMax = 100;

    // Number of scale marks to show
    private final int SCALE_MARKS = 5;

    // Padding and dimensions
    private final int PADDING_LEFT = 50;
    private final int PADDING_RIGHT = 50;
    private final int PADDING_TOP = 10;
    private final int PADDING_BOTTOM = 10;
    private final int TICK_HEIGHT = 10;

    private Paint linePaint;
    private Paint textPaint;

    public CandlestickHeader(Context context) {
        super(context);
        init();
    }

    public CandlestickHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CandlestickHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(2f);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Set the scale range for the header
     *
     * @param absoluteMin Absolute minimum of the dataset
     * @param absoluteMax Absolute maximum of the dataset
     */
    public void setScale(float absoluteMin, float absoluteMax) {
        this.absoluteMin = absoluteMin;
        this.absoluteMax = absoluteMax;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 600;
        int desiredHeight = 80; // Height for the scale with labels

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw horizontal axis
        float y = height - PADDING_BOTTOM;
        canvas.drawLine(PADDING_LEFT, y, width - PADDING_RIGHT, y, linePaint);

        // Calculate interval between scale marks
        float availableWidth = width - PADDING_LEFT - PADDING_RIGHT;
        float intervalValue = (absoluteMax - absoluteMin) / (SCALE_MARKS - 1);
        float intervalPixels = availableWidth / (SCALE_MARKS - 1);

        // Draw scale marks and labels
        for (int i = 0; i < SCALE_MARKS; i++) {
            float x = PADDING_LEFT + (intervalPixels * i);
            float value = absoluteMin + (intervalValue * i);

            // Draw tick mark
            canvas.drawLine(x, y, x, y - TICK_HEIGHT, linePaint);

            // Draw label
            canvas.drawText(String.format("%.1f", value), x, y - TICK_HEIGHT - 10, textPaint);
        }
    }
}