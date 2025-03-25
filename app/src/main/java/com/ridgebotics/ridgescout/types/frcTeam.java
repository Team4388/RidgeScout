package com.ridgebotics.ridgescout.types;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class frcTeam {
    public static final int typecode = 252;
    private static final int[] DEFAULT_COLOR_ARR = new int[]{64,64,64};
    private static final int DEFAULT_COLOR = Color.argb(255, DEFAULT_COLOR_ARR[0],DEFAULT_COLOR_ARR[1],DEFAULT_COLOR_ARR[2]);

    public int teamNumber = 0;
    public String teamName = "null";
    public String city = "null";
    public String stateOrProv = "null";
    public String school = "null";
    public String country = "null";
    public Bitmap bitmap = null;
    public int[] teamColor = DEFAULT_COLOR_ARR;
    public int startingYear = 0;

    public String getDescription(){
        return teamName + " Started in " + startingYear + ", and are from " + school + " in " + city + ", " + stateOrProv + ", " + country;
    }

    public byte[] encode(){
        try {
            return new ByteBuilder()
            .addInt(teamNumber)
            .addString(teamName)
            .addString(city)
            .addString(stateOrProv)
            .addString(school)
            .addString(country)
            .addInt(startingYear)
            .addRaw(127, encodeBitmap(bitmap))
            .addIntArray(teamColor)
            .build();
        } catch (ByteBuilder.buildingException e) {
            AlertManager.error(e);
            return null;
        }
    }
    public static frcTeam decode(byte[] bytes){
        try {
            ArrayList<BuiltByteParser.parsedObject> objects = new BuiltByteParser(bytes).parse();

            frcTeam frc = new frcTeam();

            frc.teamNumber   = (int)    objects.get(0).get();
            frc.teamName     = (String) objects.get(1).get();
            frc.city         = (String) objects.get(2).get();
            frc.stateOrProv  = (String) objects.get(3).get();
            frc.school       = (String) objects.get(4).get();
            frc.country      = (String) objects.get(5).get();
            frc.startingYear = (int)    objects.get(6).get();

            if(objects.size() == 9){
                frc.bitmap   = decodeBitmap((BuiltByteParser.rawObject) objects.get(7));
                frc.teamColor = (int[])    objects.get(8).get();

//                System.out.println(Arrays.toString(frc.teamColor));
            }

            return frc;

        } catch (BuiltByteParser.byteParsingExeption e) {
            AlertManager.error(e);
            return null;
        }
    }

    public static byte[] encodeBitmap(Bitmap bitmap){
        if(bitmap == null) return new byte[]{0};
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        if(bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob)){
            return blob.toByteArray();
        }else{
            return new byte[]{0};
        }
    }

    public static Bitmap decodeBitmap(BuiltByteParser.rawObject rawObject){
        if(rawObject.getType() != 127) return null;
        byte[] bytes = (byte[]) rawObject.get();
        if(bytes.length <= 1) return null;
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    public static int[] findPrimaryColor(Bitmap bitmap) {
        if (bitmap == null) {
            return DEFAULT_COLOR_ARR;
        }

        // Step 1: Posterize the image (reduce color levels)
        Bitmap posterizedBitmap = posterize(bitmap, 8); // 8 levels of posterization

        // Step 2: Find the most saturated and frequent color
        return findMostSaturatedAndFrequentColor(posterizedBitmap);
    }

    // Posterize the image by reducing color levels
    private static Bitmap posterize(Bitmap original, int levels) {
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap posterized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int step = 255 / (levels - 1);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = original.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Quantize the RGB values
                r = (r / step) * step;
                g = (g / step) * step;
                b = (b / step) * step;

                int newPixel = Color.rgb(r, g, b);
                posterized.setPixel(x, y, newPixel);
            }
        }

        return posterized;
    }

    // Find the most saturated and frequent color
    private static int[] findMostSaturatedAndFrequentColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Map<Integer, Integer> colorFrequency = new HashMap<>();

        // Count the frequency of each color
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                colorFrequency.put(pixel, colorFrequency.getOrDefault(pixel, 0) + 1);
            }
        }

        // Find the most saturated and frequent color
        int primaryColor = DEFAULT_COLOR; // Default fallback
        double maxColorness = -1;
        int maxFrequency = 0;

        for (Map.Entry<Integer, Integer> entry : colorFrequency.entrySet()) {
            int color = entry.getKey();
            int frequency = entry.getValue();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            double colorness = Math.pow(hsv[1],2)+Math.pow(hsv[2],2);

            // Prioritize saturation, then frequency
            if ((colorness > maxColorness && frequency > maxFrequency * 0.5) && Color.alpha(color) > 127) { //|| (colorness == maxColorness)
                maxColorness = colorness;
//                maxFrequency = frequency;
                primaryColor = color;
            }
        }

        float[] hsv = new float[3];
        Color.colorToHSV(primaryColor, hsv);

        primaryColor = Color.HSVToColor(new float[]{hsv[0], Math.max(hsv[1], (float) 0.2), Math.max(hsv[2], (float) 0.3)});

        return new int[]{Color.red(primaryColor),Color.blue(primaryColor),Color.green(primaryColor)};
    }

    public int getTeamColor(){
        return Color.argb(255,teamColor[0],teamColor[2],teamColor[1]);
    }



    @NonNull
    public String toString(){
        return "frcTeam Num: " + teamNumber + ", " + getDescription();
    }
}