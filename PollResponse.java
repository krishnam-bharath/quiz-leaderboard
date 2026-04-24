package com.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents the full response from GET /quiz/messages
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PollResponse {

    private String regNo;
    private String setId;
    private int pollIndex;
    private List<QuizEvent> events;

    public PollResponse() {}

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public int getPollIndex() { return pollIndex; }
    public void setPollIndex(int pollIndex) { this.pollIndex = pollIndex; }

    public List<QuizEvent> getEvents() { return events; }
    public void setEvents(List<QuizEvent> events) { this.events = events; }

    @Override
    public String toString() {
        return String.format("PollResponse{regNo='%s', setId='%s', pollIndex=%d, events=%s}",
                regNo, setId, pollIndex, events);
    }
}
