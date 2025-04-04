package com.ridgebotics.ridgescout.utility;

import android.content.Context;

import com.ridgebotics.ridgescout.types.frcEvent;
import com.ridgebotics.ridgescout.types.frcTeam;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class FileEditor {
    public final static String baseDir = "/data/data/com.ridgebotics.ridgescout/";
    public static final byte internalDataVersion = 0x01;
    public static final int maxCompressedBlockSize = 4096;
    public static final int lengthHeaderBytes = 3;


    public static final String TBAAddress = "https://www.thebluealliance.com/api/v3/";
    public static final String TBAHeader = "X-TBA-Auth-Key: tjEKSZojAU2pgbs2mBt06SKyOakVhLutj3NwuxLTxPKQPLih11aCIwRIVFXKzY4e";

//    private TimeZone localTimeZone = TimeZone.getDefault();



    public static String binaryVisualize(byte[] bytes){
        String returnStr = "";
        for (byte aByte : bytes) {
            for (int b = 7; b >= 0; b--) {
                returnStr += String.valueOf((aByte >> b) & 1);
            }
            returnStr += " (" + (int) aByte + ")\n";
        }
        return returnStr;
    }


    public static char byteToChar(int num){
        return new String(toBytes(num, 1), StandardCharsets.ISO_8859_1).charAt(0);
    }



    public static byte[] toBytes(int num, int byteCount){
        if(num > (Math.pow(lengthHeaderBytes,byteCount*8)-1)){
            throw new BufferOverflowException();
        }
        byte[] bytes = new byte[byteCount];
        for(int i=0;i<byteCount;i++){
            bytes[i] = (byte)(num >> (i*8));
        }
        return bytes;
    }

    public static byte[] toBytes(long num, int byteCount){
        if(num > (Math.pow(lengthHeaderBytes,byteCount*8)-1)){
            throw new BufferOverflowException();
        }
        byte[] bytes = new byte[byteCount];
        for(int i=0;i<byteCount;i++){
            bytes[i] = (byte)(num >> (i*8));
        }
        return bytes;
    }



    public static int fromBytes(byte[] bytes, int byteCount){
        int returnInt = 0;
        for(int i=0;i<byteCount;i++){
            returnInt |= (bytes[i] & 0xFF) << (i*8);
        }
        return returnInt;
    }
    public static long fromBytesLong(byte[] bytes, int byteCount){
        long returnLong = 0;
        for(int i=0;i<byteCount;i++){
            returnLong |= (bytes[i] & 0xFFL) << (i*8);
        }
        return returnLong;
    }



    public static int byteFromChar(char c){
        byte[] bytes = (String.valueOf(c)).getBytes(Charset.defaultCharset());
        return Byte.toUnsignedInt(bytes[0]);
    }


    public static byte[] getByteBlock(byte[] bytes, int start, int end){
        end = Math.min(end, bytes.length);

        byte[] dataBlock = new byte[end-start];

        for(int a=start;a<end;a++){
//            Log.i("test", start+", "+a+", "+end);
            dataBlock[a-start] = bytes[a];
        }

        return dataBlock;
    }

    public static byte[] compress(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[maxCompressedBlockSize];

        while (!deflater.finished()) {
            int compressedSize = deflater.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        return outputStream.toByteArray();
    }

    public static byte[] blockCompress(byte[] inputData) {
        List<byte[]> compiledData = new ArrayList<>();

        for(int i = 0; i<Math.ceil((double) inputData.length / FileEditor.maxCompressedBlockSize); i++){
            final int start = i* FileEditor.maxCompressedBlockSize;
            int end = ((i+1)* FileEditor.maxCompressedBlockSize);
            if(end > inputData.length) {
                end = inputData.length;
            }

            byte[] dataBlock = FileEditor.getByteBlock(inputData, start, end);

            final byte[] compressedBlock = FileEditor.compress(dataBlock);

            compiledData.add(FileEditor.toBytes(compressedBlock.length, 2));
            compiledData.add(compressedBlock);
        }
        return combineByteArrays(compiledData);
    }

    public static byte[] blockUncompress(byte[] data) throws DataFormatException {
        List<byte[]> uncompressedData = new ArrayList<>();
        int curIndex = 0;
        while (curIndex < data.length) {

            final int blockLength = FileEditor.fromBytes(FileEditor.getByteBlock(data, curIndex, curIndex + 2), 2);

            uncompressedData.add(
                    decompress(
                            FileEditor.getByteBlock(data, curIndex + 2, curIndex + blockLength + 2)
                    )
            );

            curIndex += blockLength + 2;
        }
        return combineByteArrays(uncompressedData);
    }


    public static byte[] decompress(byte[] input) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[maxCompressedBlockSize];


        while (inflater.getRemaining() > 0) {
            int decompressedSize = inflater.inflate(buffer);
            if (decompressedSize == 0) {
                break;
            }
            outputStream.write(buffer, 0, decompressedSize);
        }

        return outputStream.toByteArray();
    }

    public static byte[] combineByteArrays(List<byte[]> arrayList) {
        // Calculate the total length of the combined array
        int totalLength = arrayList.stream()
                .mapToInt(array -> array.length)
                .sum();

        // Create a new byte array with the total length
        byte[] result = new byte[totalLength];

        // Copy each array into the result array
        int offset = 0;
        for (byte[] array : arrayList) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }





//    public static Calendar getLastModified(String filepath){
//        File f = new File(baseDir + filepath);
//        if(f.exists()){
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(f.lastModified());
//            return calendar;
//        }
//        return null;
//    }
//
//    public static void setLastModified(String filepath, Calendar calendar){
//        File f = new File(baseDir + filepath);
//        if(f.exists()){
//            f.setLastModified(calendar.getTimeInMillis());
//        }
//    }



    public static boolean writeFile(String filepath, byte[] data) {
        try {
            FileOutputStream output = new FileOutputStream(baseDir + filepath);
            output.write(data);
            output.close();

//            Date d = new Date();

            new File(baseDir + filepath).setLastModified(new Date().getTime());
            return true;
        }
        catch (IOException e) {
            AlertManager.error(e);
            return false;
        }
    }

    public static boolean createFile(String filepath){
        if(fileExist(filepath)){
            return true;
        }
        try {
            File file = new File(baseDir + filepath);
            return file.createNewFile();
        }
        catch (IOException e) {
            AlertManager.error(e);
            return false;
        }
    }

    public static boolean deleteFile(String filepath){
        if(fileExist(filepath)){
            return true;
        }
        File file = new File(baseDir + filepath);
        return file.delete();
    }

    public static boolean fileExist(String path){
        File f = new File(baseDir + path);
        return f.exists() && !f.isDirectory();
    }

    public static byte[] readFile(String path){
        return readFileExact(baseDir + path);
    }
    public static byte[] readFileExact(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            return bytes;
        } catch (FileNotFoundException e) {
            AlertManager.error(e);
            return null;
        } catch (IOException e) {
            AlertManager.error(e);
            return null;
        }
    }

    private static String intSplit(int[] intArr, String splitStr){
        String returnStr = "";
        for(int i=0;i<intArr.length;i++){
            returnStr += String.valueOf(intArr[i]);
            if(i != intArr.length-1){
                returnStr += splitStr;
            }
        }
        return returnStr;
    }







    public static boolean setEvent(frcEvent event){
        final String filename = (event.eventCode + ".eventdata");

        if(SettingsManager.getEVCode().equals("unset")){
            SettingsManager.setEVCode(event.eventCode);
        }

        return writeFile(filename, event.encode());
    }

    public static ArrayList<String> getEventList(){
        File f = new File(baseDir);
        File[] files = f.listFiles();
        ArrayList<String> outFiles = new ArrayList<>();
        if(files == null){return outFiles;}
        for (File file : files) {
            if(!file.isDirectory() && file.getName().endsWith(".eventdata")) {
                outFiles.add(file.getName().substring(0,file.getName().length()-10));
            }
        }
        Collections.sort(outFiles);
        return outFiles;
    }


    public static String[] getMatchesByTeamNum(String evcode, int teamNum){
        File f = new File(baseDir);
        File[] files = f.listFiles();
        
        ArrayList<String> outFiles = new ArrayList<>();

        if(files == null){return new String[0];}

        for (File file : files) {
            String name = file.getName();
            if(!file.isDirectory() && name.startsWith(evcode+"-") && name.endsWith("-"+teamNum+".matchscoutdata")) {
                outFiles.add(file.getName());
            }
        }

        String[] filenames = outFiles.toArray(new String[0]);

        Arrays.sort(filenames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1.split("-")[1]).compareTo(Integer.valueOf(o2.split("-")[1]));
            }
        });


        return filenames;
    }






    public static String[] getEventFiles(String evcode){
        File f = new File(baseDir);
        File[] files = f.listFiles();

        if(files == null){return new String[0];}

        ArrayList<String> outFiles = new ArrayList<>();
        outFiles.add("matches.fields");
        outFiles.add("pits.fields");
//        outFiles.add(evcode + ".eventdata");

        for (File file : files) {
            String name = file.getName();
            if(!file.isDirectory() && name.startsWith(evcode)) {
                outFiles.add(file.getName());
            }
        }

        String[] filenames = outFiles.toArray(new String[0]);

        try {
            Arrays.sort(filenames, (o1, o2) -> {
                try {
                    if (!o1.contains("-") || !o2.contains("-"))
                        return 0;
                    return Integer.valueOf(o1.split("-")[1]).compareTo(Integer.valueOf(o2.split("-")[1]));
                } catch (Exception e) {
                    return 0;
                }
            });
        } catch (Exception e){
            AlertManager.error(e);
        }


        return filenames;
    }

    // https://stackoverflow.com/questions/7620401/how-to-convert-image-file-data-in-a-byte-array-to-a-bitmap
//    public static String imageToBitMap(byte[] data) throws IOException {
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        return bitmap;
//        ByteArrayOutputStream blob = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
//    }


    public static boolean setTeams(Context context, String key, ArrayList<frcTeam> teams){
        return true;
    }
}

