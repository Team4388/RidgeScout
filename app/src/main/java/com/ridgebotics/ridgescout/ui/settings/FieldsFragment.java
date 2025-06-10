package com.ridgebotics.ridgescout.ui.settings;

import static com.ridgebotics.ridgescout.utility.Colors.background_color;
import static com.ridgebotics.ridgescout.utility.Colors.unfocused_background_color;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ridgebotics.ridgescout.MainActivity;
import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentSettingsFieldsBinding;
import com.ridgebotics.ridgescout.scoutingData.Fields;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.ui.views.CustomSpinnerView;
import com.ridgebotics.ridgescout.ui.views.FieldDisplay;
import com.ridgebotics.ridgescout.utility.AlertManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Fragment that shows the field editor.
public class FieldsFragment extends Fragment {
    FragmentSettingsFieldsBinding binding;

    private static String filename;
    public static void set_filename(String tmpfilename){
        filename = tmpfilename;
    }

    private int index = -1;

    private boolean edited = false;

    List<FieldType> values;
    List<FieldDisplay> views;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsFieldsBinding.inflate(inflater, container, false);

        binding.upButton.setEnabled(false);
        binding.downButton.setEnabled(false);
        binding.saveButton.setEnabled(false);

        FieldType[][] tmp_values = Fields.load(filename);
        if(tmp_values == null || tmp_values.length == 0) return binding.getRoot();

        values = new ArrayList(List.of(tmp_values[tmp_values.length-1]));
        views = new ArrayList<>();

        for(int i = 0; i < values.size(); i++){
            createFieldDisplay(values.get(i));
        }

        // Up and down buttons
        binding.upButton.setOnClickListener(v -> {
            if(index <= 0) return;
            Collections.swap(values, index, index-1);
            Collections.swap(views, index, index-1);
            index--;
            updateRowOrder();
        });
        binding.downButton.setOnClickListener(v -> {
            if(index >= values.size()-1) return;
            Collections.swap(values, index, index+1);
            Collections.swap(views, index, index+1);
            index++;
            updateRowOrder();
        });

        // Add Field button
        binding.addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Type");

            final CustomSpinnerView dropdown = new CustomSpinnerView(getContext());
            List<String> options = new ArrayList<>();

            options.add("Slider");
            options.add("Text");
            options.add("Dropdown");
            options.add("Tally");
            options.add("Number");
            options.add("Checkbox");
            options.add("Field Position");

            dropdown.setOptions(options, 0);
            dropdown.setTitle("Type");

            builder.setView(dropdown);

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.setPositiveButton("OK", (dialog, which) -> addField(dropdown.getIndex()));

