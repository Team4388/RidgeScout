package com.ridgebotics.ridgescout.types;

import androidx.annotation.NonNull;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;

public class frcTeam {
    public static final int typecode = 252;
    public int teamNumber = 0;
    public String teamName = "null";
    public String city = "null";
    public String stateOrProv = "null";
    public String school = "null";
    public String country = "null";
    public int startingYear = 0;

    public String getDescription(){
        return teamName + " Started in " + startingYear + ", and are from " + school + " in " + city + ", " + stateOrProv + ", " + country;
    }

    public byte[] encode(){
        try {
            return new ByteBuilder()
            .addInt(teamNumber)
            .addString(teamName)
            .addString(city)
            .addString(stateOrProv)
            .addString(school)
            .addString(country)
            .addInt(startingYear)
            .build();
        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return null;
        }
    }
    public static frcTeam decode(byte[] bytes){
        try {
            ArrayList<BuiltByteParser.parsedObject> objects = new BuiltByteParser(bytes).parse();

            frcTeam frc = new frcTeam();

            frc.teamNumber   = (int)    objects.get(0).get();
            frc.teamName     = (String) objects.get(1).get();
            frc.city         = (String) objects.get(2).get();
            frc.stateOrProv  = (String) objects.get(3).get();
            frc.school       = (String) objects.get(4).get();
            frc.country      = (String) objects.get(5).get();
            frc.startingYear = (int)    objects.get(6).get();

            return frc;

        } catch (BuiltByteParser.byteParsingExeption e) {
            AlertManager.error(e);
            return null;
        }
    }

    @NonNull
    public String toString(){
        return "frcTeam Num: " + teamNumber + ", " + getDescription();
    }
}