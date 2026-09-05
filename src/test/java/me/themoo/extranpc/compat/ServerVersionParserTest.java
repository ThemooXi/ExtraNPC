package me.themoo.extranpc.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerVersionParserTest {

    @Test
    void parsesClassicMinecraftVersions() {
        ServerVersionParser.Version v = ServerVersionParser.parse("1.21.8-R0.1-SNAPSHOT");
        assertEquals(1, v.major());
        assertEquals(21, v.minor());
        assertEquals(8, v.patch());
        assertFalse(v.isYearLine());
        assertTrue(v.isAtLeast(1, 21, 0));
        assertFalse(v.isAtLeast(26, 1, 0));
    }

    @Test
    void parsesPaper26_1() {
        ServerVersionParser.Version v = ServerVersionParser.parse("26.1.2.build.74-stable");
        assertEquals(26, v.major());
        assertEquals(1, v.minor());
        assertEquals(2, v.patch());
        assertTrue(v.isYearLine());
        assertTrue(v.isAtLeast(26, 1, 0));
        assertTrue(v.isAtLeast(1, 21, 11));
        assertFalse(v.isAtLeast(26, 2, 0));
    }

    @Test
    void parsesPaper26_2() {
        ServerVersionParser.Version v = ServerVersionParser.parse("26.2.build.121-stable");
        assertEquals(26, v.major());
        assertEquals(2, v.minor());
        assertEquals(0, v.patch());
        assertTrue(v.isYearLine());
        assertTrue(v.isAtLeast(26, 1, 0));
        assertTrue(v.isAtLeast(26, 2, 0));
    }

    @Test
    void yearLineIsNewerThan1_21() {
        ServerVersionParser.Version paper261 = ServerVersionParser.parse("26.1.1");
        ServerVersionParser.Version mc12111 = ServerVersionParser.parse("1.21.11");
        assertTrue(paper261.compareTo(mc12111) > 0);
    }

    @Test
    void describeFamily() {
        assertEquals("Paper 26.1", ServerVersionParser.describeFamily(ServerVersionParser.parse("26.1.2")));
        assertEquals("Minecraft 1.21.x", ServerVersionParser.describeFamily(ServerVersionParser.parse("1.21.8")));
        assertEquals("unknown", ServerVersionParser.describeFamily(ServerVersionParser.Version.UNKNOWN));
    }

    @Test
    void normalizeKey() {
        assertEquals("HAPPY_VILLAGER", ServerVersionParser.normalizeKey("happy-villager"));
        assertEquals("MOVEMENT_SPEED", ServerVersionParser.normalizeKey(" movement speed "));
    }
}
