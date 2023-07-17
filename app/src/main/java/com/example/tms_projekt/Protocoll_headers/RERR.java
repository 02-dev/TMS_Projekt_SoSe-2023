package com.example.tms_projekt.Protocoll_headers;

public class RERR {
    private String type; // Value: '2', Number of Characters: 1
    private String unreachDestAddr; // Value: 0-65536, Number of Characters: 4

    public RERR (String incomingRERR) {
        type = incomingRERR.substring(0,1);
        unreachDestAddr = incomingRERR.substring(1);
    }

    public RERR (String targetNode, Boolean neededForConstructor) {
        type = MessageType.RERR_t.getType();
        //TODO: List mit letzten Ankuntszeiten von Hello-Messages bekannter Knoten
        unreachDestAddr = targetNode;
    }

    public String getType() {
        return type;
    }

    public String getUnreachDestAddr() {
        return unreachDestAddr;
    }

    public String buildRERR () {
        return type + unreachDestAddr;
    }
}
