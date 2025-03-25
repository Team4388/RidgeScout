package com.ridgebotics.ridgescout.ui.data;


import static android.view.View.VISIBLE;
import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.scoutingData.fields;
import com.ridgebotics.ridgescout.ui.settings.FieldsFragment;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentDataBinding;
import com.ridgebotics.ridgescout.ui.TeamSelectorFragment;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.types.frcEvent;

public class DataFragment extends Fragment {

    private FragmentDataBinding binding;

    private boolean submenu = false;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String evcode = settingsManager.getEVCode();

        if(evcode.equals("unset")){
            binding.noEventError.setVisibility(VISIBLE);

//            binding.teamsButton.setEnabled(false);


            return root;
        }

//        frcEvent event = frcEvent.decode(fileEditor.readFile(evcode + ".eventdata"));

        binding.teamsButton.setOnClickListener(v -> {
            TeamSelectorFragment.setPits_mode(false);
            TeamSelectorFragment.setOnSelect((self, team) -> {
                TeamsFragment.setTeam(team);
                findNavController(self).navigate(R.id.action_navigation_team_selector_to_navigation_data_teams);
            });
            findNavController(this).navigate(R.id.action_navigation_data_to_navigation_team_selector);
        });
        return root;
    }
}