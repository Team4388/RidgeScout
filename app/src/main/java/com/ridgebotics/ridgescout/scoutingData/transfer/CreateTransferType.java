package com.ridgebotics.ridgescout.scoutingData.transfer;

// Transfer type if a field was created
public class CreateTransferType extends TransferType {
    public transferValue getType() {return transferValue.CREATE;}
    public CreateTransferType(String UUID){
        super(UUID);
    }
}