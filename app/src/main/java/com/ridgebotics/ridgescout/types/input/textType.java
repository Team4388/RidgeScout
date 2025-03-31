package com.ridgebotics.ridgescout.types.input;

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

import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.data.stringType;
import com.ridgebotics.ridgescout.utility.SentimentAnalysis;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class textType extends inputType {

    public int get_byte_id() {return notesType;}
    public inputTypes getInputType(){return inputTypes.NOTES_INPUT;}
    public dataType.valueTypes getValueType(){return dataType.valueTypes.STRING;}
    public Object get_fallback_value(){return "<no-notes>";}
    public textType(){}
    public textType(String name, String description, String default_text){
        super(name, description);
        this.default_value = default_text;
    }
    public String get_type_name(){return "Text";}







    public byte[] encode() throws ByteBuilder.buildingException {
        ByteBuilder bb = new ByteBuilder();
        bb.addString(name);
        bb.addString(description);
        bb.addString((String) default_value);
        return bb.build();
    }
    public void decode(byte[] bytes) throws BuiltByteParser.byteParsingExeption {
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();

        name          = (String)   objects.get(0).get();
        description   = (String)   objects.get(1).get();
        default_value =            objects.get(2).get();
    }














    public EditText text = null;

    public View createView(Context context, Function<dataType, Integer> onUpdate){
        text = new EditText(context);
        text.setText((String)default_value);
        text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                onUpdate.apply(getViewValue());                }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        return text;
    }
    public void setViewValue(Object value) {
        if(text == null) return;
        if(stringType.isNull((String) value)){
            nullify();
            return;
        }
        isBlank = false;
        text.setVisibility(View.VISIBLE);
        text.setText((String) value);
    }
    public void nullify(){
        isBlank = true;
        text.setVisibility(View.GONE);
    }
    public dataType getViewValue(){
        if(text == null) return null;
        if(text.getVisibility() == View.GONE) return new stringType(name, stringType.nullval);
        return new stringType(name, text.getText().toString());
    }




    public void add_individual_view(LinearLayout parent, dataType data){
        if(data.isNull()) return;
        TextView tv = new TextView(parent.getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText((String) data.get());
        tv.setTextSize(18);
        parent.addView(tv);
    }













    float positive_mean = 0;
    int count = 0;

    TextView positive_text;

    public void add_compiled_view(LinearLayout parent, dataType[] data) {
        positive_mean = 0;
        count = 0;

        positive_text = new TextView(parent.getContext());
        positive_text.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        positive_text.setGravity(Gravity.CENTER_HORIZONTAL);
        positive_text.setTextSize(20);
        parent.addView(positive_text);

        for (int i = 0; i < data.length; i++){
            if (!data[i].isNull()) {
                SentimentAnalysis.analyse((String) data[i].get(), new SentimentAnalysis.resultCallback() {
                    @Override
                    public void onFinish(float sentiment) {
                        positive_mean += sentiment;
                        count++;

                        positive_text.setText("Sentiment: " + (positive_mean / count));
                    }
                });
            }
        }
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


        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(
                    new Entry(i,
                            SentimentAnalysis.analyse_sync(  (String) data[i].get()  )
                    )
            );
        }
        LineDataSet dataSet = new LineDataSet(entries, "Sentiment");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setValueTextColor(Color.RED);


        LineData lineData = new LineData(dataSet);

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

    public void addDataToTable(LinearLayout parent, List<dataType>[] data){

    }

    public String toString(dataType data){
        return String.valueOf(data.get());
    }
}

