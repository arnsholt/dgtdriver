package org.riisholt.dgtdriver;

import net.razorvine.pickle.Unpickler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static String readResourceFile(String filename) throws java.io.IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        InputStream resource = TestUtils.class.getClassLoader().getResourceAsStream(filename);

        if(resource == null)
            throw new RuntimeException(String.format("Failed to locate resource %s", filename));

        BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
        while((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
    }

    public static List<byte[]> readBytes(String filename) throws java.io.IOException {
        Unpickler u = new Unpickler();
        ArrayList<byte[]> a = new ArrayList<>();
        InputStream s = TestUtils.class.getClassLoader().getResourceAsStream(filename);

        if(s == null)
            throw new RuntimeException(String.format("Failed to locate resource %s", filename));

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
