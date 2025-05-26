package com.ridgebotics.ridgescout.ui.settings;

import static android.view.View.VISIBLE;
import static androidx.navigation.fragment.FragmentKt.findNavController;
import static com.ridgebotics.ridgescout.utility.SettingsManager.AllyPosKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.CustomEventsKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.EnableQuickAllianceChangeKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.FieldImageKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.MatchNumKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.SelEVCodeKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.TeamNumKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.UnameKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.WifiModeKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.YearNumKey;
import static com.ridgebotics.ridgescout.utility.SettingsManager.defaults;
import static com.ridgebotics.ridgescout.utility.SettingsManager.getEditor;
import static com.ridgebotics.ridgescout.utility.SettingsManager.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentSettingsBinding;
import com.ridgebotics.ridgescout.scoutingData.Fields;
import com.ridgebotics.ridgescout.ui.views.CustomSpinnerView;
import com.ridgebotics.ridgescout.ui.views.TallyCounterView;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// Fragment to edit settings, aswell as redirect to the fields editor.
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        reloadSettings();

        binding.fieldsButton.setOnClickListener(v -> {
            binding.fieldsButton.setEnabled(false);
            binding.fieldsButtons.setVisibility(VISIBLE);
        });

        binding.fieldsMatchesButton.setOnClickListener(v -> {
            FieldsFragment.set_filename(Fields.matchFieldsFilename);
            findNavController(this).navigate(R.id.action_navigation_settings_to_navigation_data_fields);
        });

        binding.fieldsPitsButton.setOnClickListener(v -> {
            FieldsFragment.set_filename(Fields.pitsFieldsFilename);
            findNavController(this).navigate(R.id.action_navigation_settings_to_navigation_data_fields);
        });


        return root;
    }

    private void reloadSettings(){
        String[] alliance_pos_list = new String[]{"red-1", "red-2", "red-3",
                "blue-1", "blue-2", "blue-3"};

        SettingsManager manager = new SettingsManager(getContext());


        manager.addItem(new CheckboxSettingsItem(CustomEventsKey, "Custom Events"));

        StringSettingsItem FTPKey = new StringSettingsItem(com.ridgebotics.ridgescout.utility.SettingsManager.FTPKey, "Sync Key");
        manager.addItem(FTPKey);
        StringSettingsItem FTPServer = new StringSettingsItem(com.ridgebotics.ridgescout.utility.SettingsManager.FTPServer, "Sync Server (Sync)");
        manager.addItem(FTPServer);
        CheckboxSettingsItem FTPSendMetaFiles = new CheckboxSettingsItem(com.ridgebotics.ridgescout.utility.SettingsManager.FTPSendMetaFiles, "⚠ Send meta files");
        manager.addItem(FTPSendMetaFiles);
        CheckboxSettingsItem FTPEnabled = new CheckboxSettingsItem(com.ridgebotics.ridgescout.utility.SettingsManager.FTPEnabled, "FTP Enabled", FTPServer, FTPKey, FTPSendMetaFiles);
        manager.addItem(FTPEnabled);

        manager.addItem(new CheckboxSettingsItem(WifiModeKey, "Wifi Mode", FTPEnabled));
        manager.addItem(new CheckboxSettingsItem(EnableQuickAllianceChangeKey, "Enable quick alliance swap", null));

        manager.addItem(new DropdownSettingsItem(FieldImageKey, "Field Image", new String[]{
                "2025",
                "2025 (Flipped)"
        }));

        manager.addItem(new NumberSettingsItem(YearNumKey, "Year", 0, 9999));


        manager.addItem(new DropdownSettingsItem(AllyPosKey, "Alliance Pos", alliance_pos_list));

        int max = 0;
        boolean hasEvent = false;

        if(!DataManager.getevcode().equals("unset")){
            DataManager.reload_event();
            max = DataManager.event.matches.size();
            hasEvent = true;
        }

        TallySettingsItem matchNum = new TallySettingsItem(MatchNumKey, "Match Number", max);
        matchNum.setEnabled(hasEvent);
        manager.addItem(matchNum);

        DropdownSettingsItem eventCode = new DropdownSettingsItem(SelEVCodeKey, "Event Code", FileEditor.getEventList().toArray(new String[0]));
        eventCode.reloadOnChange(true);
        manager.addItem(eventCode);

        manager.addItem(new StringSettingsItem(UnameKey, "Username"));
        manager.addItem(new NumberSettingsItem(TeamNumKey, "Team Number", 0, 99999));

        binding.SettingsTable.removeAllViews();
        manager.getView(binding.SettingsTable);

        if(!DataManager.getevcode().equals("unset")){
            Button editNoticeButton = new Button(getContext());
            editNoticeButton.setText("Edit Scout Notice");
            binding.SettingsTable.addView(editNoticeButton);
            editNoticeButton.setOnClickListener(v->editNotice());
        }

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void editNotice(){
        ScrollView sv = new ScrollView(getContext());
        EditText editText = new EditText(getContext());
        editText.setText(DataManager.scoutNotice);
        sv.addView(editText);


        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Edit Notice");
        alert.setView(sv);
        alert.setNeutralButton("Cancel", null);
        alert.setPositiveButton("Save", (dialogInterface, i) -> {
            DataManager.scoutNotice = editText.getText().toString();
            DataManager.save_scout_notice();
        });
        alert.setCancelable(false);

        alert.create().show();
    }














    public abstract class SettingsItem<T> {
        private String key;
        private String title;
        private T defaultValue;
        public View view;

        public SettingsItem(String key, String title, T defaultValue) {
            this.key = key;
            this.title = title;
            this.defaultValue = defaultValue;
        }

        private boolean reloadOnChange = false;
        public void reloadOnChange(boolean enabled){
            reloadOnChange = enabled;
        }
        public boolean isReloadOnChange(){
            return reloadOnChange;
        }

        public abstract View createView(Context context);
        public abstract T getValue();

        public String getKey() { return key; }
        public String getTitle() { return title; }
        public T getDefaultValue() { return defaultValue; }
        public abstract void setEnabled(boolean enabled);
    }

    public class StringSettingsItem extends SettingsItem<String> {
        public StringSettingsItem(String key, String title) {
            super(key, title, prefs.getString(key, (String) defaults.get(key)));
        }

        TextInputEditText editText;

        @Override
        public void setEnabled(boolean enabled){
            editText.setEnabled(enabled);
        }

        @Override
        public View createView(Context context) {
            TextInputLayout textInputLayout = new TextInputLayout(context);
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            editText = new TextInputEditText(context);
            editText.setText(getValue());

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    getEditor().putString(getKey(), s.toString()).apply();
                    if(isReloadOnChange()) reloadSettings();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            textInputLayout.addView(editText);
            return textInputLayout;
        }

        @Override
        public String getValue() {
            return prefs.getString(getKey(), (String) defaults.get(getKey()));
        }
    }

    public class NumberSettingsItem extends SettingsItem<Integer> {
        private int min;
        private int max;

        public NumberSettingsItem(String key, String title, int min, int max) {
            super(key, title, prefs.getInt(key, (int) defaults.get(key)));
            this.min = min;
            this.max = max;
        }

        TextInputEditText editText;

        @Override
        public void setEnabled(boolean enabled){
            editText.setEnabled(enabled);
        }

        @Override
        public View createView(Context context) {
            TextView titleView = new TextView(context);
            titleView.setText(getTitle());
            titleView.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle1);

            TextInputLayout textInputLayout = new TextInputLayout(context);
            editText = new TextInputEditText(context);

            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setText(String.valueOf(getValue()));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value >= min && value <= max) {
                            getEditor().putInt(getKey(), value).apply();
                        }
                    } catch (NumberFormatException e) {
                        editText.setText(String.valueOf(getDefaultValue()));
                    }
                    if(isReloadOnChange()) reloadSettings();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            textInputLayout.addView(editText);
            textInputLayout.addView(titleView);
            return textInputLayout;
        }

        @Override
        public Integer getValue() {
            return prefs.getInt(getKey(), (int) defaults.get(getKey()));
        }
    }

    public class TallySettingsItem extends SettingsItem<Integer> {
        private int max;

        public TallySettingsItem(String key, String title, int max) {
            super(key, title, prefs.getInt(key, (int) defaults.get(key)));
            this.max = max;
        }

        TallyCounterView tally;
        private boolean enabled;

        @Override
        public void setEnabled(boolean enabled){
            this.enabled = enabled;
            if(tally != null)
                tally.setEnabled(enabled);
        }

        @Override
        public View createView(Context context) {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.VERTICAL);

            tally = new TallyCounterView(getContext());

            int value = getValue()+1;
            if(value >= max){
                value = max;
                getEditor().putInt(getKey(), Math.max(0,max-1)).apply();
            }

            tally.setValue(value);
            tally.setBounds(1, max);

            tally.setOnCountChangedListener(count -> {
                getEditor().putInt(getKey(), Math.max(0,count-1)).apply();
                if(isReloadOnChange()) reloadSettings();
            });
            tally.setEnabled(enabled);

            TextView tv = new TextView(getContext());
            tv.setText(getTitle());
            tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
            tv.setGravity(Gravity.CENTER);
            ll.addView(tv);

            ll.addView(tally);




            return ll;
        }

        @Override
        public Integer getValue() {
            return prefs.getInt(getKey(), (int) defaults.get(getKey()));
        }
    }

    public class DropdownSettingsItem extends SettingsItem<String> {
        private String[] options;

        private boolean enabled = true;

        @Override
        public void setEnabled(boolean enabled){
            this.enabled = enabled;
        }

        public DropdownSettingsItem(String key, String title, String[] options) {
            super(key, title, prefs.getString(key, (String) defaults.get(key)));
            this.options = options;
        }

        @Override
        public View createView(Context context) {
            CustomSpinnerView dropdown = new CustomSpinnerView(getContext());
            dropdown.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));


            ArrayList<String> optionsList = new ArrayList<>(Arrays.asList(options));

            dropdown.setTitle(getTitle());
            dropdown.setOptions(optionsList, getValue());
            dropdown.setOption(getValue());

            dropdown.setOnClickListener((item, index) -> {
                getEditor().putString(getKey(), item).apply();
                if(isReloadOnChange()) reloadSettings();
            });

            return dropdown;
        }

        @Override
        public String getValue() {
            return prefs.getString(getKey(), (String) defaults.get(getKey()));
        }
    }

    public class CheckboxSettingsItem extends SettingsItem<Boolean> {
        private List<SettingsItem<?>> controlledItems;

        public CheckboxSettingsItem(String key, String title, @Nullable SettingsItem<?>... controlledItems) {
            super(key, title, prefs.getBoolean(key, (Boolean) defaults.get(key)));
            this.controlledItems = (controlledItems != null) ? Arrays.asList(controlledItems) : new ArrayList<>();
        }

        MaterialCheckBox checkBox;

        @Override
        public void setEnabled(boolean enabled){
            checkBox.setEnabled(enabled);
            for (SettingsItem<?> item : controlledItems) {
                item.setEnabled(enabled && checkBox.isChecked());
            }
        }

        @Override
        public View createView(Context context) {
            checkBox = new MaterialCheckBox(context);
            checkBox.setText(getTitle());
            checkBox.setChecked(getValue());
            checkBox.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle1);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                getEditor().putBoolean(getKey(), isChecked).apply();
                for (SettingsItem<?> item : controlledItems) {
                    item.setEnabled(isChecked);
                }
                if(isReloadOnChange()) reloadSettings();
            });

            for (SettingsItem<?> item : controlledItems) {
                item.setEnabled(getValue());
            }

            return checkBox;
        }

        @Override
        public Boolean getValue() {
            return prefs.getBoolean(getKey(), (Boolean) defaults.get(getKey()));
        }
    }

    public class SettingsManager {
        private Context context;
        private HashMap<String, Object> settings;
        private List<SettingsItem<?>> items;
//        private LinearLayout container;

        public SettingsManager(Context context) {
            this.context = context;
            this.items = new ArrayList<>();
//            this.container = new LinearLayout(context);
//            this.container.setOrientation(LinearLayout.VERTICAL);
//            this.container.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            ));
        }

        private final List<View> views = new ArrayList<>();
        public void addItem(SettingsItem<?> item) {
            items.add(item);

            LinearLayout itemContainer = new LinearLayout(context);
            itemContainer.setOrientation(LinearLayout.VERTICAL);
            itemContainer.setPadding(32, 0, 32, 8);

            View view = item.createView(context);
            itemContainer.addView(view);

            item.view = view;

            views.add(itemContainer);
        }

        public void getView(LinearLayout layout) {
            for(int i = views.size()-1; i >= 0; i--)
                layout.addView(views.get(i));
        }
    }
}