package com.ridgebotics.ridgescout.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.TableLayout;

import androidx.annotation.Nullable;

import com.google.android.material.divider.MaterialDivider;

import java.util.List;

// The notification view of a Custom dropdown notification
public class CustomSpinnerPopup extends TableLayout {

    public CustomSpinnerPopup(Context context) {
        super(context);
    }

    public CustomSpinnerPopup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }

    public CustomSpinnerPopup init(List<String> options, OnOptionSelectedListener onOptionSelectedListener, int defaultOption){
        CheckBox[] checkBoxes = new CheckBox[options.size()];
        setPadding(16, 16, 16, 16);
        for(int i = 0; i < options.size(); i++){
            final CheckBox cb = new CheckBox(getContext());
            cb.setText(options.get(i));
            cb.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline5);
            cb.setChecked(i == defaultOption);

            if(i > 0)
                addView(new MaterialDivider(getContext()));

            addView(cb);
            checkBoxes[i] = cb;

            final int fi = i;
            cb.setOnClickListener(a -> {
                onOptionSelectedListener.onOptionSelected(options.get(fi));
                for (CheckBox checkBox : checkBoxes)
                    checkBox.setChecked(false);
                cb.setChecked(true);
            });
        }

        return this;
    }

//    public static CustomSpinnerPopup newInstance(ArrayList<String> options) {
//        CustomSpinnerPopup dialog = new CustomSpinnerPopup();
//        Bundle args = new Bundle();
//        args.putStringArrayList("options", options);
//
//
//
////        dialog.setArguments(args);
//        return dialog;
//    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
//        if (getArguments() != null) {
//            options = getArguments().getStringArrayList("options");
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.view_custom_spinner_popup, container, false);
//
//        // Setup toolbar
//        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
//        toolbar.setNavigationOnClickListener(v -> dismiss());
//        toolbar.setTitle("Select an Option");
//
//        // Setup RecyclerView
//        recyclerView = view.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new CustomSpinnerOptionsAdapter(options, option -> {
//            if (listener != null) {
//                listener.onOptionSelected(option);
//            }
//            dismiss();
//        });
//        recyclerView.setAdapter(adapter);
//
//        return view;
//    }
//



}

