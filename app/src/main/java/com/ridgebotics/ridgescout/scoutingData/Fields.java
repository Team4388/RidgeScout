package com.ridgebotics.ridgescout.scoutingData;

import com.ridgebotics.ridgescout.types.input.CheckboxType;
import com.ridgebotics.ridgescout.types.input.DropdownType;
import com.ridgebotics.ridgescout.types.input.FieldposType;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.types.input.NumberType;
import com.ridgebotics.ridgescout.types.input.TallyType;
import com.ridgebotics.ridgescout.types.input.TextType;
import com.ridgebotics.ridgescout.types.input.SliderType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.UUID;


// The mechanism to load, save, and create the fields based off of the raw types from ScoutingDataWriter.java
public class Fields {
//    public static ScoutingVersion sv = new ScoutingVersion();

    public static final String matchFieldsFilename = "matches.fields";
    public static final String pitsFieldsFilename = "pits.fields";

    private static String uuid(){
        return UUID.randomUUID().toString();
    }

    public static final FieldType[][] default_match_fields = new FieldType[][] {
        {
            new FieldposType(uuid(),"Auto start pos", "Where does the robot start its auto?", new int[]{0,0})
        }
    };

    public static final FieldType[][] default_pit_fields = new FieldType[][] {
        {
            new DropdownType(uuid(),"Drivetrain type", "What type of drivetrain does this team have?", new String[]{"Swerve Drive", "Tank Drive (Differential)", "Other, Info in comments"}, 0),
            new DropdownType(uuid(),"Intake type", "What type of intake does this team have?", new String[]{"Ground only", "Player Station only", "Both", "Other, Info in comments"}, 0),
            new DropdownType(uuid(),"Intake Consistency", "How consistent is the robot at intakeing?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new DropdownType(uuid(),"Scoring Consistency", "How consistent is the robot at Scoring?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new TextType(uuid(),"Auto Capability", "What autos does this team have?", ""),
            new DropdownType(uuid(),"Auto Consistency", "How consistent is the robot at Auto?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new DropdownType(uuid(),"Climb type", "What does the robot do to climb?", new String[]{"No Climb", "Only Shallow", "Only Deep", "Both Shallow and Deep"}, 0),
            new DropdownType(uuid(),"Climb Consistency", "How consistent is the robot at climbing?", new String[]{"Does not work", "Worked a few times during testing", "Works most of the time", "Fails sometimes", "Never fails"}, 0),

            new TextType(uuid(),"Cool Comments", "Is there anything cool about the robot?", ""),

            new TextType(uuid(),"Comments", "Things go here", "Day 1:\n\nDay 2:\n\nDay 3:\n")
        }
    };


    public static boolean save(String filename, FieldType[][] values){
        try {
            ByteBuilder bb = new ByteBuilder();
            for (int i = 0; i < values.length; i++) {
                bb.addRaw(127, save_version(values[i]));
            }
            FileEditor.writeFile(filename, bb.build());
            return true;
        }catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return false;
//            throw new RuntimeException(e);
        }
    }

    private static byte[] save_version(FieldType[] values) throws ByteBuilder.buildingException {
        ByteBuilder bb = new ByteBuilder();
        for(int i =0; i < values.length; i++){
            bb.addRaw(values[i].get_byte_id(), values[i].encode());
        }
        return bb.build();
    }

    public static FieldType[][] load(String filename){
        byte[] bytes = FileEditor.readFile(filename);

//        System.out.println(bytes);

        try {
            BuiltByteParser bbp = new BuiltByteParser(bytes);
            ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();
            FieldType[][] values = new FieldType[objects.size()][];

            for(int i = 0 ; i < objects.size(); i++){
                values[i] = load_version((byte[]) objects.get(i).get());
            }


            return values;
        } catch (Exception e) {
            AlertManager.error(e);
            return null;
        }
    }

    private static FieldType[] load_version(byte[] bytes) throws BuiltByteParser.byteParsingExeption{
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();
        FieldType[] output = new FieldType[objects.size()];

        for(int i = 0 ; i < objects.size(); i++){
            BuiltByteParser.parsedObject obj = objects.get(i);
            FieldType t = null;
            switch (obj.getType()){
                case FieldType.slider_type_id:
                    t = new SliderType();
                    break;
                case FieldType.dropdownType:
                    t = new DropdownType();
                    break;
                case FieldType.notesType:
                    t = new TextType();
                    break;
                case FieldType.tallyType:
                    t = new TallyType();
                    break;
                case FieldType.numberType:
                    t = new NumberType();
                    break;
                case FieldType.checkboxType:
                    t = new CheckboxType();
                    break;
                case FieldType.fieldposType:
                    t = new FieldposType();
                    break;
            }

            t.decode((byte[]) obj.get());
            output[i] = t;
        }

        return output;
    }
}
