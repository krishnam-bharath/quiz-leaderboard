package com.quiz;

import com.quiz.client.QuizApiClient;
import com.quiz.model.LeaderboardEntry;
import com.quiz.model.PollResponse;
import com.quiz.model.SubmitRequest;
import com.quiz.model.SubmitResponse;
import com.quiz.service.LeaderboardService;

import java.util.List;

/**
 * Quiz Leaderboard System — Main Application
 *
 * Flow:
 *  1. Poll the validator API 10 times (poll 0–9) with a 5-second delay between each.
 *  2. Deduplicate events using (roundId + participant) as a unique key.
 *  3. Aggregate scores per participant.
 *  4. Build and print the leaderboard sorted by totalScore descending.
 *  5. Submit the leaderboard exactly once.
 */
public class QuizLeaderboardApp {

    // ─── Configuration ────────────────────────────────────────────────────────
    private static final String REG_NO           = "2024CS101"; // ← Replace with your registration number
    private static final int    TOTAL_POLLS      = 10;          // API requires exactly 10 polls
    private static final long   POLL_DELAY_MS    = 5_000L;      // Mandatory 5-second delay between polls
    // ──────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Allow regNo override via command-line argument: java -jar quiz-leaderboard.jar YOUR_REG_NO
        String regNo = (args.length > 0 && !args[0].isBlank()) ? args[0] : REG_NO;

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Quiz Leaderboard System — SRM Quiz     ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Registration Number : " + regNo);
        System.out.println("Total Polls         : " + TOTAL_POLLS);
        System.out.println("Delay Between Polls : " + (POLL_DELAY_MS / 1000) + " seconds");
        System.out.println("──────────────────────────────────────────\n");

        QuizApiClient      apiClient  = new QuizApiClient(regNo);
        LeaderboardService service    = new LeaderboardService();

        // ── Step 1: Poll the API 10 times ────────────────────────────────────
        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            try {
                PollResponse response = apiClient.fetchPoll(poll);
                service.processPoll(response);
            } catch (Exception e) {
                System.err.printf("[ERROR] Poll %d failed: %s%n", poll, e.getMessage());
                // Fail fast — a missing poll means incomplete data
                System.exit(1);
            }

            // Mandatory 5-second delay (skip after the last poll)
            if (poll < TOTAL_POLLS - 1) {
                System.out.printf("[WAIT] Sleeping %d seconds before next poll...%n%n",
                        POLL_DELAY_MS / 1000);
                try {
                    Thread.sleep(POLL_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("[ERROR] Interrupted during delay. Exiting.");
                    System.exit(1);
                }
            }
        }

        // ── Step 2–5: Build leaderboard and submit ───────────────────────────
        List<LeaderboardEntry> leaderboard = service.buildLeaderboard();
        service.printLeaderboard(leaderboard);

        SubmitRequest submitRequest = new SubmitRequest(regNo, leaderboard);

        try {
            SubmitResponse submitResponse = apiClient.submitLeaderboard(submitRequest);

            System.out.println("\n[RESULT] Submission complete!");
            System.out.println("  ✔ Correct      : " + submitResponse.isCorrect());
            System.out.println("  ✔ Idempotent   : " + submitResponse.isIdempotent());
            System.out.println("  ✔ Submitted    : " + submitResponse.getSubmittedTotal());
            System.out.println("  ✔ Expected     : " + submitResponse.getExpectedTotal());
            System.out.println("  ✔ Message      : " + submitResponse.getMessage());

            if (!submitResponse.isCorrect()) {
                System.out.println("\n[WARN] Submission was NOT correct. Check deduplication logic.");
                System.exit(2);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Submission failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
