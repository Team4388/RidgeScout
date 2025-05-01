package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.scoutingData.transfer.createTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.directTransferType;
import com.ridgebotics.ridgescout.scoutingData.transfer.transferType;
import com.ridgebotics.ridgescout.types.data.dataType;
import com.ridgebotics.ridgescout.types.data.intType;
import com.ridgebotics.ridgescout.types.data.stringType;
import com.ridgebotics.ridgescout.types.input.inputType;

public class ScoutingArray {
    public int version;
    public dataType[] array;
    public inputType[][] values;
    public int latest_version_num;
    public transferType[][] transfer_values;

    public ScoutingArray(int version, dataType[] array, inputType[][] values, transferType[][] transfer_values){
        this.version = version;
        this.array = array;
        this.values = values;
        this.latest_version_num = values.length-1;
        this.transfer_values = transfer_values;
    }

    public ScoutingArray(int version, dataType[] array, inputType[][] values){
        this(version, array, values, transferType.get_transfer_values(values));
    }

    public void update(){
        while(version<latest_version_num){
            dataType[] new_values = new dataType[transfer_values[version].length];
            for(int i = 0; i < transfer_values[version].length; i++){
                transferType tv = transfer_values[version][i];
                switch (tv.getType()){
                    case DIRECT:
                        new_values[i] = direct_transfer((directTransferType) tv);
                        continue;
//                        case RENAME:
//                            new_values[i] = rename_transfer((renameTransferType) tv);
//                            continue;
                    case CREATE:
                        new_values[i] = create_transfer((createTransferType) tv);
                        continue;
                }
            }
            this.array = new_values;
            version++;
            System.out.println("Updated to " + version);
        }
    }

    private inputType get_input_type_by_name(int version, String name){
        for(inputType it : values[version]){
            if(it.name.equals(name)){
                return it;
            }
        }
        return null;
    }

    private dataType get_data_type_by_name(String name){
        for(dataType dt : array){
            if(dt.getName().equals(name)){
                return dt;
            }
        }
        return null;
    }

    private dataType direct_transfer(directTransferType tv){
        return get_data_type_by_name(tv.name);
    }

//        private dataType rename_transfer(renameTransferType tv){
//            dataType dt = get_data_type_by_name(tv.name);
//            dt.name = tv.new_name;
//            return dt;
//        }

    private dataType create_transfer(createTransferType tv){
        inputType it = get_input_type_by_name(version+1, tv.name);
        switch (it.getValueType()){
            case NUM:
                return intType.newNull(it.name);
            case STRING:
                return stringType.newNull(it.name);
        }
        return null;
    }





}
