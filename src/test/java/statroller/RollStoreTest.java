package statroller;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RollStoreTest {

    @TempDir Path dir;
    Path file;

    @BeforeEach
    void setUp() {
        file = dir.resolve("rolls.json");
    }

    @Test
    void resultFromSpecExample() {
        var s = new RollSet(1, Instant.EPOCH, List.of(
                List.of(6, 5, 4, 1), List.of(5, 5, 4, 2), List.of(6, 4, 2, 1),
                List.of(4, 4, 3, 2), List.of(5, 3, 2, 1), List.of(3, 3, 2, 1)));
        var r = RollResult.of(s);
        assertEquals(List.of(15, 14, 12, 11, 10, 8), r.scores());
        assertEquals(25, r.pointBuyCost());
        assertEquals(-2, r.pointBuyDelta());
    }

    @Test
    void rollShape() {
        var s = new RollStore(file.toString()).roll();
        assertEquals(6, s.dice().size());
        s.dice().forEach(d -> {
            assertEquals(4, d.size());
            d.forEach(v -> assertTrue(v >= 1 && v <= 6));
        });
    }

    @Test
    void missingFileStartsEmptyAndIsCreatedOnRoll() {
        var store = new RollStore(file.toString());
        assertTrue(store.all().isEmpty());
        store.roll();
        assertTrue(Files.exists(file));
    }

    @Test
    void newestFirstAndSurvivesRestart() {
        var a = new RollStore(file.toString());
        a.roll();
        a.roll();
        var b = new RollStore(file.toString());
        assertEquals(List.of(2L, 1L), b.all().stream().map(RollSet::id).toList());
        assertEquals(a.all(), b.all());
        assertEquals(3, b.roll().id());
    }

    @Test
    void allIsASnapshotNotALiveView() {
        var store = new RollStore(file.toString());
        store.roll();
        var before = store.all();
        store.roll();
        assertEquals(1, before.size());
    }

    @Test
    void unreadableFileRefusesAndIsUntouched() throws Exception {
        for (String junk : List.of("not json", "", "null")) {
            Files.writeString(file, junk);
            assertThrows(IllegalStateException.class, () -> new RollStore(file.toString()));
            assertEquals(junk, Files.readString(file));
        }
    }
}
