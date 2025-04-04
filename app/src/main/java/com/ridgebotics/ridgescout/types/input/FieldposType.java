package com.ridgebotics.ridgescout.types.input;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntArrType;
import com.ridgebotics.ridgescout.ui.scouting.FieldPosView;
import com.ridgebotics.ridgescout.ui.scouting.MultiFieldPosView;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FieldposType extends FieldType {
    public int get_byte_id() {return fieldposType;}
    public inputTypes getInputType(){return inputTypes.FIELDPOS;}
    public DataType.valueTypes getValueType(){return DataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public FieldposType(){}
    public String get_type_name(){return "Field Pos";}
    public FieldposType(String UUID, String name, String description, int[] default_value){
        super(UUID, name, description);
        this.default_value = default_value;
    }





    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addIntArray((int[]) default_value);
    }

    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        default_value = objects.get(0).get();
    }





    public FieldPosView field = null;

    public View createView(Context context, Function<DataType, Integer> onUpdate){
        field = new FieldPosView(context, pos -> {
            onUpdate.apply(new IntArrType(name, pos));
        });
        setViewValue(default_value);
        return field;

    }

    public void setViewValue(Object value) {
        if(field == null) return;
        if(IntArrType.isNull((int[]) value)){
            nullify();
            return;
        }
        if(((int[]) value)[0] == 255 && ((int[]) value)[1] == 255){
            nullify();
            return;
        }

        isBlank = false;
        field.setVisibility(View.VISIBLE);
        field.setPos((int[]) value);
    }
    public void nullify(){
        isBlank = true;
        field.setVisibility(View.GONE);
    }
    public DataType getViewValue(){
        if(field == null) return null;
        if(field.getVisibility() == View.GONE) return IntArrType.newNull(name);
        return new IntArrType(name, field.getPos());
    }



    public void add_individual_view(LinearLayout parent, DataType data){
        if(data.isNull()) return;

        FieldPosView fp = new FieldPosView(parent.getContext());
        fp.setEnabled(false);
        fp.setPos((int[]) data.get());

        parent.addView(fp);
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
        MultiFieldPosView mfp = new MultiFieldPosView(parent.getContext());
        for(int i = 0; i < data.length; i++){
            if(data[i].isNull()) continue;
            mfp.addPos((int[]) data[i].get());
        }
        parent.addView(mfp);
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

        int min = 0;
        int max = 255;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(new Entry(i, 255-(float)((int[]) data[i].get())[1]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Field position Y value");
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

    public void addDataToTable(LinearLayout parent, List<DataType>[] data){

    }

    public String toString(DataType data){
        int[] intarr = (int[]) data.get();
        return "[" + intarr[0] + "," + intarr[1] + "]";
    }
}

