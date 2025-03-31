package com.ridgebotics.ridgescout.ui.data;


import static android.view.View.GONE;
import static android.view.View.TEXT_ALIGNMENT_VIEW_END;
import static android.view.View.VISIBLE;
import static androidx.navigation.fragment.FragmentKt.findNavController;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.scoutingData.fields;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.CustomSpinnerView;
import com.ridgebotics.ridgescout.ui.FieldBorderedRow;
import com.ridgebotics.ridgescout.ui.TeamListOption;
import com.ridgebotics.ridgescout.ui.settings.FieldsFragment;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentDataBinding;
import com.ridgebotics.ridgescout.ui.TeamSelectorFragment;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.types.frcEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFragment extends Fragment {

    private FragmentDataBinding binding;
    private int option = 0;



    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataBinding.inflate(inflater, container, false);
        binding.table.setStretchAllColumns(true);
        View root = binding.getRoot();

        if(evcode == null || evcode.equals("unset") || event == null){
            binding.noEventError.setVisibility(VISIBLE);
            binding.dataTypeDropdown.setVisibility(GONE);
            return root;
        }

        option = settingsManager.getDataMode();

        binding.dataTypeDropdown.setTitle("Data type");
        binding.dataTypeDropdown.setOptions(List.of(new String[]{
            "By Team", "By Data Field"
        }), option);
        binding.dataTypeDropdown.setOnClickListener((item, index) -> {
            option = index;
            settingsManager.setDataMode(option);
            reload_views();
        });

        reload_views();

        return root;
    }

    public void reload_views(){
        binding.table.removeViews(1, binding.table.getChildCount()-1);

        switch (option) {
            case 0:
                load_teams();
                break;
            case 1:
                load_fields();
                break;
        }

    }

    public void load_teams(){
        DataManager.reload_event();
        int[] teamNums = new int[event.teams.size()];

        for(int i = 0 ; i < event.teams.size(); i++){
            teamNums[i] = event.teams.get(i).teamNumber;
        }

        Arrays.sort(teamNums);

        for(int i = 0; i < event.teams.size(); i++){
            frcTeam team = null;
            for(int a = 0 ; a < event.teams.size(); a++){
                if(event.teams.get(a).teamNumber == teamNums[i]){
                    team = event.teams.get(a);
                    break;
                }
            }
            assert team != null;

            TeamListOption teamRow = new TeamListOption(getContext());
            binding.table.addView(teamRow);
            teamRow.fromTeam(team);

            frcTeam finalTeam = team;
            teamRow.setOnClickListener(v -> {
                TeamsFragment.setTeam(finalTeam);
                findNavController(this).navigate(R.id.action_navigation_data_to_navigation_data_teams);
            });
        }
    }
    public void load_fields(){
        DataManager.reload_match_fields();

        for(int i = 0; i < match_latest_values.length; i++){
            FieldBorderedRow tr = new FieldBorderedRow(getContext());
            tr.fromField(match_latest_values[i]);
            tr.setColor(i % 2 == 0 ? 0xff509050 : 0xff307030);
            binding.table.addView(tr);

            final int fi = i;
            tr.setOnClickListener(v -> {
                FieldDataFragment.setFieldIndex(fi);
                findNavController(this).navigate(R.id.action_navigation_data_to_navigation_data_field_data);
            });
        }
    }
}