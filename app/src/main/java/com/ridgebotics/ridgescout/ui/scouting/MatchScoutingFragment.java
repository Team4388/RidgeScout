package com.ridgebotics.ridgescout.ui.scouting;

import static com.ridgebotics.ridgescout.utility.AutoSaveManager.AUTO_SAVE_DELAY;
import static com.ridgebotics.ridgescout.utility.Colors.rescout_color;
import static com.ridgebotics.ridgescout.utility.Colors.saved_color;
import static com.ridgebotics.ridgescout.utility.Colors.unsaved_color;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.divider.MaterialDivider;
import com.ridgebotics.ridgescout.ui.views.ToggleTitleView;
import com.ridgebotics.ridgescout.utility.SettingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentScoutingMatchBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.AutoSaveManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

// Fragment for match scouting data editing.
public class MatchScoutingFragment extends Fragment {

    private FragmentScoutingMatchBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScoutingMatchBinding.inflate(inflater, container, false);

        DataManager.reload_match_fields();

        alliance_position = SettingsManager.getAllyPos();
        username = SettingsManager.getUsername();

        binding.username.setText(username);
        binding.alliancePosText.setText(alliance_position);

        binding.matchTeamCard.setVisibility(View.GONE);
        clear_fields();
        binding.matchTeamCard.setVisibility(View.VISIBLE);

        if(DataManager.match_values == null || DataManager.match_values.length == 0){
            TextView tv = new TextView(getContext());
            tv.setText("Failed to load fields.\nTry to either download or create match scouting fields.");
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            binding.MatchScoutArea.addView(tv);
            return binding.getRoot();
        }



        binding.nextButton.setOnClickListener(v -> {
            if(edited) save();
            SettingsManager.setMatchNum(cur_match_num+1);
            cur_match_num += 1;
            update_match_num();
            update_scouting_data();
        });

        if(SettingsManager.getEnableQuickAlliancePosChange())
            binding.fileIndicator.setOnClickListener(v -> {
    //            if(e.getAction() != MotionEvent.ACTION_MOVE) return true;
    //            System.out.println(e.getAxisValue(0));
                if(edited) save();

                alliance_position = incrementMatchPos(alliance_position);
                SettingsManager.setAllyPos(alliance_position);
                binding.alliancePosText.setText(alliance_position);

                update_match_num();
                update_scouting_data();
    //            return true;
            });

        binding.backButton.setOnClickListener(v -> {
            if(edited) save();
            SettingsManager.setMatchNum(cur_match_num-1);
            cur_match_num -= 1;
            update_match_num();
            update_scouting_data();
        });

//        binding.middleButton.setOnClickListener(v -> {
//            if(edited) save();
//        });

        cur_match_num = SettingsManager.getMatchNum();

        if(cur_match_num >= event.matches.size()) {
            cur_match_num = 0;
            SettingsManager.setMatchNum(0);
        }

        update_match_num();
        create_fields();
        update_scouting_data();

        if(DataManager.scoutNotice.isEmpty())
            binding.scoutingNoticeBox.setVisibility(View.GONE);
        else
            binding.scoutingNoticeText.setText(DataManager.scoutNotice);

