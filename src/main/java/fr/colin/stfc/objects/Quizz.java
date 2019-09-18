package fr.colin.stfc.objects;

import java.util.ArrayList;

public class Quizz {

    private ArrayList<Questions> questions;
    private String category;

    public Quizz(ArrayList<Questions> questions, String category) {
        this.questions = questions;
        this.category = category;
    }

    public ArrayList<Questions> getQuestions() {
        return questions;
    }

    public String getCategory() {
        return category;
    }
}
