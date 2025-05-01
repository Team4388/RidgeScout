package com.ridgebotics.ridgescout.ui.data;

import com.ridgebotics.ridgescout.types.data.RawDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Helper class for scouting data by fields.
public class DataProcessing {
    public static int[] getNumberBounds(Map<Integer, List<RawDataType>> data){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;



        for(Integer teamNum : data.keySet()){
            List<RawDataType> teamData = data.get(teamNum);

            int[] locBounds = getNumberBounds(teamData);

            if(locBounds[1] > max) max = locBounds[1];
            if(locBounds[0] < min) min = locBounds[0];

        }

        return new int[]{min, max};
    }

    public static int[] getNumberBounds(List<RawDataType> data){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        if(data == null) return new int[]{min, max};
        for(int i = 0; i < data.size(); i++){
            RawDataType dataPoint = data.get(i);
//                if(dataPoint == null) continue;
            int num = (int) dataPoint.get();
            if(num > max) max = num;
            if(num < min) min = num;
        }

        return new int[]{min, max};
    }


    //https://stackoverflow.com/questions/42381759/finding-first-quartile-and-third-quartile-in-integer-array-using-java#63891545
    public static float[] getQuartiles(List<RawDataType> data) {
        float ans[] = new float[3];

        float[] val = new float[data.size()];
        for(int i = 0; i < val.length; i++){
            val[i] = (int) data.get(i).get();
        }

        for (int quartileType = 1; quartileType < 4; quartileType++) {
            float length = val.length + 1;
            float quartile;
            float newArraySize = (length * ((float) (quartileType) * 25 / 100)) - 1;
            Arrays.sort(val);
            if (newArraySize % 1 == 0) {
                quartile = val[(int) (newArraySize)];
            } else {
                int newArraySize1 = (int) (newArraySize);
                quartile = (val[newArraySize1] + val[newArraySize1 + 1]) / 2;
            }
            ans[quartileType - 1] =  quartile;
        }
        return ans;
    }


    /**
     * Calculates the specified percentile of a dataset.
     *
     * @param data       The dataset to analyze
     * @param percentile The percentile to find (0-100)
     * @return The value at the specified percentile
     */
    public static float calculatePercentile(float[] data, float percentile) {
        // Make a copy of the data to avoid modifying the original
        float[] dataCopy = Arrays.copyOf(data, data.length);

        // Sort the data in ascending order
        Arrays.sort(dataCopy);

        // Calculate the position as a fraction
        float position = (percentile / 100.0f) * (dataCopy.length - 1);

        // Get the integer part of the position
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);

        // If position is already an integer, return the value at that position
        if (lowerIndex == upperIndex) {
            return dataCopy[lowerIndex];
        }

        // Otherwise, interpolate between the two adjacent values
        float fraction = position - lowerIndex;
        float lowerValue = dataCopy[lowerIndex];
        float upperValue = dataCopy[upperIndex];

        // Linear interpolation
        return lowerValue + fraction * (upperValue - lowerValue);
    }

}
