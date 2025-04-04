package com.ridgebotics.ridgescout.ui.transfer;

import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.event;
import static com.ridgebotics.ridgescout.utility.DataManager.match_latest_values;
import static com.ridgebotics.ridgescout.utility.DataManager.pit_latest_values;
import static com.ridgebotics.ridgescout.utility.SharePrompt.shareContent;

import android.content.Context;

import com.ridgebotics.ridgescout.scoutingData.ScoutingDataWriter;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.frcMatch;
import com.ridgebotics.ridgescout.types.frcTeam;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

public class CSVExport {
    private static String[] alliances = {"red", "blue"};

    private static String safeCSV(String input){
        return input.replace("\n", "").replace(",", ".").replace(";", ".");
    }

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
            if(!FileEditor.fileExist(filename)){
                data += ("null,".repeat(match_latest_values.length));
            }else{
                try {
                    String tempData = "";
                    ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, DataManager.match_values, DataManager.match_transferValues);
                    DataType[] matchData = psdr.data.array;
                    FieldType[] types = psdr.data.values[psdr.data.values.length-1];
                    for (int i = 0; i < types.length; i++) {
                        tempData += (safeCSV(types[i].toString(matchData[i])) + ",");
                    }
                    data += tempData;
                } catch (Exception e){
                    e.printStackTrace();
                    data += ("null,".repeat(match_latest_values.length));
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

        data += ("teamnum,teamname,city,stateOrProv,school,country,startingYear,");
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
            if(!FileEditor.fileExist(filename)){
                data += ("null,".repeat(pit_latest_values.length));
            }else{
                try {
                    String tempData = "";
                    ScoutingDataWriter.ParsedScoutingDataResult psdr = ScoutingDataWriter.load(filename, DataManager.pit_values, DataManager.pit_transferValues);
                    DataType[] teamData = psdr.data.array;
                    FieldType[] types = psdr.data.values[psdr.data.values.length-1];
                    for (int i = 0; i < types.length; i++) {
                        tempData += (safeCSV(types[i].toString(teamData[i])) + ",");
                    }
                    data += tempData;
                } catch (Exception e){
                    e.printStackTrace();
                    data += ("null,".repeat(pit_latest_values.length));
                }

            }

            data += ("\n");
        }

//        System.out.print(data);

        shareContent(c, evcode+"-pits.csv", data, "text/plain");
    }
}
