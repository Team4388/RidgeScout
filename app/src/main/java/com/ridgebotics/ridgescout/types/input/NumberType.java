package com.ridgebotics.ridgescout.types.input;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import static com.ridgebotics.ridgescout.utility.Colors.background_color;
import static com.ridgebotics.ridgescout.utility.Colors.chart_background;
import static com.ridgebotics.ridgescout.utility.Colors.chart_text;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_1;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_2;
import static com.ridgebotics.ridgescout.utility.Colors.number_data;

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
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NumberType extends FieldType {
    public int get_byte_id() {return numberType;}
    public inputTypes getInputType(){return inputTypes.NUMBER;}
    public DataType.valueTypes getValueType(){return DataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public NumberType(){}
    public String get_type_name(){return "Number";}
    public NumberType(String UUID, String name, String description, int default_value){
        super(UUID, name, description);
        this.default_value = default_value;
    }





    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt((int)default_value);
    }
    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value = objects.get(0).get();
    }





    public EditText num = null;

    public View createView(Context context, Function<DataType, Integer> onUpdate){
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
        if(IntType.isNull((int)value)){
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
    public DataType getViewValue(){
        if(num == null) return null;
        if(num.getVisibility() == View.GONE) return IntType.newNull(name);
        return new IntType(name, safeToInt(num.getText().toString()));
    }



    private int safeToInt(String num){
        if(num.isEmpty())
            return IntType.nullval;
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException e){
            return IntType.nullval;
        }
    }




    public void add_individual_view(LinearLayout parent, DataType data){
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

    private static int findMin(DataType[] data){
        int min = (int)data[0].get();
        for(int i = 1; i < data.length; i++)
            if((int)data[i].get() < min)
                min = (int)data[i].get();
        return min;
    }

    private static int findMax(DataType[] data){
        int max = (int)data[0].get();
        for(int i = 1; i < data.length; i++)
            if((int)data[i].get() > max)
                max = (int)data[i].get();
        return max;
    }

    public void add_compiled_view(LinearLayout parent, DataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(chart_background);

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
        dataSet.setColor(number_data);
        dataSet.setValueTextColor(dropdown_value_text_1);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);



        // Calculate mean and standard deviation
        float mean = calculateMean(mean_vals);
        float stdDev = calculateStandardDeviation(mean_vals, mean);

        // Generate normal distribution curve
        List<Entry> normalDistEntries = generateNormalDistribution(mean-min, stdDev, max-min+1, (max-min)/data.length);


        LineDataSet normalDistSet = new LineDataSet(normalDistEntries, "Normal Distribution");
        normalDistSet.setColor(dropdown_value_text_2);
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

        dataSet.setValueTextColor(dropdown_value_text_2);

        chart.getXAxis().setTextColor(chart_text);
        chart.getAxisLeft().setTextColor(chart_text);
        chart.getAxisRight().setTextColor(chart_text);

        Legend legend = chart.getLegend();
        legend.setTextColor(chart_text);

        parent.addView(chart);
    }




    public void add_history_view(LinearLayout parent, DataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(chart_background);

        int min = findMin(data);
        int max = findMax(data);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(new Entry(i, (float)(int) data[i].get()));
        }


        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setColor(number_data);
        dataSet.setValueTextColor(dropdown_value_text_1);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);
        chart.invalidate();

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        dataSet.setValueTextColor(dropdown_value_text_2);

        chart.getXAxis().setTextColor(chart_text);
        chart.getAxisLeft().setTextColor(chart_text);
        chart.getAxisRight().setTextColor(chart_text);

        Legend legend = chart.getLegend();
        legend.setTextColor(chart_text);


        chart.getAxisLeft().setAxisMinimum(min);
        chart.getAxisLeft().setAxisMaximum(max);

        chart.getAxisRight().setAxisMinimum(min);
        chart.getAxisRight().setAxisMaximum(max);


        parent.addView(chart);
    }

    public void addDataToTable(TableLayout parent, Map<Integer, List<DataType>> data){

    }

    public String toString(DataType data){
        return String.valueOf((int) data.get());
    }
}

