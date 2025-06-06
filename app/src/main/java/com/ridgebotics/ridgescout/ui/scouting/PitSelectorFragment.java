package com.ridgebotics.ridgescout.ui.scouting;

import static com.ridgebotics.ridgescout.utility.Colors.color_found;
import static com.ridgebotics.ridgescout.utility.Colors.color_not_found;
import static com.ridgebotics.ridgescout.utility.Colors.color_rescout;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTeamSelectorBinding;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.ui.views.TeamListOption;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.Arrays;

// Fragment for choosing which team to pit scout
public class PitSelectorFragment extends Fragment {
    private FragmentTeamSelectorBinding binding;

    private static onTeamSelected onSelect = new onTeamSelected() {@Override public void onSelect(PitSelectorFragment self, frcTeam team) {}};

    public interface onTeamSelected {
        void onSelect(PitSelectorFragment self, frcTeam team);
    }
    public static void setOnSelect(onTeamSelected tmponSelect){
        onSelect = tmponSelect;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamSelectorBinding.inflate(inflater, container, false);

//        event = fileEditor.g
        DataManager.reload_event();

        if(evcode == null || evcode.equals("unset")){
            AlertManager.addSimpleError("You somehow have not loaded an event!");
            return binding.getRoot();
        }

        load_teams();


        return binding.getRoot();
    }

    public void load_teams(){
//        binding.pitFileIndicator.setVisibility(View.GONE);
//        binding.pitTeamName.setVisibility(View.GONE);
//        binding.pitTeamDescription.setVisibility(View.GONE);
//
//        clear_fields();


        int[] teamNums = new int[event.teams.size()];

        for(int i = 0 ; i < event.teams.size(); i++){
            teamNums[i] = event.teams.get(i).teamNumber;
        }

        Arrays.sort(teamNums);

        TableLayout table = new TableLayout(getContext());
        table.setStretchAllColumns(true);
        binding.teams.addView(table);


        for(int i = 0; i < event.teams.size(); i++){
            frcTeam team = null;
            for(int a = 0 ; a < event.teams.size(); a++){
                if(event.teams.get(a).teamNumber == teamNums[i]){
                    team = event.teams.get(a);
                    break;
                }
            }
            assert team != null;

//            TableRow tr = new TableRow(getContext());
//            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
//                    FrameLayout.LayoutParams.WRAP_CONTENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            rowParams.setMargins(20,20,20,20);
//            tr.setLayoutParams(rowParams);
//            tr.setPadding(20,20,20,20);
//            table.addView(tr);

            TeamListOption teamRow = new TeamListOption(getContext());
            table.addView(teamRow);
            teamRow.fromTeam(team);


            String filename = evcode + "-" + team.teamNumber + ".pitscoutdata";

            if (FileEditor.fileExist(filename)) {
                final boolean[] rescout = {DataManager.rescout_list.contains(filename)};

                teamRow.setColor(DataManager.rescout_list.contains(filename) ? color_rescout : color_found);

                teamRow.setOnLongClickListener(v -> {
                    rescout[0] = !rescout[0];
                    if(rescout[0]){
                        DataManager.rescout_list.add(filename);
                        teamRow.setColor(color_rescout);
                        DataManager.save_rescout_list();
                    }else{
                        DataManager.rescout_list.remove(filename);
                        teamRow.setColor(color_found);
                        DataManager.save_rescout_list();
                    }


                    return true;
                });
            } else {
                teamRow.setColor(color_not_found);
                teamRow.setOnLongClickListener(v -> true);
            }


            frcTeam finalTeam = team;
            teamRow.setOnClickListener(v -> onSelect.onSelect(this, finalTeam));
        }
    }

}
