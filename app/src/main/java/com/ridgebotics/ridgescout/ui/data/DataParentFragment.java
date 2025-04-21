package com.ridgebotics.ridgescout.ui.data;


import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.AutoSaveManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.SettingsManager;

import java.util.ArrayList;
import java.util.List;

// Holds the scouting data editor aswell as the sub-fragment for the scouting data browser
public class DataParentFragment extends Fragment {

    private FragmentDataParentBinding binding;
    private DataFragment dataFragment;


    private boolean editBoxEnabled = true;
    private int teamNum = SettingsManager.getTeamNum();
    private frcMatch[] ourMatches;
    private int matchIndex = 0;

    private AutoSaveManager asm;

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

        ourMatches = event.getTeamMatches(SettingsManager.getTeamNum());
        matchIndex = SettingsManager.getReportMatchIndex(evcode);

        if(ourMatches.length == 0){
            binding.reportToggleButton.setVisibility(GONE);
            return binding.getRoot();
        }

        binding.scoutUpButton.setOnClickListener(v -> {
            matchIndex++;
            updateButtons();
        });

        binding.scoutDownButton.setOnClickListener(v -> {
            matchIndex--;
            updateButtons();
        });

        updateButtons();

        binding.reportToggleButton.setOnClickListener(view -> {
            editBoxEnabled  =! editBoxEnabled;
            binding.ScoutingEditBox.setVisibility(editBoxEnabled ? GONE : VISIBLE);
            binding.reportToggleButton.setText(editBoxEnabled ? "▲ report" : "▼ report");
        });

        binding.reportCopyButton.setOnClickListener(v -> {
//            ClipData e = new ClipData();
            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(
                    "Scouting report",
                    binding.scoutingReportEdittext.getText().toString()
            );

            clipboardManager.setPrimaryClip(clipData);
            AlertManager.toast("Copied report to clipboard!");
        });

        asm = new AutoSaveManager(() -> SettingsManager.setScoutingReport(evcode, matchIndex, binding.scoutingReportEdittext.getText().toString()), 300);
        asm.start();

        binding.scoutingReportEdittext.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                asm.update();
            }
            @Override public void afterTextChanged(Editable editable) {}
        });

        return binding.getRoot();
    }

    private void updateButtons(){
        binding.matchNum.setText(String.valueOf(ourMatches[matchIndex].matchIndex));
        binding.scoutUpButton.setEnabled(matchIndex < ourMatches.length-1);
        binding.scoutDownButton.setEnabled(matchIndex > 0);
        SettingsManager.setReportIndex(matchIndex, evcode);

        String report = SettingsManager.getScoutingReport(evcode, matchIndex);
        if(report.isEmpty()) report = generateScoutingTemplate(ourMatches[matchIndex]);
        binding.scoutingReportEdittext.setText(report);
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
    private String generateScoutingTemplate(frcMatch nextMatch){
        boolean isBlueAlliance = event.getIsBlueAlliance(teamNum, nextMatch);



        List<frcTeam> ourAlliance = new ArrayList<>();
        List<frcTeam> opposingAlliance = new ArrayList<>();

        for(int a = 0; a < nextMatch.blueAlliance.length; a++)
            if(nextMatch.blueAlliance[a] != teamNum){
                (isBlueAlliance ? ourAlliance : opposingAlliance).add(event.getTeamByNum(nextMatch.blueAlliance[a]));
            }
        for(int a = 0; a < nextMatch.redAlliance.length; a++)
            if(nextMatch.redAlliance[a] != teamNum){
                (!isBlueAlliance ? ourAlliance : opposingAlliance).add(event.getTeamByNum(nextMatch.redAlliance[a]));
            }


        String output = "Match: " + (nextMatch.matchIndex) + "\n";

        output += "## Our Alliance ##";
        output += getTeamNameAndNum(ourAlliance.get(0));
        output += getTeamNameAndNum(ourAlliance.get(1));
        output += "\n## Opposing Alliance ##";
        output += getTeamNameAndNum(opposingAlliance.get(0));
        output += getTeamNameAndNum(opposingAlliance.get(1));
        output += getTeamNameAndNum(opposingAlliance.get(2));


        return output;

    }

    private static String getTeamNameAndNum(frcTeam team){
        return "\n" + team.teamNumber + " " + team.teamName + ": \n";
    }
}