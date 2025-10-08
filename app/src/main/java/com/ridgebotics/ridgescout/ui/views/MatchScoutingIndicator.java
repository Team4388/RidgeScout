package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.frcTeam;

import org.w3c.dom.Text;

// A view for displaying information about a team.
public class MatchScoutingIndicator extends RelativeLayout {
    public MatchScoutingIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MatchScoutingIndicator(Context context) {
        super(context);
        init(context);
    }

    public TextView match_indicator_alliance_pos_text;
    public TextView match_indicator_bar_team_num;
    public TextView match_indicator_matchnum;
    public TextView match_indicator_username;
    public ImageButton match_indicator_back_button;
    public ImageButton match_indicator_next_button;
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_match_scouting_indicator, this, true);
        match_indicator_back_button = findViewById(R.id.match_indicator_back_button);
        match_indicator_next_button = findViewById(R.id.match_indicator_next_button);
        match_indicator_alliance_pos_text = findViewById(R.id.match_indicator_alliance_pos_text);
        match_indicator_username = findViewById(R.id.match_indicator_username);
        match_indicator_matchnum = findViewById(R.id.match_indicator_matchnum);
        match_indicator_bar_team_num = findViewById(R.id.match_indicator_bar_team_num);
        box =  findViewById(R.id.file_indicator_box);
        coloredBackground = findViewById(R.id.match_indicator_background);

        int currentNightMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active on device
                match_indicator_back_button.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                match_indicator_next_button.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active on device
                match_indicator_back_button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                match_indicator_next_button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    public void setUsername(String username){
        match_indicator_username.setText(username);
    }

    public void setAlliancePos(String alliancePos){
        match_indicator_alliance_pos_text.setText(alliancePos);
    }

    public void setMatchNum(String matchNum){
        match_indicator_matchnum.setText(matchNum);
    }

    public void setTeamNum(String teamNum){
        match_indicator_bar_team_num.setText(teamNum);
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

        Drawable left_drawable = match_indicator_back_button.getBackground();
        left_drawable.mutate();
        left_drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        Drawable right_drawable = match_indicator_next_button.getBackground();
        right_drawable.mutate();
        right_drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}
