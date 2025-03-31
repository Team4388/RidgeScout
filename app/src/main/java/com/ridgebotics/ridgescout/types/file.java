package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.fileEditor;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class file {
    public static final int typecode = 255;
    public String filename;
    public byte[] data;


    public file(){}

    public file(String filename){
        this(filename, fileEditor.readFile(filename));
    }

    public file(String filename, byte[] data){
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

            bb.addRaw(255, Objects.requireNonNull(fileEditor.readFile(filename)));

            return bb.build();

        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return null;
        }
    }

    public static file decode(byte[] bytes){
        try{
            ArrayList<BuiltByteParser.parsedObject> objects = new BuiltByteParser(bytes).parse();

            file f = new file();

            f.filename = (String) objects.get(0).get();

//            ByteArrayOutputStream fileData = new ByteArrayOutputStream();
//
//            for(int i = 1; i < objects.size(); i++){
//                byte[] blockBytes = (byte[]) objects.get(i).get();
//                fileData.write(blockBytes, (i-1)*65535, blockBytes.length);
//            }

            f.data = (byte[]) objects.get(1).get();

            return f;

        }catch (BuiltByteParser.byteParsingExeption e){
            AlertManager.error(e);
            return null;
        }
    }

    public boolean write(){
        if(data == null || filename == null) return false;
        return fileEditor.writeFile(filename, data);
    }
}
