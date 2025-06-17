package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
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
        Drawable drawable = box.getBackground();
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        float[] hsv = new float[3];
        Color.colorToHSV(color,hsv);

        coloredBackground.setBackgroundColor(
                Color.HSVToColor(220, new float[]{
                        hsv[0],
                        Math.min(hsv[1], 0.75f),
                        Math.min(hsv[2], 0.5f)
                })
        );
    }
}
