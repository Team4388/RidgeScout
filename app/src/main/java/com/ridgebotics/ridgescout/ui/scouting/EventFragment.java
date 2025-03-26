package com.ridgebotics.ridgescout.ui.scouting;

import static android.widget.LinearLayout.HORIZONTAL;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentScoutingEventBinding;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.CustomSpinnerView;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.utility.settingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventFragment extends Fragment {
    FragmentScoutingEventBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentScoutingEventBinding.inflate(inflater, container, false);

        reloadTable();

        return binding.getRoot();
    }

    public void reloadTable() {
        DataManager.reload_event();
        binding.teamsTable.removeAllViews();
        binding.teamsTable.setStretchAllColumns(true);
        binding.matchTable.removeAllViews();
        binding.matchTable.setStretchAllColumns(true);
        add_pit_scouting(event);
        add_match_scouting(event);
    }

    public static int color_found = 0x7f00ff00;
    public static int color_rescout = 0x7f0000ff;
    public static int color_not_found = 0x7f7f0000;

    private void addTableText(TableRow tr, String textStr){
        TextView text = new TextView(getContext());
        text.setTextSize(18);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text align center
        text.setText(textStr);
        tr.addView(text);
    }

    public void add_pit_scouting(frcEvent event){

        if(settingsManager.getCustomEvents()){
            binding.teamsMinusBtn.setVisibility(View.VISIBLE);
            binding.teamsMinusBtn.setOnClickListener(view -> removeTeam());
            binding.teamsPlusBtn.setVisibility(View.VISIBLE);
            binding.teamsPlusBtn.setOnClickListener(view -> addTeam());
        }

        int[] teams = new int[event.teams.size()];

        for(int i = 0 ; i < event.teams.size(); i++){
            teams[i] = event.teams.get(i).teamNumber;
        }

        Arrays.sort(teams);

        TableRow tr = null;
        for(int i=0; i < event.teams.size(); i++){
//            frcTeam team = event.teams.get(i);
            int num = teams[i];

            if(i % 7 == 0){
                if(i != 0)
                    binding.teamsTable.addView(tr);
                tr = new TableRow(getContext());
            }

            TextView text = new TextView(getContext());
            text.setTextSize(18);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            text.setText(String.valueOf(num));
            final String filename = event.eventCode + "-" + num + ".pitscoutdata";
            if(fileEditor.fileExist(filename)){
                final boolean[] rescout = {DataManager.rescout_list.contains(filename)};

                text.setBackgroundColor(rescout[0] ? color_rescout : color_found);

                text.setOnLongClickListener(view -> {
                    rescout[0] = !rescout[0];
                    if(rescout[0]) {
                        text.setBackgroundColor(color_rescout);
                        DataManager.rescout_list.add(filename);
                        DataManager.save_rescout_list();
                    }else{
                        text.setBackgroundColor(color_found);
                        DataManager.rescout_list.remove(filename);
                        DataManager.save_rescout_list();
                    }
                    return true;
                });
            }else{
                text.setBackgroundColor(color_not_found);
            }
            tr.addView(text);
        }
        if(tr != null)
            binding.teamsTable.addView(tr);
    }


    public void add_match_scouting(frcEvent event){


        if(settingsManager.getCustomEvents()){
            binding.matchesMinusBtn.setVisibility(View.VISIBLE);
            binding.matchesMinusBtn.setOnClickListener(view -> removeMatch());
            binding.matchesPlusBtn.setVisibility(View.VISIBLE);
            binding.matchesPlusBtn.setOnClickListener(view -> addMatch());
        }

        TableRow tr = new TableRow(getContext());
        addTableText(tr, "#");
        addTableText(tr, "Red-1");
        addTableText(tr, "Red-2");
        addTableText(tr, "Red-3");
        addTableText(tr, "Blue-1");
        addTableText(tr, "Blue-2");
        addTableText(tr, "Blue-3");
        binding.matchTable.addView(tr);

        for(frcMatch match : event.matches){

            tr = new TableRow(getContext());
            addTableText(tr, String.valueOf(match.matchIndex));
//
            for(int i=0;i<6;i++){
                TextView text = new TextView(getContext());
                text.setTextSize(18);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                int team_num;
                String alliance_position;

                if(i < 3){
                    team_num = match.redAlliance[i];
                    alliance_position = "red-"+(i+1);
                }else{
                    team_num = match.blueAlliance[i-3];
                    alliance_position = "blue-"+(i-2);
                }

                text.setText(String.valueOf(team_num));
                final String filename = event.eventCode + "-" + match.matchIndex + "-" + alliance_position + "-" + team_num + ".matchscoutdata";
                if(fileEditor.fileExist(filename)){
                    final boolean[] rescout = {DataManager.rescout_list.contains(filename)};

                    text.setBackgroundColor(rescout[0] ? color_rescout : color_found);

                    text.setOnLongClickListener(view -> {
                        rescout[0] = !rescout[0];
                        if(rescout[0]) {
                            text.setBackgroundColor(color_rescout);
                            DataManager.rescout_list.add(filename);
                            DataManager.save_rescout_list();
                        }else{
                            text.setBackgroundColor(color_found);
                            DataManager.rescout_list.remove(filename);
                            DataManager.save_rescout_list();
                        }
                        return true;
                    });

                }else{
                    text.setBackgroundColor(color_not_found);
                }
                tr.addView(text);
            }

            binding.matchTable.addView(tr);
        }
    }

    public void addTeam(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add team");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        EditText teamNum = new EditText(getContext());
        teamNum.setHint("Team Number");
        teamNum.setInputType(InputType.TYPE_CLASS_NUMBER);
        ll.addView(teamNum);

        EditText teamName = new EditText(getContext());
        teamName.setHint("Team Name");
        ll.addView(teamName);

        EditText school = new EditText(getContext());
        school.setHint("School");
        ll.addView(school);

        EditText city = new EditText(getContext());
        city.setHint("City");
        ll.addView(city);

        EditText stateOrProv = new EditText(getContext());
        stateOrProv.setHint("State Or Province");
        ll.addView(stateOrProv);

        EditText country = new EditText(getContext());
        country.setHint("Country");
        ll.addView(country);

        EditText startingYear = new EditText(getContext());
        startingYear.setHint("Starting Year");
        startingYear.setInputType(InputType.TYPE_CLASS_NUMBER);
        ll.addView(startingYear);

        builder.setView(ll);

        builder.setNeutralButton("Cancel", (dialogInterface, i) -> {});
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            if(teamNum.getText().toString().isEmpty() || teamName.getText().toString().isEmpty()) return;
            frcTeam team = new frcTeam();
            team.teamNumber = Integer.parseInt(teamNum.getText().toString());
            team.teamName = teamName.getText().toString();
            team.school = school.getText().toString();
            team.city = city.getText().toString();
            team.country = country.getText().toString();
            team.stateOrProv = stateOrProv.getText().toString();
            team.startingYear = safeToInt(startingYear.getText().toString());

            event.teams.add(team);
            fileEditor.setEvent(event);
            reloadTable();
        });

        builder.create().show();

    }
    public void removeTeam(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove team");

        CustomSpinnerView dropdown = new CustomSpinnerView(getContext());

        List<String> teamNums = new ArrayList<>();
        for(int i = 0 ;i < event.teams.size(); i++)
            teamNums.add(String.valueOf(event.teams.get(i).teamNumber));


        dropdown.setTitle("Teams");
        dropdown.setOptions(teamNums, -1);

        builder.setView(dropdown);

        builder.setNeutralButton("Cancel", (dialogInterface, i) -> {});
        builder.setPositiveButton("OK", (dialogInterface, i) -> {

            int index = dropdown.getIndex();
            System.out.println(index);
            if(!(index >= 0 && index < teamNums.size())) return;

            event.teams.remove(index);
            fileEditor.setEvent(event);
            reloadTable();
        });

        builder.create().show();

    }
    public void addMatch(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add match");

        List<String> teamNums = new ArrayList<>();
        for(int i = 0 ;i < event.teams.size(); i++)
            teamNums.add(String.valueOf(event.teams.get(i).teamNumber));

        ScrollView sv = new ScrollView(getContext());
//        sv.setLayoutDirection(ScrollView.SCROLL_AXIS_VERTICAL);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        CustomSpinnerView Red1dropdown = new CustomSpinnerView(getContext());
        Red1dropdown.setTitle("Red-1");
        Red1dropdown.setOptions(teamNums, -1);
        ll.addView(Red1dropdown);

        CustomSpinnerView Red2dropdown = new CustomSpinnerView(getContext());
        Red2dropdown.setTitle("Red-2");
        Red2dropdown.setOptions(teamNums, -1);
        ll.addView(Red2dropdown);

        CustomSpinnerView Red3dropdown = new CustomSpinnerView(getContext());
        Red3dropdown.setTitle("Red-3");
        Red3dropdown.setOptions(teamNums, -1);
        ll.addView(Red3dropdown);


        CustomSpinnerView Blue1dropdown = new CustomSpinnerView(getContext());
        Blue1dropdown.setTitle("Blue-1");
        Blue1dropdown.setOptions(teamNums, -1);
        ll.addView(Blue1dropdown);

        CustomSpinnerView Blue2dropdown = new CustomSpinnerView(getContext());
        Blue2dropdown.setTitle("Blue-2");
        Blue2dropdown.setOptions(teamNums, -1);
        ll.addView(Blue2dropdown);

        CustomSpinnerView Blue3dropdown = new CustomSpinnerView(getContext());
        Blue3dropdown.setTitle("Blue-3");
        Blue3dropdown.setOptions(teamNums, -1);
        ll.addView(Blue3dropdown);

        builder.setView(sv);

        builder.setNeutralButton("Cancel", (dialogInterface, i) -> {});
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            int red1index = Red1dropdown.getIndex();
            int red2index = Red2dropdown.getIndex();
            int red3index = Red3dropdown.getIndex();
            int blue1index = Blue1dropdown.getIndex();
            int blue2index = Blue2dropdown.getIndex();
            int blue3index = Blue3dropdown.getIndex();

            if(red1index == -1 || red2index == -1 || red3index == -1 || blue1index == -1 || blue2index == -1 || blue3index == -1) return;

            frcMatch match = new frcMatch();
            match.matchIndex = event.matches.size() + 1;
            match.redAlliance = new int[] {
                event.teams.get(red1index).teamNumber,
                event.teams.get(red2index).teamNumber,
                event.teams.get(red3index).teamNumber
            };
            match.blueAlliance = new int[] {
                event.teams.get(blue1index).teamNumber,
                event.teams.get(blue2index).teamNumber,
                event.teams.get(blue3index).teamNumber
            };

            event.matches.add(match);
            fileEditor.setEvent(event);
            reloadTable();
        });

        builder.create().show();




    }
    public void removeMatch(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove match");

        List<String> matches = new ArrayList<>();
        for(int i = 0 ;i < event.matches.size(); i++) {
            frcMatch match = event.matches.get(i);
            matches.add(match.matchIndex + " - " + Arrays.toString(match.redAlliance) + ", " + Arrays.toString(match.blueAlliance));
        }

        CustomSpinnerView dropdown = new CustomSpinnerView(getContext());
        dropdown.setTitle("Matches");
        dropdown.setOptions(matches, -1);

        builder.setView(dropdown);

        builder.setNeutralButton("Cancel", (dialogInterface, i) -> {});
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
                if(dropdown.getIndex() == -1) return;
                event.matches.remove(dropdown.getIndex());
                fileEditor.setEvent(event);
                reloadTable();
        });

        builder.create().show();

    }


    public int safeToInt(String str){
        try{
            return Integer.parseInt(str);
        }catch (Exception e){
            return 0;
        }
    }
}
