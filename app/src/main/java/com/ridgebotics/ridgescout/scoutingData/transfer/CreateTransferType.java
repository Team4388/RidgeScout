package com.ridgebotics.ridgescout.scoutingData.transfer;

public class CreateTransferType extends TransferType {
    public transferValue getType() {return transferValue.CREATE;}
    public CreateTransferType(String UUID){
        super(UUID);
    }
}