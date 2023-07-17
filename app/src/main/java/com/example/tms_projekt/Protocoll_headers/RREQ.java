package com.example.tms_projekt.Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.getOrigSeqNumInHex;
import static com.example.tms_projekt.GlobalFunctions.getRoutRequestIDInHex;
import static com.example.tms_projekt.GlobalFunctions.getTargetNodeFromRoutingTable;
import static com.example.tms_projekt.GlobalFunctions.increaseHopCountForIncomingRREQAndRREP;

import com.example.tms_projekt.GlobalFunctions;

public class RREQ {

    // RREQ are being broadcast to all known neighbors.
    private String type; // Value: ‘0’, Number of Characters: 1
    private String undefSeqNum; // Value: 'Y' for undefined, 'N' for defined, Number of Characters: 1
    private String hopCount; // Value: 0-255, Number of Characters: 2
    private String rreqID; // Value: 0-65536, Number of Characters: 4
    private String destAddr; // Value: 0-65536, Number of Characters: 4
    private String destSeqNum; // Value: 0-65536, Number of Characters: 4
    private String origAddr; // Value: 0-65536, Number of Characters: 4
    private String origSeqNum; // Value: 0-65536, Number of Characters: 4

    public RREQ (String incomingRREQ) {
        type = incomingRREQ.substring(0,1);
        undefSeqNum = incomingRREQ.substring(1,2);
        hopCount = increaseHopCountForIncomingRREQAndRREP(incomingRREQ.substring(2,4));
        rreqID = incomingRREQ.substring(4,8);
        destAddr = incomingRREQ.substring(8,12);
        destSeqNum = incomingRREQ.substring(12,16);
        origAddr = incomingRREQ.substring(16,20);
        origSeqNum = incomingRREQ.substring(20,24);
    }

    public RREQ (String targetNode, Boolean inRoutingTable) {
        if (inRoutingTable) {
            type = MessageType.RREQ_t.getType();
            undefSeqNum = "N";
            hopCount = "00";
            rreqID = getRoutRequestIDInHex();
            destAddr = targetNode;
            destSeqNum = getTargetNodeFromRoutingTable(targetNode)[3];
            origAddr = GlobalFunctions.origAddr;
            origSeqNum = getOrigSeqNumInHex();
        } else {
            type = MessageType.RREQ_t.getType();
            undefSeqNum = "Y";
            hopCount = "00";
            rreqID = getRoutRequestIDInHex();
            destAddr = targetNode;
            destSeqNum = "0000";
            origAddr = GlobalFunctions.origAddr;
            origSeqNum = getOrigSeqNumInHex();
        }
    }

    public String getType() {
        return type;
    }

    public String getUndefSeqNum() { return undefSeqNum; }

    public String getHopCount() {
        return hopCount;
    }

    public String getRreqID() {
        return rreqID;
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

    public String getOrigSeqNum() {
        return origSeqNum;
    }

    public int getHopCountAsDecimal() { return Integer.parseInt(hopCount, 16); }

    public int getRreqIDAsDecimal() { return Integer.parseInt(rreqID, 16); }

    public int getDestSeqNumAsDecimal() { return Integer.parseInt(destSeqNum, 16); }

    public int getOrigSeqNumAsDecimal() { return Integer.parseInt(origSeqNum, 16); }

    public String buildRREQ() {
        return type + undefSeqNum + hopCount + rreqID + destAddr + destSeqNum + origAddr + origSeqNum;
    }

}
