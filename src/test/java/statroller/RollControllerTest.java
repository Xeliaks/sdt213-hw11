package statroller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RollControllerTest {

    @TempDir static Path dir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("statroller.file", () -> dir.resolve("rolls.json").toString());
    }

    @Autowired TestRestTemplate rest;

    @Test
    void postCreates() {
        var r = rest.postForEntity("/rolls", null, RollResult.class);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertEquals(6, r.getBody().scores().size());
        assertEquals(r.getBody().pointBuyCost() - 27, r.getBody().pointBuyDelta());
    }

    @Test
    void webPageIsServedAtRoot() {
        var r = rest.getForEntity("/", String.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(r.getBody().contains("<title>Stat Roller</title>"));
        assertTrue(r.getBody().contains("swagger-ui.html"));
    }

    @Test
    void apiPageAndSpecAreServed() {
        assertEquals(HttpStatus.OK, rest.getForEntity("/swagger-ui/index.html", String.class).getStatusCode());
        String spec = rest.getForObject("/v3/api-docs", String.class);
        assertTrue(spec.contains("\"/rolls\""));
        assertTrue(spec.contains("Stat Roller API"));
    }

    @Test
    void getNewestFirstWithIsoDates() {
        rest.postForEntity("/rolls", null, RollResult.class);
        rest.postForEntity("/rolls", null, RollResult.class);
        var all = rest.getForObject("/rolls", RollResult[].class);
        assertTrue(all[0].id() > all[1].id());
        assertTrue(rest.getForObject("/rolls", String.class).contains("\"rolledAt\":\"20"));
    }
}
