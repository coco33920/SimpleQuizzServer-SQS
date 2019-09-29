package fr.colin.stfc.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.colin.stfc.objects.*;
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

    public ArrayList<long[]> periods(long start, long end, long i) {
        ArrayList<long[]> periods = new ArrayList<>();
        int s = 0;
        while (true) {
            boolean brea = false;
            long n = (start + i * s + 1);
            long e = n + (i - 1);
            if (e >= end) {
                e = end;
                brea = true;
            }
            periods.add(new long[]{n, e});
            if (brea)
                break;
            s++;
        }
        return periods;
    }

    public ArrayList<ArrayList<Scores>> fetchScoreData(RequestFetchQuizzs quizzs) throws SQLException {

        long start = quizzs.getStart();
        long end = quizzs.getEnd();
        long i = quizzs.getInterval();

        ArrayList<long[]> periods = periods(start, end, i);
        ArrayList<ArrayList<Scores>> scores = new ArrayList<>();
        for (long[] l : periods) {
            ArrayList<Scores> scores1 = new ArrayList<>();
            ResultSet rs = db.getResult("SELECT * FROM scores WHERE date BETWEEN " + l[0] + " AND " + l[1]);
            while (rs.next()) {
                scores1.add(new Scores(rs.getString("quizzuuid"), rs.getDouble("score"), rs.getLong("date"), rs.getString("user")));
            }
            scores.add(scores1);
        }

        return scores;
    }

    public ArrayList<ArrayList<Quizz>> fetchQuizzData(RequestFetchQuizzs quizzs) throws SQLException {
        ArrayList<ArrayList<Quizz>> data = new ArrayList<>();

        long start = quizzs.getStart();
        long end = quizzs.getEnd();
        long i = quizzs.getInterval();

        ArrayList<long[]> periods = periods(start, end, i);
        for (long[] l : periods) {
            ArrayList<Quizz> quizzes = new ArrayList<>();
            ResultSet rs = db.getResult("SELECT * FROM quizzs WHERE date BETWEEN " + l[0] + " AND " + l[1]);
            while (rs.next()) {
                quizzes.add(new Quizz(new Gson().fromJson(rs.getString("questions"), new TypeToken<ArrayList<Questions>>() {
                }.getType()), rs.getString("category"), rs.getString("uuid"), rs.getLong("date")));
            }
            data.add(quizzes);
        }
        return data;

    }


}

