package com.ridgebotics.ridgescout.types.input;

import static com.ridgebotics.ridgescout.utility.Colors.chart_background;
import static com.ridgebotics.ridgescout.utility.Colors.chart_text;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_1;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_2;
import static com.ridgebotics.ridgescout.utility.Colors.slider_data;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SliderType extends FieldType {
    //        public int defaultValue;
    public int min;
    public int max;
    public int get_byte_id() {return slider_type_id;}
    public inputTypes getInputType(){return inputTypes.SLIDER;}
    public DataType.valueTypes getValueType(){return DataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public SliderType(){};
    public String get_type_name(){return "Slider";}
    public SliderType(String UUID, String name, String description, int defaultValue, int min, int max){
        super(UUID, name, description);
        this.default_value = defaultValue;
        this.min = min;
        this.max = max;
    }




    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt((int) default_value);
        bb.addInt(min);
        bb.addInt(max);
    }
    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value =       objects.get(0).get();
        min           = (int) objects.get(1).get();
        max           = (int) objects.get(2).get();
    }




    public Slider slider = null;

    public View createView(Context context, Function<DataType, Integer> onUpdate){
        slider = new Slider(context);
        setViewValue(default_value);
        slider.setStepSize((float) 1 / (max-min));
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                onUpdate.apply(getViewValue());
            }
        });
        return slider;
    }

    public void setViewValue(Object value) {
        if(slider == null) return;
        if(IntType.isNull((int) value)){
            nullify();
            return;
        }
        float slider_position = (float) ((int) value-min) / (max-min);
        float step_size = (float) 1/(max-min);
        int round_position = Math.round(slider_position / step_size);
        isBlank = false;

        float slidervalue = round_position*step_size;
        if(slidervalue > 1 || slidervalue < 0) {
            AlertManager.addSimpleError("Error loading slider " + name);
            slider.setValue(0);
        }else{
            slider.setValue(slidervalue);
        }


        slider.setVisibility(View.VISIBLE);
    }
    public DataType getViewValue(){
        if(slider == null) return null;
        if(slider.getVisibility() == View.GONE) return IntType.newNull(name);
        return new IntType(name, min + (int) (slider.getValue() * (max-min)));
    }
    public void nullify(){
        isBlank = true;
        slider.setVisibility(View.GONE);
    }






    public void add_individual_view(LinearLayout parent, DataType data){
        if(data.isNull()) return;
        Slider slider = new Slider(parent.getContext());

        float slider_position = (float) ((int) data.get()-min) / (max-min);
        float step_size = (float) 1/(max-min);
        int round_position = Math.round(slider_position / step_size);
        float value = round_position*step_size;
        if(value > 1 || value < 0) {
            AlertManager.addSimpleError("Error loading slider " + name);
            slider.setValue(0);
        }else{
            slider.setValue(value);
            slider.setStepSize((float) 1 / (max-min));
        }

        slider.setEnabled(false);
        parent.addView(slider);
    }








    private float calculateMean(int[] data) {
        float sum = 0;
        for (int value : data) {
            sum += (float) value;
        }
        return sum / data.length;
    }

    private float calculateStandardDeviation(int[] data, float mean) {
        float sum = 0;
        for (int value : data) {
            sum += Math.pow((float) value - mean, 2);
        }
        return (float) Math.sqrt(sum / (data.length - 1));
    }

    private List<Entry> generateNormalDistribution(float mean, float stdDev, int count, int scale) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float x = i;
            float y = (float) ((1 / (stdDev * Math.sqrt(2 * Math.PI)))
                    * Math.exp(-0.5 * Math.pow((x - mean) / stdDev, 2)));
            entries.add(new Entry(x, y*scale)); // Scale y for visibility
        }
        return entries;
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

        int[] values = new int[max-min+1];

        for (int i = 0; i < data.length; i++)
            if(!data[i].isNull())
                values[(int) data[i].get()-min]++;


        ArrayList<Integer> mean_temp = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
            if(!data[i].isNull())
                mean_temp.add((int) data[i].get());

        int[] mean_vals = mean_temp.stream().mapToInt(Integer::intValue).toArray();

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++)
            entries.add(new Entry(i, values[i]));


        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setColor(slider_data);
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

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(new Entry(i, (float)(int) data[i].get()));
        }


        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setColor(slider_data);
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