package application;

import java.util.ArrayList;
import java.util.List;

public class AnswersList {
    private List<Answer> answers;
    private int nextId = 1;

    public AnswersList() {
        this.answers = new ArrayList<>();
    }

    public Answer addAnswer(int questionId, String answeredBy, String text) {
        Answer a = new Answer(nextId++, questionId, answeredBy, text);
        answers.add(a);
        return a;
    }

    public List<Answer> getAnswersForQuestion(int questionId) {
        List<Answer> result = new ArrayList<>();
        for (Answer a : answers) {
            if (a.getQuestionId() == questionId) result.add(a);
        }
        return result;
    }

    public Answer getAnswerById(int id) {
        for (Answer a : answers) {
            if (a.getId() == id) return a;
        }
        return null;
    }

    public boolean markAnswerAsChosen(int id) {
        Answer a = getAnswerById(id);
        if (a != null) {
            a.markAsChosen();
            return true;
        }
        return false;
    }
}
