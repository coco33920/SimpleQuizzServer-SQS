package fr.colin.stfc;


import com.google.gson.Gson;
import fr.colin.stfc.configuration.Config;
import fr.colin.stfc.database.Database;
import fr.colin.stfc.database.DatabaseWrapper;
import fr.colin.stfc.objects.Questions;
import fr.colin.stfc.objects.Quizz;

import java.sql.SQLException;

import static spark.Spark.*;

public class STFCQuizzGenerator {

    private static Database database;
    private static DatabaseWrapper wrapper;
    private static String TOKEN;

    public static void main(String[] args) throws SQLException {
        Config c = Config.getConfig();
        TOKEN = c.getADMIN_TOKEN();
        database = new Database(c.getDB_HOST(), c.getDB_NAME(), c.getDB_USER(), c.getDB_PASSWORD());
        wrapper = new DatabaseWrapper(database);
        port(6767);
        setupRoutes();
        wrapper.loadAllQuestions();
    }


    public static void setupRoutes() {

        get("/", (request, response) -> "Hello World");
        get("/request_quizz", (request, response) -> {
            if (!request.queryParams().contains("category") || !request.queryParams().contains("noq"))
                return "Error not enough arguments";
            String category = request.queryParams("category");
            String numberOfQuestion = request.queryParams("noq");
            int noq;
            try {
                noq = Integer.parseInt(numberOfQuestion);
            } catch (Exception e) {
                return "Error parsing number of question";
            }
            Quizz q;
            try {
                q = wrapper.makeRandomQuizz(category, noq);
            } catch (Exception e) {
                return "Error with quizz";
            }
            return new Gson().toJson(q);
        });
        get("add_category", (request, response) -> {
            if (!request.queryParams().contains("token") || !request.queryParams().contains("name"))
                return "Error";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token invalid";
            String name = request.queryParams("name");
            wrapper.addCategory(name);
            wrapper.loadAllQuestions();
            return "Success";
        });

        post("add_question", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "Error";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";

            String r = request.body();
            Questions q;
            try {
                q = new Gson().fromJson(r, Questions.class);
            } catch (Exception e) {
                return "Error : bad input";
            }
            wrapper.addQuestion(q.getTitle(), q.getContent(), q.getAnswer(), q.getCategory_uuid());
            wrapper.loadAllQuestions();
            return "Success";
        });
        get("remove_question", (request, response) -> {
            if (!request.queryParams().contains("token") || !request.queryParams().contains("uuid"))
                return "Error";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";
            String uuid = request.queryParams("uuid");
            wrapper.removeQuestion(uuid);
            wrapper.loadAllQuestions();
            return "Success";
        });
        get("remove_category", (request, response) -> {
            if (!request.queryParams().contains("token") || !request.queryParams().contains("uuid"))
                return "Error";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";
            String uuid = request.queryParams("uuid");
            wrapper.removeCategory(uuid);
            wrapper.loadAllQuestions();
            return "Success";
        });
        get("check_token", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "false";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "true";
            return "false";
        });
        get("fetch_quizz", (request, response) -> {
            if (!request.queryParams().contains("uuid"))
                return "Error no uuid";
            String uuid = request.queryParams("uuid");
            Quizz q = wrapper.fetchQuizz(uuid);
            if (q == null) {
                return "Error quizz not found";
            }
            return new Gson().toJson(q);
        });
        get("get_questions", (request, response) -> new Gson().toJson(DatabaseWrapper.categories));
        get("get_categories", (request, response) -> new Gson().toJson(DatabaseWrapper.categoriesList));
    }

}
