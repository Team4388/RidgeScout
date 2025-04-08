package com.ridgebotics.ridgescout.scoutingData;

import com.ridgebotics.ridgescout.scoutingData.transfer.TransferType;
import com.ridgebotics.ridgescout.types.ScoutingArray;
import com.ridgebotics.ridgescout.types.data.DataType;
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

public class ScoutingDataWriter {
//    private static final int int_type_id = 255;
//    private static final int string_type_id = 254;

    public static boolean save(int version, String username, String filename, DataType[] data){
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
            DataType[] dataTypes = new DataType[objects.size()-2];

            int version = ((int)objects.get(0).get());
//            System.out.println(version);
            String username = (String) objects.get(1).get();

            for(int i = 0; i < values[version].length; i++){
                switch (objects.get(i+2).getType()){
                    case 1: // Int
                        dataTypes[i] = IntType.newNull(values[version][i].UUID);
                        dataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded INT: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ dataTypes[i].get() +")");
                        break;
                    case 2: // String
                        dataTypes[i] = StringType.newNull(values[version][i].UUID);
                        dataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded STR: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ dataTypes[i].get() +")");
                        break;
                    case 3: // Int array
                        dataTypes[i] = IntArrType.newNull(values[version][i].UUID);
                        dataTypes[i].forceSetValue(objects.get(i+2).get());
                        System.out.println("Loaded intARR: " + values[version][i].name + " (" + values[version][i].UUID + ") " + ", ("+ Arrays.toString((int[])dataTypes[i].get()) +")");
                        break;
                }
            }

            ScoutingArray msa = new ScoutingArray(version, dataTypes, values, transferValues);
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

}
