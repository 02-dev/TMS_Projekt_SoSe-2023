package com.example.tms_projekt;

import java.util.ArrayList;
import java.util.List;

public class GlobalFunctions {

    public static final String origAddr = "0202";
    public static int origSeqNum = 0;
    public static int routRequestID = 0;

    public static String getOrigSeqNumInHex() {
        String temp = Integer.toHexString(origSeqNum);
        if (temp.length() == 1) { return "000" + temp; }
        else if (temp.length() == 2) {return "00" + temp;}
        else if (temp.length() == 3) {return "0" + temp;}
        else return temp;
    }
    public static String getRoutRequestIDInHex() {
        String temp = Integer.toHexString(routRequestID);
        if (temp.length() == 1) { return "000" + temp; }
        else if (temp.length() == 2) {return "00" + temp;}
        else if (temp.length() == 3) {return "0" + temp;}
        else return temp;
    }

    public static String increaseHopCountForIncomingRREQAndRREP(String hopCount) {
        return Integer.toHexString(Integer.parseInt(hopCount, 16) + 1);
    }

    // String[]: 0 = Node address/goal, 1 = Hop, 2 = Hop Count, 3 = SeqNum
    public static List<String[]> routingTable = new ArrayList<>();

    public static String[] getTargetNodeFromRoutingTable(String targetNode) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) return routingTable.get(i);
        }
        return new String[0];
    }

    public static void addTargetNodeToRoutingTable(String targetNodeAddr, String hop, String hopCount, String seqNum) {
        routingTable.add(new String[]{targetNodeAddr, hop, hopCount, seqNum});
    }

    public static int getDecimalHopCountOfTargetNodeFromRoutingTable (String targetNode) {
        return Integer.parseInt(getTargetNodeFromRoutingTable(targetNode)[2], 16);
    }

    public static int getDecimalSeqNumOfTargetNodeFromRoutingTable (String targetNode) {
        return Integer.parseInt(getTargetNodeFromRoutingTable(targetNode)[3], 16);
    }
}
