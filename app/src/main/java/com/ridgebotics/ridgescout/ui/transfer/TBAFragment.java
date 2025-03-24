package com.ridgebotics.ridgescout.ui.transfer;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTransferTbaBinding;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.RequestTask;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.JSONUtil;
import com.ridgebotics.ridgescout.utility.settingsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class TBAFragment extends Fragment {
    private static final String TBAAddress = "https://www.thebluealliance.com/api/v3/";
    private static final String TBAHeader = "X-TBA-Auth-Key: tjEKSZojAU2pgbs2mBt06SKyOakVhLutj3NwuxLTxPKQPLih11aCIwRIVFXKzY4e";

    private android.widget.TableLayout Table;
    private FragmentTransferTbaBinding binding;

    private final int year = settingsManager.getYearNum();

    private ProgressDialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferTbaBinding.inflate(inflater, container, false);

        Table = binding.matchTable;

        Table.setStretchAllColumns(true);

        TableRow tr = new TableRow(getContext());
        addTableText(tr, "Loading Events...");
        Table.addView(tr);

        startLoading("Loading Events...");

        final RequestTask rq = new RequestTask();
        rq.onResult(s -> {
            if(s == null || s.isEmpty()) {
                AlertManager.addSimpleError("Could not fetch event!");
                AlertManager.updateErrors();
                stopLoading();
                return null;
            }
            eventTable(s);
            return null;
        });
        rq.execute(TBAAddress + "events/"+year, TBAHeader);

        return binding.getRoot();
    }

    private void addTableText(TableRow tr, String textStr){
        TextView text = new TextView(getContext());
        text.setTextSize(18);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text align center
        text.setText(textStr);
        tr.addView(text);
    }

    public void eventTable(String dataString){
        stopLoading();

        Table.removeAllViews();
        Table.setStretchAllColumns(true);
        Table.bringToFront();

        Date currentTime = Calendar.getInstance().getTime();

        try {
            JSONArray data = new JSONArray(dataString);

            Table.setStretchAllColumns(true);
            Table.bringToFront();

            boolean toggle = false;

            for(int i=0;i<data.length();i++){
                TableRow tr = new TableRow(getContext());
                TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(20,20,20,20);
                tr.setLayoutParams(rowParams);
                tr.setPadding(20,20,20,20);
                tr.setBackgroundColor(0x30000000);


                JSONObject j = data.getJSONObject(i);

                String matchKey = j.getString("key");
                String name = j.getString("short_name");

                // Sometimes, a short name is not present on TBA Events
                if(name.isEmpty()){
                    name = j.getString("name");
                }

                TextView tv = new TextView(getContext());
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setTextSize(12);
                tv.setText(j.getString("key"));
                tr.addView(tv);

                tv = new TextView(getContext());
                tv.setTextSize(18);
                tv.setText(name);
                tr.addView(tv);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date startDate = format.parse(j.getString("start_date"));
                    Date endDate = format.parse(j.getString("end_date"));
                    if(currentTime.after(endDate)){
                        tr.setBackgroundColor(0x30FF0000);
                    }else if(currentTime.before(startDate)){
                        tr.setBackgroundColor(0x3000FF00);
                    }else if(currentTime.after(startDate) && currentTime.before(endDate)){
                        tr.setBackgroundColor(0x30FFFF00);
                    }
                } catch (Exception e) {
                    AlertManager.error("Failed finding start and end dates!", e);
                    stopLoading();
                }


                tr.setOnClickListener(v -> {
                    startLoading("Loading Teams and Matches...");
                    Table.removeAllViews();
                    Table.setStretchAllColumns(true);
                    Table.bringToFront();

                    TableRow tr1 = new TableRow(getContext());
                    addTableText(tr1, "Downloading Teams...");
                    Table.addView(tr1);

                    final RequestTask rq = new RequestTask();
                    rq.onResult(teamsStr -> {
                        stopLoading();
                        TableRow tr11 = new TableRow(getContext());
                        addTableText(tr11, "Downloading Matches...");
                        Table.addView(tr11);

                        final RequestTask rq1 = new RequestTask();
                        rq1.onResult(matchesStr -> {
                            matchTable(matchesStr, teamsStr, j);
                            return null;
                        });
                        rq1.execute((TBAAddress + "event/" + matchKey + "/matches"), TBAHeader);
                        return null;
                    });
                    rq.execute((TBAAddress + "event/" + matchKey + "/teams"), TBAHeader);
                });

//                tr.addView(cl);
                Table.addView(tr, rowParams);

                toggle = !toggle;
            }
        }catch (JSONException j){
            AlertManager.error("Failed Downloading", j);
            stopLoading();
        }
    }

    static class matchComparator implements Comparator<JSONObject>
    {

        public int compare(JSONObject a, JSONObject b)
        {
            try {
                return a.getInt("match_number") - b.getInt("match_number");
            }catch (JSONException j){
                return 0;
            }
        }
    }



    public void matchTable(String matchesString, String teamsString, JSONObject eventData){
        Table.removeAllViews();
        Table.setStretchAllColumns(true);
        Table.bringToFront();

        try {
            final JSONArray matchData = new JSONArray(matchesString);
//            final JSONArray matchData = new JSONArray();
            final JSONArray teamData = new JSONArray(teamsString);

            String matchKey = eventData.getString("key");
            String matchName = eventData.getString("short_name");

            // Sometimes, a short name is not present on TBA Events
            if(matchName.isEmpty()){
                matchName = eventData.getString("name");
            }

            // Event code at top
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setText(matchKey);
            tv.setTextSize(18);
            Table.addView(tv);

            // Event Name
            tv = new TextView(getContext());
            tv.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(matchName);
            tv.setTextSize(28);
            Table.addView(tv);



            // Save button
            Button btn = new Button(getContext());
            btn.setText("Save");
            btn.setTextSize(18);
            btn.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            Table.addView(btn);




            if(teamData.length() == 0){
                tv = new TextView(getContext());
                tv.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText("This event has no teams released yet...");
                tv.setTextSize(18);
                Table.addView(tv);

                tv = new TextView(getContext());
                tv.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText("This event has no teams released yet...");
                tv.setTextSize(18);
                Table.addView(tv);

                btn.setVisibility(View.GONE);
                return;
            }else if(matchData.length() == 0){
                tv = new TextView(getContext());
                tv.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText("This event has no matches released yet...");
                tv.setTextSize(18);
                Table.addView(tv);

                tv = new TextView(getContext());
                tv.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setText("Try manually adding practice matches.");
                tv.setTextSize(18);
                Table.addView(tv);
            }



            tv = new TextView(getContext());
            tv.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("Teams");
            tv.setTextSize(28);
            Table.addView(tv);






            int[] teams = new int[teamData.length()];

            for(int i = 0 ; i < teamData.length(); i++){
                teams[i] = teamData.getJSONObject(i).getInt("team_number");
            }

            Arrays.sort(teams);

            TableRow tr = null;
            for(int i=0; i < teamData.length(); i++){
//            frcTeam team = event.teams.get(i);
                int num = teams[i];

                if(i % 7 == 0){
                    if(i != 0)
                        Table.addView(tr);
                    tr = new TableRow(getContext());
                }

                TextView text = new TextView(getContext());
                text.setTextSize(18);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                text.setText(String.valueOf(num));
//                if(fileEditor.fileExist(event.eventCode + "-" + num + ".pitscoutdata")){
//                    text.setBackgroundColor(0x3000FF00);
//                }else{
//                    text.setBackgroundColor(0x30FF0000);
//                }
                tr.addView(text);
            }
            if(tr != null)
                Table.addView(tr);

            final ArrayList<frcMatch> matchesOBJ = new ArrayList<>();

            btn.setOnClickListener(v -> {
                if(saveData(matchesOBJ, teamData, eventData)){
                    AlertManager.toast("Saved!");
                }else{
                    AlertManager.addSimpleError("Error saving files.");
                    stopLoading();
                }
            });



            tv = new TextView(getContext());
            tv.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("Matches");
            tv.setTextSize(28);
            Table.addView(tv);




            tr = new TableRow(getContext());
            addTableText(tr, "#");
            addTableText(tr, "Red-1");
            addTableText(tr, "Red-2");
            addTableText(tr, "Red-3");
            addTableText(tr, "Blue-1");
            addTableText(tr, "Blue-2");
            addTableText(tr, "Blue-3");
            Table.addView(tr);


            if(matchData.length() == 0)
                return;


            final JSONArray sortedMatchData = JSONUtil.sort(matchData, (a, b) -> {
                JSONObject    ja = (JSONObject)a;
                JSONObject    jb = (JSONObject)b;
                try {
                    return ja.getInt("match_number") - jb.getInt("match_number");
                }catch (JSONException j){
                    return 0;
                }
            });


            boolean toggle = false;
            int matchCount = 1;

            for(int a=0;a<sortedMatchData.length();a++){
                final JSONObject match = sortedMatchData.getJSONObject(a);

                if(!match.getString("comp_level").equals("qm")){
                    continue;
                }

                final JSONObject alliances = match.getJSONObject("alliances");
                final JSONArray redAlliance = alliances.getJSONObject("red").getJSONArray("team_keys");
                final JSONArray blueAlliance = alliances.getJSONObject("blue").getJSONArray("team_keys");

                tr = new TableRow(getContext());

                if (toggle) {
                    tr.setBackgroundColor(0x30000000);
                }

                addTableText(tr, String.valueOf(matchCount));
//                addTableText(tr, match.getString("key"));

                int[] blueKeys = new int[3];
                int[] redKeys = new int[3];

                for(int b=0;b<6;b++){
                    TextView text = new TextView(getContext());
                    text.setTextSize(18);
                    text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text align center
                    tr.addView(text);

                    if(b < 3){
                        String str = redAlliance.getString(b).substring(3);
                        redKeys[b] = Integer.parseInt(str);
                        text.setText(str);
                        text.setBackgroundColor(0x50ff0000);
                    }else{
                        String str = blueAlliance.getString(b-3).substring(3);
                        blueKeys[b-3] = Integer.parseInt(str);
                        text.setText(str);
                        text.setBackgroundColor(0x500000ff);
                    }
                }

                Table.addView(tr);

                frcMatch matchOBJ = new frcMatch();
                matchOBJ.matchIndex = matchCount;
                matchOBJ.blueAlliance = blueKeys;
                matchOBJ.redAlliance = redKeys;
                matchesOBJ.add(matchOBJ);

                matchCount += 1;
                toggle = !toggle;
            }

//            btn.setOnClickListener(v -> {
//                if(saveData(matchesOBJ, teamData, eventData)){
//                    alert("Info", "Saved!");
//                }else{
//                    alert("Error", "Error saving files.");
//                }
//            });

        }catch (JSONException j){
            AlertManager.error("Failed Downloading", j);
            stopLoading();
        }
    }

    private boolean saveData(ArrayList<frcMatch> matchData, JSONArray teamData, JSONObject eventData){
        try {
            final String matchKey = eventData.getString("key");
            String matchName = eventData.getString("short_name");

            // Sometimes, a short name is not present on TBA Events
            if(matchName.isEmpty()){
                matchName = eventData.getString("name");
            }


            ArrayList<frcTeam> teams = new ArrayList<>();
            for(int i=0;i<teamData.length();i++){
                frcTeam teamObj = new frcTeam();
                JSONObject team = teamData.getJSONObject(i);

                teamObj.teamNumber = team.getInt("team_number");
                teamObj.teamName = team.getString("nickname");
                teamObj.city = team.getString("city");
                teamObj.stateOrProv = team.getString("state_prov");
                teamObj.school = team.getString("school_name");
                teamObj.country = team.getString("country");
                teamObj.startingYear = team.getInt("rookie_year");

                teams.add(teamObj);
            }

            frcEvent event = new frcEvent();
            event.name = matchName;
            event.eventCode = matchKey;
            event.teams = teams;
            event.matches = matchData;

            return fileEditor.setEvent(event);
        }catch (JSONException j){
            AlertManager.error(j);
            stopLoading();
            return false;
        }
    }

    private void startLoading(String title){
        loadingDialog = ProgressDialog.show(getActivity(), title, "Please wait...");
    }

    private void stopLoading(){
        if(loadingDialog != null)
            loadingDialog.cancel();
        loadingDialog = null;
    }
}
