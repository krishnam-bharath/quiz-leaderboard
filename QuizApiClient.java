package com.quiz.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.model.PollResponse;
import com.quiz.model.SubmitRequest;
import com.quiz.model.SubmitResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for the Quiz API.
 * Handles GET /quiz/messages and POST /quiz/submit
 */
public class QuizApiClient {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String regNo;

    public QuizApiClient(String regNo) {
        this.regNo = regNo;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Fetches one poll from the messages endpoint.
     *
     * @param pollIndex poll number (0–9)
     * @return parsed PollResponse
     * @throws IOException on network or parse error
     */
    public PollResponse fetchPoll(int pollIndex) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL + "/quiz/messages")
                .newBuilder()
                .addQueryParameter("regNo", regNo)
                .addQueryParameter("poll", String.valueOf(pollIndex))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        System.out.printf("[POLL %d] GET %s%n", pollIndex, url);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(String.format(
                    "Poll %d failed: HTTP %d — %s", pollIndex, response.code(), response.message()
                ));
            }
            String body = response.body().string();
            System.out.printf("[POLL %d] Response: %s%n", pollIndex, body);
            return objectMapper.readValue(body, PollResponse.class);
        }
    }

    /**
     * Submits the final leaderboard.
     *
     * @param submitRequest leaderboard payload
     * @return SubmitResponse from the server
     * @throws IOException on network or parse error
     */
    public SubmitResponse submitLeaderboard(SubmitRequest submitRequest) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(submitRequest);

        System.out.println("\n[SUBMIT] POST " + BASE_URL + "/quiz/submit");
        System.out.println("[SUBMIT] Payload: " + jsonBody);

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/quiz/submit")
                .post(body)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("[SUBMIT] Response: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException(String.format(
                    "Submit failed: HTTP %d — %s | Body: %s",
                    response.code(), response.message(), responseBody
                ));
            }
            return objectMapper.readValue(responseBody, SubmitResponse.class);
        }
    }
}
