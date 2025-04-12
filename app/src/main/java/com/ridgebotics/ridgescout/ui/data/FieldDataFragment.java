package com.ridgebotics.ridgescout.ui.data;


import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.match_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.match_values;
import static com.ridgebotics.ridgescout.utility.DataManager.rescout_list;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentDataFieldDataBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldDataFragment extends Fragment {

    private FragmentDataFieldDataBinding binding;

    private static int fieldIndex = -1;
    public static void setFieldIndex(int index){
        fieldIndex = index;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataFieldDataBinding.inflate(inflater, container, false);
        binding.table.setStretchAllColumns(true);
        View root = binding.getRoot();

        if (fieldIndex == -1)
            return root;

        startLoading("Loading data...");

        Thread t = new Thread(() -> {

            Map<Integer, List<DataType>> data = new HashMap<>();
            for (int teamIndex = 0; teamIndex < event.teams.size(); teamIndex++) {
                int teamNum = event.teams.get(teamIndex).teamNumber;
                List<String> filenames = new ArrayList<>(List.of(FileEditor.getMatchesByTeamNum(evcode, event.teams.get(teamIndex).teamNumber)));
                filenames.removeAll(rescout_list);

                ArrayList<DataType> teamData = new ArrayList<>();

                for (int i = 0; i < filenames.size(); i++) {
                    try {
                        System.out.println("Loading: " + filenames.get(i));
                        ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(filenames.get(i), match_values, match_transferValues);
                        if (psda.data.array[fieldIndex] != null && psda.data.array[fieldIndex].get() != null && !psda.data.array[fieldIndex].isNull())
                            teamData.add(psda.data.array[fieldIndex]);
                    } catch (Exception e) {
                        AlertManager.error("Failure to load file " + filenames.get(i), e);
                    }
                }

                data.put(teamNum, teamData);
            }

            System.out.println("Finished!");



            getActivity().runOnUiThread(() -> {
                binding.table.setStretchAllColumns(false);
                match_latest_values[fieldIndex].addDataToTable(binding.table, data);
                stopLoading();
            });
        });

        t.start();

        return root;
    }


    private ProgressDialog loadingDialog;

    private void startLoading(String title){
        getActivity().runOnUiThread(() -> {
            if(loadingDialog != null && loadingDialog.isShowing())
                loadingDialog.dismiss();
            loadingDialog = ProgressDialog.show(getActivity(), title, "Please wait...");
        });
    }

    private void stopLoading(){
        getActivity().runOnUiThread(() -> {
            if (loadingDialog != null)
                loadingDialog.cancel();
            loadingDialog = null;
        });
    }

}