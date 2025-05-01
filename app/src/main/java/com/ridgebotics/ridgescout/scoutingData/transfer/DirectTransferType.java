package com.ridgebotics.ridgescout.scoutingData.transfer;

// Transfer type if a field directly transfers
public class DirectTransferType extends TransferType {
    public transferValue getType() {return transferValue.DIRECT;}
    public DirectTransferType(String UUID){
        super(UUID);
    }
}
