package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

// Basically just holds a byte array and a filename, and then us just encoded.
public class ScoutingFile {
    public static final int typecode = 255;
    public String filename;
    public byte[] data;


    public ScoutingFile(){}

    public ScoutingFile(String filename){
        this(filename, FileEditor.readFile(filename));
    }

    public ScoutingFile(String filename, byte[] data){
        this.filename = filename;
        this.data = data;
    }

    public byte[] encode(){
        try {
            ByteBuilder bb = new ByteBuilder()
                    .addString(filename);

//            byte[] data = Objects.requireNonNull(fileEditor.readFile(filename));

//            for(int i = 0; i < data.length / 65535; i++){
//                bb.addRaw(255, fileEditor.getByteBlock(data, i*65535, (i+1)*65535));
//            }

            bb.addRaw(255, Objects.requireNonNull(FileEditor.readFile(filename)));

            return bb.build();

        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return null;
        }
    }

    public static ScoutingFile decode(byte[] bytes){
        try{
            ArrayList<BuiltByteParser.parsedObject> objects = new BuiltByteParser(bytes).parse();

            ScoutingFile f = new ScoutingFile();

            f.filename = (String) objects.get(0).get();
            f.data = (byte[]) objects.get(1).get();

            return f;

        }catch (Exception e){
            AlertManager.error(e);
            return null;
        }
    }

    public boolean write(){
        if(data == null || filename == null) return false;
        return FileEditor.writeFile(filename, data);
    }
}
