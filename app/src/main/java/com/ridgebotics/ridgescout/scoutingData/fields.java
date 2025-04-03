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
import java.util.UUID;

public class fields {
//    public static ScoutingVersion sv = new ScoutingVersion();

    public static final String matchFieldsFilename = "matches.fields";
    public static final String pitsFieldsFilename = "pits.fields";

    private static String uuid(){
        return UUID.randomUUID().toString();
    }

    public static final inputType[][] default_match_fields = new inputType[][] {
        {
            new fieldposType(uuid(),"Auto start pos", "Where does the robot start its auto?", new int[]{0,0}),

            new tallyType(uuid(),"Auto L4 Coral", "How many coral did this robot score in L4 during auto?", 0),
            new tallyType(uuid(),"Auto L3 Coral", "How many coral did this robot score in L3 during auto?", 0),
            new tallyType(uuid(),"Auto L2 Coral", "How many coral did this robot score in L2 during auto?", 0),
            new tallyType(uuid(),"Auto L1/Trough Coral", "How many coral did this robot score in L1 during auto?", 0),
            new tallyType(uuid(),"Auto Processor Algae", "How many algae did this robot score in the Barge during auto?", 0),
            new tallyType(uuid(),"Auto Barge Algae", "How many algae did this robot score in the Barge during auto?", 0),

            new dropdownType(uuid(),"Auto Quality", "How did the robot drive during auto?", new String[]{"Smooth", "Jittery"}, 0),
            new textType(uuid(),"Auto Comments", "Anything interesting about auto", ""),

            new tallyType(uuid(),"Teleop L4 Coral", "How many coral did this robot score in L4 during auto?", 0),
            new tallyType(uuid(),"Teleop L3 Coral", "How many coral did this robot score in L3 during auto?", 0),
            new tallyType(uuid(),"Teleop L2 Coral", "How many coral did this robot score in L2 during auto?", 0),
            new tallyType(uuid(),"Teleop L1 Coral", "How many coral did this robot score in L1 during auto?", 0),
            new tallyType(uuid(),"Teleop Processor Algae", "How many algae did this robot score in the Barge during auto?", 0),
            new tallyType(uuid(),"Teleop Barge Algae", "How many algae did this robot score in the Barge during auto?", 0),

            new checkboxType(uuid(),"Upper Algae Removal", "Did the robot remove upper Algae?", 0),
            new checkboxType(uuid(),"Lower Algae Removal", "Did the robot remove lower Algae?", 0),

            new dropdownType(uuid(),"Teleop Quality", "How did the robot drive during Teleop?", new String[]{"Smooth", "Jittery"}, 0),
            new textType(uuid(),"Teleop Comments", "Anything interesting about Teleop", ""),

            new dropdownType(uuid(),"Climb State", "What was the final condition of the robot?", new String[]{"Nothing", "Continued Cycling", "Park", "Attempted Shallow", "Shallow", "Attempted Deep", "Deep"}, 0),

            new dropdownType(uuid(),"Robot Condition", "Was anything broken?", new String[]{"Everything was working", "Something was maybe broken", "Something was broken", "Robot was disabled for part of the match", "Missing robot"}, 0),

            new textType(uuid(),"Other Comments", "Any other comments you have", "")
        }
    };

    public static final inputType[][] default_pit_fields = new inputType[][] {
        {
            new dropdownType(uuid(),"Drivetrain type", "What type of drivetrain does this team have?", new String[]{"Swerve Drive", "Tank Drive (Differential)", "Other, Info in comments"}, 0),
            new dropdownType(uuid(),"Intake type", "What type of intake does this team have?", new String[]{"Ground only", "Player Station only", "Both", "Other, Info in comments"}, 0),
            new dropdownType(uuid(),"Intake Consistency", "How consistent is the robot at intakeing?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new dropdownType(uuid(),"Score Area", "What does this robot score?", new String[]{"Only Algae", "Mostly Algae", "Both", "Mostly Coral", "Only Coral"}, 0),

            new checkboxType(uuid(),"L4 Scoring", "Will the robot score in Layer 4?", 0),
            new checkboxType(uuid(),"L3 Scoring", "Will the robot score in Layer 3?", 0),
            new checkboxType(uuid(),"L2 Scoring", "Will the robot score in Layer 3?", 0),
            new checkboxType(uuid(),"L1/Trough Scoring", "Will the robot score in Layer 1?", 0),
            new checkboxType(uuid(),"Processor Scoring", "Will the robot score in the Processor?", 0),
            new checkboxType(uuid(),"Barge Scoring", "Will the robot score algae in the Barge?", 0),
            new dropdownType(uuid(),"Scoring Consistency", "How consistent is the robot at Scoring?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new textType(uuid(),"Auto Capability", "What autos does this team have?", ""),
            new dropdownType(uuid(),"Auto Consistency", "How consistent is the robot at Auto?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new dropdownType(uuid(),"Climb type", "What does the robot do to climb?", new String[]{"No Climb", "Only Shallow", "Only Deep", "Both Shallow and Deep"}, 0),
            new dropdownType(uuid(),"Climb Consistency", "How consistent is the robot at climbing?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new textType(uuid(),"Cool Comments", "Is there anything cool about the robot?", ""),

            new textType(uuid(),"Comments", "Things go here", "Day 1:\n\nDay 2:\n\nDay 3:\n")
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
}
