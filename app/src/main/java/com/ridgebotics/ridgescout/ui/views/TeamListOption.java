package com.ridgebotics.ridgescout.ui.views;

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

// A view that acts as a row specifically to display a team and their icon in a list formmt.
public class TeamListOption extends LinearLayout {
    public TeamListOption(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TeamListOption(Context context) {
        super(context);
        init(context);
    }

    private TextView teamNumber;
    private TextView teamName;

    private ImageView teamLogo;
    private ConstraintLayout box;
    private View coloredBackground;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_team_option, this, true);

        teamNumber = findViewById(R.id.field_option_type);
        teamName = findViewById(R.id.field_option_name);
        teamLogo = findViewById(R.id.team_option_logo);


        box =  findViewById(R.id.team_option_box);
        coloredBackground = findViewById(R.id.team_option_background);
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
            setTeamLogo(team.bitmap);
        }else{
            hideLogo();
        }

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
