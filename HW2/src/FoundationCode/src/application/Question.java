package application;

import java.time.LocalDateTime;

public class Question {
    private int id;
    private String text;
    private AnswersList answers;
    private boolean isResolved;
    private LocalDateTime createdAt;
    private Integer followUpOf; // ID of parent question if this is a follow-up

    public Question(int id, String text) {
        this.id = id;
        this.text = text;
        this.answers = new AnswersList();
        this.isResolved = false;
        this.createdAt = LocalDateTime.now();
        this.followUpOf = null;
    }

    // Overloaded constructor for follow-up questions
    public Question(int id, String text, int followUpOf) {
        this(id, text);
        this.followUpOf = followUpOf;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public AnswersList getAnswers() { return answers; }
    public boolean isResolved() { return isResolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getFollowUpOf() { return followUpOf; }

    public void addAnswer(Answer answer) {
        answers.addAnswer(answer.getQuestionId(), answer.getAnsweredBy(), answer.getText());
        if (answer.isChosen()) {
            isResolved = true;
        }
    }

    public void markResolved() { isResolved = true; }

    @Override
    public String toString() {
        return "Q[" + id + "]: " + text +
               (isResolved ? " [RESOLVED]" : "") +
               (followUpOf != null ? " (Follow-up of Q[" + followUpOf + "])" : "");
    }
}