package org.riisholt.dgtdriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.riisholt.dgtdriver.moveparser.MoveParser;
import org.riisholt.dgtdriver.moveparser.PlayedMove;

class DgtDriverTest {
    @Test
    void testReading() throws java.io.IOException {
        List<byte[]> msgbytes = TestUtils.readBytes("1.pickle");
        List<DgtMessage> first = processBytes(msgbytes);
        List<DgtMessage> second = processBytesSingly(msgbytes);
        List<DgtMessage> third = processBytesAll(msgbytes);
        assertEquals(first.size(), second.size());
        assertEquals(first.size(), third.size());

        for(int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getClass(), second.get(i).getClass());
            assertEquals(first.get(i).getClass(), third.get(i).getClass());
            // TODO: Make sure messages are exactly equal too.
        }
    }

    List<DgtMessage> processBytes(List<byte[]> msgbytes) {
        ArrayList<DgtMessage> msgs = new ArrayList<>();
        DgtDriver driver = new DgtDriver(msgs::add, null);
        for(byte[] msg: msgbytes) {
            driver.gotBytes(msg);
        }
        return msgs;
    }

    List<DgtMessage> processBytesSingly(List<byte[]> msgbytes) {
        ArrayList<DgtMessage> msgs = new ArrayList<>();
        DgtDriver driver = new DgtDriver(msgs::add, null);
        for(byte[] msg: msgbytes) {
            for(byte b: msg) {
                driver.gotBytes(new byte[]{b});
            }
        }
        return msgs;
    }

    List<DgtMessage> processBytesAll(List<byte[]> msgbytes) {
        ArrayList<DgtMessage> msgs = new ArrayList<>();
        DgtDriver driver = new DgtDriver(msgs::add, null);
        byte[] data = new byte[0];
        for(byte[] chunk: msgbytes) {
            byte[] newData = Arrays.copyOf(data, data.length + chunk.length);
            System.arraycopy(chunk, 0, newData, data.length, chunk.length);
            data = newData;
        }
        driver.gotBytes(data);
        return msgs;
    }
}
