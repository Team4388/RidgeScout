package com.ridgebotics.ridgescout.scoutingData.transfer;

import com.ridgebotics.ridgescout.types.input.FieldType;

public abstract class TransferType {
    public enum transferValue {
        DIRECT,
        CREATE
    }
    public String name;
    public abstract transferValue getType();
    public TransferType(String name){
        this.name = name;
    }

    private static FieldType get_input_type_by_name(FieldType[] values, String name){
        for(FieldType it : values){
            if(it.name.equals(name)){
                return it;
            }
        }
        return null;
    }

    public static TransferType[][] get_transfer_values(FieldType[][] values) {
        TransferType[][] output = new TransferType[values.length][];
        for(int a = 1; a < values.length; a++){
            TransferType[] v = new TransferType[values[a].length];
            for(int b = 0; b < values[a].length; b++){
                String name = values[a][b].name;
                if(get_input_type_by_name(values[a-1], name) != null){
                    v[b] = new DirectTransferType(name);
                }else{
                    v[b] = new CreateTransferType(name);
                }
            }
            output[a-1] = v;
        }
        return output;
    }
}