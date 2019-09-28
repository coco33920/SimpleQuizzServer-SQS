package fr.colin.stfc.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.colin.stfc.objects.Category;
import fr.colin.stfc.objects.CompletedQuizz;
import fr.colin.stfc.objects.Questions;
import fr.colin.stfc.objects.Quizz;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class DatabaseWrapper {

    Database db;

    public static HashMap<Questions, Category> questions = new HashMap<>();
    public static HashMap<String, ArrayList<Questions>> categories = new HashMap<>();
    public static ArrayList<Category> categoriesList = new ArrayList<>();

    public DatabaseWrapper(Database db) {
        this.db = db;
    }

    public void loadAllQuestions() throws SQLException {
        questions.clear();
        categories.clear();
        ArrayList<Category> categories = new ArrayList<>();
        ResultSet rs = db.getResult("SELECT * FROM categories");
        while (rs.next()) {
            categories.add(new Category(rs.getString("uuid"), rs.getString("name")));
        }
        categoriesList = categories;
        for (Category c : categories) {
            ArrayList<Questions> questions = new ArrayList<>();
            ResultSet rse = db.getResult("SELECT * FROM questions WHERE category_uuid='" + c.getUuid() + "'");
            while (rse.next()) {
                Questions q = new Questions(rse.getString("uuid"), rse.getString("title"), rse.getString("content"), rse.getString("answer"), rse.getString("category_uuid"));
                DatabaseWrapper.questions.put(q, c);
                questions.add(q);
            }
            DatabaseWrapper.categories.put(c.getUuid(), questions);
        }
    }

    public Quizz makeRandomQuizz(String category, int noq) throws Exception {
        if (!categories.containsKey(category)) {
            System.out.println("do not exist");
            throw new Exception();
        }
        ArrayList<Questions> q = categories.get(category);
        ArrayList<Questions> randomQuestion = pickRandomElements(q, noq);
        Quizz quizz = new Quizz(randomQuestion, category, UUID.randomUUID().toString().split("-")[0].toUpperCase(), System.currentTimeMillis());
        String query = String.format("INSERT INTO quizzs(uuid,category,questions,answers,date) VALUES('%s','%s','%s','%s','%s')", quizz.getUuid(), quizz.getCategory(), new Gson().toJson(quizz.getQuestions()).replace("\"", "\\\""), new Gson().toJson(Quizz.arrayOfQuestionToAnswer(quizz.getQuestions())).replace("\"", "\\\"" +
                ""), quizz.getDate());
        db.update(query);
        return quizz;
    }

    public void addScore(double score, String user, CompletedQuizz quizz) {
        db.update(String.format("INSERT INTO scores(quizzuuid,score,date,user) VALUES('%s','%s','%s','%s')", quizz.getQuizz().getUuid(), score, quizz.getQuizz().getDate(), user));
    }

    public Quizz fetchQuizz(String uuid) {

        Type questions = new TypeToken<ArrayList<Questions>>() {
        }.getType();
        ResultSet rs = db.getResult("SELECT * FROM quizzs WHERE UUID='" + uuid + "'");
        while (true) {
            try {
                if (!rs.next()) break;
                return new Quizz(new Gson().fromJson(rs.getString("questions"), questions), rs.getString("category"), rs.getString("uuid"), Long.parseLong(rs.getString("date")));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;

    }


    public static ArrayList pickRandomElements(ArrayList e, int noe) {
        ArrayList es = (ArrayList) e.clone();
        if (es.size() < noe) {
            noe = es.size();
        }
        ArrayList re = new ArrayList();
        int remaining = noe;
        while (remaining > 0) {
            Object s = es.get(new Random().nextInt(es.size()));
            re.add(s);
            es.remove(s);
            remaining--;
        }
        return re;
    }

    public void addCategory(String name) throws SQLException {
        if (catExist(name))
            return;
        db.update("INSERT INTO categories(name,uuid) VALUES('" + name + "', '" + UUID.randomUUID().toString() + "')");
    }

    public void removeQuestion(String uuid) {
        db.update("DELETE FROM questions WHERE uuid='" + uuid + "'");
    }

    public void removeCategory(String uuid) {
        db.update("DELETE FROM questions WHERE category_uuid='" + uuid + "'");
        db.update("DELETE FROM categories WHERE uuid='" + uuid + "'");
    }

    public void addQuestion(String title, String content, String answer, String category) {
        db.update(String.format("INSERT INTO questions(uuid,title,content,answer,category_uuid) VALUES('%s','%s','%s','%s','%s')", UUID.randomUUID().toString(), title, content, answer, category));
    }


    private boolean catExist(String name) throws SQLException {
        return db.getResult("SELECT * FROM categories WHERE name='" + name + "'").next();
    }

    public Database getDb() {
        return db;
    }
}
