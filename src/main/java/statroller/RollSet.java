package statroller;

import java.time.Instant;
import java.util.List;

/** One saved roll: six groups of four d6. This is what goes into rolls.json. */
public record RollSet(long id, Instant rolledAt, List<List<Integer>> dice) {}
