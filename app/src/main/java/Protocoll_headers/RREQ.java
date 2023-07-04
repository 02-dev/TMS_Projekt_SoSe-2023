package Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static com.example.tms_projekt.GlobalFunctions.getTargetNodeFromRoutingTable;
import static Protocoll_headers.MessageType.RREQ_t;

import com.example.tms_projekt.GlobalFunctions;

import java.util.Arrays;

public class RREQ {
    private byte[] type;
    private byte[] hopCount;
    private byte[] rreqID;
    private byte[] destAddr;
    private byte[] destSeqNum;
    private byte[] origAddr;
    private byte[] origSeqNum;

    public RREQ (byte[] incomingRREQ) {
        type[0] = incomingRREQ[0];
        hopCount = Arrays.copyOfRange(incomingRREQ, 1, 2);
        rreqID = Arrays.copyOfRange(incomingRREQ, 3, 6);
        destAddr = Arrays.copyOfRange(incomingRREQ, 7, 10);
        destSeqNum = Arrays.copyOfRange(incomingRREQ, 11, 14);
        origAddr = Arrays.copyOfRange(incomingRREQ, 15, 18);
        origSeqNum = Arrays.copyOfRange(incomingRREQ, 19, 22);
    }

    public RREQ (String targetNode) {
        type = asciiToByte(RREQ_t.getType());
        hopCount = asciiToByte(getTargetNodeFromRoutingTable(targetNode)[2]);
        // TODO: rreqID
        destAddr = asciiToByte(targetNode);
        destSeqNum = asciiToByte(getTargetNodeFromRoutingTable(targetNode)[4]);
        origAddr = asciiToByte(GlobalFunctions.origAddr);
        origSeqNum = asciiToByte(String.valueOf(GlobalFunctions.origSeqNum));
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getHopCount() {
        return hopCount;
    }

    public byte[] getRreqID() {
        return rreqID;
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

    public byte[] getOrigSeqNum() {
        return origSeqNum;
    }

}
