package fr.colin.stfc;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.colin.stfc.configuration.Config;
import fr.colin.stfc.database.Database;
import fr.colin.stfc.database.DatabaseWrapper;
import fr.colin.stfc.objects.Category;
import fr.colin.stfc.objects.CompletedQuizz;
import fr.colin.stfc.objects.Questions;
import fr.colin.stfc.objects.Quizz;
import org.apache.commons.io.FileUtils;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static spark.Spark.*;

public class STFCQuizzGenerator {

    private static Database database;
    private static DatabaseWrapper wrapper;
    private static String TOKEN;
    public static SimpleDateFormat fullDate = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
    private static Mailer mailer;
    private static String name;

    public static void main(String[] args) throws SQLException {
        Config c = Config.getConfig();
        TOKEN = c.getADMIN_TOKEN();
        database = new Database(c.getDB_HOST(), c.getDB_NAME(), c.getDB_USER(), c.getDB_PASSWORD());
        wrapper = new DatabaseWrapper(database);
        port(6767);
        setupRoutes();
        wrapper.loadAllQuestions();
        mailer = MailerBuilder
                .withSMTPServer(c.getSMTP_SERVER(), c.getPORT(), c.getUSERNAME(), c.getPASSWORD())
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
        mailer.testConnection();
        name = c.getUSERNAME();
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

        post("add_question_bulk", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "Error";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";
            String r = request.body();
            Type ts = new TypeToken<ArrayList<Questions>>() {
            }.getType();
            ArrayList<Questions> qs = new Gson().fromJson(r, ts);
            for (Questions q : qs) {
                wrapper.addQuestion(q.getTitle(), q.getContent(), q.getAnswer(), q.getCategory_uuid());
            }
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
        post("remove_category_bulk", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "Error no token";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";
            Type ts = new TypeToken<ArrayList<String>>() {
            }.getType();
            String s = request.body();
            ArrayList<String> ser = new Gson().fromJson(s, ts);
            for (String cat : ser) {
                getWrapper().removeCategory(cat);
            }
            getWrapper().loadAllQuestions();
            return "Success";
        });
        post("remove_question_bulk", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "Error no token";
            String token = request.queryParams("token");
            if (!token.equalsIgnoreCase(TOKEN))
                return "Token Invalid";
            Type ts = new TypeToken<ArrayList<String>>() {
            }.getType();
            String s = request.body();
            ArrayList<String> ser = new Gson().fromJson(s, ts);
            for (String cat : ser) {
                getWrapper().removeQuestion(cat);
            }
            getWrapper().loadAllQuestions();
            return "Success";
        });
        get("check_token", (request, response) -> {
            if (!request.queryParams().contains("token"))
                return "false";
            String token = request.queryParams("token");
            if (token.equalsIgnoreCase(TOKEN))
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
        post("send_completed_quizz", (request, response) -> {
            if (!request.queryParams().contains("dest"))
                return "Error no receiver";
            String rec = request.queryParams("dest");
            String s = request.body();
            CompletedQuizz sd = new Gson().fromJson(s, CompletedQuizz.class);
            sd.transformToPDF(rec);

            Email email = EmailBuilder.startingBlank()
                    .from("SQS Quizz", name)
                    .to("SQS Quizz", rec)
                    .withSubject("Quizz Complété : Feuille de réponses")
                    .withAttachment("quizz.pdf", new FileDataSource(System.getProperty("user.home") + "/completedQuizz" + sd.getQuizz().getUuid() + ".pdf"))
                    .withPlainText("Quizz Complété")
                    .buildEmail();


            Thread thread = new Thread(() -> {
                System.out.println("Start Mailing Thread");
                ;
                mailer.sendMail(email);
                try {
                    FileUtils.forceDelete(new File(System.getProperty("user.home") + "/completedQuizz" + sd.getQuizz().getUuid() + ".pdf"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

            return "Success";
        });
        get("get_questions", (request, response) -> new Gson().toJson(DatabaseWrapper.categories));
        get("get_categories", (request, response) -> new Gson().toJson(DatabaseWrapper.categoriesList));
    }

    public static Database getDatabase() {
        return database;
    }

    public static DatabaseWrapper getWrapper() {
        return wrapper;
    }
}
