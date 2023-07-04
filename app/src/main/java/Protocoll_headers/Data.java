package Protocoll_headers;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static com.example.tms_projekt.GlobalFunctions.byteToAscii;
import static com.example.tms_projekt.GlobalFunctions.decimalToHex;
import static com.example.tms_projekt.GlobalFunctions.hexToDecimal;
import static Protocoll_headers.MessageType.DATA_t;

import com.example.tms_projekt.GlobalFunctions;

import java.util.Arrays;

public class Data {
    private byte[] type;
    private byte[] destAddr;
    private byte[] origAddr;
    private byte[] dataSeqNum;
    private byte[] payload;

    public Data (byte[] incomingData) {
        type[0] = incomingData[0];
        destAddr = Arrays.copyOfRange(incomingData, 1, 4);
        origAddr = Arrays.copyOfRange(incomingData, 5, 8);
        dataSeqNum = Arrays.copyOfRange(incomingData, 9, 10);
        payload = Arrays.copyOfRange(incomingData, 11, Integer.valueOf(hexToDecimal(byteToAscii(dataSeqNum))) + 10);
    }

    public Data (String targetNode, String payload) {
        type = asciiToByte(DATA_t.getType());
        destAddr = asciiToByte(targetNode);
        origAddr = asciiToByte(GlobalFunctions.origAddr);
        dataSeqNum = asciiToByte(decimalToHex(String.valueOf(payload.length())));
        this.payload = asciiToByte(payload);
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

    public byte[] getPayload() {
        return payload;
    }
}
