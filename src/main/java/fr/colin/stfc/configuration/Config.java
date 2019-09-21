package fr.colin.stfc.configuration;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {

    private String DB_HOST;
    private String DB_NAME;
    private String DB_USER;
    private String DB_PASSWORD;
    private String ADMIN_TOKEN;
    private String SMTP_SERVER;
    private int PORT;
    private String USERNAME;
    private String PASSWORD;

    public Config(String DB_HOST, String DB_NAME, String DB_USER, String DB_PASSWORD, String ADMIN_TOKEN, String SMTP_SERVER, int PORT, String USERNAME, String PASSWORD) {
        this.DB_HOST = DB_HOST;
        this.DB_NAME = DB_NAME;
        this.DB_USER = DB_USER;
        this.DB_PASSWORD = DB_PASSWORD;
        this.ADMIN_TOKEN = ADMIN_TOKEN;
        this.SMTP_SERVER = SMTP_SERVER;
        this.PORT = PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
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

    public int getPORT() {
        return PORT;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public String getSMTP_SERVER() {
        return SMTP_SERVER;
    }

    public String getUSERNAME() {
        return USERNAME;
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
