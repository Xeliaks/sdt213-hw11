package statroller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RollController {

    private final RollStore store;

    public RollController(RollStore store) {
        this.store = store;
    }

    @Operation(summary = "Roll a new stat set",
            description = "Rolls six scores (4d6, drop lowest), saves them to history and returns them "
                    + "with the point-buy cost and how far that is from the 27-point budget.")
    @PostMapping("/rolls")
    @ResponseStatus(HttpStatus.CREATED)
    public RollResult roll() {
        return RollResult.of(store.roll());
    }

    @Operation(summary = "List all saved stat sets, newest first")
    @GetMapping("/rolls")
    public List<RollResult> all() {
        return store.all().stream().map(RollResult::of).toList();
    }
}
