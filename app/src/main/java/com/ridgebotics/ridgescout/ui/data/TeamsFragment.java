package com.ridgebotics.ridgescout.ui.data;

import static com.ridgebotics.ridgescout.utility.Colors.toggletitle_black_background;
import static com.ridgebotics.ridgescout.utility.Colors.toggletitle_unselected;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.match_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.match_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_values;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.SettingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentDataTeamsBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment {
    FragmentDataTeamsBinding binding;

    private static frcTeam team;
    public static void setTeam(frcTeam tmpteam){
        team = tmpteam;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataTeamsBinding.inflate(inflater, container, false);

        DataManager.reload_match_fields();
        DataManager.reload_pit_fields();

        binding.dataTypeSpinner.setTitle("Data Mode");

        List<String> options = new ArrayList<>();
        options.add("Individual");
        options.add("Compiled");
        options.add("History");

        binding.dataTypeSpinner.setOptions(options, SettingsManager.getTeamsDataMode());

        binding.dataTypeSpinner.setOnClickListener((item, index) -> {
            SettingsManager.setTeamsDataMode(index);
            loadTeam(index);
        });

        loadTeam(SettingsManager.getTeamsDataMode());

        return binding.getRoot();
    }

    public void loadTeam(int mode) {

//        LinearLayout ll = new LinearLayout(getContext());
//        ll.setLayoutParams(new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        ));
//        ll.setOrientation(LinearLayout.VERTICAL);
//        binding.teamsArea.addView(ll);

        binding.dataTeamCard.fromTeam(team);

//        tv = new TextView(getContext());
//        tv.setLayoutParams(new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        ));
//        tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        tv.setText(team.getDescription());
//        tv.setTextSize(16);
//        ll.addView(tv);

        try {add_pit_data(team);}catch(Exception e){AlertManager.error(e);}
        try {add_match_data(team, mode);}catch(Exception e){AlertManager.error(e);}
    }

    public void add_pit_data(frcTeam team){
        binding.pitArea.removeAllViews();
        final String filename = evcode+"-"+team.teamNumber+".pitscoutdata";

//        ll.addView(new MaterialDivider(getContext()));

//        TextView tv = new TextView(getContext());
//        tv.setLayoutParams(new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        ));
//        tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        tv.setPadding(0,10,0,10);
//        tv.setText("----- Pit data -----");
//        tv.setTextSize(30);
//        ll.addView(tv);

//        ll.addView(new MaterialDivider(getContext()));

        if(!FileEditor.fileExist(filename)){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("No pit data has been collected!");
            tv.setTextSize(23);
            binding.pitArea.addView(tv);
            return;
        }

        ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(filename, pit_values, pit_transferValues);

        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setPadding(0, 20, 0, 5);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText("Pit scouting by " + psda.username);
        tv.setTextSize(30);
        binding.pitArea.addView(tv);

        for (int a = 0; a < psda.data.array.length; a++) {
            tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(pit_latest_values[a].name);
            tv.setTextSize(25);

            if(psda.data.array[a].isNull()){
                tv.setBackgroundColor(toggletitle_unselected);
                tv.setTextColor(toggletitle_black_background);
            }



            binding.pitArea.addView(tv);


            pit_latest_values[a].add_individual_view(binding.pitArea, psda.data.array[a]);
        }
    }


    private int matchIndex = 0;

    public void add_match_data(frcTeam team, int mode){
        binding.matchArea.removeAllViews();
        binding.individualViewSelector.setVisibility(View.GONE);
        String[] files = FileEditor.getMatchesByTeamNum(evcode, team.teamNumber);

        if(files.length == 0){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("No match data has been collected!");
            tv.setTextSize(23);
            binding.matchArea.addView(tv);
            return;
        }

        switch (mode){
            case 0:
                add_individual_views(files);
                break;
            case 1:
                add_compiled_views(files);
                break;
            case 2:
                add_history_views(files);
                break;
        }
    }




    public void add_individual_views(String[] files) {


        matchIndex = 0;

        binding.individualViewSelector.setVisibility(View.VISIBLE);

        binding.matchesPlusBtn.setOnClickListener(view -> {
            matchIndex++;
            update_individual_view(files);
        });

        binding.matchesMinusBtn.setOnClickListener(view -> {
            matchIndex--;
            update_individual_view(files);
        });

        update_individual_view(files);
    }

    private void update_individual_view(String[] files){
        binding.matchesPlusBtn.setEnabled(matchIndex < files.length - 1);
        binding.matchesMinusBtn.setEnabled(matchIndex > 0);
        binding.matchArea.removeAllViews();


        try {
            String[] split = files[matchIndex].split("-");
            int match_num = Integer.parseInt(split[1]);
            binding.matchNum.setText(split[1]);

            ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[matchIndex], match_values, match_transferValues);

            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(0, 40, 0, 5);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("M" + (match_num) + " " + split[2] + "-" + split[3] + " by " + psda.username);
            tv.setTextSize(30);
            binding.matchArea.addView(tv);

            for (int i = 0; i < psda.data.array.length; i++) {
                tv = new TextView(getContext());
                tv.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText(match_latest_values[i].name);
                tv.setTextSize(25);

                if (psda.data.array[i].isNull()) {
                    tv.setBackgroundColor(toggletitle_unselected);
                    tv.setTextColor(toggletitle_black_background);
                }

                binding.matchArea.addView(tv);


                if(psda.data.array[i] != null)
                    match_latest_values[i].add_individual_view(binding.matchArea, psda.data.array[i]);
            }
        }catch (Exception e){
            AlertManager.error("Failure to load file " + files[matchIndex], e);
        }

    }






    public void add_compiled_views(String[] files){
        DataType[][] data = new DataType[match_latest_values.length][files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[i], match_values, match_transferValues);
                for (int a = 0; a < data.length; a++) {
                    if(psda.data.array[a] != null && psda.data.array[a].get() != null)
                        data[a][i] = psda.data.array[a];
                }
            } catch (Exception e){
                AlertManager.error("Failure to load file " + files[i], e);
            }
        }

        for(int i = 0; i < match_latest_values.length; i++){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(0, 20, 0, 5);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(match_latest_values[i].name);
            tv.setTextSize(30);
            binding.matchArea.addView(tv);

            if(data[i] != null)
                match_latest_values[i].add_compiled_view(binding.matchArea, data[i]);
        }
    }





    public void add_history_views(String[] files){
        DataType[][] data = new DataType[match_latest_values.length][files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[i], match_values, match_transferValues);
                for (int a = 0; a < data.length; a++) {
                    if(psda.data.array[a] != null && psda.data.array[a].get() != null)
                        data[a][i] = psda.data.array[a];
                }
            }catch (Exception e){
                AlertManager.error("Failure to load file " + files[i], e);
            }
        }

        for(int i = 0; i < match_latest_values.length; i++){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(0, 20, 0, 5);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(match_latest_values[i].name);
            tv.setTextSize(30);
            binding.matchArea.addView(tv);

            if(data[i] != null)
                match_latest_values[i].add_history_view(binding.matchArea, data[i]);
        }
    }
}
