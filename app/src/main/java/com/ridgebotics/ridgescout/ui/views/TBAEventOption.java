package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;

// Class for custom view displaying an event in the TBASelector.java
public class TBAEventOption extends LinearLayout {
    public TBAEventOption(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TBAEventOption(Context context) {
        super(context);
        init(context);
    }

    private TextView eventCode;
    private TextView eventName;
    private TextView eventType;

    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_tba_event, this, true);

        eventCode = findViewById(R.id.tba_event_code);
        eventName = findViewById(R.id.tba_event_name);
        eventType = findViewById(R.id.tba_event_type);


        box =  findViewById(R.id.tba_event_box);
        coloredBackground = findViewById(R.id.tba_event_background);
    }

    public void setCode(String code){
        eventCode.setText(String.valueOf(code));
    }

    public void setName(String name){
        eventName.setText(name);
    }
    public void setType(String name){
        eventType.setText(name);
    }


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
}
