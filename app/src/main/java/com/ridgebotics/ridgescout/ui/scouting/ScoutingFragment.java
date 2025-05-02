package com.ridgebotics.ridgescout.ui.scouting;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentScoutingBinding;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.TeamSelectorFragment;
import com.ridgebotics.ridgescout.utility.DataManager;

public class ScoutingFragment extends Fragment {

    private FragmentScoutingBinding binding;
    private boolean is_main_page = true;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentScoutingBinding.inflate(inflater, container, false);

        binding.buttons.setVisibility(View.VISIBLE);

        String evcode = settingsManager.getEVCode();

        DataManager.reload_event();

        if(event == null){
            binding.noEventError.setVisibility(View.VISIBLE);
            binding.matchScoutingButton.setEnabled(false);
            binding.pitScoutingButton.setEnabled(false);
            binding.statusButton.setEnabled(false);
            is_main_page = false;
            return binding.getRoot();
        }

        binding.matchScoutingButton.setOnClickListener(v -> {
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_match_scouting);
        });

        binding.pitScoutingButton.setOnClickListener(v -> {
            TeamSelectorFragment.setPits_mode(true);
            TeamSelectorFragment.setOnSelect(new TeamSelectorFragment.onTeamSelected() {
                @Override
                public void onSelect(TeamSelectorFragment self, frcTeam team) {
                    PitScoutingFragment.setTeam(team);
                    findNavController(self).navigate(R.id.action_navigation_team_selector_to_navigation_pit_scouting);
                }
            });
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_team_selector);
        });

        binding.statusButton.setOnClickListener(v -> {
            findNavController(this).navigate(R.id.action_navigation_scouting_to_navigation_scouting_status);
        });

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
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK
                        && !is_main_page){

//                    binding.buttons.setVisibility(View.VISIBLE);
//                    binding.matchScoutingView.setVisibility(View.GONE);
//                    binding.pitScoutingView.setVisibility(View.GONE);
                    is_main_page = true;

                    return true;
                }
                return false;
            }
        });
    }

}