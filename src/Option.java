public class Option {
    private int optionID;     
    private int questionID;   
    private String optionText;

    public Option(int optionID, int questionID, String optionText) {
        this.optionID = optionID;
        this.questionID = questionID;
        this.optionText = optionText;
    }

    public String getOptionText() {
        return optionText;
    }

    @Override
    public String toString() {
        return optionText;
    }
}
