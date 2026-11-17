package statroller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StatRollerClientTest {

    @Test
    void deltaWording() {
        assertEquals("2 behind", StatRollerClient.deltaLabel(-2));
        assertEquals("15 ahead", StatRollerClient.deltaLabel(15));
        assertEquals("even", StatRollerClient.deltaLabel(0));
    }

    @Test
    void requestsTimeOut() {
        assertEquals(Optional.of(Duration.ofSeconds(5)), StatRollerClient.request("GET").timeout());
    }

    @Test
    void strikesExactlyOneLowestDie() {
        assertEquals("6 5 4 <s>1</s>", StatRollerClient.diceHtml(List.of(6, 5, 4, 1)));
        assertEquals("<s>3</s> 3 3 3", StatRollerClient.diceHtml(List.of(3, 3, 3, 3)));
        assertEquals("<s>1</s> 6 1 4", StatRollerClient.diceHtml(List.of(1, 6, 1, 4)));
    }
}
