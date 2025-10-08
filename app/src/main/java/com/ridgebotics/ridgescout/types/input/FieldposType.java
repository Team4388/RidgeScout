package com.ridgebotics.ridgescout.types.input;

import static com.ridgebotics.ridgescout.utility.Colors.chart_background;
import static com.ridgebotics.ridgescout.utility.Colors.chart_text;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_1;
import static com.ridgebotics.ridgescout.utility.Colors.dropdown_value_text_2;
import static com.ridgebotics.ridgescout.utility.Colors.fieldpos_data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.data.IntArrType;
import com.ridgebotics.ridgescout.ui.views.FieldPosView;
import com.ridgebotics.ridgescout.ui.views.MultiFieldPosView;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FieldposType extends FieldType {
    public static final FieldImage DEFAULT_FIELD_IMAGE = FieldImage.F2025;
    public enum FieldImage {
        F2025(0, "2025", R.drawable.field_2025, R.drawable.field_2025_flipped),
        F2025_analogous(1, "2025 - analogous", R.drawable.field_2025_analogous);


        public int index, resId_normal, resId_flipped;
        public String name;
        public boolean flippable;

        FieldImage(int index, String name, int resId) {
            this.index = index;
            this.name = name;
            this.resId_normal = resId;
            this.resId_flipped = resId;
            this.flippable = false;
        }

        FieldImage(int index, String name, int resId_normal, int resId_flipped) {
            this.index = index;
            this.name = name;
            this.resId_normal = resId_normal;
            this.resId_flipped = resId_flipped;
            this.flippable = true;
        }

        public static FieldImage from_index(int index) {
            return FieldImage.values()[index];
        }

        public int get_index() {
            return this.index;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public FieldImage fieldImage;


    public int get_byte_id() {return fieldposType;}
    public inputTypes getInputType(){return inputTypes.FIELDPOS;}
    public RawDataType.valueTypes getValueType(){return RawDataType.valueTypes.NUM;}
    public Object get_fallback_value(){return 0;}
    public FieldposType(){}
    public String get_type_name(){return "Field Pos";}
    public FieldposType(String UUID, String name, String description, FieldImage fieldImage, int[] default_value){
        super(UUID, name, description);
        this.fieldImage = fieldImage;
        this.default_value = default_value;
    }





    public void encodeData(ByteBuilder bb) throws ByteBuilder.buildingException {
        bb.addInt(this.fieldImage.get_index());
        bb.addIntArray((int[]) default_value);
    }

    public void decodeData(ArrayList<BuiltByteParser.parsedObject> objects) {
        fieldImage = FieldImage.from_index((int) objects.get(0).get());
        default_value = objects.get(1).get();

    }





    public FieldPosView field = null;

    public View createView(Context context, Function<RawDataType, Integer> onUpdate){
        field = new FieldPosView(context, pos -> {
            onUpdate.apply(new IntArrType(name, pos));
        });
        setViewValue(default_value);
        field.setFieldImage(fieldImage);
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
    public RawDataType getViewValue(){
        if(field == null) return null;
        if(field.getVisibility() == View.GONE) return IntArrType.newNull(name);
        return new IntArrType(name, field.getPos());
    }



    public void add_individual_view(LinearLayout parent, RawDataType data){
        if(data.isNull()) return;

        FieldPosView fp = new FieldPosView(parent.getContext());
        fp.setEnabled(false);
        fp.setPos((int[]) data.get());
        fp.setFieldImage(this.fieldImage);

        parent.addView(fp);
    }








    public void add_compiled_view(LinearLayout parent, RawDataType[] data){
        MultiFieldPosView mfp = new MultiFieldPosView(parent.getContext());
        for(int i = 0; i < data.length; i++){
            if(data[i].isNull()) continue;
            mfp.addPos((int[]) data[i].get());
        }
        mfp.setFieldImage(fieldImage);
        parent.addView(mfp);
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

        int min = 0;
        int max = 255;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if(data[i] == null) continue;
            if(data[i].isNull()) continue;

            entries.add(new Entry(i, 255-(float)((int[]) data[i].get())[1]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Field position Y value");
        dataSet.setColor(fieldpos_data);
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

    public void addDataToTable(TableLayout parent, Map<Integer, List<RawDataType>> data){

    }

    public String toString(RawDataType data){
        int[] intarr = (int[]) data.get();
        return "[" + intarr[0] + "," + intarr[1] + "]";
    }
}

