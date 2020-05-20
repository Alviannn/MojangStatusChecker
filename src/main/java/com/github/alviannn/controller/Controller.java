package com.github.alviannn.controller;

import com.github.alviannn.Main;
import com.github.alviannn.closer.Closer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;

public class Controller {

    @FXML private Label fetch_status_text;
    @FXML private JFXButton reset_button;

    private CompletableFuture<JsonObject> fetchTask;

    @FXML private ImageView minecraft_net;
    @FXML private ImageView session_minecraft;
    @FXML private ImageView account_mojang;
    @FXML private ImageView auth_server;
    @FXML private ImageView session_server;
    @FXML private ImageView api_mojang;
    @FXML private ImageView textures_minecraft;
    @FXML private ImageView mojang_com;

    public void refreshAction() {
        if (fetchTask != null && !fetchTask.isDone())
            return;

        fetch_status_text.setText("FETCHING DATA...");
        reset_button.setText("CANCEL");
        AtomicLong time = new AtomicLong(0L);

        this.fetchTask = Main.supplyAsync(() -> {
            time.set(System.currentTimeMillis());
            return this.fetchStatus().join();
        }).whenComplete((result, error) -> {
            reset_button.setText("RESET");

            if (error != null) {
                this.resetAction();
                fetch_status_text.setText("FAILED TO FETCH!");
                return;
            }

            minecraft_net.setImage(Main.getImage("images/status/" + result.get("minecraft.net").getAsString() + ".png"));
            session_minecraft.setImage(Main.getImage("images/status/" + result.get("session.minecraft.net").getAsString() + ".png"));
            account_mojang.setImage(Main.getImage("images/status/" + result.get("account.mojang.com").getAsString() + ".png"));
            auth_server.setImage(Main.getImage("images/status/" + result.get("authserver.mojang.com").getAsString() + ".png"));
            session_server.setImage(Main.getImage("images/status/" + result.get("sessionserver.mojang.com").getAsString() + ".png"));
            api_mojang.setImage(Main.getImage("images/status/" + result.get("api.mojang.com").getAsString() + ".png"));
            textures_minecraft.setImage(Main.getImage("images/status/" + result.get("textures.minecraft.net").getAsString() + ".png"));
            mojang_com.setImage(Main.getImage("images/status/" + result.get("mojang.com").getAsString() + ".png"));

            fetch_status_text.setText("DATA IS FETCHED! \nIt took " + (System.currentTimeMillis() - time.get()) + " ms!");
        });
    }

    public void resetAction() {
        if (reset_button.getText().equals("CANCEL")) {
            fetchTask.cancel(true);
            reset_button.setText("RESET");
        }

        minecraft_net.setImage(Main.getImage("images/status/grey.png"));
        session_minecraft.setImage(Main.getImage("images/status/grey.png"));
        account_mojang.setImage(Main.getImage("images/status/grey.png"));
        auth_server.setImage(Main.getImage("images/status/grey.png"));
        session_server.setImage(Main.getImage("images/status/grey.png"));
        api_mojang.setImage(Main.getImage("images/status/grey.png"));
        textures_minecraft.setImage(Main.getImage("images/status/grey.png"));
        mojang_com.setImage(Main.getImage("images/status/grey.png"));

        fetch_status_text.setText("");
    }

    /**
     * fetches the mojang status from the website
     *
     * @return a JSON object
     */
    private CompletableFuture<JsonObject> fetchStatus() {
        return CompletableFuture.supplyAsync(() -> {
            JsonArray array;
            try (Closer closer = new Closer()) {
                URL url = new URL("https://status.mojang.com/check");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream stream = closer.add(connection.getInputStream());
                InputStreamReader reader = closer.add(new InputStreamReader(stream));

                array = JsonParser.parseReader(reader).getAsJsonArray();
                connection.disconnect();
            } catch (Exception e) {
                throw new CompletionException(e);
            }

            long millis = System.currentTimeMillis();
            JsonObject result = new JsonObject();

            for (JsonElement element : array) {
                JsonObject part = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : part.entrySet())
                    result.addProperty(entry.getKey(), entry.getValue().getAsString());
            }

            result.addProperty("timestamp", millis);
            return result;
        });
    }

}
