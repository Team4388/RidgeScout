package com.ridgebotics.ridgescout.ui.data;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.match_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.match_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_transferValues;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_values;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentDataTeamsBinding;
import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.google.android.material.divider.MaterialDivider;
import com.skydoves.powerspinner.IconSpinnerAdapter;
import com.skydoves.powerspinner.IconSpinnerItem;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.skydoves.powerspinner.SpinnerGravity;

import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment {
    FragmentDataTeamsBinding binding;

    private static frcTeam team;
    public static void setTeam(frcTeam tmpteam){
        team = tmpteam;
    }

    private static final int background_color = 0x5000ff00;
    private static final int unsaved_background_color = 0x2000ff00;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDataTeamsBinding.inflate(inflater, container, false);

        binding.teamsArea.removeAllViews();

        DataManager.reload_match_fields();
        DataManager.reload_pit_fields();

        TableLayout table = new TableLayout(getContext());
        table.setStretchAllColumns(true);
        binding.teamsArea.addView(table);

        loadTeam(settingsManager.getDataMode());

        return binding.getRoot();
    }

    public void loadTeam(int mode) {
        binding.teamsArea.removeAllViews();

        LinearLayout ll = new LinearLayout(getContext());
        ll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        ll.setOrientation(LinearLayout.VERTICAL);
        binding.teamsArea.addView(ll);



        PowerSpinnerView dropdown = new PowerSpinnerView(getContext());

        List<IconSpinnerItem> iconSpinnerItems = new ArrayList<>();

        iconSpinnerItems.add(new IconSpinnerItem("Individual"));
        iconSpinnerItems.add(new IconSpinnerItem("Compiled"));
        iconSpinnerItems.add(new IconSpinnerItem("History"));

        IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(dropdown);
        dropdown.setSpinnerAdapter(iconSpinnerAdapter);
        dropdown.setItems(iconSpinnerItems);

        dropdown.selectItemByIndex(0);

        dropdown.setPadding(10,20,10,20);
        dropdown.setBackgroundColor(0xf0000000);
        dropdown.setTextColor(0xff00ff00);
        dropdown.setTextSize(15);
        dropdown.setArrowGravity(SpinnerGravity.END);
        dropdown.setArrowPadding(8);
//        dropdown.setSpinnerItemHeight(46);
        dropdown.setSpinnerPopupElevation(14);


        dropdown.selectItemByIndex(mode);


        dropdown.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<IconSpinnerItem>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable IconSpinnerItem oldItem, int newIndex,
                                       IconSpinnerItem newItem) {

                settingsManager.setDataMode(newIndex);
                loadTeam(newIndex);
            }
        });

        ll.addView(dropdown);







        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(String.valueOf(team.teamNumber));
        tv.setTextSize(28);
        ll.addView(tv);

        tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(team.teamName);
        tv.setTextSize(28);
        ll.addView(tv);

        tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(team.getDescription());
        tv.setTextSize(16);
        ll.addView(tv);

        add_pit_data(ll, team);
        add_match_data(ll, team, mode);
    }

    public void add_pit_data(LinearLayout ll, frcTeam team){
        final String filename = evcode+"-"+team.teamNumber+".pitscoutdata";

        ll.addView(new MaterialDivider(getContext()));

        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setPadding(0,10,0,10);
        tv.setText("----- Pit data -----");
        tv.setTextSize(30);
        ll.addView(tv);

        ll.addView(new MaterialDivider(getContext()));

        if(!fileEditor.fileExist(filename)){
            tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("No pit data has been collected!");
            tv.setTextSize(23);
            ll.addView(tv);
            return;
        }

        ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(filename, pit_values, pit_transferValues);

        tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setPadding(0, 20, 0, 5);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText("Pit scouting by " + psda.username);
        tv.setTextSize(30);
        ll.addView(tv);

        for (int a = 0; a < psda.data.array.length; a++) {
            tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(psda.data.array[a].getName());
            tv.setTextSize(25);

            if(psda.data.array[a].isNull()){
                tv.setBackgroundColor(0xffff0000);
                tv.setTextColor(0xff000000);
            }


            ll.addView(tv);


            pit_latest_values[a].add_individual_view(ll, psda.data.array[a]);
        }
    }




    public void add_match_data(LinearLayout ll, frcTeam team, int mode){
        String[] files = fileEditor.getMatchesByTeamNum(evcode, team.teamNumber);

        ll.addView(new MaterialDivider(getContext()));

        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText("----- Match data -----");
        tv.setPadding(0,10,0,10);
        tv.setTextSize(30);
        ll.addView(tv);

        ll.addView(new MaterialDivider(getContext()));



        if(files.length == 0){
            tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("No match data has been collected!");
            tv.setTextSize(23);
            ll.addView(tv);
            return;
        }

        switch (mode){
            case 0:
                add_individual_views(ll,files);
                break;
            case 1:
                add_compiled_views(ll,files);
                break;
            case 2:
                add_history_views(ll,files);
                break;
        }
    }




    public void add_individual_views(LinearLayout ll, String[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                String[] split = files[i].split("-");
                int match_num = Integer.parseInt(split[1]);

                ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[i], match_values, match_transferValues);

                TextView tv = new TextView(getContext());
                tv.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setPadding(0, 40, 0, 5);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText("M" + (match_num) + " " + split[2] + "-" + split[3] + " by " + psda.username);
                tv.setTextSize(30);
                ll.addView(tv);

                for (int a = 0; a < psda.data.array.length; a++) {
                    tv = new TextView(getContext());
                    tv.setLayoutParams(new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    tv.setText(psda.data.array[a].getName());
                    tv.setTextSize(25);

                    if (psda.data.array[a].isNull()) {
                        tv.setBackgroundColor(0xffff0000);
                        tv.setTextColor(0xff000000);
                    }

                    ll.addView(tv);


                    match_latest_values[a].add_individual_view(ll, psda.data.array[a]);
                }
            }catch (Exception e){
                e.printStackTrace();
                AlertManager.alert("Warning!", "Failure to load file " + files[i]);
            }
        }
    }






    public void add_compiled_views(LinearLayout ll, String[] files){
        dataType[][] data = new dataType[match_latest_values.length][files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[i], match_values, match_transferValues);
                for (int a = 0; a < data.length; a++) {
                    data[a][i] = psda.data.array[a];
                }
            } catch (Exception e){
                e.printStackTrace();
                AlertManager.alert("Warning!", "Failure to load file " + files[i]);
            }
        }

        for(int i = 0; i < match_latest_values.length; i++){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(0, 20, 0, 5);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(match_latest_values[i].name);
            tv.setTextSize(30);
            ll.addView(tv);

            match_latest_values[i].add_compiled_view(ll, data[i]);
        }
    }





    public void add_history_views(LinearLayout ll, String[] files){
        dataType[][] data = new dataType[match_latest_values.length][files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                ScoutingDataWriter.ParsedScoutingDataResult psda = ScoutingDataWriter.load(files[i], match_values, match_transferValues);
                for (int a = 0; a < data.length; a++) {
                    data[a][i] = psda.data.array[a];
                }
            }catch (Exception e){
                e.printStackTrace();
                AlertManager.alert("Warning!", "Failure to load file " + files[i]);
            }
        }

        for(int i = 0; i < match_latest_values.length; i++){
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(0, 20, 0, 5);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(match_latest_values[i].name);
            tv.setTextSize(30);
            ll.addView(tv);

            match_latest_values[i].add_history_view(ll, data[i]);
        }
    }
}
