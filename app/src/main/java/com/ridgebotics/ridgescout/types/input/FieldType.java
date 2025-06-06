package com.ridgebotics.ridgescout.types.input;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// Abstract class for fields.
public abstract class FieldType {
    // Define what the IDS are for each type
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
    public abstract RawDataType.valueTypes getValueType();
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

    public abstract View createView(Context context, Function<RawDataType, Integer> onUpdate);
    public boolean isBlank = false;
    public abstract void nullify();
    public void setViewValue(RawDataType type){setViewValue(type.get());}
    public abstract void setViewValue(Object value);
    public abstract RawDataType getViewValue();



    public abstract void add_individual_view(LinearLayout parent, RawDataType data);
    public abstract void add_compiled_view(LinearLayout parent, RawDataType[] data);
    public abstract void add_history_view(LinearLayout parent, RawDataType[] data);


    public abstract void addDataToTable(TableLayout parent, Map<Integer, List<RawDataType>> data);


    public abstract String toString(RawDataType data);


}