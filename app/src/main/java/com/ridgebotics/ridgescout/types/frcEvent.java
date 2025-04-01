package com.ridgebotics.ridgescout.types;

import static com.ridgebotics.ridgescout.utility.DataManager.event;

import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.settingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class frcEvent {

    public static final int typecode = 254;
    public String eventCode;
    public String name;
    public ArrayList<frcMatch> matches;
    public ArrayList<frcTeam> teams;

    public byte[] encode() {
        try {
            ByteBuilder bb = new ByteBuilder()
                .addString(eventCode)
                .addString(name);

            for (frcTeam teams : teams) {
                bb.addRaw(frcTeam.typecode, teams.encode());
            }

            for (frcMatch match : matches) {
                bb.addRaw(frcMatch.typecode, match.encode());
            }

            if(settingsManager.getEVCode().equals("unset")){
                settingsManager.setEVCode(eventCode);
            }

            return bb.build();
        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return null;
        }
    }

    public static frcEvent decode(byte[] bytes) {
        try {
            ArrayList<BuiltByteParser.parsedObject> objects =
                new BuiltByteParser(bytes).parse();

            frcEvent frc = new frcEvent();

            frc.eventCode = (String) objects.get(0).get();
            frc.name = (String) objects.get(1).get();

            frc.matches = new ArrayList<>();
            frc.teams = new ArrayList<>();

            for (BuiltByteParser.parsedObject object : objects) {
                if (object.getType() == frcTeam.typecode) {
                    frc.teams.add(frcTeam.decode((byte[]) object.get()));
                } else if (object.getType() == frcMatch.typecode) {
                    frc.matches.add(frcMatch.decode((byte[]) object.get()));
                }
            }

            return frc;
        } catch (BuiltByteParser.byteParsingExeption e) {
            AlertManager.error(e);
            return null;
        }
    }

    @NonNull
    public String toString() {
        return (
            "frcEvent Name: " +
            name +
            ", Code: " +
            eventCode +
            " numTeams: " +
            teams.size() +
            " numMatches: " +
            matches.size()
        );
    }

    // A func that will return every match a team is in.
    public frcMatch[] getTeamMatches(int teamNum){
        List<frcMatch> teamMatches = new ArrayList<>();
        for(int i = 0; i < matches.size(); i++){
            frcMatch match = matches.get(i);
            boolean isTeamMatch = false;
            isTeamMatch = IntStream.of(match.redAlliance).anyMatch(x -> x == teamNum);
            isTeamMatch = isTeamMatch || IntStream.of(match.blueAlliance).anyMatch(x -> x == teamNum);
            if(isTeamMatch)
                teamMatches.add(match);
        }
        return teamMatches.toArray(new frcMatch[0]);
    }

    // A func that will return the most recent match a team is in. (Not up until the current match)
    public int getMostRecentTeamMatch(int teamNum, int curMatch){
        frcMatch[] teamMatches = getTeamMatches(teamNum);
        int maxMatch = - 1;

        for(int i = 0; i < teamMatches.length; i++) {
            if (teamMatches[i].matchIndex < curMatch &&
                    teamMatches[i].matchIndex > maxMatch) {
                maxMatch = teamMatches[i].matchIndex;
            }

        }

        if(maxMatch == -1)
            return curMatch;
        else
            return maxMatch;
    }

//    public

    // Returns the soonest match that there will be all the possible upcoming data on other teams
    public void getReportMatches(int ourTeamNum){
        frcMatch[] teamMatches = event.getTeamMatches(ourTeamNum);

        for(int i = 0; i < teamMatches.length; i++){
            int maxMatch = -1;
            for(int a = 0; a < 6; a++){
                int teamNum;
                if(a < 3)
                    teamNum = teamMatches[i].redAlliance[a];
                else
                    teamNum = teamMatches[i].blueAlliance[a-3];

                if(teamNum == ourTeamNum)
                    continue;

                int matchNum = event.getMostRecentTeamMatch(teamNum, teamMatches[i].matchIndex);
                if(maxMatch < matchNum)
                    maxMatch = matchNum;
            }
        }
    }
}
