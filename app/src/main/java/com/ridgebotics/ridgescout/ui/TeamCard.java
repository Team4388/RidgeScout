package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.frcTeam;

public class TeamCard extends LinearLayout {
    public TeamCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TeamCard(Context context) {
        super(context);
        init(context);
    }

    private TextView teamNumber;
    private TextView teamName;
    private TextView teamDescription;

    private ImageView teamLogo;
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_team_card, this, true);

        teamNumber = findViewById(R.id.team_card_number);
        teamName = findViewById(R.id.team_card_name);
        teamLogo = findViewById(R.id.team_card_logo);
        teamDescription = findViewById(R.id.team_card_description);


        box =  findViewById(R.id.team_card_box);
        coloredBackground = findViewById(R.id.team_card_background);
    }

    public void setTeamNumber(int num){
        teamNumber.setText(String.valueOf(num));
    }

    public void setTeamName(String name){
        teamName.setText(name);
    }

    public void setTeamLogo(Bitmap bitmap){
        teamLogo.setImageBitmap(bitmap);
    }

    public void setTeamDescription(String description){
        teamDescription.setText(description);
    }

    public void hideLogo(){
        teamLogo.setVisibility(View.GONE);
    }
    public void showLogo(){
        teamLogo.setVisibility(View.VISIBLE);
    }

    public void fromTeam(frcTeam team){
        setTeamNumber(team.teamNumber);
        setTeamName(team.teamName);

        if(team.bitmap != null) {
            showLogo();
            setTeamLogo(team.bitmap);
        }else{
            hideLogo();
        }

        setTeamDescription(team.getDescription());

        setColor(team.getTeamColor());
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
}
