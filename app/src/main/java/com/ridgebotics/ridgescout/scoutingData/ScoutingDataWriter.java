package com.ridgebotics.ridgescout.scoutingData;

import com.ridgebotics.ridgescout.scoutingData.transfer.TransferType;
import com.ridgebotics.ridgescout.types.ScoutingArray;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.data.IntArrType;
import com.ridgebotics.ridgescout.types.data.StringType;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// Mostly an extension of Fields.java. Saves the raw data from each Field Type.
public class ScoutingDataWriter {
//    private static final int int_type_id = 255;
//    private static final int string_type_id = 254;

    public static boolean save(int version, String username, String filename, RawDataType[] data){
        ByteBuilder bb = new ByteBuilder();
        try {
            bb.addInt(version);
            bb.addString(username);
            for(int i = 0; i < data.length; i++){
                switch (data[i].getValueType()){
                    case NUM:
                        bb.addInt((int) data[i].forceGetValue());
                        System.out.println("Saved INT: " + data[i].getUUID() + ", ("+ data[i].get() +")");
                        break;
                    case STRING:
                        bb.addString((String) data[i].forceGetValue());
                        System.out.println("Saved STR: " + data[i].getUUID() + ", ("+ data[i].get() +")");
                        break;
                    case NUMARR:
                        bb.addIntArray((int[]) data[i].forceGetValue());
                        System.out.println("Saved INT Array: " + data[i].getUUID() + ", ("+ Arrays.toString((int[]) data[i].get()) +")");
                }
            }
            byte[] bytes = bb.build();
            FileEditor.writeFile(filename, bytes);
            return true;
        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return false;
        }
    }

    public static class ParsedScoutingDataResult {
        public String filename;
        public String username;
        public int version;
        public ScoutingArray data;
    }

    public static ParsedScoutingDataResult load(String filename, FieldType[][] values , TransferType[][] transferValues){
        byte[] bytes = FileEditor.readFile(filename);
        BuiltByteParser bbp = new BuiltByteParser(bytes);

        try {
            ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();
            RawDataType[] rawDataTypes = new RawDataType[objects.size()-2];

            int version = ((int)objects.get(0).get());

            if(values.length <= version) {
//                AlertManager.addSimpleError("Error loading " + filename);
                AlertManager.error(new BuiltByteParser.byteParsingExeption("Field version (" +version + ") is too recent as compared to latest version (" + (values.length-1) + ")!"));
                return null;
            }

//            System.out.println(version);
            String username = (String) objects.get(1).get();

            for(int i = 0; i < values[version].length; i++){
                switch (objects.get(i+2).getType()){
                    case 1: // Int
                        rawDataTypes[i] = IntType.newNull(values[version][i].UUID);
                        rawDataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded INT: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ rawDataTypes[i].get() +")");
                        break;
                    case 2: // String
                        rawDataTypes[i] = StringType.newNull(values[version][i].UUID);
                        rawDataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded STR: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ rawDataTypes[i].get() +")");
                        break;
                    case 3: // Int array
                        rawDataTypes[i] = IntArrType.newNull(values[version][i].UUID);
                        rawDataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded intARR: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ Arrays.toString((int[]) rawDataTypes[i].get()) +")");
                        break;
                }
            }

            ScoutingArray msa = new ScoutingArray(version, rawDataTypes, values, transferValues);
            msa.update();

            ParsedScoutingDataResult psda = new ParsedScoutingDataResult();

            psda.filename = filename;
            psda.username = username;
            psda.version = version;
            psda.data = msa;

            return psda;

        } catch (BuiltByteParser.byteParsingExeption e){
            AlertManager.error(e);
            return null;
        }
    }

    // A function that takes in a list of names seperated by commas, and adds a name if it is not included
    // This is used for multi-scouter attribution to data.
    public static String checkAddName(String prevnames, String name){
        List<String> names = new ArrayList<>(List.of(prevnames.split(", ")));

        if(!names.contains(name))
            names.add(name);

        return String.join(", ", names);
    }

}
