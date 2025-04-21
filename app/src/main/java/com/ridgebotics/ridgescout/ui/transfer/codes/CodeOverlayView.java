package com.ridgebotics.ridgescout.ui.transfer.codes;

// From https://github.com/dlazaro66/QRCodeReaderView/blob/master/samples/src/main/java/com/example/qr_readerexample/PointsOverlayView.java

import static com.ridgebotics.ridgescout.utility.Colors.code_scanned_color;
import static com.ridgebotics.ridgescout.utility.Colors.code_selected_color;
import static com.ridgebotics.ridgescout.utility.Colors.code_unscanned_color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

// Shows the progress of receiving data over codes.
public class CodeOverlayView extends View {

        PointF[] points;
        int[] barColors;
        private Paint paint;
        private final int barHeight = 50;

        public CodeOverlayView(Context context) {
            super(context);
            init();
        }

        public CodeOverlayView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CodeOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setColor(code_selected_color);
            paint.setStyle(Paint.Style.FILL);
        }

        public void setPoints(PointF[] points) {
            this.points = points;
            invalidate();
        }

        public void setBar(int[] barColors){
            this.barColors = barColors;
            invalidate();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
            if (points != null) {
                for (PointF pointF : points) {
                    canvas.drawCircle(pointF.x, pointF.y, 10, paint);
                }
            }
            if(barColors != null){
                final double width = getWidth()/barColors.length;

                final int top = 0;
                final int bottom = barHeight;

                for(int i=0;i<barColors.length;i++){

                    final int num = barColors[i];

                    int c = code_unscanned_color;

                    if(num == 2){
                        c = code_scanned_color;
                    }else if(num == 1){
                        c = code_selected_color;
                    }

                    final Paint p = new Paint();
                    p.setColor(c);

                    canvas.drawRect(new Rect(
                            (int)(i*width), top,
                            (int)((i+1)*width), bottom
                    ), p);
                }
            }
        }
}
