package com.ridgebotics.ridgescout.ui.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentDataFieldsBinding;
import com.ridgebotics.ridgescout.scoutingData.fields;
import com.ridgebotics.ridgescout.types.input.checkboxType;
import com.ridgebotics.ridgescout.types.input.dropdownType;
import com.ridgebotics.ridgescout.types.input.fieldposType;
import com.ridgebotics.ridgescout.types.input.inputType;
import com.ridgebotics.ridgescout.types.input.numberType;
import com.ridgebotics.ridgescout.types.input.sliderType;
import com.ridgebotics.ridgescout.types.input.tallyType;
import com.ridgebotics.ridgescout.types.input.textType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.skydoves.powerspinner.IconSpinnerAdapter;
import com.skydoves.powerspinner.IconSpinnerItem;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.skydoves.powerspinner.SpinnerGravity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldsFragment extends Fragment {
    FragmentDataFieldsBinding binding;

    private static String filename;
    public static void set_filename(String tmpfilename){
        filename = tmpfilename;
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDataFieldsBinding.inflate(inflater, container, false);

        binding.valueEditScrollview.setOnTouchListener((v, event) -> true);

        binding.saveButton.setVisibility(View.GONE);
        binding.cancelEditButton.setVisibility(View.GONE);
        binding.editButton.setVisibility(View.GONE);
        binding.upButton.setVisibility(View.GONE);
        binding.addButton.setVisibility(View.GONE);
        binding.downButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.GONE);

        binding.valueEditContainer.setVisibility(View.GONE);

        load_field_menu();

        return binding.getRoot();
    }

    private static final int background_color = 0x5000ff00;
    private static final int unfocused_background_color = 0x2000ff00;

    inputType[][] values;

    private void load_field_menu() {

        values = fields.load(filename);

        binding.fieldsArea.bringToFront();
        binding.fieldsArea.setStretchAllColumns(true);
        binding.fieldsArea.removeAllViews();
        binding.fieldsArea.setReorderingEnabled(false);

        binding.upButton.setActivated(false);
        binding.addButton.setActivated(false);

        if(values == null) return;

        for(int i = 0; i < values.length; i++){

            TableRow tr = new TableRow(getContext());
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );

            rowParams.setMargins(20,20,20,20);
            tr.setLayoutParams(rowParams);
            tr.setPadding(20,20,20,20);

            TextView tv = new TextView(getContext());
            tv.setText("v" + i);
            tv.setTextSize(20);
            tr.addView(tv);

            tv = new TextView(getContext());
            tv.setText(values[i].length + " Fields");
            tv.setTextSize(16);
            tr.addView(tv);

            binding.fieldsArea.addView(tr);

            if(i == values.length-1) {
                tr.setBackgroundColor(background_color);
                int fi = i;
                tr.setOnClickListener(v -> {
                    display_fields(values[fi]);
                });
            }else{
                tr.setBackgroundColor(unfocused_background_color);
            }
        }

        if(values.length > 1) {
            binding.revertVersionButton.setVisibility(View.VISIBLE);
            binding.revertVersionButton.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Warning!");
                alert.setMessage("If there is any data set this version, it will be deleted!");
                alert.setPositiveButton("OK", (dialog, which) -> {
                    inputType[][] newArr = new inputType[values.length - 1][];
                    System.arraycopy(values, 0, newArr, 0, values.length - 1);
                    if(fields.save(filename, newArr))
                        AlertManager.toast("Saved");
                    load_field_menu();
                });
                alert.setNegativeButton("Cancel", null);
                alert.setCancelable(true);
                alert.create().show();
            });
        }
    }

    private void display_fields(inputType[] version_values) {
        binding.fieldsArea.removeAllViews();
        binding.fieldsArea.setReorderingEnabled(false);

        binding.revertVersionButton.setVisibility(View.GONE);

        binding.saveButton.setVisibility(View.GONE);
        binding.editButton.setVisibility(View.GONE);
        binding.upButton.setVisibility(View.VISIBLE);
        binding.addButton.setVisibility(View.VISIBLE);

        binding.downButton.setVisibility(View.VISIBLE);

        binding.valueEditContainer.setVisibility(View.GONE);

        for(int i = 0; i < version_values.length; i++){
            addRow(version_values[i]);
        }
        selindex = -1;


        binding.addButton.setOnClickListener(this::addField);
        binding.saveButton.setOnClickListener(this::buttonfunc);
    }

    private void addRow(inputType field){
        TableRow tr = getTableRow(field);

        binding.fieldsArea.addView(tr);

        tr.setOnClickListener(v -> {
            binding.editButton.setVisibility(View.VISIBLE);
            binding.upButton.setActivated(true);
            binding.downButton.setActivated(true);
            trOnClick(values[values.length-1], tr);
        });

        binding.upButton.setOnClickListener(v -> {
            if(selindex != 0 && selindex != -1) {
                binding.saveButton.setVisibility(View.VISIBLE);
                binding.fieldsArea.updateRowOrder(selindex, selindex - 1);
                selindex -= 1;
            }
        });

        binding.downButton.setOnClickListener(v -> {
            if(selindex != values[values.length-1].length-1) {
                binding.saveButton.setVisibility(View.VISIBLE);
                binding.fieldsArea.updateRowOrder(selindex, selindex + 1);
                selindex += 1;
            }
        });

    }

    private void buttonfunc(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Warning!");
        alert.setMessage("Changing or removing some values will result in lost data!\nBut this will create a new field version, and you can revert at any time.");
        alert.setPositiveButton("OK", (dialog, which) -> {
            inputType[][] currentValues = fields.load(filename);
            assert currentValues != null;
            inputType[][] newValues = new inputType[currentValues.length+1][];
            System.arraycopy(currentValues, 0, newValues, 0, currentValues.length);

            newValues[newValues.length-1] = new inputType[values[values.length-1].length];

            for(int i = 0; i < values[values.length-1].length; i++){
                newValues[newValues.length-1][i] = values[values.length-1][binding.fieldsArea.getReorderedIndexes().get(i)];
            }
//            newValues[newValues.length-1] = values[values.length-1];

            if(fields.save(filename, newValues))
                AlertManager.toast("Saved");

            Navigation.findNavController((Activity) getContext(), R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_data_fields_to_navigation_data_fields_chooser);
        });
        alert.setNegativeButton("Cancel", null);
        alert.setCancelable(true);
        alert.create().show();
    }

    private int selindex = -1;

    private void trOnClick(inputType[] version_values, TableRow tr){
        selindex = -1;
        for(int i = 0; i < binding.fieldsArea.getChildCount(); i++){
            View v = binding.fieldsArea.getChildAt(i);

            if(v.equals(tr)) {
                tr.setBackgroundColor(background_color);
                selindex = i;
            } else
                binding.fieldsArea.getChildAt(i).setBackgroundColor(unfocused_background_color);
        }

        onFieldSelect(version_values[binding.fieldsArea.getReorderedIndexes().get(selindex)]);
    }

    private void onFieldSelect(inputType field){
        //System.out.println(field.name);

        binding.editButton.setOnClickListener(v -> {
            binding.editButton.setVisibility(View.GONE);
            binding.addButton.setVisibility(View.GONE);
            binding.upButton.setVisibility(View.GONE);
            binding.downButton.setVisibility(View.GONE);

            binding.ValueEditTable.removeAllViews();
            binding.valueEditContainer.setVisibility(View.VISIBLE);
            TextView tv = new TextView(getContext());

            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setText(field.name);
            tv.setPadding(8,8,8,8);
            tv.setTextSize(24);

            binding.ValueEditTable.addView(tv);

            final FieldEditorHelper fe = new FieldEditorHelper(
                getContext(),
                field,
                binding.ValueEditTable
            );

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.saveButton.setOnClickListener(a -> {
                System.out.println(fe.save());
                defaultVisibility();
            });

            binding.cancelEditButton.setVisibility(View.VISIBLE);
            binding.cancelEditButton.setOnClickListener(a -> {
                defaultVisibility();
            });

            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setOnClickListener(a -> {
                deleteField(field);
            });
        });

    }

    private void deleteField(inputType field){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Warning!");
        alert.setMessage("Removing a value will result in data being deleted in subsequent field versions!");
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                defaultVisibility();

                int oldindex = -1;
                for(int i = 0; i < values[values.length - 1].length; i++){
                    if(values[values.length - 1][i].equals(field)){
                        oldindex = i;
                        break;
                    }
                }

                if(oldindex != -1) {
                    System.out.println(Arrays.toString(values[values.length - 1]));
                    binding.fieldsArea.removeViewAt(selindex);
                    binding.fieldsArea.removeElement(oldindex);
                    values[values.length - 1] = removeElement(values[values.length - 1], oldindex);
                    selindex = -1;
                    AlertManager.toast("Removed!");
                    binding.editButton.setVisibility(View.GONE);
                    System.out.println(Arrays.toString(values[values.length - 1]));
                    //System.out.println(values[values.length-1].length);
                }

            }
        });
        alert.setCancelable(true);
        alert.create().show();
    }

    public inputType[] removeElement(inputType[] src, int i) {
        inputType[] newArray = new inputType[src.length - 1];
        if (i > 0){
            System.arraycopy(src, 0, newArray, 0, i);
        }

        if (newArray.length > i){
            System.arraycopy(src, i + 1, newArray, i, newArray.length - i);
        }

        return newArray;
    }










    private void defaultVisibility() {
        binding.editButton.setVisibility(View.VISIBLE);
        binding.addButton.setVisibility(View.VISIBLE);
        binding.upButton.setVisibility(View.VISIBLE);
        binding.downButton.setVisibility(View.VISIBLE);
        binding.ValueEditTable.removeAllViews();
        binding.valueEditContainer.setVisibility(View.GONE);
        binding.cancelEditButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.GONE);
        binding.saveButton.setOnClickListener(this::buttonfunc);
    }




    private void addField(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Title");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                if(title.isEmpty() || title.isBlank()) {
                    AlertManager.error("Title cannot be blank!");
                    return;
                }
                addField_Part_2(title);
            }
        });

        builder.show();
    }

    private void addField_Part_2(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Title");

        final PowerSpinnerView dropdown = new PowerSpinnerView(getContext());

        List<IconSpinnerItem> iconSpinnerItems = new ArrayList<>();

        iconSpinnerItems.add(new IconSpinnerItem("Slider"));
        iconSpinnerItems.add(new IconSpinnerItem("Text"));
        iconSpinnerItems.add(new IconSpinnerItem("Dropdown"));
        iconSpinnerItems.add(new IconSpinnerItem("Tally"));
        iconSpinnerItems.add(new IconSpinnerItem("Number"));
        iconSpinnerItems.add(new IconSpinnerItem("Checkbox"));
        iconSpinnerItems.add(new IconSpinnerItem("Field Position"));

        IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(dropdown);

        dropdown.setGravity(Gravity.CENTER);

        dropdown.setSpinnerAdapter(iconSpinnerAdapter);
        dropdown.setItems(iconSpinnerItems);

        dropdown.selectItemByIndex(0);

        dropdown.setPadding(10,20,10,20);
        dropdown.setBackgroundColor(0xf0000000);
        dropdown.setTextColor(0xff00ff00);
        dropdown.setTextSize(14.5f);
        dropdown.setArrowGravity(SpinnerGravity.END);
        dropdown.setArrowPadding(8);
//        dropdown.setSpinnerItemHeight(46);
        dropdown.setSpinnerPopupElevation(14);

        builder.setView(dropdown);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addField_Part_3(title, dropdown.getSelectedIndex());
            }
        });

        builder.show();
    }

    private void addField_Part_3(String title, int typeIndex) {
        switch (typeIndex){
            case 0:
                sliderType slider = new sliderType();
                slider.name = title;
                FieldEditorHelper.setSliderParams(slider, FieldEditorHelper.defaultSliderParams);
                addField_Part_4(slider);
                break;
            case 1:
                textType text = new textType();
                text.name = title;
                FieldEditorHelper.setTextParams(text, FieldEditorHelper.defaultTextParams);
                addField_Part_4(text);
                break;
            case 2:
                dropdownType dropdown = new dropdownType();
                dropdown.name = title;
                FieldEditorHelper.setDropdownParams(dropdown, FieldEditorHelper.defaultDropdownParams);
                addField_Part_4(dropdown);
                break;
            case 3:
                tallyType tally = new tallyType();
                tally.name = title;
                FieldEditorHelper.setTallyParams(tally, FieldEditorHelper.defaultTallyParams);
                addField_Part_4(tally);
                break;
            case 4:
                numberType num = new numberType();
                num.name = title;
                FieldEditorHelper.setNumberParams(num, FieldEditorHelper.defaultNumberParams);
                addField_Part_4(num);
                break;
            case 5:
                checkboxType cb = new checkboxType();
                cb.name = title;
                FieldEditorHelper.setCheckboxParam(cb, FieldEditorHelper.defaultCheckboxParam);
                addField_Part_4(cb);
                break;
            case 6:
                fieldposType fp = new fieldposType();
                fp.name = title;
                FieldEditorHelper.setFieldPosParam(fp, FieldEditorHelper.defaultFieldPosParam);
                addField_Part_4(fp);
                break;

        }

    }
    private void addField_Part_4(inputType field) {
        binding.editButton.setVisibility(View.GONE);
        binding.addButton.setVisibility(View.GONE);
        binding.upButton.setVisibility(View.GONE);
        binding.downButton.setVisibility(View.GONE);

        binding.ValueEditTable.removeAllViews();
        binding.valueEditContainer.setVisibility(View.VISIBLE);
        TextView tv = new TextView(getContext());

        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setText(field.name);
        tv.setPadding(8,8,8,8);
        tv.setTextSize(24);

        binding.ValueEditTable.addView(tv);

        final FieldEditorHelper fe = new FieldEditorHelper(
                getContext(),
                field,
                binding.ValueEditTable
        );

        binding.saveButton.setVisibility(View.VISIBLE);
        binding.saveButton.setOnClickListener(a -> {
            inputType[] newValues = new inputType[values[values.length-1].length+1];
            System.arraycopy(values[values.length-1], 0, newValues, 0, values[values.length-1].length);
            newValues[newValues.length-1] = field;
            values[values.length-1] = newValues;

//            AlertManager.alert("Test", String.valueOf(binding.fieldsArea.getReorderedIndexes()));

            //TableRow tr = getTableRow(field);
            //binding.fieldsArea.addView(tr);
            addRow(field);

            System.out.println(fe.save());
            defaultVisibility();
        });

        binding.cancelEditButton.setVisibility(View.VISIBLE);
        binding.cancelEditButton.setOnClickListener(a -> {
            defaultVisibility();
        });

    }

    private @NonNull TableRow getTableRow(inputType field) {
        TableRow tr = new TableRow(getContext());
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        rowParams.setMargins(20,20,20,20);
        tr.setLayoutParams(rowParams);
        tr.setPadding(20,20,20,20);
        tr.setBackgroundColor(unfocused_background_color);

        TextView tv = new TextView(getContext());
        tv.setText(field.get_type_name());
        tv.setTextSize(12);
        tr.addView(tv);

        tv = new TextView(getContext());
        tv.setText(field.name);
        tv.setTextSize(20);
        tr.addView(tv);
        return tr;
    }
}
