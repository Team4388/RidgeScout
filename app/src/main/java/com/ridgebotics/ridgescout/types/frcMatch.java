package com.ridgebotics.ridgescout.types;

import androidx.annotation.NonNull;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.Arrays;

// Class to contain data for the six teams numbers in a single match
// Easily encoded and decoded to binary format.
public class frcMatch {
    public static final int typecode = 253;
    public frcMatch(){}
    public int matchIndex = 0;
    public int[] blueAlliance = new int[3];
    public int[] redAlliance = new int[3];

    public byte[] encode(){
        try {
            return new ByteBuilder()
                    .addInt(matchIndex)
                    .addInt(blueAlliance[0])
                    .addInt(blueAlliance[1])
                    .addInt(blueAlliance[2])
                    .addInt(redAlliance[0])
                    .addInt(redAlliance[1])
                    .addInt(redAlliance[2])
                    .build();
        } catch (Exception e) {
            AlertManager.error(e);
            return new byte[1];
        }
    }
    public static frcMatch decode(byte[] bytes){
        try {
            ArrayList<BuiltByteParser.parsedObject> objects = new BuiltByteParser(bytes).parse();

            frcMatch frc = new frcMatch();

            frc.matchIndex      = (int) objects.get(0).get();
            frc.blueAlliance[0] = (int) objects.get(1).get();
            frc.blueAlliance[1] = (int) objects.get(2).get();
            frc.blueAlliance[2] = (int) objects.get(3).get();
            frc.redAlliance[0]  = (int) objects.get(4).get();
            frc.redAlliance[1]  = (int) objects.get(5).get();
            frc.redAlliance[2]  = (int) objects.get(6).get();


            return frc;

        } catch (Exception e) {
            AlertManager.error(e);
            return null;
        }
    }

    @NonNull
    public String toString(){
        return "frcMatch Num: " + matchIndex + ", Blue: " + Arrays.toString(blueAlliance) + ", Red: " + Arrays.toString(redAlliance);
    }

    public String getTeamAlliance(int teamNum){
        if(redAlliance[0] == teamNum) return "red-1";
        if(redAlliance[1] == teamNum) return "red-2";
        if(redAlliance[2] == teamNum) return "red-3";
        if(blueAlliance[0] == teamNum) return "blue-1";
        if(blueAlliance[1] == teamNum) return "blue-2";
        if(blueAlliance[2] == teamNum) return "blue-3";
        return "";
    }

}