        return binding.getRoot();
    }

    private static String incrementMatchPos(String input){
        switch(input){ // There's probably a better solution than this.
            case "red-1":
                return "red-2";
            case "red-2":
                return "red-3";
            case "red-3":
                return "blue-1";
            case "blue-1":
                return "blue-2";
            case "blue-2":
                return "blue-3";
            case "blue-3":
                return "red-1";
        }
        return "red-1";
    }

    String alliance_position;
    int cur_match_num;
    String username;
    String fileUsernames = "";
    String filename;
    boolean edited = false;
    boolean rescout = false;
    ToggleTitleView[] titles;
    AutoSaveManager asm = new AutoSaveManager(this::save, AUTO_SAVE_DELAY);



    public void save(){
        System.out.println("Saved!");
        edited = false;
        enableRescoutButton();
        AlertManager.toast("Saved " + filename);
        save_fields();
    }

    public void set_indicator_color(int color){
        binding.fileIndicator.setBackgroundColor(color);
    }

    public void update_asm(){
//        v.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
        edited = true;
        set_indicator_color(unsaved_color);
        disableRescoutButton();
        asm.update();
    }


    public void clear_fields(){
        int childCount = binding.MatchScoutArea.getChildCount();
        View[] views = new View[childCount];

        for(int i = 0; i < childCount; i++){
            views[i] = binding.MatchScoutArea.getChildAt(i);
        }

        for(int i = 0; i < childCount; i++){
            if(!views[i].isShown()) continue;
            binding.MatchScoutArea.removeView(views[i]);
        }
    }

    private int default_text_color = 0;

    private void create_fields(){
        if(asm.isRunning){
            asm.stop();
        }

        titles = new ToggleTitleView[DataManager.match_latest_values.length];

        for(int i = 0 ; i < DataManager.match_latest_values.length; i++) {
            binding.MatchScoutArea.addView(new MaterialDivider(getContext()));


            final ToggleTitleView ttv = new ToggleTitleView(getContext());
            ttv.setTitle(DataManager.match_latest_values[i].name);
            ttv.setDescription(DataManager.match_latest_values[i].description);
            titles[i] = ttv;


            final View v = DataManager.match_latest_values[i].createView(getContext(), dataType -> {
//                edited = true;
                if(asm.isRunning)
                    update_asm();
                return 0;
            });

            binding.MatchScoutArea.addView(ttv);
            int fi = i;

            ttv.setOnToggleListener(enabled -> {
                if(asm.isRunning)
                    update_asm();

//                System.out.println("Checked!");

                if(enabled){
                    DataManager.match_latest_values[fi].nullify();
                }else
                    DataManager.match_latest_values[fi].setViewValue(DataManager.match_latest_values[fi].default_value);
            });


            binding.MatchScoutArea.addView(v);
        }
    }




    private void update_match_num(){
//        cur_match_num = latestSettings.settings.get_match_num();

        edited = false;

        binding.matchnum.setText(String.valueOf(cur_match_num+1));

        if(cur_match_num <= 0){
            binding.backButton.setVisibility(View.GONE);
        }else{
            binding.backButton.setVisibility(View.VISIBLE);
        }

        if(cur_match_num >= event.matches.size()-1){
            binding.nextButton.setVisibility(View.GONE);
        }else{
            binding.nextButton.setVisibility(View.VISIBLE);
        }
    }




    private frcTeam get_team(frcMatch match){

        // Get team number
        String[] split = alliance_position.split("-");
        Integer team_num = null;

        switch (split[0]){
            case "red":
                team_num = match.redAlliance[Integer.parseInt(split[1])-1];
                break;
            case "blue":
                team_num = match.blueAlliance[Integer.parseInt(split[1])-1];
                break;
        }

        binding.barTeamNum.setText(String.valueOf(team_num));

        frcTeam team = null;
        for(int i=0; i < event.teams.size(); i++){
            frcTeam tmpteam = event.teams.get(i);
            if(tmpteam.teamNumber == team_num){
                team = tmpteam;
                break;
            }
        }

        filename = evcode + "-" + (cur_match_num+1) + "-" + alliance_position + "-" + team_num + ".matchscoutdata";

        rescout = DataManager.rescout_list.contains(filename);

        return team;
    }




    public void update_scouting_data(){

        frcMatch match = event.matches.get(cur_match_num);
        frcTeam team = get_team(match);

        if(team == null) {
            AlertManager.addSimpleError("This team does not exist!");
            binding.matchTeamCard.setTeamName("Error!");
            binding.matchTeamCard.setTeamDescription("Error!");
            return;
        }

        binding.matchTeamCard.fromTeam(team);

        boolean new_file = !FileEditor.fileExist(filename);

        if(asm.isRunning){
            asm.stop();
        }

        if(new_file){
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
                disableRescoutButton();
            }
        }

        asm.start();

    }



    public void default_fields(){
        for(int i = 0; i < DataManager.match_latest_values.length; i++){
            FieldType input = DataManager.match_latest_values[i];
            input.setViewValue(input.default_value);

            titles[i].enable();
        }
    }



    public void get_fields(){

        ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, DataManager.match_values, DataManager.match_transferValues);
        RawDataType[] types = psdr.data.array;
        fileUsernames = psdr.username;


        for(int i = 0; i < DataManager.match_latest_values.length; i++){
//            types[i] = latest_values[i].getViewValue();
            try {
                DataManager.match_latest_values[i].setViewValue(types[i].get());
            } catch (Exception e){
                AlertManager.error(e);
                DataManager.match_latest_values[i].setViewValue(DataManager.match_latest_values[i].default_value);
            }

            titles[i].setEnabled(DataManager.match_latest_values[i].isBlank);

        }
    }



    public void save_fields(){

        RawDataType[] types = new RawDataType[DataManager.match_latest_values.length];

        for(int i = 0; i < DataManager.match_latest_values.length; i++){
            types[i] = DataManager.match_latest_values[i].getViewValue();
        }

        if(ScoutingDataWriter.save(DataManager.match_values.length-1, ScoutingDataWriter.checkAddName(fileUsernames, username), filename, types))
            System.out.println("Saved!");
        else
            System.out.println("Error saving");
    }

    private void enableRescoutButton(){
        set_indicator_color(rescout ? rescout_color : saved_color);
        binding.fileIndicator.setOnLongClickListener(v -> {
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
        binding.fileIndicator.setOnLongClickListener(null);
    }
}
