package com.ridgebotics.ridgescout.types.input;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.ui.CandlestickHeader;
import com.ridgebotics.ridgescout.ui.CandlestickView;
import com.ridgebotics.ridgescout.ui.data.DataProcessing;
import com.ridgebotics.ridgescout.ui.scouting.TallyCounterView;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TallyType extends FieldType {
    public int get_byte_id() {return tallyType;}
    public inputTypes getInputType(){return inputTypes.TALLY;}
    public DataType.valueTypes getValueType(){return DataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public TallyType(){}
    public String get_type_name(){return "Tally";}
    public TallyType(String UUID, String name, String description, int default_value){
        super(UUID, name, description);
        this.default_value = default_value;
    }





    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt((int) default_value);
    }
    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value = objects.get(0).get();
    }





    public TallyCounterView tally = null;

    public View createView(Context context, Function<DataType, Integer> onUpdate){
        tally = new TallyCounterView(context);
        tally.setOnCountChangedListener(n -> onUpdate.apply(getViewValue()));

        setViewValue(default_value);

        return tally;

    }

    public void setViewValue(Object value) {
        if(tally == null) return;
        if(IntType.isNull((int)value)){
            nullify();
            return;
        }

        isBlank = false;
        tally.setVisibility(View.VISIBLE);
        tally.setValue((int) value);
    }
    public void nullify(){
        isBlank = true;
        tally.setVisibility(View.GONE);
    }
    public DataType getViewValue(){
        if(tally == null) return null;
        if(tally.getVisibility() == View.GONE) return IntType.newNull(name);
        return new IntType(name, tally.getValue());
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




    public void add_history_view(LinearLayout parent, DataType[] data){
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

    public void addDataToTable(TableLayout parent, Map<Integer, List<DataType>> data){
        int[] tmp_abs_bounds = DataProcessing.getNumberBounds(data);
        int absmin = tmp_abs_bounds[0];
        int absmax = tmp_abs_bounds[1];

        //(int[]) teamData.get(i).get())[0];
//        AlertManager.alert("Results","Min: " + min + " Max: " + max);

        parent.removeAllViews();

        List<CandlestickView> views = new ArrayList<>();

        for(Integer teamNum : data.keySet()){
            CandlestickView candlestickView = new CandlestickView(parent.getContext());
            candlestickView.fromTeamData(data.get(teamNum), teamNum, absmin, absmax);
            views.add(candlestickView);
        }


        TableRow row = new TableRow(parent.getContext());

        // Make candlestick chart fill full width
        parent.setColumnStretchable(1, true);

        // Fill in top left cell
        row.addView(new View(parent.getContext()));

        CandlestickHeader header = new CandlestickHeader(parent.getContext());
        header.setScale(absmin, absmax);
        row.addView(header);

        parent.addView(row);

//        parent.addView(new );

        Collections.sort(views, (a, b) -> (int) ((b.average - a.average)*10.f));
        for(int i = 0; i < views.size(); i++){
            row = new TableRow(parent.getContext());
            CandlestickView view = views.get(i);

            TextView teamNum = new TextView(parent.getContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.gravity = Gravity.CENTER;
            teamNum.setLayoutParams(params);
            teamNum.setPadding(10,10,10,10);
            teamNum.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
            teamNum.setText(String.valueOf(view.teamNum));


            row.addView(teamNum);
            row.addView(view);

            parent.addView(row);
        }
    }

    public String toString(DataType data){
        return String.valueOf((int) data.get());
    }
}

