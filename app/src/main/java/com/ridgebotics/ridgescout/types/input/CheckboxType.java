package com.ridgebotics.ridgescout.types.input;

import static com.ridgebotics.ridgescout.utility.Colors.chart_background;
import static com.ridgebotics.ridgescout.utility.Colors.checkbox_colors;
import static com.ridgebotics.ridgescout.utility.Colors.checkbox_data_color;
import static com.ridgebotics.ridgescout.utility.Colors.checkbox_value_text_color;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CheckboxType extends FieldType {
    public int get_byte_id() {return checkboxType;}
    public inputTypes getInputType(){return inputTypes.CHECKBOX;}
    public RawDataType.valueTypes getValueType(){return RawDataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public CheckboxType(){};
    public String get_type_name(){return "Checkbox";}
    public CheckboxType(String UUID, String name, String description, int isChecked){
        super(UUID, name, description);
        this.default_value = isChecked;
    }


    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt((int)default_value);
    }

    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value =            objects.get(0).get();
    }

//    public PowerSpinnerView dropdown = null;

    public CheckBox checkBox = null;

    public View createView(Context context, Function<RawDataType, Integer> onUpdate){
        checkBox = new CheckBox(context);
        checkBox.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
        checkBox.setText(name);

        setViewValue(default_value);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> onUpdate.apply(getViewValue()));

        return checkBox;

    }
    public void setViewValue(Object value) {
        if(checkBox == null) return;
        if(IntType.isNull((int) value)){
            nullify();
            return;
        }

        isBlank = false;

        checkBox.setVisibility(View.VISIBLE);
        checkBox.setChecked((int) value == 1);
    }
    public void nullify(){
        isBlank = true;
        checkBox.setVisibility(View.GONE);
    }
    public RawDataType getViewValue(){
        if(checkBox == null) return null;
        if(checkBox.getVisibility() == View.GONE) return new IntType(name, IntType.nullval);
        return new IntType(name, checkBox.isChecked() ? 1 : 0);
    }






    public void add_individual_view(LinearLayout parent, RawDataType data){
        if(data.isNull()) return;
        CheckBox cb = new CheckBox(parent.getContext());
        cb.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
        cb.setText(name);
        cb.setChecked((int) data.get() == 1);
        cb.setEnabled(false);
        parent.addView(cb);
    }


    public void add_compiled_view(LinearLayout parent, RawDataType[] data){
        PieChart chart = new PieChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(0xff252025);
        parent.addView(chart);

        int numTrue = 0;
        int numFalse = 0;

        for(int i = 0; i < data.length; i++)
            if(!data[i].isNull()){
                if((int) data[i].get() == 1)
                    numTrue += 1;
                else
                    numFalse += 1;
            }


        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry((float) numTrue, "True"));
        entries.add(new PieEntry((float) numFalse, "False"));

        PieDataSet pieDataSet = new PieDataSet(entries, name);
        pieDataSet.setColors(checkbox_colors);
        PieData pieData = new PieData(pieDataSet);
        chart.setDrawHoleEnabled(false);
        chart.setData(pieData);
    }






    public void add_history_view(LinearLayout parent, RawDataType[] data){
        LineChart chart = new LineChart(parent.getContext());
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.height = 350;
        chart.setLayoutParams(layout);
        chart.setBackgroundColor(chart_background);

        LineData lineData = new LineData();

        List<Entry> entries = new ArrayList<>();
        for (int a = 0; a < data.length; a++) {
            if(data[a].isNull()) continue;

            entries.add(
                    new Entry(a,
                        ((int) data[a].get())
                    )
            );
        }

        LineDataSet dataSet = new LineDataSet(entries, "is checked");
        dataSet.setColor(checkbox_data_color);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setValueTextColor(checkbox_value_text_color);
        lineData.addDataSet(dataSet);




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

    public void addDataToTable(TableLayout parent, Map<Integer, List<RawDataType>> data){

    }

    public String toString(RawDataType data){
        return  (int) data.get() == 1 ? "true" : "false";
    }
}

