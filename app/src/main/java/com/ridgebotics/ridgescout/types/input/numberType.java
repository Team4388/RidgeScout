package com.ridgebotics.ridgescout.types.input;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.data.intType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class numberType extends inputType {
    public int get_byte_id() {return numberType;}
    public inputTypes getInputType(){return inputTypes.NUMBER;}
    public dataType.valueTypes getValueType(){return dataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public numberType(){}
    public String get_type_name(){return "Number";}
    public numberType(String name, String description, int default_value){
        super(name, description);
        this.default_value = default_value;
    }





    public byte[] encode() throws ByteBuilder.buildingException {
        ByteBuilder bb = new ByteBuilder();
        bb.addString(name);
        bb.addString(description);
        bb.addInt((int)default_value);
        return bb.build();
    }
    public void decode(byte[] bytes) throws BuiltByteParser.byteParsingExeption {
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();

        name          = (String)   objects.get(0).get();
        description   = (String)   objects.get(1).get();
        default_value =            objects.get(2).get();
    }





    public EditText num = null;

    public View createView(Context context, Function<dataType, Integer> onUpdate){
        num = new EditText(context);
        num.setInputType(TYPE_CLASS_NUMBER);
        num.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdate.apply(getViewValue());}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        setViewValue(default_value);

        return num;

    }

    public void setViewValue(Object value) {
        if(num == null) return;
        if(intType.isNull((int)value)){
            nullify();
            return;
        }

        isBlank = false;
        num.setVisibility(View.VISIBLE);
        num.setText(String.valueOf(value));
    }
    public void nullify(){
        isBlank = true;
        num.setVisibility(View.GONE);
    }
    public dataType getViewValue(){
        if(num == null) return null;
        if(num.getVisibility() == View.GONE) return intType.newNull(name);
        return new intType(name, safeToInt(num.getText().toString()));
    }



    private int safeToInt(String num){
        if(num.isEmpty())
            return intType.nullval;
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException e){
            return intType.nullval;
        }
    }




    public void add_individual_view(LinearLayout parent, dataType data){
        if(data.isNull()) return;

        TextView tv = new TextView(parent.getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(String.valueOf((int) data.get()));
        tv.setTextSize(24);
        parent.addView(tv);
    }








    private static float calculateMean(int[] data) {
        float sum = 0;
        for (int value : data) {
            sum += (float) value;
        }
        return sum / data.length;
    }

    private static float calculateStandardDeviation(int[] data, float mean) {
        float sum = 0;
        for (int value : data) {
            sum += (float) Math.pow((float) value - mean, 2);
        }
        return (float) Math.sqrt(sum / (data.length - 1));
    }

    private static List<Entry> generateNormalDistribution(float mean, float stdDev, int count, int scale) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float y = (float) ((1 / (stdDev * Math.sqrt(2 * Math.PI)))
                    * Math.exp(-0.5 * Math.pow(((float) i - mean) / stdDev, 2)));
            entries.add(new Entry((float) i, y*scale)); // Scale y for visibility
        }
        return entries;
    }

    private static int findMin(dataType[] data){
        int min = (int)data[0].get();
        for(int i = 1; i < data.length; i++)
            if((int)data[i].get() < min)
                min = (int)data[i].get();
        return min;
    }

    private static int findMax(dataType[] data){
        int max = (int)data[0].get();
        for(int i = 1; i < data.length; i++)
            if((int)data[i].get() > max)
                max = (int)data[i].get();
        return max;
    }

    public void add_compiled_view(LinearLayout parent, dataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(0xff252025);

        int min = findMin(data);
        int max = findMax(data);

        int[] values = new int[max-min+1];

        for (int i = 0; i < data.length; i++)
            if(data[i] != null && data[i].isNull())
                values[(int) data[i].get()-min]++;


        ArrayList<Integer> mean_temp = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
            if((int)data[i].get() != 0)
                mean_temp.add((int) data[i].get());

        int[] mean_vals = mean_temp.stream().mapToInt(Integer::intValue).toArray();

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++)
            entries.add(new Entry(i, values[i]));


        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);



        // Calculate mean and standard deviation
        float mean = calculateMean(mean_vals);
        float stdDev = calculateStandardDeviation(mean_vals, mean);

        // Generate normal distribution curve
        List<Entry> normalDistEntries = generateNormalDistribution(mean-min, stdDev, max-min+1, (max-min)/data.length);


        LineDataSet normalDistSet = new LineDataSet(normalDistEntries, "Normal Distribution");
        normalDistSet.setColor(Color.RED);
        normalDistSet.setDrawCircles(false);
        normalDistSet.setDrawValues(false);
        normalDistSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet, normalDistSet);

        chart.setData(lineData);
        chart.invalidate();

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        dataSet.setValueTextColor(Color.RED);

        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setTextColor(Color.WHITE);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);

        parent.addView(chart);
    }




    public void add_history_view(LinearLayout parent, dataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(0xff252025);

        int min = findMin(data);
        int max = findMax(data);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(new Entry(i, (float)(int) data[i].get()));
        }


        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);
        chart.invalidate();

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        dataSet.setValueTextColor(Color.RED);

        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setTextColor(Color.WHITE);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);


        chart.getAxisLeft().setAxisMinimum(min);
        chart.getAxisLeft().setAxisMaximum(max);

        chart.getAxisRight().setAxisMinimum(min);
        chart.getAxisRight().setAxisMaximum(max);


        parent.addView(chart);
    }
}

