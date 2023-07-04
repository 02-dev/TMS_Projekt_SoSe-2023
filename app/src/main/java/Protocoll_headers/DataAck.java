package Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static Protocoll_headers.MessageType.DATA_ACK;

import com.example.tms_projekt.GlobalFunctions;

import java.util.Arrays;

public class DataAck {
    private byte[] type;
    private byte[] destAddr;
    private byte[] origAddr;
    private byte[] dataSeqNum;

    public DataAck (byte[] incomingDataAck) {
        type[0] = incomingDataAck[0];
        destAddr = Arrays.copyOfRange(incomingDataAck, 1, 4);
        origAddr = Arrays.copyOfRange(incomingDataAck, 5, 8);
        dataSeqNum = Arrays.copyOfRange(incomingDataAck, 9, 10);
    }

    public DataAck (byte[] destAddr, byte[] dataSeqNum) {
        type = asciiToByte(DATA_ACK.getType());
        this.destAddr = destAddr;
        origAddr = asciiToByte(GlobalFunctions.origAddr);
        this.dataSeqNum = dataSeqNum;
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public byte[] getOrigAddr() {
        return origAddr;
    }

    public byte[] getDataSeqNum() {
        return dataSeqNum;
    }
}
