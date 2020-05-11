package org.riisholt.dgtdriver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
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
        ArrayList<byte[]> a = new ArrayList<>();
        InputStream s = TestUtils.class.getClassLoader().getResourceAsStream(filename);

        if(s == null)
            throw new RuntimeException(String.format("Failed to locate resource %s", filename));

        Base64.Decoder decoder = Base64.getDecoder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s));
        String line;
        while((line = reader.readLine()) != null) {
            a.add(decoder.decode(line));
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
