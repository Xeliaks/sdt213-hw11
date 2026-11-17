# Stat Roller

A REST web service (Spring Boot) and clients for rolling D&D 5e ability scores.
Each set is six scores of **4d6, drop the lowest**. Every set is saved to
`rolls.json` and compared with the standard **27-point point buy**: the result
says how many points the roll is *ahead of* or *behind* a point-buy character.

## Requirements

Java 21. Gradle is included via the wrapper (`gradlew`).

## Run

```
gradlew bootRun       # start the service on http://localhost:8090
gradlew runClient     # Swing desktop client (in a second terminal)
gradlew test          # run the tests
```

With the service running:

| Page | URL |
|---|---|
| Web client | http://localhost:8090/ |
| API page (Swagger UI) | http://localhost:8090/swagger-ui.html |

## API

| Method | Path | Result |
|---|---|---|
| `POST` | `/rolls` | Rolls and saves a new set. Returns `201` with the set. |
| `GET` | `/rolls` | All saved sets, newest first. |

```json
{
  "id": 3,
  "rolledAt": "2026-11-17T14:02:00Z",
  "dice": [[6,5,4,1], [5,5,4,2], [6,4,2,1], [4,4,3,2], [5,3,2,1], [3,3,2,1]],
  "scores": [15, 14, 12, 11, 10, 8],
  "pointBuyCost": 25,
  "pointBuyDelta": -2
}
```

Point-buy costs: 8–15 use the official table (0,1,2,3,4,5,7,9). Rolled scores
outside that range use the common extension: 3→−9, 4→−6, 5→−4, 6→−2, 7→−1,
16→12, 17→15, 18→19.

## Code

All code is in `src/main/java/statroller/`.

| File | Role |
|---|---|
| `StatRollerApplication` | Spring Boot entry point |
| `RollController` | REST endpoints |
| `RollStore` | Rolls dice and saves history to `rolls.json` |
| `StatRules` | Drop-lowest and point-buy rules |
| `RollSet` / `RollResult` | Stored form and API form of a roll |
| `StatRollerClient` | Swing client (Java `HttpClient` + Jackson) |

The web client is in `src/main/resources/static/index.html`.
