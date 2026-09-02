package com.questionnaire.core.utils;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_PATH = "config.properties";
    public static final String TELEGRAM_TOKEN_KEY = "telegram.bot.token";
    public static final String CHATGPT_TOKEN_KEY = "chatgpt.token";
    public static final String CHATGPT_URL_KEY = "chatgpt.url";

    public static Properties properties = new Properties();

    public static void loadConfig() {

        // Load the file from src/main/resources/
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {

            if (input == null) {
                throw new RuntimeException("Unable to find config.properties in src/main/resources/");
            }

            // Load properties into the object
            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read config.properties file", e);
        }
    }
}