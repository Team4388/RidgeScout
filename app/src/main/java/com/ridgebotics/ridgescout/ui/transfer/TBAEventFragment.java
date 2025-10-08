package com.ridgebotics.ridgescout.ui.transfer;

import static androidx.navigation.fragment.FragmentKt.findNavController;
import static com.ridgebotics.ridgescout.utility.Colors.tba_blue;
import static com.ridgebotics.ridgescout.utility.Colors.tba_red;
import static com.ridgebotics.ridgescout.utility.Colors.tba_toggle_background;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAAddress;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAHeader;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import com.google.android.material.button.MaterialButton;
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
import com.ridgebotics.ridgescout.utility.builders.TextViewBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

// Class to download data from a specific event and encode it.
public class TBAEventFragment extends Fragment {

    private TableLayout Table;
    private FragmentTransferTbaBinding binding;

    private final int year = SettingsManager.getYearNum();

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

        AlertManager.startLoading("Loading Teams and Matches...");

//        Table.removeAllViews();
        Table.setStretchAllColumns(true);
        Table.bringToFront();

        final RequestTask rq = new RequestTask();
        rq.onResult(teamsStr -> {
            final RequestTask rq1 = new RequestTask();
            rq1.onResult(matchesStr -> {
                matchTable(matchesStr, teamsStr, eventData);
                AlertManager.stopLoading();
                return null;
            });
            rq1.execute((TBAAddress + "event/" + matchKey + "/matches"), TBAHeader);
            return null;
        });
        rq.execute((TBAAddress + "event/" + matchKey + "/teams"), TBAHeader);

        return binding.getRoot();
    }

    private void addTableText(TableRow tr, String textStr){
        tr.addView(new TextViewBuilder(getContext(), textStr)
                .size(18)
//                .align_center()
                .build());
    }

    public void matchTable(String matchesString, String teamsString, JSONObject eventData){
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
            Table.addView(new TextViewBuilder(getContext(), matchKey)
                    .align_center()
                    .size(18)
                    .build());

            // Event Name
            Table.addView(new TextViewBuilder(getContext(), matchName)
                    .align_center()
                    .size(28)
                    .build());

            // Save button
            MaterialButton btn = new MaterialButton(getContext());
            btn.setText("Save");
            btn.setTextSize(18);
            btn.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            Table.addView(btn);




            // If there are no matches, add the error.
            // If there are no teams, don't allow the user to save the event and set the button to be invisible
            if(teamData.length() == 0){
                Table.addView(new TextViewBuilder(getContext(), "This event has no teams released yet...")
                        .align_center()
                        .size(18)
                        .build());

                btn.setVisibility(View.GONE);
                return;
            }else if(matchData.length() == 0){
                Table.addView(new TextViewBuilder(getContext(), "This event has no matches released yet...")
                        .align_center()
                        .size(18)
                        .build());

                Table.addView(new TextViewBuilder(getContext(), "Try manually adding practice matches.")
                        .align_center()
                        .size(18)
                        .build());
            }



            Table.addView(
                new TextViewBuilder(getContext(), "Teams")
                    .align_center()
                    .size(28)
                    .build()
            );





            // Sort the teams into numerical order
            int[] teams = new int[teamData.length()];

            for(int i = 0 ; i < teamData.length(); i++){
                teams[i] = teamData.getJSONObject(i).getInt("team_number");
            }

            Arrays.sort(teams);


            // Loop through each match
            TableRow tr = null;
            for(int i=0; i < teamData.length(); i++){
                int num = teams[i];

                // If this is every 7th row, add the new row.
                if(i % 7 == 0){
                    if(i != 0)
                        Table.addView(tr);
                    tr = new TableRow(getContext());
                }


                tr.addView(
                    new TextViewBuilder(getContext(), String.valueOf(num))
                        .align_center()
                        .size(18)
                        .build()
                );
            }
            if(tr != null)
                Table.addView(tr);

            final ArrayList<frcMatch> matchesOBJ = new ArrayList<>();

            btn.setOnClickListener(v -> {
                saveData(matchesOBJ, teamData, eventData);
            });



            Table.addView(
                new TextViewBuilder(getContext(), "Matches")
                    .align_center()
                    .size(28)
                    .build()
            );
            
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
                    tr.setBackgroundColor(tba_toggle_background);
                }

                addTableText(tr, String.valueOf(matchCount));
//                addTableText(tr, match.getString("key"));

                int[] blueKeys = new int[3];
                int[] redKeys = new int[3];

                for(int b=0;b<6;b++){
                    TextViewBuilder text = new TextViewBuilder(getContext())
                            .size(18)
                            .align_center();

                    if(b < 3){
                        String str = redAlliance.getString(b).substring(3);
                        redKeys[b] = Integer.parseInt(str);
                        text.text(str);
                        text.tv.setBackgroundColor(tba_red);
                    }else{
                        String str = blueAlliance.getString(b-3).substring(3);
                        blueKeys[b-3] = Integer.parseInt(str);
                        text.text(str);
                        text.tv.setBackgroundColor(tba_blue);
                    }


                    tr.addView(text.build());
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

        }catch (JSONException j){
            AlertManager.error("Failed Downloading", j);
            AlertManager.stopLoading();
        }
    }

    private boolean saveData(ArrayList<frcMatch> matchData, JSONArray teamData, JSONObject eventData){
        AlertManager.startLoading("Downloading team data...");

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


                    RequestTask rq = new RequestTask();
                    rq.onResult(s -> {
                        try {
                            JSONArray jsonArray = new JSONArray(s);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String base64 = jsonObject.getJSONObject("details").getString("base64Image");

                            byte[] decodedData = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedData, 0, decodedData.length);

//                            System.out.println(base64);

                            teamObj.bitmap = bitmap;
                            teamObj.teamColor = frcTeam.findPrimaryColor(bitmap);

                            Log.i("TBA", "Got icon for team " + teamObj.teamNumber);


                        } catch (Exception e){
                            Log.i("TBA", "Failed to icon for team " + teamObj.teamNumber);
                        } finally {
                            teams.add(teamObj);
                        }
                        return null;
                    });
                    rq.execute((TBAAddress + "team/frc" + teamObj.teamNumber + "/media/" + year), TBAHeader);

//                    ImageRequestTask imageRequestTask = new ImageRequestTask();
//
//                    imageRequestTask.onResult(bitmap -> {
//                        teamObj.bitmap = bitmap;
//                        teamObj.teamColor = frcTeam.findPrimaryColor(bitmap);
//                        teams.add(teamObj);
//
//                        return null;
//                    });
//                    imageRequestTask.execute("https://www.thebluealliance.com/avatar/" + year + "/frc" + teamObj.teamNumber + ".png");
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
                AlertManager.stopLoading();

            }catch(Exception j) {
                AlertManager.error(j);
                AlertManager.stopLoading();
            }
        });
        t.start();

        return false;
    }
}
