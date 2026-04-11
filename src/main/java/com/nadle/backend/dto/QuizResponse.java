package com.nadle.backend.dto;

public class QuizResponse {

    private String question;
    private boolean answer;
    private String explanation;

    public QuizResponse() {}

    public QuizResponse(String question, boolean answer, String explanation) {
        this.question = question;
        this.answer = answer;
        this.explanation = explanation;
    }

    public String getQuestion() { return question; }
    public boolean isAnswer() { return answer; }
    public String getExplanation() { return explanation; }
}
