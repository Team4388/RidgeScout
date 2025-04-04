package com.ridgebotics.ridgescout.ui.settings;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ridgebotics.ridgescout.types.input.CheckboxType;
import com.ridgebotics.ridgescout.types.input.DropdownType;
import com.ridgebotics.ridgescout.types.input.FieldposType;
import com.ridgebotics.ridgescout.types.input.FieldType;
import com.ridgebotics.ridgescout.types.input.NumberType;
import com.ridgebotics.ridgescout.types.input.SliderType;
import com.ridgebotics.ridgescout.types.input.TallyType;
import com.ridgebotics.ridgescout.types.input.TextType;
import com.ridgebotics.ridgescout.utility.AlertManager;

import java.lang.reflect.Field;
import java.util.UUID;

public class FieldEditorHelper {
    private enum parameterTypeEnum {
        paramNumber,
        paramString,
        paramStringArray,
        paramNumberArray
    }

    public static class parameterType {
        public String name;
        public parameterTypeEnum id;
    }

    public static class paramNumber extends parameterType{
        public int val;
        public paramNumber(String name, int val){
            this.name = name + " (Number)";
            this.val = val;
            this.id = parameterTypeEnum.paramNumber;
        }
    }

    public static class paramString extends parameterType {
        public String val;
        public paramString(String name, String val){
            this.name = name + " (String)";
            this.val = val;
            this.id = parameterTypeEnum.paramString;
        }
    }

    public static class paramStringArray extends parameterType{
        public String[] val;
        public paramStringArray(String name, String[] val){
            this.name = name + " (String array)";
            this.val = val;
            this.id = parameterTypeEnum.paramStringArray;
        }
    }

//    public static class paramNumberArray extends parameterType{
//        public int[] val;
//        public paramNumberArray(String name, int[] val){
//            this.name = name + " (Number array)";
//            this.val = val;
//            this.id = parameterTypeEnum.paramNumberArray;
//        }
//    }

    public static final parameterType[] defaultSliderParams = new parameterType[]{
            new paramString("Name", "New Slider"),
            new paramString("Description", ""),
            new paramNumber("Min", 0),
            new paramNumber("Max", 10),
            new paramNumber("Default Value", 5)
    };
    public static final parameterType[] defaultDropdownParams = new parameterType[]{
            new paramString("Name", "New Dropdown"),
            new paramString("Description", ""),
            new paramStringArray("Default Value", new String[]{"Zero","One","Two","Three"}),
            new paramNumber("Default Option", 0),
    };
    public static final parameterType[] defaultTextParams = new parameterType[]{
            new paramString("Name", "New Text"),
            new paramString("Description", ""),
            new paramString("Default Value", "")
    };
    public static final parameterType[] defaultTallyParams = new parameterType[]{
            new paramString("Name", "New Tally"),
            new paramString("Description", ""),
            new paramNumber("Default Value", 0)
    };
    public static final parameterType[] defaultNumberParams = new parameterType[]{
            new paramString("Name", "New Number"),
            new paramString("Description", ""),
            new paramNumber("Default Value", 0)
    };
    public static final parameterType[] defaultCheckboxParam = new parameterType[]{
            new paramString("Name", "New Checkbox"),
            new paramString("Description", ""),
            new paramNumber("Default Value ( 1 or 0 )", 0)
    };
    public static final parameterType[] defaultFieldPosParam = new parameterType[]{
            new paramString("Name", "New Field Position"),
            new paramString("Description", ""),
            new paramNumber("Default X", 0),
            new paramNumber("Default Y", 0)
    };


    private static parameterType[] getSliderParams(SliderType s){
        return new parameterType[]{
            new paramString("Name", s.name),
            new paramString("Description", s.description),
            new paramNumber("Min", s.min),
            new paramNumber("Max", s.max),
            new paramNumber("Default Value", (int) s.default_value)
        };
    }

