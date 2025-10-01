package application;

import java.time.LocalDateTime;

public class Answer {
    private int id;
    private int questionId;
    private String text;
    private String answeredBy;  // username
    private boolean isChosen;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public Answer(int id, int questionId, String answeredBy, String text) {
        this(id, questionId, answeredBy, text, false, false); // default: not read, not chosen
    }
    public Answer(int id, int questionId, String answeredBy, String text, boolean isRead, boolean isChosen) {
        this.id = id;
        this.questionId = questionId;
        this.answeredBy = answeredBy;
        this.text = text;
        this.isRead = isRead;
        this.isChosen = isChosen;
    }

    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getText() { return text; }
    public String getAnsweredBy() { return answeredBy; }
    public boolean isChosen() { return isChosen; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void markAsChosen() { this.isChosen = true; }
    public void markAsRead() { this.isRead = true; }

    @Override
    public String toString() {
        return "A[" + id + "] by " + answeredBy + ": " + text + 
               (isChosen ? " [RESOLVED]" : "") +
               (isRead ? " [READ]" : "");
    }
}
