package com.ridgebotics.ridgescout.types.data;

public abstract class dataType {
    public enum valueTypes {
        NUM,
        NUMARR,
        STRING,
    }

    private Object value;
    private final String name;

    public abstract valueTypes getValueType();

    public Object forceGetValue(){return value;}
    public void forceSetValue(Object value){this.value = value;}

    public abstract Object get();
    public abstract void set(Object value);

//    public abstract Object getNullValue();
//    public abstract Object getUnselectedValue();

    public abstract boolean isNull();
//    public abstract boolean isUnselected();

    public String getName() {return name;}

    public dataType(String name){
        this.name = name;
    }
}