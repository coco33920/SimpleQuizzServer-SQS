package fr.colin.stfc.objects;

import java.util.ArrayList;

public class Quizz {

    private ArrayList<Questions> questions;
    private String category;
    private String uuid;
    private Long date;

    public Quizz(ArrayList<Questions> questions, String category, String uuid, Long date) {
        this.questions = questions;
        this.category = category;
        this.uuid = uuid;
        this.date = date;
    }

    public ArrayList<Questions> getQuestions() {
        return questions;
    }

    public Long getDate() {
        return date;
    }

    public String getUuid() {
        return uuid;
    }

    public static ArrayList<String> arrayOfQuestionToAnswer(ArrayList<Questions> questions) {
        ArrayList<String> answers = new ArrayList<>();
        questions.forEach(questions1 -> answers.add(questions1.getAnswer()));
        return answers;
    }

    public String getCategory() {
        return category;
    }
}
