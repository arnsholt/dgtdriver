package org.riisholt.dgtdriver.moveparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.riisholt.dgtdriver.DgtDriver;
import org.riisholt.dgtdriver.TestUtils;

import java.util.ArrayList;
import java.util.List;

class MoveParserTest {
    @Test
    void testParsing() throws java.io.IOException {
        testUci("1");
        testUci("2");
        //testPgn("johan");
    }

    void testUci(String prefix) throws java.io.IOException {
        assertEquals(TestUtils.readResourceFile(prefix + ".uci"), readGame(prefix + ".pickle").uci());
    }

    void testPgn(String prefix) throws java.io.IOException {
        assertEquals(TestUtils.readResourceFile(prefix + ".pgn"), readGame(prefix + ".pickle").pgn(false));
    }

    Game readGame(String filename) throws java.io.IOException {
        List<Game> games = new ArrayList<>();
        MoveParser parser = new MoveParser(games::add);
        processFile(filename, parser::gotMove);
        parser.close();

        assertEquals(1, games.size());
        return games.get(0);
    }

    void processFile(String filename, DgtDriver.ReadCallback cb) throws java.io.IOException {
        DgtDriver driver = new DgtDriver(cb, null);
        for(byte[] b: TestUtils.readBytes(filename)) {
            driver.gotBytes(b);
        }
    }
}
