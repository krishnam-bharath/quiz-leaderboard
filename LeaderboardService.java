package com.quiz.service;

import com.quiz.model.LeaderboardEntry;
import com.quiz.model.PollResponse;
import com.quiz.model.QuizEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic for the Quiz Leaderboard System.
 *
 * Responsibilities:
 *  1. Deduplicate events using (roundId + participant) as a unique key.
 *  2. Aggregate scores per participant.
 *  3. Build a leaderboard sorted by totalScore descending.
 */
public class LeaderboardService {

    /**
     * Set of seen deduplication keys — prevents double-counting
     * events that reappear across polls.
     *
     * Key format: "roundId|participant"
     */
    private final Set<String> seenKeys = new HashSet<>();

    /**
     * Running total score per participant.
     * Key: participant name | Value: accumulated score
     */
    private final Map<String, Integer> scoreMap = new LinkedHashMap<>();

    /**
     * Total number of raw events received (including duplicates)
     */
    private int totalRawEvents = 0;

    /**
     * Total number of duplicate events skipped
     */
    private int totalDuplicatesSkipped = 0;

    /**
     * Processes one poll response.
     * Iterates all events, skips duplicates, and accumulates scores.
     *
     * @param pollResponse the API response for one poll
     */
    public void processPoll(PollResponse pollResponse) {
        if (pollResponse == null || pollResponse.getEvents() == null) {
            System.out.println("[WARN] Poll response has no events — skipping.");
            return;
        }

        List<QuizEvent> events = pollResponse.getEvents();
        System.out.printf("[PROCESS] Poll %d has %d event(s)%n",
                pollResponse.getPollIndex(), events.size());

        for (QuizEvent event : events) {
            totalRawEvents++;
            String key = event.getDeduplicationKey();

            if (seenKeys.contains(key)) {
                // Duplicate — skip it
                totalDuplicatesSkipped++;
                System.out.printf("  [SKIP] Duplicate: %s (key=%s)%n", event, key);
            } else {
                // New event — record and accumulate
                seenKeys.add(key);
                scoreMap.merge(event.getParticipant(), event.getScore(), Integer::sum);
                System.out.printf("  [ADD]  %s → %s +%d (total now: %d)%n",
                        key, event.getParticipant(), event.getScore(),
                        scoreMap.get(event.getParticipant()));
            }
        }
    }

    /**
     * Builds the final leaderboard sorted by totalScore descending.
     * Ties are broken alphabetically by participant name (consistent ordering).
     *
     * @return sorted list of LeaderboardEntry
     */
    public List<LeaderboardEntry> buildLeaderboard() {
        return scoreMap.entrySet().stream()
                .map(entry -> new LeaderboardEntry(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparingInt(LeaderboardEntry::getTotalScore).reversed()
                        .thenComparing(LeaderboardEntry::getParticipant))
                .collect(Collectors.toList());
    }

    /**
     * Computes grand total score across all participants.
     */
    public int computeTotalScore() {
        return scoreMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Prints a formatted leaderboard summary to stdout.
     */
    public void printLeaderboard(List<LeaderboardEntry> leaderboard) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║       FINAL LEADERBOARD          ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf("║  %-4s %-18s %-7s ║%n", "Rank", "Participant", "Score");
        System.out.println("╠══════════════════════════════════╣");

        int rank = 1;
        for (LeaderboardEntry entry : leaderboard) {
            System.out.printf("║  %-4d %-18s %-7d ║%n",
                    rank++, entry.getParticipant(), entry.getTotalScore());
        }

        System.out.println("╠══════════════════════════════════╣");
        System.out.printf("║  GRAND TOTAL: %-19d ║%n", computeTotalScore());
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf("║  Raw events   : %-16d ║%n", totalRawEvents);
        System.out.printf("║  Duplicates   : %-16d ║%n", totalDuplicatesSkipped);
        System.out.printf("║  Unique events: %-16d ║%n", totalRawEvents - totalDuplicatesSkipped);
        System.out.println("╚══════════════════════════════════╝");
    }

    // Getters for stats
    public int getTotalRawEvents() { return totalRawEvents; }
    public int getTotalDuplicatesSkipped() { return totalDuplicatesSkipped; }
    public int getUniqueEventCount() { return seenKeys.size(); }
}
