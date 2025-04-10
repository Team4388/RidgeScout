package com.ridgebotics.ridgescout.ui;

import static com.ridgebotics.ridgescout.utility.Colors.toggletitle_black_background;
import static com.ridgebotics.ridgescout.utility.Colors.toggletitle_no_background;
import static com.ridgebotics.ridgescout.utility.Colors.toggletitle_unselected;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ridgebotics.ridgescout.R;

public class ToggleTitleView extends ConstraintLayout {
    public ToggleTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ToggleTitleView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public interface OnToggleListener {
        void onToggle(boolean enabled);
    }

    TextView titleView;
    CheckBox toggle_title_checkbox;
    TextView toggle_title_description;
    OnToggleListener onToggleListener;



    public void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.view_toggle_title, this, true);

        titleView = findViewById(R.id.toggle_title);
        toggle_title_checkbox = findViewById(R.id.toggle_title_checkbox);
        toggle_title_description = findViewById(R.id.toggle_title_description);

        toggle_title_checkbox.setOnCheckedChangeListener((compoundButton, checked) -> {
            // If checkbox has already updated
            if(enabled == checked) return;

            if (checked)
                enable();
            else
                disable();

            if(onToggleListener != null)
                onToggleListener.onToggle(!checked);
        });
    }

    public void setTitle(String title){
        titleView.setText(title);
    }

    public void setDescription(String description){
        toggle_title_description.setText(description);
    }

    public void setOnToggleListener(OnToggleListener onToggleListener){
        this.onToggleListener = onToggleListener;
    }

    public boolean enabled = true;

    public boolean isEnabled(){return enabled;}

    public void disable(){
        enabled = false;
        toggle_title_checkbox.setChecked(false);
        toggle_title_description.setVisibility(View.GONE);
        setBackgroundColor(toggletitle_unselected);
        titleView.setTextColor(toggletitle_black_background);
    }
    public void enable(){
        enabled = true;
        toggle_title_checkbox.setChecked(true);
        toggle_title_description.setVisibility(View.VISIBLE);
        setBackgroundColor(toggletitle_no_background);
        titleView.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline5);
    }

    public void setEnabled(boolean enabled){
        if(enabled)
            disable();
        else
            enable();
    }
}
