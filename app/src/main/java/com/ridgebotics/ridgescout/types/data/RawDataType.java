package com.ridgebotics.ridgescout.types.data;

// Abstract class for raw data types for use in fields.
public abstract class RawDataType {
    public enum valueTypes {
        NUM,
        NUMARR,
        STRING,
    }

    private Object value;
    private final String UUID;

    public abstract valueTypes getValueType();

    public Object forceGetValue(){return value;}
    public void forceSetValue(Object value){this.value = value;}

    public abstract Object get();
    public abstract void set(Object value);

//    public abstract Object getNullValue();
//    public abstract Object getUnselectedValue();

    public abstract boolean isNull();
//    public abstract boolean isUnselected();

    public String getUUID() {return UUID;}

    public RawDataType(String UUID){
        this.UUID = UUID;
    }
}