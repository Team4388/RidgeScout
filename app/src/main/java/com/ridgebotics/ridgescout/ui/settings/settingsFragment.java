package com.ridgebotics.ridgescout.ui.settings;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static com.ridgebotics.ridgescout.utility.settingsManager.AllyPosKey;
import static com.ridgebotics.ridgescout.utility.settingsManager.FTPEnabled;
import static com.ridgebotics.ridgescout.utility.settingsManager.FTPSendMetaFiles;
import static com.ridgebotics.ridgescout.utility.settingsManager.FTPServer;
import static com.ridgebotics.ridgescout.utility.settingsManager.SelEVCodeKey;
import static com.ridgebotics.ridgescout.utility.settingsManager.TeamNumKey;
import static com.ridgebotics.ridgescout.utility.settingsManager.UnameKey;
import static com.ridgebotics.ridgescout.utility.settingsManager.WifiModeKey;
import static com.ridgebotics.ridgescout.utility.settingsManager.defaults;
import static com.ridgebotics.ridgescout.utility.settingsManager.getEditor;
import static com.ridgebotics.ridgescout.utility.settingsManager.prefs;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.divider.MaterialDivider;
import com.ridgebotics.ridgescout.databinding.FragmentSettingsBinding;
import com.ridgebotics.ridgescout.types.data.intType;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.settingsManager;

import com.skydoves.powerspinner.IconSpinnerAdapter;
import com.skydoves.powerspinner.IconSpinnerItem;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.skydoves.powerspinner.SpinnerGravity;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class settingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private android.widget.ScrollView ScrollArea;
    private android.widget.TableLayout Table;

//    private void setDropdownItems(Spinner dropdown, String[] items){
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, items);
//        dropdown.setAdapter(adapter);
//    }

    private View[] concatArrays(View[] a, View[] b){
        return Stream.of(a, b).flatMap(Stream::of).toArray(View[]::new);
    }


    private View[] addViews(View[] a){
        for(int i = 0; i < a.length; i++){
            binding.SettingsTable.addView(a[i]);
        }
        return a;
    }

    private int safeToInt(String num){
        if(num.isEmpty())
            return 0;
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException e){
            return 0;
        }
    }

    private View[] createHeading(String name){
        TextView tv = new TextView(getContext());
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 100;
        tv.setLayoutParams(params);
        tv.setTextSize(20);
        tv.setText(name);

        View divider = new MaterialDivider(getContext());

        return new View[]{tv, divider};
    }

    private View[] addStringEdit(String name, String key){
        View[] heading = createHeading(name);
        EditText et = new EditText(getContext());
        et.setText(prefs.getString(key, (String) defaults.get(key)));

        et.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                getEditor().putString(key, s.toString()).apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        return concatArrays(heading, new View[]{et});
    }

    private View[] addNumberEdit(String name, String key){
        View[] heading = createHeading(name);
        EditText et = new EditText(getContext());
        et.setText(String.valueOf(prefs.getInt(key, (Integer) defaults.get(key))));
        et.setInputType(TYPE_CLASS_NUMBER);

        et.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                getEditor().putInt(key, safeToInt(s.toString())).apply();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        return concatArrays(heading, new View[]{et});
    }

    private PowerSpinnerView addDropdownEdit(String name, String[] options, String key){
        PowerSpinnerView dropdown = new PowerSpinnerView(getContext());

        List<IconSpinnerItem> iconSpinnerItems = new ArrayList<>();
        for(int i = 0; i < options.length; i++){
            iconSpinnerItems.add(new IconSpinnerItem(options[i]));
        }
        IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(dropdown);

        dropdown.setGravity(Gravity.CENTER);

        dropdown.setSpinnerAdapter(iconSpinnerAdapter);
        dropdown.setItems(iconSpinnerItems);
        dropdown.setHint("Unselected");

        dropdown.setPadding(10,20,10,20);
        dropdown.setBackgroundColor(0xf0000000);
        dropdown.setTextColor(0xff00ff00);
        dropdown.setTextSize(14.5f);
        dropdown.setArrowGravity(SpinnerGravity.END);
        dropdown.setArrowPadding(8);
        dropdown.setSpinnerPopupElevation(14);

        return dropdown;
    }

    private View[] addDropdownByString(String name, String[] options, String key){
        View[] heading = createHeading(name);
        PowerSpinnerView dropdown = addDropdownEdit(name, options, key);
        int index = Arrays.asList(options).indexOf(prefs.getString(key, (String) defaults.get(key)));
        System.out.println(index);

        if(options.length != 0 && index != -1){
            dropdown.selectItemByIndex(index);
        }

        dropdown.setOnSpinnerItemSelectedListener(
                (OnSpinnerItemSelectedListener<IconSpinnerItem>)
                        (oldIndex, oldItem, newIndex, newItem) ->  getEditor().putString(key, newItem.getText().toString()).apply()
        );

        return concatArrays(heading, new View[]{dropdown});
    }

    private View[]  addDropdownByIndex(String name, String[] options, String key){
        View[] heading = createHeading(name);
        PowerSpinnerView dropdown = addDropdownEdit(name, options, key);

        int index = prefs.getInt(key, (Integer) defaults.get(key));

        if(dropdown.length() != 0 && index != -1){
            dropdown.selectItemByIndex(index);
        }

        dropdown.setOnSpinnerItemSelectedListener(
                (OnSpinnerItemSelectedListener<IconSpinnerItem>)
                        (oldIndex, oldItem, newIndex, newItem) -> getEditor().putInt(key, newIndex).apply()
        );

        return concatArrays(heading, new View[]{dropdown});
    }

    private View[] addCheckbox(String name, String key, View[] dependency){
        CheckBox cb = new CheckBox(getContext());
        cb.setText(name);
        cb.setTextSize(22);
        boolean checked = prefs.getBoolean(key, (Boolean) defaults.get(key));
        cb.setChecked(checked);

        if(dependency != null && !checked){
            for(int i = 0; i < dependency.length; i++){
                dependency[i].setVisibility(View.GONE);
            }
        }

        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getEditor().putBoolean(key, isChecked).apply();
            if(dependency != null){
                for(int i = 0; i < dependency.length; i++){
                    dependency[i].setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    System.out.println(dependency[i]);
                }
            }
        });

        return new View[]{new MaterialDivider(getContext()), cb};
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String[] alliance_pos_list = new String[]{"red-1", "red-2", "red-3",
                                                  "blue-1", "blue-2", "blue-3"};

        addViews(addStringEdit("Username", UnameKey));
        addViews(addDropdownByString("Event Code", fileEditor.getEventList().toArray(new String[0]), SelEVCodeKey));
        addViews(addDropdownByString("Alliance Position", alliance_pos_list, AllyPosKey));
        addViews(addNumberEdit("Team Number", TeamNumKey));

        View[] FTPDependency = concatArrays(
                addCheckbox("Send Meta Files", FTPSendMetaFiles, new View[]{}),
                addStringEdit("FTP Server", FTPServer)
        );

        View[] WifiDependency = addCheckbox("FTP Enabled", FTPEnabled, FTPDependency);
        addViews(addCheckbox("Wifi Mode", WifiModeKey, concatArrays(FTPDependency, WifiDependency)));
        addViews(WifiDependency);
        addViews(FTPDependency);

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}