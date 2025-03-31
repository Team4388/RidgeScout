package com.ridgebotics.ridgescout.types.input;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.List;
import java.util.function.Function;

public abstract class inputType {
    public static final int slider_type_id = 255;
    public static final int dropdownType = 254;
    public static final int notesType = 253;
    public static final int tallyType = 252;
    public static final int numberType = 251;
    public static final int checkboxType = 250;
    public static final int fieldposType = 249;

    public enum inputTypes {
        SLIDER,
        DROPDOWN,
        NOTES_INPUT,
        TALLY,
        NUMBER,
        CHECKBOX,
        FIELDPOS
    }
    public String name;
    public String description;
    public Object default_value;
    public abstract inputTypes getInputType();
    public abstract dataType.valueTypes getValueType();
    public abstract Object get_fallback_value();
    public abstract int get_byte_id();
    public inputType(){}
    public inputType(String name, String description){
        this.name = name;
        this.description = description;
    }

    public abstract String get_type_name();

    public abstract byte[] encode() throws ByteBuilder.buildingException;
    public abstract void decode(byte[] bytes) throws BuiltByteParser.byteParsingExeption;

//    public abstract dataType[] getConfig();
//    public abstract void setConfig(dataType[] config);

    public abstract View createView(Context context, Function<dataType, Integer> onUpdate);
    public boolean isBlank = false;
    public abstract void nullify();
    public void setViewValue(dataType type){setViewValue(type.get());}
    public abstract void setViewValue(Object value);
    public abstract dataType getViewValue();



    public abstract void add_individual_view(LinearLayout parent, dataType data);
    public abstract void add_compiled_view(LinearLayout parent, dataType[] data);
    public abstract void add_history_view(LinearLayout parent, dataType[] data);


    public abstract void addDataToTable(LinearLayout parent, List<dataType>[] data);


    public abstract String toString(dataType data);


    public int[] getNumberBounds(List<dataType>[] data){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int teamNum = 0; teamNum < data.length; teamNum++){
            if(data[teamNum] == null) continue;
            for(int i = 0; i < data[teamNum].size(); i++){
                dataType dataPoint = data[teamNum].get(i);
                if(dataPoint == null || dataPoint.getValueType() != getValueType()) continue;
                int num = (int) dataPoint.get();
                if(num > max) max = num;
                if(num < min) min = num;
            }
        }

        return new int[]{min, max};
    }


}