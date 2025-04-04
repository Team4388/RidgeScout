package com.ridgebotics.ridgescout.ui;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ridgebotics.ridgescout.R;

import java.util.List;

public class CustomSpinnerView extends LinearLayout {

    public interface onClickListener {
        void onClick(String item, int index);
    }

    public CustomSpinnerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomSpinnerView(Context context) {
        super(context);
        init(context);
    }

    private List<String> options;
    private onClickListener onClickListener;

    private TextView title;
    private TextView item;

    private int index = -1;

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_spinner, this, true);

        title = findViewById(R.id.title);
        item = findViewById(R.id.item);
    }

    public void setOnClickListener(onClickListener listener){
        this.onClickListener = listener;
    }


    public void setOptions(List<String> options, String defaultOption){
        setOptions(options, options.indexOf(defaultOption));
    }

    public void setOptions(List<String> options, int defaultOption){
        this.options = options;
        this.index = defaultOption;

        if(defaultOption != -1)
            this.item.setText(options.get(defaultOption));


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        ScrollView sv = new ScrollView(getContext());
//        sv.setLayoutDirection(ScrollView.SCROLL_AXIS_VERTICAL);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        builder.setPositiveButton("OK", (dialog, which) -> {});
        CustomSpinnerPopup popup = new CustomSpinnerPopup(getContext()).init(options, option -> {
//            dialog.();
            if(!isEnabled()) return;
            item.setText(option);
            index = options.indexOf(option);
            if(onClickListener != null) {
                onClickListener.onClick(option, options.indexOf(option));
            }
        }, index);

        ll.addView(popup);

//        popup.setLayoutDirection(0);
        builder.setView(sv);
        AlertDialog dialog = builder.create();


//        popup.setOnOptionSelectedListener();

        this.setOnClickListener(v -> {
            if(!isEnabled()) return;
            dialog.show();
        });
    }

    public void setTitle(String text){
        title.setText(text);
    }

    public void setOption(String option) {
        item.setText(option);
        index = options.indexOf(option);
    }

    public void setOption(int index) {
        item.setText(options.get(index));
        this.index = index;
    }

    public int getIndex(){
        return index;
    }
    public String getOption(){
        return options.get(index);
    }
}
