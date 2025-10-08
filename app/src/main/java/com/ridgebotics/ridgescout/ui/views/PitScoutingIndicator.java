package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;

// A view for displaying information about a team.
public class PitScoutingIndicator extends RelativeLayout {
    public PitScoutingIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PitScoutingIndicator(Context context) {
        super(context);
        init(context);
    }

    public TextView pit_indicator_username;
    public TextView pit_indicator_team_num;
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_pit_scouting_indicator, this, true);
        pit_indicator_username = findViewById(R.id.pit_indicator_username);
        pit_indicator_team_num = findViewById(R.id.pit_indicator_teamnum);

        box =  findViewById(R.id.pit_indicator_box);
        coloredBackground = findViewById(R.id.pit_indicator_background);

    }

    public void setUsername(String username){
        pit_indicator_username.setText(username);
    }

    public void setTeamNum(int teamNum) {
        pit_indicator_team_num.setText(String.valueOf(teamNum));
    }

    public void setColor(int color){
        // Set color of main background rectangle
        Drawable box_drawable = box.getBackground();
        box_drawable.mutate();
        box_drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        float[] hsv = new float[3];
        Color.colorToHSV(color,hsv);

        int background_color = Color.HSVToColor(220, new float[]{
                hsv[0],
                Math.min(hsv[1], 0.75f),
                Math.min(hsv[2], 0.5f)
        });

        // Set color of main background rectangle, slightly dimmer
        coloredBackground.setBackgroundColor(
                background_color
        );
    }
}
