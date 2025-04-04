package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.input.FieldType;

public class FieldBorderedRow extends TableRow {
    public FieldBorderedRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FieldBorderedRow(Context context) {
        super(context);
        init(context);
    }
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_field_border_row, this, true);


        box =  findViewById(R.id.field_option_box);
        coloredBackground = findViewById(R.id.field_option_background);
    }

    public void setColor(int color){
        Drawable drawable = box.getBackground();
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        float[] hsv = new float[3];
        Color.colorToHSV(color,hsv);

        coloredBackground.setBackgroundColor(
                Color.HSVToColor(127, new float[]{
                        hsv[0],
                        Math.min(hsv[1], 0.75f),
                        Math.min(hsv[2], 0.5f)
                })
        );
    }

    public void fromField(FieldType field){
        ((TextView) findViewById(R.id.field_option_name)).setText(field.name);
        ((TextView) findViewById(R.id.field_option_type)).setText(field.get_type_name());
    }

    @Override
    public void addView(View v){
        box.addView(v);
    }
}
