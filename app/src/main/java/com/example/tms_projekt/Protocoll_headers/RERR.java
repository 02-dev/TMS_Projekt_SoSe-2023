package com.example.tms_projekt.Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;

import java.util.Arrays;

public class RERR {
    private byte[] type;
    private byte[] destCount;
    private byte[] unreachDestAddr;
    private byte[] unreachDestSeqNum;

    public RERR (byte[] incomingRERR) {
        type[0] = incomingRERR[0];
        destCount = Arrays.copyOfRange(incomingRERR, 1, 2);
        unreachDestAddr = Arrays.copyOfRange(incomingRERR, 3, 6);
        unreachDestSeqNum = Arrays.copyOfRange(incomingRERR, 7, 10);
    }

    public RERR (String targetNode) {
        type = asciiToByte(MessageType.RERR_t.getType());
        //TODO:
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getDestCount() {
        return destCount;
    }

    public byte[] getUnreachDestAddr() {
        return unreachDestAddr;
    }

    public byte[] getUnreachDestSeqNum() {
        return unreachDestSeqNum;
    }
}
