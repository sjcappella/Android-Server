package androidserver;

//Question object used in the tests
public class Question {
    //Variables/Attributes
    private String question;
    private String answerA;
    private String answerB;
    private String answerC;
    private String answerD;
    private String correctAnswer;

    //Constructor
    Question(String q, String a, String b, String c, String d, String ca){
            question = q;
            answerA = a;
            answerB = b;
            answerC = c;
            answerD = d;
            correctAnswer = ca;
    }

    //Get the question
    public String getQuestion(){
        return question;
    }

    //Get the AnswerA option
    public String getAnswerA(){
        return answerA;
    }

    //Get the AnswerB option
    public String getAnswerB(){
        return answerB;
    }

    //Get the AnswerC option
    public String getAnswerC(){
        return answerC;
    }

    //Get the AnswerD option
    public String getAnswerD(){
        return answerD;
    }

    //Get the correct answer
    public String getCorrectAnswer(){
        return correctAnswer;
    }

}