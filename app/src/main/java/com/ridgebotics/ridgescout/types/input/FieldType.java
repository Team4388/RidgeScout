package com.ridgebotics.ridgescout.types.input;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class FieldType {
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
    public String UUID;
    public String name;
    public String description;
    public Object default_value;
    public abstract inputTypes getInputType();
    public abstract DataType.valueTypes getValueType();
    public abstract Object get_fallback_value();
    public abstract int get_byte_id();
    public FieldType(){}
    public FieldType(String UUID, String name, String description){
        this.UUID = UUID;
        this.name = name;
        this.description = description;
    }

    public abstract String get_type_name();

    public byte[] encode() throws ByteBuilder.buildingException {
        ByteBuilder bb = new ByteBuilder();
        bb.addString(UUID);
        bb.addString(name);
        bb.addString(description);

        encodeData(bb);

        return bb.build();
    }


    public abstract void encodeData(ByteBuilder byteBuilder) throws ByteBuilder.buildingException;
    public void decode(byte[] bytes) throws BuiltByteParser.byteParsingExeption {
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        ArrayList<BuiltByteParser.parsedObject> objects = bbp.parse();

        UUID          = (String)   objects.remove(0).get();
        name          = (String)   objects.remove(0).get();

        description   = (String)   objects.remove(0).get();

        decodeData(objects);
    }


    public abstract void decodeData(ArrayList<BuiltByteParser.parsedObject> objects);

//    public abstract dataType[] getConfig();
//    public abstract void setConfig(dataType[] config);

    public abstract View createView(Context context, Function<DataType, Integer> onUpdate);
    public boolean isBlank = false;
    public abstract void nullify();
    public void setViewValue(DataType type){setViewValue(type.get());}
    public abstract void setViewValue(Object value);
    public abstract DataType getViewValue();



    public abstract void add_individual_view(LinearLayout parent, DataType data);
    public abstract void add_compiled_view(LinearLayout parent, DataType[] data);
    public abstract void add_history_view(LinearLayout parent, DataType[] data);


    public abstract void addDataToTable(LinearLayout parent, List<DataType>[] data);


    public abstract String toString(DataType data);


    public int[] getNumberBounds(List<DataType>[] data){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int teamNum = 0; teamNum < data.length; teamNum++){
            if(data[teamNum] == null) continue;
            for(int i = 0; i < data[teamNum].size(); i++){
                DataType dataPoint = data[teamNum].get(i);
                if(dataPoint == null || dataPoint.getValueType() != getValueType()) continue;
                int num = (int) dataPoint.get();
                if(num > max) max = num;
                if(num < min) min = num;
            }
        }

        return new int[]{min, max};
    }


}