package com.ridgebotics.ridgescout.ui.scouting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ridgebotics.ridgescout.R;

public class TallyCounterView extends LinearLayout {
    private int count = 0;
    private TextView countDisplay;
    private Button minusButton;
    private Button plusButton;
    private OnCountChangedListener onCountChangedListener;
    private int min = 0;
    private int max = -1;

    public interface OnCountChangedListener {
        void onCountChanged(int newCount);
    }

    public TallyCounterView(Context context) {
        super(context);
        init(context);
    }

    public TallyCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TallyCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_tally_counter, this, true);

        countDisplay = findViewById(R.id.count_display);
        minusButton = findViewById(R.id.minus_button);
        plusButton = findViewById(R.id.plus_button);

        updateDisplay();

        minusButton.setOnClickListener(v -> {
            if(count > min) {
                count--;
                updateDisplay();
            }
        });

        plusButton.setOnClickListener(v -> {
            if(max == -1 || count < max){
                count++;
                updateDisplay();
            }
        });
    }

    private void updateDisplay() {
        countDisplay.setText(String.valueOf(count));

        minusButton.setEnabled(count > min);
        plusButton.setEnabled(max == -1 || count < max);

        if (onCountChangedListener != null) {
            onCountChangedListener.onCountChanged(count);
        }
    }

    public void setValue(int value) {
        count = value;
        updateDisplay();
    }

    public void setBounds(int min, int max){
        this.min = min;
        this.max = max;
        updateDisplay();
    }

    public int getValue() {
        return count;
    }

    public void setOnCountChangedListener(OnCountChangedListener listener) {
        this.onCountChangedListener = listener;
    }
}