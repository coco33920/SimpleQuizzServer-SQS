package fr.colin.stfc.objects;

public class Category {

    private String uuid;
    private String name;

    public Category(String uuid, String name){
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }
}
