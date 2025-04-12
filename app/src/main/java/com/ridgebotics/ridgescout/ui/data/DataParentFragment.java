package com.ridgebotics.ridgescout.ui.data;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.navigation.fragment.FragmentKt.findNavController;
import static com.ridgebotics.ridgescout.utility.Colors.datafragment_option_1;
import static com.ridgebotics.ridgescout.utility.Colors.datafragment_option_2;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentDataParentBinding;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.FieldBorderedRow;
import com.ridgebotics.ridgescout.ui.TeamListOption;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataParentFragment extends Fragment {

    private FragmentDataParentBinding binding;

    private DataFragment dataFragment;

    private boolean editBoxEnabled = true;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataParentBinding.inflate(inflater, container, false);
        DataManager.reload_event();

        if (savedInstanceState == null && dataFragment == null){
            dataFragment = new DataFragment();

            //add child fragment
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.data_subfragment, dataFragment, "Data Subfragment")
                    .commit();
        }

        if(evcode.equals("unset") || event == null){
            binding.reportToggleButton.setVisibility(GONE);
            return binding.getRoot();
        }

        binding.reportToggleButton.setOnClickListener(view -> {
            editBoxEnabled  =! editBoxEnabled;
            binding.ScoutingEditBox.setVisibility(editBoxEnabled ? GONE : VISIBLE);
            binding.reportToggleButton.setText(editBoxEnabled ? "▲ report" : "▼ report");
        });

        generateScoutingTemplate(SettingsManager.getMatchNum());

        return binding.getRoot();
    }

    public void moveToFragment(Fragment newFragment){
        // consider using Java coding conventions (upper first char class names!!!)
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.data_subfragment, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }


    // Generate format for scouting data
    public void generateScoutingTemplate(int currentMatch){

        int teamNum = SettingsManager.getTeamNum();
        boolean isBlueAlliance = false;


        frcMatch nextMatch = event.getNextTeamMatch(teamNum, currentMatch);


        List<frcTeam> ourAlliance = new ArrayList<>();
        List<frcTeam> opposingAlliance = new ArrayList<>();

        for(int a = 0; a < nextMatch.blueAlliance.length; a++)
            if(nextMatch.blueAlliance[a] != teamNum){
                (!isBlueAlliance ? ourAlliance : opposingAlliance).add(event.getTeamByNum(nextMatch.blueAlliance[a]));
            }
        for(int a = 0; a < nextMatch.redAlliance.length; a++)
            if(nextMatch.redAlliance[a] != teamNum){
                (isBlueAlliance ? ourAlliance : opposingAlliance).add(event.getTeamByNum(nextMatch.redAlliance[a]));
            }


        String output = "Match: " + (nextMatch.matchIndex+1) + "\n";

        output += "## Our Alliance ##";
        output += getTeamNameAndNum(ourAlliance.get(0));
        output += getTeamNameAndNum(ourAlliance.get(1));
        output += "\n## Opposing Alliance ##";
        output += getTeamNameAndNum(opposingAlliance.get(0));
        output += getTeamNameAndNum(opposingAlliance.get(1));
        output += getTeamNameAndNum(opposingAlliance.get(2));

        binding.scoutingReportEdittext.setText(output);

    }

    private static String getTeamNameAndNum(frcTeam team){
        return "\n" + team.teamNumber + " " + team.teamName + ": \n";
    }
}