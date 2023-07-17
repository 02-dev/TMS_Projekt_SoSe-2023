package com.example.tms_projekt;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalTime;
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
        else if (temp.length() == 5) {
            origSeqNum = 0;
            return "0000";
        }
        else return temp;
    }
    public static String getRoutRequestIDInHex() {
        String temp = Integer.toHexString(routRequestID);
        if (temp.length() == 1) { return "000" + temp; }
        else if (temp.length() == 2) {return "00" + temp;}
        else if (temp.length() == 3) {return "0" + temp;}
        else if (temp.length() == 5) {
            routRequestID = 0;
            return "0000";
        }
        else return temp;
    }

    public static String increaseHopCountForIncomingRREQAndRREP(String hopCount) {
        return Integer.toHexString(Integer.parseInt(hopCount, 16) + 1);
    }

    // String[]: 0 = Node address/goal, 1 = Hop, 2 = Hop Count, 3 = SeqNum, 4 = time added/updated
    public static List<String[]> routingTable = new ArrayList<>();

    public static String[] getTargetNodeFromRoutingTable(String targetNode) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) return routingTable.get(i);
        }
        return new String[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void addTargetNodeToRoutingTable(String targetNodeAddr, String hop, String hopCount, String seqNum) {
        routingTable.add(new String[]{targetNodeAddr, hop, hopCount, seqNum, LocalTime.now().toString()});
    }

    public static void deleteTargetFromRoutingTable(String targetNode) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) routingTable.remove(i);
        }
    }

    public static int getDecimalHopCountOfTargetNodeFromRoutingTable (String targetNode) {
        return Integer.parseInt(getTargetNodeFromRoutingTable(targetNode)[2], 16);
    }

    public static int getDecimalSeqNumOfTargetNodeFromRoutingTable (String targetNode) {
        return Integer.parseInt(getTargetNodeFromRoutingTable(targetNode)[3], 16);
    }

    public static void updateSeqNumOfTargetNodeFromRT (String targetNode, String seqNum) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) routingTable.get(i)[3] = seqNum;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void updateTimeOfTargetNodeFromRT (String targetNode) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) routingTable.get(i)[4] = LocalTime.now().toString();
        }
    }

    public static boolean isTargetInRoutingTable(String targetNode) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0] == targetNode) return true;
        }
        return false;
    }
}
