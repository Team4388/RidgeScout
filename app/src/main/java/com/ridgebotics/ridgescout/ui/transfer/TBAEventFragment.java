package com.ridgebotics.ridgescout.ui.transfer;

import static androidx.navigation.fragment.FragmentKt.findNavController;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAAddress;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAHeader;

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

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentTransferTbaBinding;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.ImageRequestTask;
import com.ridgebotics.ridgescout.utility.JSONUtil;
import com.ridgebotics.ridgescout.utility.RequestTask;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.SettingsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class TBAEventFragment extends Fragment {

    private TableLayout Table;
    private FragmentTransferTbaBinding binding;

    private final int year = SettingsManager.getYearNum();

    private ProgressDialog loadingDialog;

    private static JSONObject eventData = null;
    public static void setEventData(JSONObject j){
        eventData = j;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferTbaBinding.inflate(inflater, container, false);

        final String matchKey;
        try {
            matchKey = eventData.getString("key");
        } catch (JSONException e) {
            AlertManager.error("Failed loading event key!", e);
            return binding.getRoot();
        }

        Table = binding.matchTable;

        Table.setStretchAllColumns(true);

        startLoading("Loading Teams and Matches...");
        Table.removeAllViews();
        Table.setStretchAllColumns(true);
        Table.bringToFront();

        TableRow tr1 = new TableRow(getContext());
        addTableText(tr1, "Downloading Teams...");
        Table.addView(tr1);

        final RequestTask rq = new RequestTask();
        rq.onResult(teamsStr -> {
            TableRow tr11 = new TableRow(getContext());
            addTableText(tr11, "Downloading Matches...");
            Table.addView(tr11);

            final RequestTask rq1 = new RequestTask();
            rq1.onResult(matchesStr -> {
                matchTable(matchesStr, teamsStr, eventData);
                stopLoading();
                return null;
            });
            rq1.execute((TBAAddress + "event/" + matchKey + "/matches"), TBAHeader);
            return null;
        });
        rq.execute((TBAAddress + "event/" + matchKey + "/teams"), TBAHeader);

        return binding.getRoot();
    }

    private void addTableText(TableRow tr, String textStr){
        TextView text = new TextView(getContext());
        text.setTextSize(18);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text align center
        text.setText(textStr);
        tr.addView(text);
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
                saveData(matchesOBJ, teamData, eventData);
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
        startLoading("Saving data...");

        Thread t = new Thread(() -> {
            try {
                final String matchKey = eventData.getString("key");
                String matchName = eventData.getString("short_name");

                // Sometimes, a short name is not present on TBA Events
                if (matchName.isEmpty()) {
                    matchName = eventData.getString("name");
                }

                ArrayList<frcTeam> teams = new ArrayList<>();
                for (int i = 0; i < teamData.length(); i++) {
                    frcTeam teamObj = new frcTeam();
                    JSONObject team = teamData.getJSONObject(i);

                    teamObj.teamNumber = team.getInt("team_number");
                    teamObj.teamName = team.getString("nickname");
                    teamObj.city = team.getString("city");
                    teamObj.stateOrProv = team.getString("state_prov");
                    teamObj.school = team.getString("school_name");
                    teamObj.country = team.getString("country");
                    teamObj.startingYear = team.getInt("rookie_year");

                    ImageRequestTask imageRequestTask = new ImageRequestTask();

                    imageRequestTask.onResult(bitmap -> {
                        teamObj.bitmap = bitmap;
                        teamObj.teamColor = frcTeam.findPrimaryColor(bitmap);
                        teams.add(teamObj);

                        return null;
                    });
                    imageRequestTask.execute("https://www.thebluealliance.com/avatar/" + year + "/frc" + teamObj.teamNumber + ".png");
                }

                while (teams.size() != teamData.length()) {
                    Thread.sleep(100);
                }

                frcEvent event = new frcEvent();
                event.name = matchName;
                event.eventCode = matchKey;
                event.teams = teams;
                event.matches = matchData;

                FileEditor.setEvent(event);
                AlertManager.toast("Saved!");

                getActivity().runOnUiThread(() -> findNavController(this).navigate(R.id.action_navigation_tba_event_to_navigation_transfer));
                stopLoading();

            }catch(Exception j) {
                AlertManager.error(j);
                stopLoading();
            }
        });
        t.start();

        return false;
    }

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
