package com.ridgebotics.ridgescout.utility;

import static com.ridgebotics.ridgescout.utility.FileEditor.lengthHeaderBytes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BuiltByteParser {
    public static final Integer boolType = 0;
    public static final Integer intType = 1;
    public static final Integer stringType = 2;
    public static final Integer intArrayType = 3;
    public static final Integer stringArrayType = 4;
    public static final Integer longType = 5;

    public static class byteParsingExeption extends Exception {
        public byteParsingExeption() {}
        public byteParsingExeption(String message) {
            super(message);
        }
    }

    public static String unBlankStrNull(String str){
        if(str.equals("ƒ")){
            return "";
        }
        else return str;
    }

    public abstract class parsedObject {
        public abstract Integer getType();
        public abstract Object get();
    }

    public class boolObject extends parsedObject{
        boolean val;
        public Integer getType(){return boolType;}
        public Object get(){return val;}
    }

    public class intObject extends parsedObject{
        int num;
        public Integer getType(){return intType;}
        public Object get(){return num;}
    }
    public class stringObject extends parsedObject{
        String str;
        public Integer getType(){return stringType;}
        public Object get(){return str;}
    }


    public class intArrayObject extends parsedObject{
        int[] arr;
        public Integer getType(){return intArrayType;}
        public Object get(){return arr;}
    }
    public class stringArrayObject extends parsedObject{
        String[] arr;
        public Integer getType(){return stringArrayType;}
        public Object get(){return arr;}
    }
    public class longObject extends parsedObject{
        long num;
        public Integer getType(){return longType;}
        public Object get(){return num;}
    }


    public class rawObject extends parsedObject {
        private int type;
        public rawObject(int type){this.type = type;}
        byte[] bytes;
        public Integer getType(){return type;}
        public Object get(){return bytes;}
    }

    byte[] bytes;
    ArrayList<parsedObject> objects = new ArrayList<>();
    public BuiltByteParser(byte[] bytes){
        this.bytes = bytes;
    }
    public ArrayList<parsedObject> parse() throws byteParsingExeption {
        if(bytes.length < lengthHeaderBytes + 1){throw new byteParsingExeption("Invalid length");}
        int curIndex = 0;
        while(true){
//            Log.i("t", String.valueOf(curIndex));
            final int length = FileEditor.fromBytes(FileEditor.getByteBlock(bytes, curIndex, curIndex+lengthHeaderBytes), lengthHeaderBytes);
            final int type = bytes[curIndex+lengthHeaderBytes] & 0xFF;

            if(length == 0){
                curIndex += lengthHeaderBytes;
                continue;
            }

            final byte[] block;

            try {
                block = FileEditor.getByteBlock(bytes, curIndex + lengthHeaderBytes + 1, curIndex + length + lengthHeaderBytes + 1);
            } catch(Exception e){
                throw new byteParsingExeption("Array out of bounds");
            }

            switch(type){
                case 0:
                    boolObject bo = new boolObject();
                    bo.val = block[0] == (byte) 1;
                    objects.add(bo);
                    break;
                case 1:
                    intObject io = new intObject();
                    io.num = FileEditor.fromBytes(block, length);
                    objects.add(io);
                    break;
                case 2:
                    stringObject so = new stringObject();
                    so.str = unBlankStrNull(new String(block, StandardCharsets.UTF_8));
                    objects.add(so);
                    break;
                case 3:
                    BuiltByteParser int_bbp = new BuiltByteParser(block);
                    ArrayList<parsedObject> intArrayObjects = int_bbp.parse();

                    int[] intArr = new int[intArrayObjects.size()];

                    for(int i = 0; i < intArrayObjects.size(); i ++){
                        intArr[i] = (int) intArrayObjects.get(i).get();
                    }

                    intArrayObject ia = new intArrayObject();
                    ia.arr = intArr;
//                    System.out.println(Arrays.toString(intArr));
                    objects.add(ia);
                    break;
                case 4:

                    BuiltByteParser str_bbp = new BuiltByteParser(block);
                    ArrayList<parsedObject> strArrayObjects = str_bbp.parse();

                    String[] StringArr = new String[strArrayObjects.size()];

                    for(int i = 0; i < strArrayObjects.size(); i ++){
                        StringArr[i] = (String) strArrayObjects.get(i).get();
                    }

                    stringArrayObject sa = new stringArrayObject();
                    sa.arr = StringArr;
                    objects.add(sa);
                    break;
                case 5:
                    longObject lo = new longObject();
                    lo.num = FileEditor.fromBytesLong(block, length);
                    objects.add(lo);
                    break;
                default:
                    rawObject ro = new rawObject(type);
                    ro.bytes = block;
                    objects.add(ro);
                    break;
            }

            curIndex += length + lengthHeaderBytes + 1;

            if(curIndex == bytes.length){
                break;
            }else if(curIndex > bytes.length){
                throw new byteParsingExeption("Block length problem");
            }
        }

        return objects;
    }
}