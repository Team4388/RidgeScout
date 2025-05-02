package com.ridgebotics.ridgescout.scoutingData.transfer;

import com.ridgebotics.ridgescout.types.input.inputType;

public abstract class transferType {
    public enum transferValue {
        DIRECT,
        CREATE
    }
    public String name;
    public abstract transferValue getType();
    public transferType(String name){
        this.name = name;
    }

    private static inputType get_input_type_by_name(inputType[] values, String name){
        for(inputType it : values){
            if(it.name.equals(name)){
                return it;
            }
        }
        return null;
    }

    public static transferType[][] get_transfer_values(inputType[][] values) {
        transferType[][] output = new transferType[values.length][];
        for(int a = 1; a < values.length; a++){
            transferType[] v = new transferType[values[a].length];
            for(int b = 0; b < values[a].length; b++){
                String name = values[a][b].name;
                if(get_input_type_by_name(values[a-1], name) != null){
                    v[b] = new directTransferType(name);
                }else{
                    v[b] = new createTransferType(name);
                }
            }
            output[a-1] = v;
        }
        return output;
    }
}