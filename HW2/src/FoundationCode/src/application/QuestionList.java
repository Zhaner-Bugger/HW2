package application;

import java.util.ArrayList;
import java.util.List;

public class QuestionList {
    private List<Question> questions;
    private int nextId = 1;

    public QuestionList() {
        this.questions = new ArrayList<>();
    }

    public Question addQuestion(String text) {
        Question q = new Question(nextId++, text);
        questions.add(q);
        return q;
    }

    public Question addFollowUpQuestion(int parentId, String text) {
        Question q = new Question(nextId++, text, parentId);
        questions.add(q);
        return q;
    }

    public List<Question> getAllQuestions() { return questions; }

    public List<Question> getUnresolvedQuestions() {
        List<Question> result = new ArrayList<>();
        for (Question q : questions) {
            if (!q.isResolved()) result.add(q);
        }
        return result;
    }

    public Question getQuestionById(int id) {
        for (Question q : questions) {
            if (q.getId() == id) return q;
        }
        return null;
    }

    public List<Question> getRecentQuestions() {
        List<Question> copy = new ArrayList<>(questions);
        copy.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return copy;
    }
    
    public void addExistingQuestion(Question q) {
        questions.add(q);
        nextId = Math.max(nextId, q.getId() + 1); 
    }
}