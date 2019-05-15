package org.riisholt.dgtdriver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.razorvine.pickle.Unpickler;
import org.riisholt.dgtdriver.moveparser.Move;
import org.riisholt.dgtdriver.moveparser.MoveParser;

class DgtDriverTest {
    @Test
    void testReading() throws java.io.IOException {
        List<byte[]> msgbytes = readBytes(getClass().getClassLoader().getResourceAsStream("1.pickle"));
        List<DgtMessage> first = processBytes(msgbytes);
        List<DgtMessage> second = processBytesSingly(msgbytes);
        assertEquals(first.size(), second.size());

        for(int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getClass(), second.get(i).getClass());
            // TODO: Make sure messages are exactly equal too.
        }
    }

    @Test
    void testParsing() throws java.io.IOException {
        testParsingForFile("1");
        testParsingForFile("2");
    }

    void testParsingForFile(String prefix) throws java.io.IOException {
        List<byte[]> msgbytes = readBytes(getClass().getClassLoader().getResourceAsStream(prefix + ".pickle"));
        List<DgtMessage> messages = processBytes(msgbytes);
        String pgn = readResourceFile(prefix + ".pgn");
        List<Move> moves = MoveParser.parseMoves(messages);
        assertEquals(pgn, asPgn(moves));
    }

    String asPgn(List<Move> moves) {
        int ply = 0;
        StringBuilder builder = new StringBuilder();
        for(Move m: moves) {
            if(ply % 2 == 0) {
                builder.append(String.format("%d. %s", 1+ply/2, m.uci()));
            }
            else {
                builder.append(String.format(" %s\n", m.uci()));
            }
            ply++;
        }
        builder.append('\n');
        return builder.toString();
    }

    String readResourceFile(String filename) throws java.io.IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename)));
        while((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
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

    List<byte[]> readBytes(InputStream s) throws java.io.IOException {
        Unpickler u = new Unpickler();
        ArrayList<byte[]> a = new ArrayList<>();
        while(!streamAtEnd(s)) {
            a.add((byte[]) u.load(s));
        }
        return a;
    }

    static boolean streamAtEnd(InputStream s) throws java.io.IOException {
        if(!s.markSupported())
            throw new RuntimeException("mark() not supported");

        s.mark(1);
        boolean more = s.read() == -1;
        s.reset();
        return more;
    }
}
