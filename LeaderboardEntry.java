package com.quiz.model;

/**
 * Represents one entry in the final leaderboard.
 */
public class LeaderboardEntry {

    private String participant;
    private int totalScore;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore = totalScore;
    }

    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    @Override
    public String toString() {
        return String.format("%-20s %d", participant, totalScore);
    }
}
