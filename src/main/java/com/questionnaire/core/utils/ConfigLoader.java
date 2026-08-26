package com.questionnaire.core.utils;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_PATH = "config.properties";
    private static final String TELEGRAM_TOKEN_CONF_KEY = "telegram.bot.token";

    public static String getBotToken() {
        Properties properties = new Properties();

        // Load the file from src/main/resources/
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {

            if (input == null) {
                throw new RuntimeException("Unable to find config.properties in src/main/resources/");
            }

            // Load properties into the object
            properties.load(input);

            // Fetch the property
            return properties.getProperty(TELEGRAM_TOKEN_CONF_KEY);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read config.properties file", e);
        }
    }
}