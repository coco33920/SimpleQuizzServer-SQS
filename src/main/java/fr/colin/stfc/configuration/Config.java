package fr.colin.stfc.configuration;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {

    private String DB_HOST;
    private String DB_NAME;
    private String DB_USER;
    private String DB_PASSWORD;
    private String ADMIN_TOKEN;

    public Config(String DB_HOST, String DB_NAME, String DB_USER, String DB_PASSWORD, String ADMIN_TOKEN) {
        this.DB_HOST = DB_HOST;
        this.DB_NAME = DB_NAME;
        this.DB_USER = DB_USER;
        this.DB_PASSWORD = DB_PASSWORD;
        this.ADMIN_TOKEN = ADMIN_TOKEN;
    }

    public String getDB_HOST() {
        return DB_HOST;
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public String getDB_USER() {
        return DB_USER;
    }

    public String getDB_PASSWORD() {
        return DB_PASSWORD;
    }

    public String getADMIN_TOKEN() {
        return ADMIN_TOKEN;
    }

    public static Config getConfig() {
        InputStream inputStream = Config.class.getResourceAsStream("/config.json");
        BufferedReader b = new BufferedReader(new InputStreamReader(inputStream));
        String rawConfig = StringUtils.join(b.lines().toArray());
        return new Gson().fromJson(rawConfig, Config.class);
    }
}
