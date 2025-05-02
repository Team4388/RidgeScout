package com.ridgebotics.ridgescout.ui.scouting;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_values;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentScoutingPitBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.types.input.inputType;
import com.ridgebotics.ridgescout.utility.AutoSaveManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.fileEditor;

import java.util.ArrayList;
import java.util.function.Function;

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

        username = settingsManager.getUsername();
        DataManager.reload_pit_fields();

        loadTeam();

        return binding.getRoot();
    }
    private static final int unsaved_color = 0x60ff0000;
    private static final int saved_color = 0x6000ff00;

    boolean edited = false;

    String filename;
    String username;

    TextView[] titles;

    AutoSaveManager asm = new AutoSaveManager(this::save);

    ArrayList<dataType> dataTypes;

    public void save(){
        edited = false;
        set_indicator_color(saved_color);

        dataType[] types = new dataType[pit_latest_values.length];

        for(int i = 0; i < pit_latest_values.length; i++){
            types[i] = pit_latest_values[i].getViewValue();
        }

        if(ScoutingDataWriter.save(pit_values.length-1, username, filename, types)) {
            System.out.println("Saved!");
            AlertManager.toast("Saved " + filename);
        }else
            System.out.println("Error saving");
    }

    public void set_indicator_color(int color){
        binding.pitFileIndicator.setBackgroundColor(color);
    }

    public void update_asm(){
//        v.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
        edited = true;
        set_indicator_color(unsaved_color);
        asm.update();
    }


    public void loadTeam(){
//        clear_fields();

        binding.pitFileIndicator.setVisibility(View.VISIBLE);
        binding.pitTeamName.setVisibility(View.VISIBLE);
        binding.pitTeamDescription.setVisibility(View.VISIBLE);

        binding.pitTeamName.setText(team.teamName);
        binding.pitTeamDescription.setText(team.getDescription());
        binding.pitBarTeamNum.setText(String.valueOf(team.teamNumber));

        filename = evcode + "-" + team.teamNumber + ".pitscoutdata";

        boolean new_file = !fileEditor.fileExist(filename);

        if(asm.isRunning){
            asm.stop();
        }

        create_fields();

        if(new_file){
            default_fields();
            set_indicator_color(unsaved_color);
        }else{
            try {
                get_fields();
                set_indicator_color(saved_color);
            } catch (Exception e){
                AlertManager.error(e);
                default_fields();
                set_indicator_color(unsaved_color);
            }
        }

        asm.start();

    }

    private int default_text_color = 0;


    private void create_fields() {
        if(asm.isRunning){
            asm.stop();
        }

        titles = new TextView[pit_latest_values.length];

        for(int i = 0 ; i < pit_latest_values.length; i++) {
            TextView tv = new TextView(getContext());
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setText(pit_latest_values[i].name);
            tv.setTextSize(24);
            tv.setPadding(8,8,8,8);
            titles[i] = tv;
            binding.pitScoutArea.addView(tv);

            default_text_color = tv.getCurrentTextColor();

            int fi = i;
            tv.setOnClickListener(p -> {
                update_asm();

                if(!pit_latest_values[fi].isBlank){
                    tv.setBackgroundColor(0xffff0000);
                    tv.setTextColor(0xff000000);
                    pit_latest_values[fi].nullify();
                }else{
                    tv.setBackgroundColor(0x00000000);
                    tv.setTextColor(default_text_color);
                    pit_latest_values[fi].setViewValue(pit_latest_values[fi].default_value);
                }
            });

            View v = pit_latest_values[i].createView(getContext(), new Function<dataType, Integer>() {
                @Override
                public Integer apply(dataType dataType) {
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
            inputType input = pit_latest_values[i];
            input.setViewValue(input.default_value);

            titles[i].setBackgroundColor(0x00000000);
            titles[i].setTextColor(default_text_color);
        }
    }

    public void get_fields(){

        ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, pit_values, pit_transferValues);
        dataType[] types = psdr.data.array;

        for(int i = 0; i < pit_latest_values.length; i++){
//            types[i] = latest_values[i].getViewValue();
            pit_latest_values[i].setViewValue(types[i]);

            if(pit_latest_values[i].isBlank){
                titles[i].setBackgroundColor(0xffff0000);
                titles[i].setTextColor(0xff000000);
            }else{
                titles[i].setBackgroundColor(0x00000000);
                titles[i].setTextColor(default_text_color);
            }
        }
    }
}
