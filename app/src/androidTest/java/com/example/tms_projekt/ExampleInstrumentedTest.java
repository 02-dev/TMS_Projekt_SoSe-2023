package com.example.tms_projekt;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.fazecast.jSerialComm.SerialPort;

import java.util.logging.Logger;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.tms_projekt", appContext.getPackageName());
    }

    @Test
    public void testingSomething() {
        for (SerialPort sp:
             SerialPort.getCommPorts()) {
            Log.println(1, "TEST", sp.getDescriptivePortName());
        }
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