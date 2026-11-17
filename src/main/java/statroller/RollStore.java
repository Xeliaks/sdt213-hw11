package statroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Rolls stat sets and keeps the history in a JSON file. */
@Component
public class RollStore {

    private final ObjectMapper json = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    private final Path file;
    private final List<RollSet> rolls; // oldest first

    public RollStore(@Value("${statroller.file:rolls.json}") String file) {
        this.file = Path.of(file);
        this.rolls = load();
    }

    public synchronized RollSet roll() {
        long id = rolls.stream().mapToLong(RollSet::id).max().orElse(0) + 1;
        List<List<Integer>> dice = IntStream.range(0, 6)
                .mapToObj(i -> IntStream.range(0, 4)
                        .mapToObj(j -> ThreadLocalRandom.current().nextInt(1, 7))
                        .toList())
                .toList();
        RollSet set = new RollSet(id, Instant.now(), dice);

        List<RollSet> updated = new ArrayList<>(rolls);
        updated.add(set);
        save(updated); // keep memory and file in step: only add once it's on disk
        rolls.add(set);
        return set;
    }

    /** All rolls, newest first. */
    public synchronized List<RollSet> all() {
        return List.copyOf(rolls.reversed());
    }

    private List<RollSet> load() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<RollSet> loaded;
        try {
            loaded = json.readValue(file.toFile(), new TypeReference<List<RollSet>>() {});
        } catch (IOException e) {
            throw unreadable(e.getMessage(), e);
        }
        if (loaded == null) {
            throw unreadable("file contains null", null);
        }
        return new ArrayList<>(loaded);
    }

    private IllegalStateException unreadable(String reason, Exception cause) {
        return new IllegalStateException(
                "Cannot read " + file.toAbsolutePath() + ": " + reason + ". Fix or delete it.", cause);
    }

    private void save(List<RollSet> data) {
        Path tmp = Path.of(file + ".tmp");
        try {
            json.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), data);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save " + file.toAbsolutePath(), e);
        }
    }
}
