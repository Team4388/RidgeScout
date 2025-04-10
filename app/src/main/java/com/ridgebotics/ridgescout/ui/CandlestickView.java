package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.ui.data.DataProcessing;

import java.util.List;

public class CandlestickView extends View {

    // Data points
    private float min = 0;
    private float max = 0;
    private float lowerQuartile = 0;
    private float upperQuartile = 0;
    public float average = 0;

    // Dataset absolute bounds for scaling
    private float absoluteMin = 0;
    private float absoluteMax = 100;

    // Padding and dimensions
    private final int PADDING_LEFT = 50;
    private final int PADDING_RIGHT = 50;
    private final int PADDING_TOP = 20;
    private final int PADDING_BOTTOM = 20;
    private final int CANDLESTICK_HEIGHT = 60;
    private final int WHISKER_HEIGHT = 10;

    // Paint objects
    private Paint boxPaint;
    private Paint whiskerPaint;
    private Paint averagePaint;
    private Paint textPaint;

    public CandlestickView(Context context) {
        super(context);
        init();
    }

    public CandlestickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CandlestickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paint objects
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2f);

        whiskerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiskerPaint.setColor(Color.GREEN);
        whiskerPaint.setStrokeWidth(2f);
        whiskerPaint.setStyle(Paint.Style.STROKE);

        averagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        averagePaint.setColor(Color.GREEN);
        averagePaint.setStrokeWidth(3f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(30f);
    }

    /**
     * Set data for the candlestick chart
     *
     * @param min Minimum value
     * @param lowerQuartile Lower quartile value
     * @param average Average value
     * @param upperQuartile Upper quartile value
     * @param max Maximum value
     * @param absoluteMin Absolute minimum of the dataset
     * @param absoluteMax Absolute maximum of the dataset
     */
    public void setData(float min, float lowerQuartile, float average,
                        float upperQuartile, float max,
                        float absoluteMin, float absoluteMax) {
        this.min = min;
        this.lowerQuartile = lowerQuartile;
        this.average = average;
        this.upperQuartile = upperQuartile;
        this.max = max;
        this.absoluteMin = absoluteMin;
        this.absoluteMax = absoluteMax;

        // Request redraw
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 600;
        int desiredHeight = CANDLESTICK_HEIGHT + PADDING_TOP + PADDING_BOTTOM;

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Calculate scaling factor for data points
        float availableWidth = width - PADDING_LEFT - PADDING_RIGHT;
        float scale = availableWidth / (absoluteMax - absoluteMin);

        // Calculate vertical center of the view
        int centerY = height / 2;

        // Calculate scaled positions
        float scaledMin = PADDING_LEFT + (min - absoluteMin) * scale;
        float scaledLQ = PADDING_LEFT + (lowerQuartile - absoluteMin) * scale;
        float scaledAvg = PADDING_LEFT + (average - absoluteMin) * scale;
        float scaledUQ = PADDING_LEFT + (upperQuartile - absoluteMin) * scale;
        float scaledMax = PADDING_LEFT + (max - absoluteMin) * scale;

        // Draw the box (interquartile range)
        RectF box = new RectF(
                scaledLQ,
                centerY - CANDLESTICK_HEIGHT / 2,
                scaledUQ,
                centerY + CANDLESTICK_HEIGHT / 2);
        canvas.drawRect(box, boxPaint);

        // Draw whiskers (min to lower quartile and upper quartile to max)
        // Left whisker
        canvas.drawLine(scaledMin, centerY, scaledLQ, centerY, whiskerPaint);
        canvas.drawLine(
                scaledMin,
                centerY - WHISKER_HEIGHT,
                scaledMin,
                centerY + WHISKER_HEIGHT,
                whiskerPaint);

        // Right whisker
        canvas.drawLine(scaledUQ, centerY, scaledMax, centerY, whiskerPaint);
        canvas.drawLine(
                scaledMax,
                centerY - WHISKER_HEIGHT,
                scaledMax,
                centerY + WHISKER_HEIGHT,
                whiskerPaint);

        // Draw average line
        canvas.drawLine(
                scaledAvg,
                centerY - CANDLESTICK_HEIGHT / 2,
                scaledAvg,
                centerY + CANDLESTICK_HEIGHT / 2,
                averagePaint);

        // Draw labels
//        canvas.drawText(String.format("%.1f", min), scaledMin - 20, centerY - CANDLESTICK_HEIGHT / 2 - 10, textPaint);
//        canvas.drawText(String.format("%.1f", max), scaledMax - 20, centerY - CANDLESTICK_HEIGHT / 2 - 10, textPaint);
//        canvas.drawText(String.format("%.1f", average), scaledAvg - 20, centerY + CANDLESTICK_HEIGHT / 2 + 30, textPaint);
    }

    public int teamNum;

    public void fromTeamData(List<DataType> teamData, Integer teamNum, float absmin, float absmax){
        this.teamNum = teamNum;
        int[] tmp_loc_bounds = DataProcessing.getNumberBounds(teamData);
        int locmin = tmp_loc_bounds[0];
        int locmax = tmp_loc_bounds[1];

        float avg = 0;

        for(int i = 0; i < teamData.size(); i++){
            avg += (int) teamData.get(i).get();
        }

        avg /= teamData.size();

        float[] teamDataArray = new float[teamData.size()];
        for(int i = 0; i < teamData.size(); i++){
            teamDataArray[i] = (int) teamData.get(i).get();
        }

        float lowerQuartile = DataProcessing.calculatePercentile(teamDataArray, 25);
        float upperQuartile = DataProcessing.calculatePercentile(teamDataArray, 75);

        System.out.println(locmin + ", " + lowerQuartile  + ", " + avg  + ", " + upperQuartile  + ", " + locmax);
        setData(locmin, lowerQuartile, avg, upperQuartile, locmax, absmin, absmax);
    }
}