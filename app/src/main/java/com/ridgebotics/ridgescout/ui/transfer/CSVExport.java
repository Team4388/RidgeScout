package com.ridgebotics.ridgescout.ui.transfer;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.SharePrompt.shareContent;

import android.content.Context;

import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.fileEditor;

public class CSVExport {
    private static String[] alliances = {"red", "blue"};


    public static void exportMatches(Context c){
        DataManager.reload_event();
        DataManager.reload_match_fields();

        String data = "";

        data += ("num,alliance,alliance_position,teamnum,");
        for(int i = 0; i < match_latest_values.length; i++){
            data += (match_latest_values[i].name + ",");
        }
        data += ("\n");


        for(int matchNum = 1; matchNum <= event.matches.size(); matchNum++){
        for(int allianceIndex = 0; allianceIndex <= 1; allianceIndex++){
            String alliance = alliances[allianceIndex];
        for(int alliancePos = 1; alliancePos <= 3; alliancePos++){
            data += (matchNum + ",");
            data += (alliance + ",");
            data += (alliancePos + ",");

            frcMatch match = event.matches.get(matchNum-1);
            int teamNum = 0;

            if(allianceIndex == 0){
                teamNum = match.redAlliance[alliancePos-1];
            }else{
                teamNum = match.blueAlliance[alliancePos-1];
            }

            data += (teamNum + ",");

            String filename = evcode+"-"+matchNum+"-"+alliance+"-"+alliancePos+"-"+teamNum+".matchscoutdata";
            if(!fileEditor.fileExist(filename)){
                data += ("null,".repeat(match_latest_values.length));
            }else{
                ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, DataManager.match_values, DataManager.match_transferValues);
                dataType[] types = psdr.data.array;
                for(int i = 0; i < types.length; i++) {
                    data += (types[i].get() + ",");
                }

            }

            data += ("\n");
        }}}

        shareContent(c, evcode+"-matches.csv", data, "text/plain");
    }



    public static void exportPits(Context c){
        DataManager.reload_event();
        DataManager.reload_pit_fields();

        String data = "";

        data += ("teamnum,teamname,city,teamnum,stateOrProv,school,country,startingYear,");
        for(int i = 0; i < pit_latest_values.length; i++){
            data += (pit_latest_values[i].name + ",");
        }
        data += ("\n");


        for(int teamIndex = 0; teamIndex < event.teams.size(); teamIndex++){
            frcTeam team = event.teams.get(teamIndex);

            data += (team.teamNumber + ",");
            data += (team.teamName + ",");
            data += (team.city + ",");
            data += (team.stateOrProv + ",");
            data += (team.school + ",");
            data += (team.country + ",");
            data += (team.startingYear + ",");

            String filename = evcode+"-"+team.teamNumber+".pitscoutdata";
            if(!fileEditor.fileExist(filename)){
                data += ("null,".repeat(pit_latest_values.length));
            }else{
                ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, DataManager.pit_values, DataManager.pit_transferValues);
                dataType[] types = psdr.data.array;
                for(int i = 0; i < types.length; i++) {
                    data += (types[i].get() + ",");
                }

            }

            data += ("\n");
        }

//        System.out.print(data);

        shareContent(c, evcode+"-pits.csv", data, "text/plain");
    }
}
