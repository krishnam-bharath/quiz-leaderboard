package com.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a single quiz event from the API response.
 * Used as the raw event data before deduplication.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizEvent {

    private String roundId;
    private String participant;
    private int score;

    public QuizEvent() {}

    public QuizEvent(String roundId, String participant, int score) {
        this.roundId = roundId;
        this.participant = participant;
        this.score = score;
    }

    /**
     * Deduplication key: combination of roundId and participant.
     * If two events share the same key, they are duplicates.
     */
    public String getDeduplicationKey() {
        return roundId + "|" + participant;
    }

    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }

    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    @Override
    public String toString() {
        return String.format("QuizEvent{roundId='%s', participant='%s', score=%d}", roundId, participant, score);
    }
}
