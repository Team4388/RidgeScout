package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.scoutingData.transfer.CreateTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.DirectTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.TransferType;
import com.ridgebotics.ridgescout.types.data.RawDataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.types.data.StringType;
import com.ridgebotics.ridgescout.types.input.FieldType;

// A wrapper class for the stuff going on in Fields.java and ScoutingDataWriter.java.
public class ScoutingArray {
    public int version;
    public RawDataType[] array;
    public FieldType[][] values;
    public int latest_version_num;
    public TransferType[][] transfer_values;

    public ScoutingArray(int version, RawDataType[] array, FieldType[][] values, TransferType[][] transfer_values){
        this.version = version;
        this.array = array;
        this.values = values;
        this.latest_version_num = values.length-1;
        this.transfer_values = transfer_values;
    }

    public ScoutingArray(int version, RawDataType[] array, FieldType[][] values){
        this(version, array, values, TransferType.get_transfer_values(values));
    }

    public void update(){
        while(version<latest_version_num){
            RawDataType[] new_values = new RawDataType[transfer_values[version].length];
            for(int i = 0; i < transfer_values[version].length; i++){
                TransferType tv = transfer_values[version][i];
                switch (tv.getType()){
                    case DIRECT:
                        new_values[i] = direct_transfer((DirectTransferType) tv);
                        continue;
                    case CREATE:
                        new_values[i] = create_transfer((CreateTransferType) tv);
                        continue;
                }
            }
            this.array = new_values;
            version++;
            System.out.println("Updated to " + version);
        }
    }

    private FieldType get_input_type_by_UUID(int version, String UUID){
        for(FieldType it : values[version]){
            if(it.UUID.equals(UUID)){
                return it;
            }
        }
        return null;
    }

    private RawDataType get_data_type_by_UUID(String UUID){
        for(RawDataType dt : array){
            if(dt.getUUID().equals(UUID)){
                return dt;
            }
        }
        return null;
    }

    private RawDataType direct_transfer(DirectTransferType tv){
        return get_data_type_by_UUID(tv.UUID);
    }

//        private dataType rename_transfer(renameTransferType tv){
//            dataType dt = get_data_type_by_name(tv.name);
//            dt.name = tv.new_name;
//            return dt;
//        }

    private RawDataType create_transfer(CreateTransferType tv){
        FieldType it = get_input_type_by_UUID(version+1, tv.UUID);
        switch (it.getValueType()){
            case NUM:
                return IntType.newNull(it.UUID);
            case STRING:
                return StringType.newNull(it.UUID);
        }
        return null;
    }





}
