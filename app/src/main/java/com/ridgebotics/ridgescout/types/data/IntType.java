package com.ridgebotics.ridgescout.types.data;

// Number raw data type
public class IntType extends RawDataType {
    public static final int nullval = 255;
//    public static final int unselectedval = 1;

    public valueTypes getValueType() {
        return valueTypes.NUM;
    }

//    public Object getNullValue(){
//        return nullval;
//    }
//    public Object getUnselectedValue(){
//        return unselectedval;
//    }

    public Object get(){
        return (int) forceGetValue();
    }

    public void set(Object value){
        forceSetValue((int) value);
    }

    public IntType(String name, int value) {
        super(name);
        set(value);
    }

    public static IntType newNull(String name){
        final IntType a = new IntType(name, 0);
        a.forceSetValue(nullval);
        return a;
    }

//    public static intType newUnselected(String name){
//        final intType a = new intType(name, 0);
//        a.forceSetValue(unselectedval);
//        return a;
//    }

    public static boolean isNull(int obj){
        return obj == nullval;
    }
    public boolean isNull() {
        return isNull((int) forceGetValue());
    }

//    public static boolean isUnselected(int obj){
//        return obj == unselectedval;
//    }
//    public boolean isUnselected() {
//        return isUnselected((int) forceGetValue());
//    }
}