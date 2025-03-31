package com.ridgebotics.ridgescout.utility;

import com.ridgebotics.ridgescout.scoutingData.fields;
import com.ridgebotics.ridgescout.scoutingData.transfer.transferType;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.input.inputType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManager {
    public static String evcode;
    public static frcEvent event;
    public static void reload_event(){
        if(event != null) return;
        evcode = getevcode();

        if(evcode.equals("unset")) return;

        event = frcEvent.decode(fileEditor.readFile(evcode + ".eventdata"));

        if(event == null) {
            AlertManager.addSimpleError("Failed to load event!");
            settingsManager.setEVCode("unset");
            evcode = "unset";
        }else{
            AlertManager.toast("Reloaded event!");
            reload_rescout_list();
        }
    }

    public static String getevcode() {
        return settingsManager.getEVCode();
    }

    public static inputType[][] match_values;
    public static inputType[] match_latest_values;
    public static transferType[][] match_transferValues;
    public static void reload_match_fields(){
        try {
            match_values = fields.load(fields.matchFieldsFilename);
            match_latest_values = match_values[match_values.length - 1];
            match_transferValues = transferType.get_transfer_values(match_values);
        } catch (Exception e){
            AlertManager.error("Error reading match fields", e);
        }
    }

    public static inputType[][] pit_values;
    public static inputType[] pit_latest_values;
    public static transferType[][] pit_transferValues;
    public static void reload_pit_fields(){
        try {
            pit_values = fields.load(fields.pitsFieldsFilename);
            pit_latest_values = pit_values[pit_values.length-1];
            pit_transferValues = transferType.get_transfer_values(pit_values);
        } catch (Exception e){
            AlertManager.error("Error reading pit fields", e);
        }
    }

    public static List<String> rescout_list = new ArrayList<>();
    public static void reload_rescout_list(){
        if(!fileEditor.fileExist(evcode + ".rescout")) {rescout_list = new ArrayList<>(); return;}
        byte[] file = fileEditor.readFile(evcode + ".rescout");
        if(file == null) {rescout_list =  new ArrayList<>(); return;}

        try {
            BuiltByteParser bbp = new BuiltByteParser(file);
            rescout_list = new ArrayList<>(Arrays.asList((String[]) (bbp.parse().get(0).get())));

        } catch (Exception e){
            AlertManager.error("Error loading scout fields", e);
            rescout_list =  new ArrayList<>();
        }
    }

    public static void save_rescout_list() {
        try {
            if(rescout_list.size() == 0){
                fileEditor.deleteFile(evcode + ".rescout");
                return;
            }

            ByteBuilder bb = new ByteBuilder();
            bb.addStringArray(rescout_list.toArray(new String[0]));
            fileEditor.writeFile(evcode + ".rescout", bb.build());
        } catch (Exception e){
            AlertManager.error("Error saving scout fields", e);
        }
    }
}
