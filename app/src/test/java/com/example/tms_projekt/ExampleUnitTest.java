package com.example.tms_projekt;

import static com.example.tms_projekt.GlobalFunctions.asciiToByte;
import static com.example.tms_projekt.GlobalFunctions.byteToAscii;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testestestestes() {
        assertEquals(4, asciiToByte("1234").length);
    }

    @Test
    public void testestesteste1s() {
        String s = "1234";
        assertEquals(4, s.getBytes().length);
    }

    @Test
    public void tese2() {
        String s = "adfgerw3453472h57jfgdhbb6h45ff";
        assertEquals(s, byteToAscii(asciiToByte(s)));
    }
}