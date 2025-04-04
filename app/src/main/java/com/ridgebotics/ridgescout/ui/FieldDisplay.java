package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.input.FieldType;

public class FieldDisplay extends ConstraintLayout {
    public FieldDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FieldDisplay(Context context) {
        super(context);
        init(context);
    }

    private FieldType field;

    public Button editButton;

    private TextView titleText;
    private TextView typeText;

//    private View fieldView;
    private LinearLayout buttonBox;
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_field_display, this, true);

        box =  findViewById(R.id.field_box);
        coloredBackground = findViewById(R.id.field_background);

        editButton = findViewById(R.id.button_edit);

        titleText = findViewById(R.id.field_title);
        typeText = findViewById(R.id.field_description);

        buttonBox = findViewById(R.id.buttons);
    }

    public void setInputType(FieldType field){
        this.field = field;

        titleText.setText(field.name);
        typeText.setText(field.get_type_name());
    }

//    public void setField(View newView){
//        if(fieldView != null)
//            box.removeView(fieldView);
//        box.addView(newView);
//        fieldView = newView;
//    }


//    public void fromTeam(frcTeam team){
//        setTeamNumber(team.teamNumber);
//        setTeamName(team.teamName);
//
//        if(team.bitmap != null) {
//            setTeamLogo(team.bitmap);
//        }else{
//            hideLogo();
//        }
//
//        setColor(team.getTeamColor());
//    }
    public void showButtons(){
        findViewById(R.id.buttons).setVisibility(View.VISIBLE);
    }

    public void hideButtons(){
        findViewById(R.id.buttons).setVisibility(View.GONE);
    }

    public FieldType getField(){
        return field;
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
                        Math.min(hsv[1]  - 0.25f, 0.75f),
                        Math.min(hsv[2] - 0.25f, 0.5f)
                })
        );
    }
}
