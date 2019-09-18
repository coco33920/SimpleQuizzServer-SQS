package fr.colin.stfc.objects;

public class Questions {

    private String uuid;
    private String title;
    private String content;
    private String answer;
    private String category_uuid;

    public Questions(String uuid, String title, String content, String answer, String category_uuid) {
        this.uuid = uuid;
        this.title = title;
        this.content = content;
        this.answer = answer;
        this.category_uuid = category_uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory_uuid() {
        return category_uuid;
    }
}
