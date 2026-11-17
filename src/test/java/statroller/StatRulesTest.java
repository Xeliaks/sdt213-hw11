package statroller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class StatRulesTest {

    @Test
    void dropsLowest() {
        assertEquals(15, StatRules.scoreOf(List.of(6, 5, 4, 1)));
    }

    @Test
    void dropsOnlyOneOfTie() {
        assertEquals(9, StatRules.scoreOf(List.of(3, 3, 3, 3)));
        assertEquals(11, StatRules.scoreOf(List.of(1, 6, 1, 4)));
    }

    @Test
    void costTable() {
        assertEquals(-9, StatRules.cost(3));
        assertEquals(-1, StatRules.cost(7));
        assertEquals(0, StatRules.cost(8));
        assertEquals(5, StatRules.cost(13));
        assertEquals(7, StatRules.cost(14));
        assertEquals(9, StatRules.cost(15));
        assertEquals(12, StatRules.cost(16));
        assertEquals(19, StatRules.cost(18));
    }

    @Test
    void costOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> StatRules.cost(2));
        assertThrows(IllegalArgumentException.class, () -> StatRules.cost(19));
    }

    @Test
    void specExamples() {
        assertEquals(25, StatRules.pointBuyCost(List.of(15, 14, 12, 11, 10, 8)));
        assertEquals(42, StatRules.pointBuyCost(List.of(18, 16, 14, 13, 9, 6)));
    }
}
