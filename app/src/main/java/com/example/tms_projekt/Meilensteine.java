package com.example.tms_projekt;
import com.fazecast.jSerialComm.*;

enum LoraCommandReplies {
    AT("AT,OK\r\n"),
    ATRST("AT,OK\r\n"),
    ATVER("AT,V0.3,OK\r\n"),
    ATSEND("AT,OK\r\n"),
    ATSENDING("AT,SENDING\r\n"),
    ATSENDED("AT,SENDED\r\n"),
    ERRCMD("ERR:CMD\r\n"),
    ERRPARA("ERR:PARA\r\n");

    private String reply;

    LoraCommandReplies(String replyString) {
        this.reply = replyString;
    }

    public String getReply() {
        return reply;
    }
}

public class Meilensteine {
    public static void main(String[] args) {

        /**
         * Damit der Code funktioniert muss ggf. der Name/portDescriptor des Ports angepassst werden an. Zudem muss die
         * README befolgt werden. Auch muss darauf geachtet werden, dass die Baud Rate und die Anzahl an Data- und Stop-
         * Bits übereinstimmen.
         */

        SerialPort sp = SerialPort.getCommPort("COM4");
        sp.setBaudRate(115200);
        sp.setNumDataBits(8);
        sp.setNumStopBits(1);

        System.out.println("System name: " + sp.getSystemPortName());
        System.out.println("Descriptive name: " + sp.getDescriptivePortName());
        System.out.println("Baud: " + sp.getBaudRate());
        System.out.println("Parity: " + sp.getParity());
        System.out.println("Data: " + sp.getNumDataBits());
        System.out.println("Stop: " + sp.getNumStopBits());


        String comStart = "Started Lora module. Awaiting commands...\r\n";
        byte[] buffer = comStart.getBytes();

        sp.openPort();
        sp.writeBytes(buffer, buffer.length);

        String msg = "";

        while (true) {
            while (sp.bytesAvailable() > 0)
            {
                byte[] readBuffer = new byte[sp.bytesAvailable()];
                int numRead = sp.readBytes(readBuffer, readBuffer.length);
                for (byte b : readBuffer) msg = msg + (char) b;

                if (msg.contains("\r\n")) {
                    String regex = "AT\\+SEND=\\d+\r\n";
                    if (msg.matches(regex) && msg.length() <= 15) {
                        int expectedMsgLength = Integer.parseInt(msg.substring(8, msg.length()-2));
                        if (expectedMsgLength <= 0 || expectedMsgLength > 250) {
                            msg = LoraCommandReplies.ERRPARA.getReply();
                        } else {
                            msg = LoraCommandReplies.ATSEND.getReply();
                            buffer = msg.getBytes();
                            sp.openPort();
                            sp.writeBytes(buffer, buffer.length);

                            msg = "";
                            while (msg.length() < expectedMsgLength) {
                                while (sp.bytesAvailable() > 0)
                                {
                                    readBuffer = new byte[sp.bytesAvailable()];
                                    numRead = sp.readBytes(readBuffer, readBuffer.length);
                                    for (byte b : readBuffer) msg = msg + (char) b;
                                }
                            }
                            String msg2 = LoraCommandReplies.ATSENDING.getReply();
                            buffer = msg2.getBytes();
                            sp.openPort();
                            sp.writeBytes(buffer, buffer.length);

                            msg = msg.substring(0, expectedMsgLength) + "\r\n";
                            buffer = msg.getBytes();
                            sp.openPort();
                            sp.writeBytes(buffer, buffer.length);

                            msg = LoraCommandReplies.ATSENDED.getReply();
                        }
                    } else {
                        System.out.println ("MSG:" + msg);
                        switch (msg) {
                            case "AT\r\n":
                                msg = LoraCommandReplies.AT.getReply();
                                break;
                            case "AT+RST\r\n":
                                msg = LoraCommandReplies.ATRST.getReply();
                                break;
                            case "AT+VER\r\n":
                                msg = LoraCommandReplies.ATVER.getReply();
                                break;
                            default:
                                msg = LoraCommandReplies.ERRCMD.getReply();
                                break;
                        }
                    }
                    buffer = msg.getBytes();
                    sp.openPort();
                    sp.writeBytes(buffer, buffer.length);
                    msg = "";
                }
            }
        }
    }
}
