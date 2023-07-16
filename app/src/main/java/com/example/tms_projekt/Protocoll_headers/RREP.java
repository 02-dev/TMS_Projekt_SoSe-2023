package com.example.tms_projekt.Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static com.example.tms_projekt.GlobalFunctions.getTargetNodeFromRoutingTable;
;

import com.example.tms_projekt.GlobalFunctions;

import java.util.Arrays;

public class RREP {

    // destAddr is same destAddr as in incoming RREQ, same applies to origAddr. Consequently,
    // destSeqNum is current own SeqNum. TODO: Can also be SeqNum of someone on list if self isn't
    // target of RREQ but target is known.

    private String type; // Value: '1', Number of Characters: 1
    private String hopCount; // Value: 0-255, Number of Characters: 2
    private String destAddr; // Value: 0-65536, Number of Characters: 4
    private String destSeqNum; // Value: 0-65536, Number of Characters: 4
    private String origAddr; // Value: 0-65536, Number of Characters: 4


    // TODO: Bei Incoming RREQ prüfen, ob man selbst das Target ist oder target kennt. Ansonsten Broadcast. SeqNum in RREP entsprechend setzen.
    public RREP (String incomingRREP) {
        type = incomingRREP.substring(0,1);
        hopCount = incomingRREP.substring(1,3);
        destAddr = incomingRREP.substring(3,7);
        destSeqNum = incomingRREP.substring(7,11);
        origAddr = incomingRREP.substring(11,15);
    }

    public RREP (String destAddr, String origAddr) {
        type = MessageType.RREP_t.getType();
        hopCount = "0";
        this.destAddr = destAddr;
        destSeqNum = String.valueOf(GlobalFunctions.origSeqNum);
        this.origAddr = origAddr;
    }

    public String getType() {
        return type;
    }

    public String getHopCount() {
        return hopCount;
    }

    public String getDestAddr() {
        return destAddr;
    }

    public String getDestSeqNum() {
        return destSeqNum;
    }

    public String getOrigAddr() {
        return origAddr;
    }

    public int getHopCountAsDecimal() { return Integer.parseInt(hopCount, 16); }

    public int getDestSeqNumAsDecimal() { return Integer.parseInt(destSeqNum, 16); }

}
