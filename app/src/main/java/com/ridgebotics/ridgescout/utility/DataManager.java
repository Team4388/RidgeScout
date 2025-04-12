package com.ridgebotics.ridgescout.utility;

import com.ridgebotics.ridgescout.scoutingData.Fields;
import com.ridgebotics.ridgescout.scoutingData.transfer.TransferType;
import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.input.FieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManager {
    public static String evcode;
    public static frcEvent event;
    public static void reload_event(){
//        if(event != null) return;
        evcode = getevcode();

        if(evcode.equals("unset")) return;

        event = frcEvent.decode(FileEditor.readFile(evcode + ".eventdata"));

        if(event == null) {
            AlertManager.addSimpleError("Failed to load event!");
            SettingsManager.setEVCode("unset");
            evcode = "unset";
        }else{
            reload_rescout_list();
            reload_scout_notice();
            AlertManager.toast("Reloaded event!");
        }
    }

    public static String getevcode() {
        return SettingsManager.getEVCode();
    }

    public static FieldType[][] match_values;
    public static FieldType[] match_latest_values;
    public static TransferType[][] match_transferValues;
    public static void reload_match_fields(){
        try {
            match_values = Fields.load(Fields.matchFieldsFilename);
            match_latest_values = match_values[match_values.length - 1];
            match_transferValues = TransferType.get_transfer_values(match_values);
        } catch (Exception e){
            AlertManager.error("Error reading match fields", e);
        }
    }

    public static FieldType[][] pit_values;
    public static FieldType[] pit_latest_values;
    public static TransferType[][] pit_transferValues;
    public static void reload_pit_fields(){
        try {
            pit_values = Fields.load(Fields.pitsFieldsFilename);
            pit_latest_values = pit_values[pit_values.length-1];
            pit_transferValues = TransferType.get_transfer_values(pit_values);
        } catch (Exception e){
            AlertManager.error("Error reading pit fields", e);
        }
    }

    public static List<String> rescout_list = new ArrayList<>();
    public static void reload_rescout_list(){
        if(!FileEditor.fileExist(evcode + ".rescout")) {rescout_list = new ArrayList<>(); return;}
        byte[] file = FileEditor.readFile(evcode + ".rescout");
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
                FileEditor.deleteFile(evcode + ".rescout");
                return;
            }

            ByteBuilder bb = new ByteBuilder();
            bb.addStringArray(rescout_list.toArray(new String[0]));
            FileEditor.writeFile(evcode + ".rescout", bb.build());
        } catch (Exception e){
            AlertManager.error("Error saving scout fields", e);
        }
    }

    public static String scoutNotice = "";

    public static void reload_scout_notice(){
        if(!FileEditor.fileExist(evcode + ".scoutnotice")) {scoutNotice = ""; return;}
        byte[] file = FileEditor.readFile(evcode + ".scoutnotice");
        if(file == null) {scoutNotice = ""; return;}

        try {
            BuiltByteParser bbp = new BuiltByteParser(file);
            scoutNotice = (String) (bbp.parse().get(0).get());

        } catch (Exception e){
            AlertManager.error("Error loading scout notice", e);
            rescout_list =  new ArrayList<>();
        }
    }

    public static void save_scout_notice() {
        try {
            if(scoutNotice.isEmpty()){
                FileEditor.deleteFile(evcode + ".scoutnotice");
                return;
            }

            ByteBuilder bb = new ByteBuilder();
            bb.addString(scoutNotice);
            FileEditor.writeFile(evcode + ".scoutnotice", bb.build());
        } catch (Exception e){
            AlertManager.error("Error saving scout notice", e);
        }
    }
}
