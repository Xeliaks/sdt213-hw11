package statroller;

import java.util.Collections;
import java.util.List;

/** D&D 5e rules: 4d6-drop-lowest scores and the point-buy price of a score set. */
public final class StatRules {

    /** Standard 5e point-buy budget. */
    public static final int BUDGET = 27;

    private static final int[] COST = {-9, -6, -4, -2, -1, 0, 1, 2, 3, 4, 5, 7, 9, 12, 15, 19};

    private StatRules() {}

    /** Sum of the dice minus the single lowest die. */
    public static int scoreOf(List<Integer> dice) {
        return dice.stream().mapToInt(Integer::intValue).sum() - Collections.min(dice);
    }

    public static int cost(int score) {
        if (score < 3 || score > 18) {
            throw new IllegalArgumentException("Score out of range 3..18: " + score);
        }
        return COST[score - 3];
    }

    public static int pointBuyCost(List<Integer> scores) {
        return scores.stream().mapToInt(StatRules::cost).sum();
    }
}
