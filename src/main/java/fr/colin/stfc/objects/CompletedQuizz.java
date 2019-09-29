package fr.colin.stfc.objects;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import fr.colin.stfc.STFCQuizzGenerator;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class CompletedQuizz {

    private Quizz quizz;
    private ArrayList<String> answers;
    private boolean corrected = false;
    private int score = 0;

    public void setScore(int score) {
        this.score = score;
    }

    public CompletedQuizz(Quizz quizz, ArrayList<String> answers) {
        this.quizz = quizz;
        this.answers = answers;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public Quizz getQuizz() {
        return quizz;
    }

    public void transformToPDF(String user) {
        String destination = System.getProperty("user.home") + "/completedQuizz" + quizz.getUuid() + ".pdf";
        Document document = new Document(PageSize.A4, 1, 1, 1, 1);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
            writer.setStrictImageSequence(true);

            document.addTitle("Quizz Complété " + quizz.getUuid());
            document.addCreator("Simple Quizz System");
            document.addAuthor("STFC");
            document.addCreationDate();
            document.open();

            PdfContentByte canvas = writer.getDirectContent();
            BaseFont b = BaseFont.createFont("font/DS9_Credits.ttf", "", BaseFont.EMBEDDED);
            Font little = new Font(b, 6);
            Font littlecolor = new Font(b, 6, Font.NORMAL, BaseColor.GREEN);
            Font big = new Font(b, 8, Font.ITALIC);
            Phrase cat = new Phrase("Thème : " + quizz.getCategory(), big);
            Phrase date = new Phrase("Date : " + STFCQuizzGenerator.fullDate.format(quizz.getDate()), big);
            Phrase uuid = new Phrase("UUID : " + quizz.getUuid(), big);

            int totalLines = quizz.getQuestions().size() * 3;
            int actualLine = 0;

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, cat, 20, 830, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, date, 20, 818, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, uuid, 20, 806, 0);
            int y = 786;
            int x = 20;
            if (corrected) {
                double scores = ((double) score / (double) getQuizz().getQuestions().size() + 0.0d) * 100;
                STFCQuizzGenerator.getWrapper().addScore(scores, user, this);
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Score : " + score + " / " + getQuizz().getQuestions().size() + " ( " + scores + " % )", big), x, 794, 0);
                y = 774;
            }

            for (int i = 0; i < quizz.getQuestions().size(); i++) {
                if (y < 62) {
                    document.newPage();
                    y = 830;
                }
                if (quizz.getQuestions().get(i).getContent().length() > 109) {
                    Questions q = quizz.getQuestions().get(i);
                    String content = q.getContent();
                    String patern = "(?<=\\G";
                    for (int sd = 0; sd < 110; sd += 10) {
                        patern += "..........";
                    }
                    patern += ")";
                    String f1 = content.split(patern)[0];
                    content = content.substring(110);

                    String first = "Question " + i + " : " + f1;
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(first, little), x, y, 0);
                    y -= 6;
                    String pattern = "(?<=\\G";
                    for (int s = 0; s < 124; s += 4) {
                        pattern += "....";
                    }
                    pattern += ")";
                    String[] test = content.split(pattern);
                    for (String t : test) {
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(t, little), x, y, 0);
                        y -= 6;
                    }

                } else {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Question : " + quizz.getQuestions().get(i).getContent(), little), x, y, 0);
                    y -= 6;
                }
                y -= 4;
                if (answers.get(i).length() > 106) {
                    String answer = answers.get(i);
                    String patern = "(?<=\\G";
                    for (int sd = 0; sd < 107; sd += 1) {
                        patern += ".";
                    }
                    patern += ")";
                    String f1 = answer.split(patern)[0];
                    answer = answer.substring(107);
                    String first = "Réponse donnée : " + f1;
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(first, little), x, y, 0);
                    y -= 6;
                    String pattern = "(?<=\\G";
                    for (int s = 0; s < 124; s += 4) {
                        pattern += "....";
                    }
                    pattern += ")";
                    String[] t = answer.split(pattern);
                    for (String ts : t) {
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(ts, little), x, y, 0);
                        y -= 6;
                    }
                } else {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Réponse donnée : " + answers.get(i), little), x, y, 0);
                    y -= 6;
                }
                String a = quizz.getQuestions().get(i).getAnswer();
                Phrase s;
                if (a.contains("Bonne Réponse ! +1 Point")) {
                    s = new Phrase(a, littlecolor);
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, s, x, y, 0);
                } else {
                    if (quizz.getQuestions().get(i).getAnswer().length() > 104) {
                        String answer = quizz.getQuestions().get(i).getAnswer();
                        String patern = "(?<=\\G";
                        for (int sd = 0; sd < 107; sd += 1) {
                            patern += ".";
                        }
                        patern += ")";
                        String f1 = answer.split(patern)[0];
                        answer = answer.substring(110);
                        String first = "Réponse attendue : " + f1;
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(first, little), x, y, 0);
                        y -= 6;
                        String pattern = "(?<=\\G";
                        for (int wx = 0; wx < 124; wx += 4) {
                            pattern += "....";
                        }
                        pattern += ")";
                        String[] t = answer.split(pattern);
                        for (String ts : t) {
                            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(ts, little), x, y, 0);
                            y -= 6;
                        }
                    } else {
                        s = new Phrase("Réponse attendue : " + quizz.getQuestions().get(i).getAnswer());
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, s, x, y, 0);
                        y -= 6;
                    }
                }
                y -= 20;
            }

            document.close();
            //TODO : MULTI LINE ANSWERS

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String... args) {
        ArrayList<Questions> questions = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            questions.add(new Questions("", "SDF", "DFEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEG", "DFEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEG", "lol"));
            answers.add("Answer");
        }
        CompletedQuizz c = new CompletedQuizz(new Quizz(questions, "e", "e", (long) System.currentTimeMillis()), answers);
        c.transformToPDF("lol");
    }

}
