import java.util.List;

public class Question {
    private String questionText;
    private List<Option> options;
    private int correctAnswer;
    private int questionID;

    public Question(int questionID, String questionText, List<Option> options, int correctAnswer) {
        this.questionID = questionID;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public boolean isCorrect(int chosenOption) {
        return chosenOption == correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public List<Option> getOptions() {
        return options;
    }
}

