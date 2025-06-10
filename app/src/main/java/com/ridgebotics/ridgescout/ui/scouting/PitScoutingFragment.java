package com.ridgebotics.ridgescout.ui.scouting;

import static com.ridgebotics.ridgescout.utility.AutoSaveManager.AUTO_SAVE_DELAY;
import static com.ridgebotics.ridgescout.utility.Colors.rescout_color;
import static com.ridgebotics.ridgescout.utility.Colors.saved_color;
import static com.ridgebotics.ridgescout.utility.Colors.unsaved_color;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_values;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.divider.MaterialDivider;
import com.ridgebotics.ridgescout.ui.views.ToggleTitleView;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.SettingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentScoutingPitBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.utility.AutoSaveManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.ArrayList;
import java.util.function.Function;

// Fragment for pit scouting data editing
public class PitScoutingFragment extends Fragment {

    FragmentScoutingPitBinding binding;

    private static frcTeam team;
    public static void setTeam(frcTeam tmpteam){
        team = tmpteam;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentScoutingPitBinding.inflate(inflater, container, false);

        username = SettingsManager.getUsername();
        DataManager.reload_pit_fields();

        if(pit_latest_values == null) {
            AlertManager.addSimpleError("Error loading pit fields!");
            return binding.getRoot();
        }

        if(DataManager.scoutNotice.isEmpty())
            binding.scoutingNoticeBox.setVisibility(View.GONE);
        else
            binding.scoutingNoticeText.setText(DataManager.scoutNotice);

        loadTeam();

        return binding.getRoot();
    }
    boolean edited = false;
    boolean rescout = false;

    String filename;
    String username;

    String fileUsernames = "";
    ToggleTitleView[] titles;

    AutoSaveManager asm = new AutoSaveManager(this::save, AUTO_SAVE_DELAY);

    ArrayList<RawDataType> rawDataTypes;

    public void save(){
        edited = false;
        enableRescoutButton();

        RawDataType[] types = new RawDataType[pit_latest_values.length];

        for(int i = 0; i < pit_latest_values.length; i++){
            types[i] = pit_latest_values[i].getViewValue();
        }

        if(ScoutingDataWriter.save(pit_values.length-1, ScoutingDataWriter.checkAddName(fileUsernames, username), filename, types)) {
            Log.i(getClass().toString(), "Saved!");
            Log.i(getClass().toString(), "Saved " + filename);
        }else
            Log.i(getClass().toString(), "Error saving");
    }

    public void set_indicator_color(int color){
        binding.pitFileIndicator.setBackgroundColor(color);
    }

    public void update_asm(){
//        v.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
        edited = true;
        set_indicator_color(unsaved_color);
        disableRescoutButton();
        asm.update();
    }


    public void loadTeam(){
//        clear_fields();

        binding.pitFileIndicator.setVisibility(View.VISIBLE);
        binding.pitsTeamCard.setVisibility(View.VISIBLE);
        binding.pitBarTeamNum.setText(String.valueOf(team.teamNumber));
        binding.pitUsername.setText(SettingsManager.getUsername());
        binding.pitsTeamCard.fromTeam(team);

        filename = evcode + "-" + team.teamNumber + ".pitscoutdata";
        rescout = DataManager.rescout_list.contains(filename);

        if(asm.isRunning){
            asm.stop();
        }

        create_fields();

        if(!FileEditor.fileExist(filename)){
            default_fields();
            set_indicator_color(unsaved_color);
            disableRescoutButton();
        }else{
            try {
                get_fields();

                enableRescoutButton();

            } catch (Exception e){
                AlertManager.error(e);
                default_fields();
                set_indicator_color(unsaved_color);
            }
        }

        binding.pitFileIndicator.bringToFront();

        asm.start();

    }

    private void enableRescoutButton(){
        set_indicator_color(rescout ? rescout_color : saved_color);
        binding.pitFileIndicator.setOnLongClickListener(v -> {
            rescout = !rescout;
            if(rescout){
                set_indicator_color(rescout_color);
                DataManager.rescout_list.add(filename);
                DataManager.save_rescout_list();
            }else{
                set_indicator_color(saved_color);
                DataManager.rescout_list.remove(filename);
                DataManager.save_rescout_list();
            }

            return true;
        });
    }

    private void disableRescoutButton(){
        binding.pitFileIndicator.setOnLongClickListener(null);
    }


    private void create_fields() {
        if(asm.isRunning){
            asm.stop();
        }

        titles = new ToggleTitleView[pit_latest_values.length];

        for(int i = 0 ; i < pit_latest_values.length; i++) {
            binding.pitScoutArea.addView(new MaterialDivider(getContext()));

            ToggleTitleView ttv = new ToggleTitleView(getContext());
            ttv.setTitle(pit_latest_values[i].name);
            ttv.setDescription(pit_latest_values[i].description);
            titles[i] = ttv;
            binding.pitScoutArea.addView(ttv);


            int fi = i;
            ttv.setOnToggleListener(enabled -> {
                update_asm();

                if(enabled){
                    pit_latest_values[fi].nullify();
                }else{
                    pit_latest_values[fi].setViewValue(pit_latest_values[fi].default_value);
                }
            });

            View v = pit_latest_values[i].createView(getContext(), new Function<RawDataType, Integer>() {
                @Override
                public Integer apply(RawDataType dataType) {
//                    edited = true;
                    if(asm.isRunning)
                        update_asm();
                    return 0;
                }
            });

            binding.pitScoutArea.addView(v);
        }
    }

    public void default_fields(){
        for(int i = 0; i < pit_latest_values.length; i++){
            FieldType input = pit_latest_values[i];
            input.setViewValue(input.default_value);
            titles[i].enable();
        }
    }

    public void get_fields(){

        ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, pit_values, pit_transferValues);
        RawDataType[] types = psdr.data.array;
        fileUsernames = psdr.username;


        for(int i = 0; i < pit_latest_values.length; i++){
            pit_latest_values[i].setViewValue(types[i]);

            if(pit_latest_values[i].isBlank){
                titles[i].disable();
            }else{
                titles[i].enable();
            }
        }
    }
}
