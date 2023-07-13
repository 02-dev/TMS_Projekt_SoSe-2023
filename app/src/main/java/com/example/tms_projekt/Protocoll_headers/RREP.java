package com.example.tms_projekt.Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static com.example.tms_projekt.GlobalFunctions.getTargetNodeFromRoutingTable;
;

import com.example.tms_projekt.GlobalFunctions;

import java.util.Arrays;

public class RREP {
    private byte[] type;
    private byte[] hopCount;
    private byte[] destAddr;
    private byte[] destSeqNum;
    private byte[] origAddr;

    public RREP (byte[] incomingRREP) {
        type[0] = incomingRREP[0];
        hopCount = Arrays.copyOfRange(incomingRREP, 1, 2);
        destAddr = Arrays.copyOfRange(incomingRREP, 3, 6);
        destSeqNum = Arrays.copyOfRange(incomingRREP, 7, 10);
        origAddr = Arrays.copyOfRange(incomingRREP, 11, 14);
    }

    public RREP (String targetNode) {
        type = asciiToByte(MessageType.RREP_t.getType());
        hopCount = asciiToByte(getTargetNodeFromRoutingTable(targetNode)[2]);
        destAddr = asciiToByte(targetNode);
        destSeqNum = asciiToByte(getTargetNodeFromRoutingTable(targetNode)[4]);
        origAddr = asciiToByte(GlobalFunctions.origAddr);
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getHopCount() {
        return hopCount;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public byte[] getDestSeqNum() {
        return destSeqNum;
    }

    public byte[] getOrigAddr() {
        return origAddr;
    }

}
