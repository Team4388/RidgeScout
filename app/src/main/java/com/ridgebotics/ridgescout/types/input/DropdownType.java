package com.ridgebotics.ridgescout.types.input;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.ui.CustomSpinnerView;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DropdownType extends FieldType {
    public String[] text_options;
    public int get_byte_id() {return dropdownType;}
    public inputTypes getInputType(){return inputTypes.DROPDOWN;}
    public DataType.valueTypes getValueType(){return DataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public DropdownType(){};
    public String get_type_name(){return "Dropdown";}
    public DropdownType(String UUID, String name, String description, String[] text_options, int defaultSelIndex){
        super(UUID, name, description);
        this.text_options = text_options;
        this.default_value = defaultSelIndex;
    }


    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt((int)default_value);
        bb.addStringArray(text_options);
    }
    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value =            objects.get(0).get();
        text_options  = (String[]) objects.get(1).get();
    }

    public CustomSpinnerView dropdown = null;

    public View createView(Context context, Function<DataType, Integer> onUpdate){
        dropdown = new CustomSpinnerView(context);

        ArrayList<String> iconSpinnerItems = new ArrayList<>(Arrays.asList(text_options));

        dropdown.setTitle(name);
        dropdown.setOptions(iconSpinnerItems, (int) default_value);
        onUpdate.apply(getViewValue());

        dropdown.setOnClickListener((item, index) -> onUpdate.apply(getViewValue()));

        return dropdown;

    }
    public void setViewValue(Object value) {
        if(dropdown == null) return;
        if(IntType.isNull((int) value)){
            nullify();
            return;
        }

        isBlank = false;

        dropdown.setVisibility(View.VISIBLE);
        dropdown.setOption((int) value);
    }
    public void nullify(){
        isBlank = true;
        dropdown.setVisibility(View.GONE);
    }
    public DataType getViewValue(){
        if(dropdown == null) return null;
        if(dropdown.getVisibility() == View.GONE) return new IntType(name, IntType.nullval);
        return new IntType(name, dropdown.getIndex());
    }






    public void add_individual_view(LinearLayout parent, DataType data){
        if(data.isNull()) return;
        TextView tv = new TextView(parent.getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setPadding(20,20,20,20);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(text_options[(int) data.get()]);
        tv.setTextSize(18);
        parent.addView(tv);
    }








    private static int[] generateEquidistantColors(int N) {
        int[] colors = new int[N];
        float[] hsv = new float[3]; // Hue, Saturation, Value

        for (int i = 0; i < N; i++) {
            float hue = i * 1.0F / N;
            hsv[0] = hue * 360; // Convert hue to degrees (0 to 360)
            hsv[1] = 1; // Maximum saturation
            hsv[2] = 1; // Maximum brightness (value)

            colors[i] = Color.HSVToColor(hsv);
        }
        return colors;
    }

    public void add_compiled_view(LinearLayout parent, DataType[] data){
        PieChart chart = new PieChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(0xff252025);
        parent.addView(chart);

        int[] data_2 = new int[text_options.length];
        for(int i = 0; i < data.length; i++)
            if(!data[i].isNull())
                data_2[(int) data[i].get()]++;

        List<PieEntry> entries = new ArrayList<>();
        for(int i = 0; i < data_2.length; i++) {
            PieEntry entry = new PieEntry((float) data_2[i], text_options[i]);
            entries.add(entry);
        }

        PieDataSet pieDataSet = new PieDataSet(entries, name);
        pieDataSet.setColors(generateEquidistantColors(text_options.length));
        PieData pieData = new PieData(pieDataSet);
        chart.setDrawHoleEnabled(false);
        chart.setData(pieData);
    }






    public void add_history_view(LinearLayout parent, DataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(0xff252025);



        int[] colors = generateEquidistantColors(text_options.length);

        LineData lineData = new LineData();

        for(int i = 0; i < text_options.length; i++){
            List<Entry> entries = new ArrayList<>();
            for (int a = 0; a < data.length; a++) {
                if(data[a].isNull()) continue;

                entries.add(
                        new Entry(a,
                            ((int) data[a].get()) == i ? 1.f : 0.f
                        )
                );
            }

            LineDataSet dataSet = new LineDataSet(entries, text_options[i]);
            dataSet.setColor(colors[i]);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setValueTextColor(Color.RED);
            lineData.addDataSet(dataSet);
        }




        chart.setData(lineData);
        chart.invalidate();

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);


        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setTextColor(Color.WHITE);

        chart.getAxisLeft().setAxisMinimum(0.f);
        chart.getAxisLeft().setAxisMaximum(1.f);

        chart.getAxisRight().setAxisMinimum(0.f);
        chart.getAxisRight().setAxisMaximum(1.f);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);

        chart.invalidate();
        parent.addView(chart);
    }

    public void addDataToTable(LinearLayout parent, List<DataType>[] data){

    }

    public String toString(DataType data){
        return text_options[(int) data.get()];
    }
}

