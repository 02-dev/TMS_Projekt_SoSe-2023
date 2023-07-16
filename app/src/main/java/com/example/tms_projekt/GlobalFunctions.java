package com.example.tms_projekt;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GlobalFunctions {

    public static final String origAddr = "0202";
    public static int origSeqNum = 0;

    public static byte [] getOrigAddrAsByteArray() {
        return asciiToByte(origAddr);
    }

    /**
     * source: https://www.baeldung.com/java-convert-hex-to-ascii
     */
    public static String decimalToHex(String decimalStr) {
        char[] chars = decimalStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();
    }

    public static byte[] asciiToByte(String asciiStr) {
        Charset charset = Charset.forName("ASCII");
        //return charset.encode(asciiToHex(asciiStr)).array();
        return charset.encode(asciiStr).array();
    }

    /**
     * source: https://www.baeldung.com/java-convert-hex-to-ascii
     */
    public static String hexToDecimal(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static String byteToAscii(byte[] byteArr) {
        Charset charset = Charset.forName("ASCII");
        //return hexToAscii(charset.decode(ByteBuffer.wrap(byteArr)).toString());
        return charset.decode(ByteBuffer.wrap(byteArr)).toString();
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
