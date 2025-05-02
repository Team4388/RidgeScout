package com.ridgebotics.ridgescout.scoutingData.transfer;

public class createTransferType extends transferType {
    public transferValue getType() {return transferValue.CREATE;}
    public createTransferType(String name){
        super(name);
    }
}