package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.scoutingData.transfer.CreateTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.DirectTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.TransferType;
import com.ridgebotics.ridgescout.types.data.DataType;
import com.ridgebotics.ridgescout.types.data.IntType;
import com.ridgebotics.ridgescout.types.data.StringType;
import com.ridgebotics.ridgescout.types.input.FieldType;

public class ScoutingArray {
    public int version;
    public DataType[] array;
    public FieldType[][] values;
    public int latest_version_num;
    public TransferType[][] transfer_values;

    public ScoutingArray(int version, DataType[] array, FieldType[][] values, TransferType[][] transfer_values){
        this.version = version;
        this.array = array;
        this.values = values;
        this.latest_version_num = values.length-1;
        this.transfer_values = transfer_values;
    }

    public ScoutingArray(int version, DataType[] array, FieldType[][] values){
        this(version, array, values, TransferType.get_transfer_values(values));
    }

    public void update(){
        while(version<latest_version_num){
            DataType[] new_values = new DataType[transfer_values[version].length];
            for(int i = 0; i < transfer_values[version].length; i++){
                TransferType tv = transfer_values[version][i];
                switch (tv.getType()){
                    case DIRECT:
                        new_values[i] = direct_transfer((DirectTransferType) tv);
                        continue;
//                        case RENAME:
//                            new_values[i] = rename_transfer((renameTransferType) tv);
//                            continue;
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

    private FieldType get_input_type_by_name(int version, String name){
        for(FieldType it : values[version]){
            if(it.name.equals(name)){
                return it;
            }
        }
        return null;
    }

    private DataType get_data_type_by_name(String name){
        for(DataType dt : array){
            if(dt.getName().equals(name)){
                return dt;
            }
        }
        return null;
    }

    private DataType direct_transfer(DirectTransferType tv){
        return get_data_type_by_name(tv.name);
    }

//        private dataType rename_transfer(renameTransferType tv){
//            dataType dt = get_data_type_by_name(tv.name);
//            dt.name = tv.new_name;
//            return dt;
//        }

    private DataType create_transfer(CreateTransferType tv){
        FieldType it = get_input_type_by_name(version+1, tv.name);
        switch (it.getValueType()){
            case NUM:
                return IntType.newNull(it.name);
            case STRING:
                return StringType.newNull(it.name);
        }
        return null;
    }





}
