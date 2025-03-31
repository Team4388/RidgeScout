package com.ridgebotics.ridgescout.ui.data;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.navigation.fragment.FragmentKt.findNavController;
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

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentDataBinding;
import com.ridgebotics.ridgescout.databinding.FragmentDataFieldDataBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.FieldBorderedRow;
import com.ridgebotics.ridgescout.ui.TeamListOption;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.settingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

            List<dataType>[] data = new ArrayList[event.teams.size()];
            for (int teamIndex = 0; teamIndex < event.teams.size(); teamIndex++) {

                List<String> filenames = new ArrayList<>(List.of(fileEditor.getMatchesByTeamNum(evcode, event.teams.get(teamIndex).teamNumber)));
                filenames.removeAll(rescout_list);

                for (int i = 0; i < filenames.size(); i++) {
                    data[teamIndex] = new ArrayList<>();
                    try {
                        ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(filenames.get(i), match_values, match_transferValues);
                        if (psda.data.array[fieldIndex] != null && psda.data.array[fieldIndex].get() != null)
                            data[teamIndex].add(psda.data.array[fieldIndex]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertManager.addSimpleError("Failure to load file " + filenames.get(i));
                    }
                }
            }

            System.out.println("Finished!");



            getActivity().runOnUiThread(() -> {
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