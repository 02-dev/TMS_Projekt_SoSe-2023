package com.example.tms_projekt.Protocoll_headers;

import com.example.tms_projekt.GlobalFunctions;

public class Data {
    private String type; // Value: '3', Number of Characters: 1
    private String destAddr; // Value: 0-65536, Number of Characters: 4
    private String origAddr; // Value: 0-65536, Number of Characters: 4
    private String payload; // Value: 0-65536, Number of Characters: 0-241

    public Data (String incomingData) {
        type = incomingData.substring(0,1);
        destAddr = incomingData.substring(1,5);
        origAddr = incomingData.substring(5,9);
        payload = incomingData.substring(9);
    }

    public Data (String targetNode, String payload) {
        type = MessageType.DATA_t.getType();
        destAddr = targetNode;
        origAddr = GlobalFunctions.origAddr;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public String getDestAddr() {
        return destAddr;
    }

    public String getOrigAddr() {
        return origAddr;
    }

    public String getPayload() {
        return payload;
    }

    public String buildData() {
        return type + destAddr + origAddr + payload;
    }
}