            builder.show();
        });

        // Back button listener
        ((MainActivity) getActivity()).setOnBackPressed(() -> {
            if(!edited) return true;

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning!");
            alert.setMessage("You have not saved your progress!");
            alert.setPositiveButton("Return", null);
            alert.setNeutralButton("Quit without saving", (dialogInterface, i) -> {
                edited = false;
                if(getActivity() != null)
                    getActivity().onBackPressed();
            });
            alert.setCancelable(true);

            alert.create().show();

            return false;
        });

        binding.saveButton.setOnClickListener(l -> save());

        if(tmp_values.length > 1)
            binding.revertButton.setOnClickListener(v -> revertPopup());
        else
            binding.revertButton.setEnabled(false);


        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFieldDisplay(FieldType field){
        final FieldDisplay fd = new FieldDisplay(getContext());
        views.add(fd);

        fd.setField(field);
        fd.setColor(unfocused_background_color);
        fd.coloredBackground.setOnTouchListener((view, motionEvent) -> {
            FieldsFragment.this.setFocus(fd, false);
            return true;
        });
        fd.editButton.setOnClickListener(v -> openEditor(fd));

        binding.fieldsArea.addView(fd);
    }
    private void updateRowOrder(){
        enableSaving();
        binding.fieldsArea.removeAllViews();
        for(int i = 0; i < views.size(); i++){
            binding.fieldsArea.addView(views.get(i));
        }

        binding.upButton.setEnabled(index != -1 && index > 0);
        binding.downButton.setEnabled(index != -1 && index < views.size()-1);
    }

    private void setFocus(FieldDisplay fd, boolean scroll){
        index = views.indexOf(fd);
        for(int a = 0; a < values.size(); a++) {
            views.get(a).setColor(unfocused_background_color);
            views.get(a).hideButtons();
        }
        fd.setColor(background_color);
        fd.showButtons();

        binding.upButton.setEnabled(index != -1 && index > 0);
        binding.downButton.setEnabled(index != -1 && index < views.size()-1);
        if(scroll)
            binding.scrollView.post(() -> binding.scrollView.scrollTo(0, fd.getTop()));
    }

    private void openEditor(FieldDisplay fd){
        FieldType field = fd.getField();

        ScrollView sv = new ScrollView(getContext());
        TableLayout table = new TableLayout(getContext());
        table.setStretchAllColumns(true);
        table.setPadding(10, 10, 10, 10);

        sv.addView(table);

        TextView UUID = new TextView(getContext());
        UUID.setText("Type: " + field.get_type_name() + "\nUUID: " + field.UUID);

        table.addView(UUID);

        FieldEditorHelper f = new FieldEditorHelper(getContext(), field, table);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Edit " + field.name);
        alert.setView(sv);
        alert.setCancelable(false);
        alert.setNeutralButton("Cancel", (dialogInterface, i) -> {});
        alert.setPositiveButton("Save", (dialogInterface, i) -> {
            f.save();
            fd.setField(field);
            enableSaving();
        });

        AlertDialog dialog = alert.create();
        dialog.show();

        Button deleteButton = new Button(getContext());
        deleteButton.setText("DELETE");
        deleteButton.setOnClickListener(l -> {
            AlertDialog.Builder alert2 = new AlertDialog.Builder(getContext());
            alert2.setTitle("Warning!");
            alert2.setMessage("This may destroy any data after being saved!");
            alert2.setPositiveButton("Return", (dialogInterface, i) -> {});
            alert2.setNeutralButton("DELETE", (dialogInterface, i) -> {
                removeField(field);
                dialog.cancel();
            });

            alert2.setCancelable(true);
            alert2.create().show();
        });

        table.addView(deleteButton);
    }

    private void enableSaving(){
        edited = true;
        binding.saveButton.setEnabled(true);
    }

    private void addField(int n){
        FieldType field = FieldEditorHelper.createNewFieldType(n);

        values.add(field);
        createFieldDisplay(field);
        setFocus(views.get(views.size()-1), true);
        enableSaving();
    }

    private void removeField(FieldType field){
        int fieldIndex = values.indexOf(field);

        views.remove(fieldIndex);
        values.remove(fieldIndex);

        index = -1;

        updateRowOrder();
    }

    private void save(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Warning!");
        alert.setMessage("Changing or removing some values will result in lost data! but you can revert at any time.");
        alert.setNeutralButton("Save", (dialog, which) -> {
            FieldType[][] currentValues = Fields.load(filename);
            assert currentValues != null;
            FieldType[][] newValues = new FieldType[currentValues.length+1][];

            System.arraycopy(currentValues, 0, newValues, 0, currentValues.length);

            Log.i(getClass().toString(), "Length: " + values.size());

            newValues[currentValues.length] = new FieldType[values.size()];
           for(int i = 0; i < values.size(); i++) {
               FieldType value = values.get(i);
               newValues[currentValues.length][i] = value;
           }

            if(Fields.save(filename, newValues))
                AlertManager.toast("Saved");

            Navigation.findNavController((Activity) getContext(), R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_data_fields_to_navigation_settings);
        });
        alert.setNegativeButton("Cancel", null);
        alert.setCancelable(true);
        alert.create().show();
    }

    public void revertPopup(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Warning!");
        alert.setMessage("If there is any data scouted with this version of the fields, it will cause conflicts!\nYou should know what you are doing");
        alert.setNeutralButton("Revert and delete version", (dialog, which) -> {
            FieldType[][] currentValues = Fields.load(filename);
            assert currentValues != null;
            FieldType[][] newValues = new FieldType[currentValues.length-1][];

            System.arraycopy(currentValues, 0, newValues, 0, currentValues.length - 1);

            if(Fields.save(filename, newValues))
                AlertManager.toast("Saved");

            Navigation.findNavController((Activity) getContext(), R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_data_fields_to_navigation_settings);
        });
        alert.setNegativeButton("Cancel", null);
        alert.setCancelable(true);
        alert.create().show();
    }
}
