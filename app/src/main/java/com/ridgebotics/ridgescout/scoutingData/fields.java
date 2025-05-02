package com.ridgebotics.ridgescout.scoutingData;

import com.ridgebotics.ridgescout.types.input.checkboxType;
import com.ridgebotics.ridgescout.types.input.dropdownType;
import com.ridgebotics.ridgescout.types.input.fieldposType;
import com.ridgebotics.ridgescout.types.input.inputType;
import com.ridgebotics.ridgescout.types.input.numberType;
import com.ridgebotics.ridgescout.types.input.tallyType;
import com.ridgebotics.ridgescout.types.input.textType;
import com.ridgebotics.ridgescout.types.input.sliderType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;

public class fields {
//    public static ScoutingVersion sv = new ScoutingVersion();

    public static final String matchFieldsFilename = "matches.fields";
    public static final String pitsFieldsFilename = "pits.fields";

    public static final inputType[][] default_match_fields = new inputType[][] {
        {
            new fieldposType("Auto start pos", new int[]{0,0}),
            new tallyType("Auto Notes", 0),
            new sliderType("Auto Performance", 5, 0, 10),
            new textType("Auto Comments", ""),
            new tallyType("Teleop Notes", 0),
            new sliderType("Teleop Performance", 5, 0, 10),
            new textType("Teleop Comments", ""),
            new sliderType("Overall Driving Performance", 5, 0, 10),
            new textType("Overall Driving Comments", ""),
            new sliderType("Score area (AMP <-> Speaker)", 5, 0, 10),
            new dropdownType("End Condition", new String[]{"Nothing", "Attempted Climb", "Successful Climbed", "Climbed with multiple robots", "Climbed with trap"}, 0),
            new dropdownType("Robot Condition", new String[]{"Everything was working", "Something was maybe broken", "Something was broken", "Robot was disabled for part of the match", "Missing robot (Joe Johnson)"}, 0),
            new textType("Other Comments", "")
        }
    };

    public static final inputType[][] default_pit_fields = new inputType[][] {
        {
            new sliderType("How good is robot", 5, 0, 10),
            new sliderType("Test", 1, 0, 10),
            new textType("notes", ""),
        }
    };


    public static boolean save(String filename, inputType[][] values){
        try {
            ByteBuilder bb = new ByteBuilder();
            for (int i = 0; i < values.length; i++) {
                bb.addRaw(127, save_version(values[i]));
            }
            fileEditor.writeFile(filename, bb.build());
            return true;
        }catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return false;
//            throw new RuntimeException(e);
        }
    }

    private static byte[] save_version(inputType[] values) throws ByteBuilder.buildingException {
        ByteBuilder bb = new ByteBuilder();
        for(int i =0; i < values.length; i++){
            bb.addRaw(values[i].get_byte_id(), values[i].encode());
        }
        return bb.build();
    }

    public static inputType[][] load(String filename){
        byte[] bytes = fileEditor.readFile(filename);

//        System.out.println(bytes);

        try {
            BuiltByteParser bbp = new BuiltByteParser(bytes);
            ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();
            inputType[][] values = new inputType[objects.size()][];

            for(int i = 0 ; i < objects.size(); i++){
                values[i] = load_version((byte[]) objects.get(i).get());
            }


            return values;
        } catch (Exception e) {
            AlertManager.error(e);
            return null;
        }
    }

    private static inputType[] load_version(byte[] bytes) throws BuiltByteParser.byteParsingExeption{
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();
        inputType[] output = new inputType[objects.size()];

        for(int i = 0 ; i < objects.size(); i++){
            BuiltByteParser.parsedObject obj = objects.get(i);
            inputType t = null;
            switch (obj.getType()){
                case inputType.slider_type_id:
                    t = new sliderType();
                    break;
                case inputType.dropdownType:
                    t = new dropdownType();
                    break;
                case inputType.notesType:
                    t = new textType();
                    break;
                case inputType.tallyType:
                    t = new tallyType();
                    break;
                case inputType.numberType:
                    t = new numberType();
                    break;
                case inputType.checkboxType:
                    t = new checkboxType();
                    break;
                case inputType.fieldposType:
                    t = new fieldposType();
                    break;
            }

            t.decode((byte[]) obj.get());
            output[i] = t;
        }

        return output;
    }

//    public static void test(){
//        ScoutingVersion.transferType[][] transferValues = sv.get_transfer_values(values);
//
//        ScoutingVersion.ScoutingArray msa = sv.new ScoutingArray(0, new ScoutingVersion.dataType[]{
//                sv.new stringType("name", "test-username"),
//                sv.new intType("How good is robot", 12)
//        }, values, transferValues);
//
//        msa.update();
//
//        for(ScoutingVersion.dataType dt : msa.array){
//            if(dt == null) continue;
//            switch (dt.getValueType()){
//                case NUM:
//                    System.out.println(dt.name + " " + (int) dt.get());
//                    break;
//                case STRING:
//                    System.out.println(dt.name + " " + (String) dt.get());
//                    break;
//            }
//
//        }
//    }
}
