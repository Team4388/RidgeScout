package com.ridgebotics.ridgescout.scoutingData.transfer;

public class DirectTransferType extends TransferType {
    public transferValue getType() {return transferValue.DIRECT;}
    public DirectTransferType(String name){
        super(name);
    }
}
