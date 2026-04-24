# Quiz Leaderboard System — SRM Internship Assignment  Krishnam Bharath(RA2311028010016)

A Java application that polls a quiz validator API, deduplicates event data, aggregates scores, and submits a correct leaderboard.

---

## Problem Summary

- Poll the validator API **10 times** (poll indices 0–9)
- Each poll returns quiz events: `{ roundId, participant, score }`
- The **same event can appear in multiple polls** (duplicates must be ignored)
- Deduplication key: **`roundId + participant`**
- Build a leaderboard sorted by `totalScore` (descending) and submit exactly once

---

## Project Structure

```
quiz-leaderboard/
├── pom.xml
└── src/main/java/com/quiz/
    ├── QuizLeaderboardApp.java       ← Main entry point
    ├── client/
    │   └── QuizApiClient.java        ← HTTP GET + POST logic
    ├── model/
    │   ├── QuizEvent.java            ← Raw event from API
    │   ├── PollResponse.java         ← Full poll response
    │   ├── LeaderboardEntry.java     ← Final leaderboard row
    │   ├── SubmitRequest.java        ← POST body
    │   └── SubmitResponse.java       ← POST response
    └── service/
        └── LeaderboardService.java   ← Deduplication + aggregation logic
```

---

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.6+

### 1. Clone and configure

```bash
git clone https://github.com/your-username/quiz-leaderboard.git
cd quiz-leaderboard
```

Open `src/main/java/com/quiz/QuizLeaderboardApp.java` and update:
```java
private static final String REG_NO = "YOUR_REGISTRATION_NUMBER"; // ← Change this
```

### 2. Build

```bash
mvn clean package -q
```

### 3. Run

```bash
java -jar target/quiz-leaderboard.jar
```

Or pass your registration number as a command-line argument:

```bash
java -jar target/quiz-leaderboard.jar 2024CS101
```

> ⏱️ The program takes **~50 seconds** to complete (10 polls × 5-second mandatory delay).

---

## How It Works

### Flow

```
Poll 0 ──┐
Poll 1 ──┤
  ...     ├──► Deduplicate (roundId|participant) ──► Aggregate ──► Submit
Poll 9 ──┘
```

### Deduplication Logic

```java
// For each event in every poll:
String key = roundId + "|" + participant;

if (seenKeys.contains(key)) {
    // DUPLICATE — skip, do not add to score
} else {
    seenKeys.add(key);
    scoreMap.merge(participant, score, Integer::sum);
}
```

### Example

| Poll | Event              | Action  |
|------|--------------------|---------|
| 0    | R1 + Alice = 10    | ✅ Add  |
| 0    | R1 + Bob   = 20    | ✅ Add  |
| 3    | R1 + Alice = 10    | ❌ Skip (duplicate) |
| 7    | R2 + Alice = 15    | ✅ Add  |

Final: Alice = 25, Bob = 20 → Grand Total = 45

---

## Dependencies

| Library           | Purpose              |
|-------------------|----------------------|
| OkHttp 4.12       | HTTP client          |
| Jackson Databind  | JSON serialization   |
| SLF4J Simple      | Logging              |

---

## Sample Output

```
╔══════════════════════════════════╗
║       FINAL LEADERBOARD          ║
╠══════════════════════════════════╣
║  Rank Participant          Score   ║
╠══════════════════════════════════╣
║  1    Alice               100     ║
║  2    Bob                 80      ║
╠══════════════════════════════════╣
║  GRAND TOTAL: 180                 ║
╠══════════════════════════════════╣
║  Raw events   : 24                ║
║  Duplicates   : 14                ║
║  Unique events: 10                ║
╚══════════════════════════════════╝

[RESULT] Submission complete!
  ✔ Correct      : true
  ✔ Submitted    : 180
  ✔ Expected     : 180
  ✔ Message      : Correct!
```

---

## Key Design Decisions

1. **HashSet for O(1) dedup** — `seenKeys` stores all processed `roundId|participant` keys for instant lookup.
2. **`Map.merge()` for accumulation** — cleanly handles both first-time inserts and subsequent additions.
3. **Fail-fast on poll error** — any failed poll means incomplete data, so the program exits immediately.
4. **Mandatory 5-second delay** — enforced between every poll as required by the API spec.
5. **Single submission** — the program submits exactly once after all 10 polls are complete.
