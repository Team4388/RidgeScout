package com.astatin3.scoutingapp2025.ui.data;


import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.astatin3.scoutingapp2025.SettingsVersionStack.latestSettings;
import com.astatin3.scoutingapp2025.databinding.FragmentDataBinding;
import com.astatin3.scoutingapp2025.fileEditor;
import com.astatin3.scoutingapp2025.types.frcEvent;
import com.astatin3.scoutingapp2025.types.frcTeam;

public class dataFragment extends Fragment {

    private FragmentDataBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String evcode = latestSettings.settings.get_evcode();

        if(evcode.equals("unset")){
            binding.noEventError.setVisibility(View.VISIBLE);
            binding.buttons.setVisibility(View.GONE);
            binding.matchTable.setVisibility(View.GONE);
            return root;
        }

        frcEvent event = frcEvent.decode(fileEditor.readFile(evcode + ".eventdata"));

//        for(frcTeam team : event.teams){
//            System.out.println(team.getDescription());
//        }

        binding.overviewButton.setOnClickListener(v -> {
            binding.buttons.setVisibility(View.GONE);
            binding.overviewView.setVisibility(View.VISIBLE);
            binding.overviewView.start(binding, event);
        });

        return root;
    }
}