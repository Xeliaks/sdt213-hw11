package statroller;

import java.time.Instant;
import java.util.List;

/** A roll as the REST API returns it: the dice plus scores and the point-buy comparison. */
public record RollResult(long id, Instant rolledAt, List<List<Integer>> dice,
                         List<Integer> scores, int pointBuyCost, int pointBuyDelta) {

    public static RollResult of(RollSet s) {
        List<Integer> scores = s.dice().stream().map(StatRules::scoreOf).toList();
        int cost = StatRules.pointBuyCost(scores);
        return new RollResult(s.id(), s.rolledAt(), s.dice(), scores, cost, cost - StatRules.BUDGET);
    }
}