    private static parameterType[] getDropdownParams(DropdownType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramStringArray("Default Value",s.text_options),
                new paramNumber("Default Option", (int) s.default_value),
        };
    }

    private static parameterType[] getTextParams(TextType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramString("Default Value", (String) s.default_value)
        };
    }

    private static parameterType[] getTallyParams(TallyType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramNumber("Default Value", (int) s.default_value)
        };
    }

    private static parameterType[] getNumberParams(NumberType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramNumber("Default Value", (int) s.default_value)
        };
    }

    private static parameterType[] getCheckboxParam(CheckboxType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramNumber("Default Value ( 1 or 0 )", (int) s.default_value)
        };
    }

    private static parameterType[] getFieldPosParam(FieldposType s){
        return new parameterType[]{
                new paramString("Name", s.name),
                new paramString("Description", s.description),
                new paramNumber("Default X", ((int[]) s.default_value)[0]),
                new paramNumber("Default Y", ((int[]) s.default_value)[1])
        };
    }



    public static void setSliderParams(SliderType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.min = ((paramNumber) types[2]).val;
        s.max = ((paramNumber) types[3]).val;
        s.default_value = ((paramNumber) types[4]).val;
    }

    public static void setDropdownParams(DropdownType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.text_options = ((paramStringArray) types[2]).val;
        s.default_value = ((paramNumber) types[3]).val;
    }

    public static void setTextParams(TextType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.default_value = ((paramString) types[2]).val;
    }

    public static void setTallyParams(TallyType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.default_value = ((paramNumber) types[2]).val;
    }

    public static void setNumberParams(NumberType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.default_value = ((paramNumber) types[2]).val;
    }

    public static void setCheckboxParam(CheckboxType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.default_value = ((paramNumber) types[2]).val;
    }

    public static void setFieldPosParam(FieldposType s, parameterType[] types){
        s.name = ((paramString) types[0]).val;
        s.description = ((paramString) types[1]).val;
        s.default_value = new int[]{
                ((paramNumber) types[2]).val,
                ((paramNumber) types[3]).val
        };
    }


    private static void setInputParameter(FieldType t, parameterType[] types){
        switch (t.getInputType()){
            case TALLY:
                setTallyParams((TallyType) t, types);
                break;
            case SLIDER:
                setSliderParams((SliderType) t, types);
                break;
            case DROPDOWN:
                setDropdownParams((DropdownType) t, types);
                break;
            case NOTES_INPUT:
                setTextParams((TextType) t, types);
                break;
            case NUMBER:
                setNumberParams((NumberType) t, types);
                break;
            case CHECKBOX:
                setCheckboxParam((CheckboxType) t, types);
                break;
            case FIELDPOS:
                setFieldPosParam((FieldposType) t, types);
                break;
        }
    }



    private static parameterType[] getParamsFromInputType(FieldType t){
        switch (t.getInputType()){
            case TALLY:
                return getTallyParams((TallyType) t);
            case SLIDER:
                return getSliderParams((SliderType) t);
            case DROPDOWN:
                return getDropdownParams((DropdownType) t);
            case NOTES_INPUT:
                return getTextParams((TextType) t);
            case NUMBER:
                return getNumberParams((NumberType) t);
            case CHECKBOX:
                return getCheckboxParam((CheckboxType) t);
            case FIELDPOS:
                return getFieldPosParam((FieldposType) t);
        }
        return new parameterType[]{};
    }



    private static View createNumberEdit(Context c, int value){
        EditText text = new EditText(c);
        text.setInputType(TYPE_CLASS_NUMBER);
        text.setText(String.valueOf(value));
        text.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return text;
    }

    private static View createStringEdit(Context c, String value){
        EditText text = new EditText(c);
        text.setText(value);
        text.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return text;
    }

    private static View createStringArrayEdit(Context c, String[] value){
        EditText text = new EditText(c);
        text.setText(String.join("\n", value));
        text.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return text;
    }

    private static View createEdit(Context c, parameterType t){
        switch (t.id){
            case paramNumber:
                return createNumberEdit(c, ((paramNumber) t).val);
            case paramString:
                return createStringEdit(c, ((paramString) t).val);
            case paramStringArray:
                return createStringArrayEdit(c, ((paramStringArray) t).val);
        }
        return null;
    }


    private static boolean readEdit(View v, parameterType t){
        try{
            String val;
            switch (t.id) {
                case paramNumber:
                    val = ((EditText) v).getText().toString();
                    if(val.isEmpty() || val.isBlank()) return false;
                    ((paramNumber) t).val = Integer.parseInt(val);
                    break;
                case paramString:
                    val = ((EditText) v).getText().toString();
                    //if(val.isEmpty() || val.isBlank()) return false;
                    ((paramString) t).val = val;
                    break;
                case paramStringArray:
                    val = ((EditText) v).getText().toString();
                    if(val.isEmpty() || val.isBlank()) return false;
                    ((paramStringArray) t).val = val.split("\n");
                    break;
            }
        } catch (Exception e) {
            AlertManager.error(e);
            return false;
        }

        return true;
    }

    public static FieldType createNewFieldType(int n){
        switch(n){
            case 0:
                SliderType slider = new SliderType();
                slider.UUID = UUID.randomUUID().toString();
                setSliderParams(slider, defaultSliderParams);
                return slider;
            case 1:
                TextType textType = new TextType();
                textType.UUID = UUID.randomUUID().toString();
                setTextParams(textType, defaultTextParams);
                return textType;
            case 2:
                DropdownType dropdownType = new DropdownType();
                dropdownType.UUID = UUID.randomUUID().toString();
                setDropdownParams(dropdownType, defaultDropdownParams);
                return dropdownType;
            case 3:
                TallyType tallyType = new TallyType();
                tallyType.UUID = UUID.randomUUID().toString();
                setTallyParams(tallyType, defaultTallyParams);
                return tallyType;
            case 4:
                NumberType numberType = new NumberType();
                numberType.UUID = UUID.randomUUID().toString();
                setNumberParams(numberType, defaultNumberParams);
                return numberType;
            case 5:
                CheckboxType checkboxType = new CheckboxType();
                checkboxType.UUID = UUID.randomUUID().toString();
                setCheckboxParam(checkboxType, defaultCheckboxParam);
                return checkboxType;
            case 6:
                FieldposType fieldposType = new FieldposType();
                fieldposType.UUID = UUID.randomUUID().toString();
                setFieldPosParam(fieldposType, defaultFieldPosParam);
                return fieldposType;
        }
        return null;
    }


    private parameterType[] types;
    private View[] views;
    private FieldType t;
    public FieldEditorHelper(Context c, FieldType t, TableLayout parentView, parameterType[] tmptypes){
        this.types = tmptypes;
        this.t = t;
        views = new View[types.length];
        for(int i = 0; i < types.length; i++){
            TextView tv = new TextView(c);
            tv.setText(types[i].name);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(20);
            parentView.addView(tv);

            views[i] = createEdit(c, types[i]);
            parentView.addView(views[i]);
        }
    }
    public FieldEditorHelper(Context c, FieldType t, TableLayout parentView){
        this(c,t,parentView,getParamsFromInputType(t));
    }

    public boolean save(){
        for(int i = 0; i < types.length; i++){
            if(!readEdit(views[i], types[i]))
                return false;
        }
        setInputParameter(t, types);
        return true;
    }

}
