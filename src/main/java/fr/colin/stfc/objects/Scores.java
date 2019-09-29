package fr.colin.stfc.objects;

public class Scores {

    private String quizzid;
    private Double score;
    private Long date;
    private String user;

    public Scores(String quizzid, Double score, Long date, String user) {
        this.quizzid = quizzid;
        this.score = score;
        this.date = date;
        this.user = user;
    }

    public String getQuizzid() {
        return quizzid;
    }

    public Double getScore() {
        return score;
    }

    public Long getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }
}
