package com.ridgebotics.ridgescout.ui.scouting;

import static android.widget.LinearLayout.VERTICAL;
import static androidx.navigation.fragment.FragmentKt.findNavController;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.SettingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentScoutingBinding;
import com.ridgebotics.ridgescout.utility.DataManager;

import java.util.ArrayList;
import java.util.Set;

// Main dashbord page
public class ScoutingFragment extends Fragment {

    private FragmentScoutingBinding binding;
    private boolean is_main_page = true;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentScoutingBinding.inflate(inflater, container, false);

        binding.buttons.setVisibility(View.VISIBLE);

        DataManager.reload_event();

        if(SettingsManager.getCustomEvents()){
            binding.eventAddButton.setVisibility(View.VISIBLE);
            binding.eventAddButton.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Chose event name");

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(VERTICAL);
                EditText eventName = new EditText(getContext());
                eventName.setHint("Event Name");
                EditText eventCode = new EditText(getContext());
                eventCode.setHint("Event Code");
                layout.addView(eventName);
                layout.addView(eventCode);


                builder.setPositiveButton("Create", (dialog, which) -> {
                    String name = eventName.getText().toString();
                    String code = eventCode.getText().toString();
                    if(name.isEmpty() || code.isEmpty()) return;

                    frcEvent event = new frcEvent();
                    event.name = name;
                    event.eventCode = code;
                    event.teams = new ArrayList<>();
                    event.matches = new ArrayList<>();

                    FileEditor.setEvent(event);


                });
                builder.setNeutralButton("Cancel", (dialog, which) -> {});

                builder.setView(layout);
                builder.create().show();
            });
        }

        if(event == null){
            binding.noEventError.setVisibility(View.VISIBLE);

            binding.textMatchAlliance.setVisibility(View.GONE);
            binding.textName.setVisibility(View.GONE);
            binding.infoBox.setVisibility(View.GONE);
            binding.textRescoutIndicator.setVisibility(View.GONE);

            binding.matchScoutingButton.setEnabled(false);
            binding.pitScoutingButton.setEnabled(false);
            binding.eventButton.setEnabled(false);
            is_main_page = false;
            return binding.getRoot();
        }

        if(event.matches.isEmpty()){
            binding.matchScoutingButton.setEnabled(false);
        }

        if(event.teams.isEmpty()){
            binding.pitScoutingButton.setEnabled(false);
        }

        binding.matchScoutingButton.setOnClickListener(v -> {
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_match_scouting);
        });

        binding.pitScoutingButton.setOnClickListener(v -> {
            PitSelectorFragment.setOnSelect((self, team) -> {
                PitScoutingFragment.setTeam(team);
                findNavController(self).navigate(R.id.action_navigation_scouting_pit_selector_to_navigation_pit_scouting);
            });
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_scouting_pit_selector);
        });

        binding.eventButton.setOnClickListener(v -> {
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_scouting_event);
        });

        updateDashboard();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_UP
                    && keyCode == KeyEvent.KEYCODE_BACK
                    && !is_main_page){

                is_main_page = true;

                return true;
            }
            return false;
        });
    }

    private void updateDashboard() {
        binding.textName.setText("Welcome, " + SettingsManager.getUsername() + "!");

        int curMatchNum = SettingsManager.getMatchNum();
        int nextMatch;
        int teamNum = SettingsManager.getTeamNum();
        try {
            nextMatch = event.getNextTeamMatch(teamNum, curMatchNum).matchIndex;
        } catch (Exception e){
            AlertManager.error("Sorry, in event ("+evcode+"), your team number ("+teamNum+") wasn't found!", e);
            return;
        }

        binding.textMatchAlliance.setText("Match: " + (curMatchNum+1) + ", " + SettingsManager.getAllyPos());
        binding.textRescoutIndicator.setText("Things to rescout: " + DataManager.rescout_list.size());

        TextView nextMatchText = new TextView(getContext());
        nextMatchText.setText("Our next match: Match " + nextMatch);
        nextMatchText.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1);
        binding.infoBox.addView(nextMatchText);

        int informedBy = event.getMostInformedBy(teamNum, curMatchNum);

        TextView mostInformedText = new TextView(getContext());
        mostInformedText.setText("Most informed by: Match " + informedBy);
        mostInformedText.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1);
        binding.infoBox.addView(mostInformedText);
    }
}