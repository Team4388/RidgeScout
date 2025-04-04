package com.ridgebotics.ridgescout.utility;

import static com.ridgebotics.ridgescout.utility.FileEditor.lengthHeaderBytes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ByteBuilder {
    public static final int bool_id = 0;
    public static final int int_id = 1;
    public static final int string_id = 2;
    public static final int int_arr_id = 3;
    public static final int string_arr_id = 4;
    public static final int long_id = 5;

    ArrayList<byteType> bytesToBuild = new ArrayList<>();

    public class buildingException extends Exception {
        public buildingException() {}
        public buildingException(String message) {
            super(message);
        }
    }

    private abstract static class byteType {
        public abstract byte getType();
        public abstract int length();
        public abstract byte[] build();
    }

    public static String blankStrNull(String str){
        if(str.isEmpty() || str.isEmpty()){
            return "ƒ";
        }
        else return str;
    }

    private class boolType extends byteType {
        public boolean val;
        public byte getType(){return bool_id;}
        public int length(){return 1;}
        public byte[] build(){
            return new byte[]{(byte) (val ? 1 : 0)};
        }
    }

    public ByteBuilder addBool(boolean n) throws buildingException {
        boolType boolType = new boolType();
        boolType.val = n;

        bytesToBuild.add(boolType);
        return this;
    }

    private class intType extends byteType {
        public int precision;
        public int num;
        public byte getType(){return int_id;}
        public int length(){return precision;}
        public byte[] build(){
            return FileEditor.toBytes(num, precision);
        }
    }
    private int getLeastBytePrecision(int num){
        if(num <= 1){return 1;}
        return (int) Math.ceil(Math.log(Math.abs(num))/Math.log(8));
    }
    public ByteBuilder addInt(int num) throws buildingException {
        // Get closest number of bytes
        int precision = getLeastBytePrecision(num);
        return addInt(num, precision);
    }
    public ByteBuilder addInt(int num, int precision) throws buildingException {
        if(precision <= 0){throw new buildingException("Invalid precision: " + precision);}

        if(precision > Math.pow(lengthHeaderBytes,8)){throw new buildingException("Precision too large (greter than 16777216)");}
        if(precision < getLeastBytePrecision(num)){throw new buildingException("Precision too small");}

        if(num > Integer.MAX_VALUE){throw new buildingException("Integer overflow");}
        if(num < Integer.MIN_VALUE){throw new buildingException("Integer overflow");}

        intType intType = new intType();
        intType.num = num;
        intType.precision = precision;

        bytesToBuild.add(intType);
        return this;
    }

    private class stringType extends byteType {
        public byte[] bytes;
        public byte getType(){return string_id;}
        public int length(){return bytes.length;}
        public byte[] build(){
            return bytes;
//            return str.getBytes(charset);
        }
    }
    public ByteBuilder addString(String str) throws buildingException {
        str = blankStrNull(str);
        if(str.length() > Math.pow(lengthHeaderBytes,8)){throw new buildingException("String too long (greater than 16777216)");}

        stringType stringType = new stringType();
        // To get the length correctly, the string bytes need to be precalculated
        stringType.bytes = str.getBytes(StandardCharsets.UTF_8);

        bytesToBuild.add(stringType);
        return this;
    }

    private class intArrayType extends byteType {
        public byte[] bytes;
        public byte getType(){return int_arr_id;}
        public int length(){return bytes.length;}
        public byte[] build(){
            return bytes;
//            return str.getBytes(charset);
        }
    }
    public ByteBuilder addIntArray(int[] arr) throws buildingException {
        intArrayType intArrayType = new intArrayType();

        ByteBuilder bb = new ByteBuilder();

        for(int i = 0; i < arr.length; i++){
            bb.addInt(arr[i]);
        }

        intArrayType.bytes = bb.build();

        bytesToBuild.add(intArrayType);
        return this;
    }

    private class stringArrayType extends byteType {
        public byte[] bytes;
        public byte getType(){return string_arr_id;}
        public int length(){return bytes.length;}
        public byte[] build(){
            return bytes;
//            return str.getBytes(charset);
        }
    }
    public ByteBuilder addStringArray(String[] arr) throws buildingException {
        stringArrayType stringArrayType = new stringArrayType();

        ByteBuilder bb = new ByteBuilder();

        for(int i = 0; i < arr.length; i++){
            bb.addString(arr[i]);
        }

        stringArrayType.bytes = bb.build();

        bytesToBuild.add(stringArrayType);
        return this;
    }

    private class longType extends byteType {
        public int precision;
        public long num;
        public byte getType(){return long_id;}
        public int length(){return precision;}
        public byte[] build(){
            return FileEditor.toBytes(num, precision);
        }
    }
    private int getLeastBytePrecision(long num){
        if(num <= 1){return 1;}
        return (int) Math.ceil(Math.log(Math.abs(num))/Math.log(8));
    }
    public ByteBuilder addLong(long num) throws buildingException {
        // Get closest number of bytes
        int precision = getLeastBytePrecision(num);
        return addLong(num, precision);
    }
    public ByteBuilder addLong(long num, int precision) throws buildingException {
        if(precision <= 0){throw new buildingException("Invalid precision: " + precision);}

        if(precision > Math.pow(lengthHeaderBytes,8)){throw new buildingException("Precision too large (greter than 16777216)");}
        if(precision < getLeastBytePrecision(num)){throw new buildingException("Precision too small");}

        if(num > Long.MAX_VALUE){throw new buildingException("Long overflow");}
        if(num < Long.MIN_VALUE){throw new buildingException("Long overflow");}

        longType longType = new longType();
        longType.num = num;
        longType.precision = precision;

        bytesToBuild.add(longType);
        return this;
    }

    private class rawType extends byteType {
        public int type;
        public byte[] bytes;
        public byte getType(){return (byte) type;}
        public int length(){return bytes.length;}
        public byte[] build(){
            return bytes;
        }
    }

    public ByteBuilder addRaw(int type, byte[] bytes) throws buildingException {
        if(bytes.length > 16777216){throw new buildingException("Byte array length to long (greater than 16777216)");}

        rawType rawType = new rawType();
        rawType.type = type;
        rawType.bytes = bytes;
        bytesToBuild.add(rawType);

        return this;
    }

    public byte[] build() throws buildingException {
        if(bytesToBuild.size() == 0){throw new buildingException("Cannot build null data");}

        int length = bytesToBuild.size() * (lengthHeaderBytes + 1);
        for(byteType bt : bytesToBuild){
            length += bt.length();
        }

        byte[] bytes = new byte[length];
        int bytesFilled = 0;

        for(byteType bt : bytesToBuild){

            byte[] blockLength = FileEditor.toBytes(bt.length(), lengthHeaderBytes + 1);

            for(int i = 0; i < lengthHeaderBytes; i++) {
                bytes[bytesFilled] = blockLength[i];
                bytesFilled += 1;
            }

            bytes[bytesFilled] = bt.getType();
            bytesFilled += 1;

//            if(bt.length == 0)
//                continue;

            int i = 1;
            byte[] newBytes = bt.build();
            for(byte b : newBytes){
//                Log.i("i", (bytesFilled+1) + "/" + length + " (" + i + "/" + bt.length() + ")");
                bytes[bytesFilled] = b;
                bytesFilled += 1;
                i++;
            }
        }

        return bytes;
    }
}
