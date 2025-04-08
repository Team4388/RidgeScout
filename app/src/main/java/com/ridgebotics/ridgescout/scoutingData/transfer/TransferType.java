package com.ridgebotics.ridgescout.scoutingData.transfer;

import com.ridgebotics.ridgescout.types.input.FieldType;

public abstract class TransferType {
    public enum transferValue {
        DIRECT,
        CREATE
    }
    public String UUID;
    public abstract transferValue getType();
    public TransferType(String UUID){
        this.UUID = UUID;
    }

    private static FieldType get_input_type_by_UUID(FieldType[] values, String UUID){
        for(FieldType it : values){
            if(it.UUID.equals(UUID)){
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
                String UUID = values[a][b].UUID;
                if(get_input_type_by_UUID(values[a-1], UUID) != null){
                    v[b] = new DirectTransferType(UUID);
                }else{
                    v[b] = new CreateTransferType(UUID);
                }
            }
            output[a-1] = v;
        }
        return output;
    }
}