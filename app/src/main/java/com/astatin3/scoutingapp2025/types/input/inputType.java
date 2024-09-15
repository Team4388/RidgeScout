package com.astatin3.scoutingapp2025.types.input;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.astatin3.scoutingapp2025.types.data.dataType;
import com.astatin3.scoutingapp2025.ui.data.FieldEditorHelper;
import com.astatin3.scoutingapp2025.utility.BuiltByteParser;
import com.astatin3.scoutingapp2025.utility.ByteBuilder;

import java.util.function.Function;

public abstract class inputType {
    public static final int slider_type_id = 255;
    public static final int dropdownType = 254;
    public static final int notesType = 253;
    public static final int tallyType = 252;
    public enum inputTypes {
        SLIDER,
        DROPDOWN,
        NOTES_INPUT,
        TALLY
    }
    public String name;
    public Object default_value;
    public abstract inputTypes getInputType();
    public abstract dataType.valueTypes getValueType();
    public abstract Object get_fallback_value();
    public abstract int get_byte_id();
    public inputType(){}
    public inputType(String name){
        this.name = name;
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



//    private enum parameterTypeEnum {
//        paramNumber,
//        paramString,
//        paramStringArray
//    }
//
//    public static class parameterType {
//        public String name;
//        public parameterTypeEnum id;
//    }
//
//    public static class paramNumber extends parameterType {
//        public int val;
//        public paramNumber(String name, int val){
//            this.name = name + " (Number)";
//            this.val = val;
//            this.id = parameterTypeEnum.paramNumber;
//        }
//    }
//
//    public static class paramString extends parameterType {
//        public String val;
//        public paramString(String name, String val){
//            this.name = name + " (String)";
//            this.val = val;
//            this.id = parameterTypeEnum.paramString;
//        }
//    }
//
//    public static class paramStringArray extends parameterType {
//        public String[] val;
//        public paramStringArray(String name, String[] val){
//            this.name = name + " (String array)";
//            this.val = val;
//            this.id = parameterTypeEnum.paramStringArray;
//        }
//    }

//    public abstract parameterType[] getDefaultParameters();


    public abstract void add_individual_view(LinearLayout parent, dataType data);


    public abstract void add_compiled_view(LinearLayout parent, dataType[] data);


    public abstract void add_history_view(LinearLayout parent, dataType[] data);
}