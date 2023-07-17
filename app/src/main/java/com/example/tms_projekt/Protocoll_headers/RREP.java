package com.example.tms_projekt.Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.getOrigSeqNumInHex;
import static com.example.tms_projekt.GlobalFunctions.getTargetNodeFromRoutingTable;
import static com.example.tms_projekt.GlobalFunctions.increaseHopCountForIncomingRREQAndRREP;

import com.example.tms_projekt.GlobalFunctions;

public class RREP {

    // destAddr is same destAddr as in incoming RREQ, same applies to origAddr. Consequently,
    // destSeqNum is current own SeqNum. Can also be SeqNum of someone on list if self isn't
    // target of RREQ but target is known and SeqNum of RREQ is older than SeqNum of target in RT.

    private String type; // Value: '1', Number of Characters: 1
    private String hopCount; // Value: 0-255, Number of Characters: 2
    private String destAddr; // Value: 0-65536, Number of Characters: 4
    private String destSeqNum; // Value: 0-65536, Number of Characters: 4
    private String origAddr; // Value: 0-65536, Number of Characters: 4


    // TODO: Bei Incoming RREQ prüfen, ob man selbst das Target ist oder target kennt. Ansonsten Broadcast. SeqNum in RREP entsprechend setzen.
    public RREP (String incomingRREP) {
        type = incomingRREP.substring(0,1);
        hopCount = increaseHopCountForIncomingRREQAndRREP(incomingRREP.substring(1,3));
        destAddr = incomingRREP.substring(3,7);
        destSeqNum = incomingRREP.substring(7,11);
        origAddr = incomingRREP.substring(11,15);
    }

    public RREP (String destAddr, String origAddr) {
        if (destAddr.equals(GlobalFunctions.origAddr)) {
            type = MessageType.RREP_t.getType();
            hopCount = "0";
            this.destAddr = destAddr;
            destSeqNum = getOrigSeqNumInHex();
            this.origAddr = origAddr;
        } else {
            type = MessageType.RREP_t.getType();
            hopCount = getTargetNodeFromRoutingTable(destAddr)[2];
            this.destAddr = destAddr;
            destSeqNum = getTargetNodeFromRoutingTable(destAddr)[3];
            this.origAddr = origAddr;
        }
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

    public String buildRREP() {
        return type + hopCount + destAddr + destSeqNum + origAddr;
    }

}
