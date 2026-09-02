package com.questionnaire.core;

import org.json.JSONObject;

import com.questionnaire.Globals;
import com.questionnaire.core.utils.ConfigLoader;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatGpt {
    private String token;
    private String url;

    public ChatGpt() {
        this.token = ConfigLoader.properties.getProperty(ConfigLoader.CHATGPT_TOKEN_KEY);
        this.url = ConfigLoader.properties.getProperty(ConfigLoader.CHATGPT_URL_KEY);
    }

    public String getResponse(String request) {
        OkHttpClient client = new OkHttpClient();

        String question = request;

        HttpUrl httpUrl = HttpUrl.parse(this.url)
                .newBuilder()
                .addQueryParameter("token", this.token)
                .addQueryParameter("text", question)
                .build();

        Request req = new Request.Builder()
                .url(httpUrl)
                .build();

        try {
            Response res = client.newCall(req).execute();
            JSONObject json = new JSONObject(res.body().string());
            // * TODO: when the api will work continue here
            if (json.has("value")) {
                return json.get("value").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Globals.toast.error("Something went wrong when connecting to chatgpt");
        }
        return "";
    }
}
