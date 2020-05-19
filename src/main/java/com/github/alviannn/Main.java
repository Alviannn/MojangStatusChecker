package com.github.alviannn;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Getter
public class Main extends Application {

    @Getter private static Parent parent;
    @Getter private static Stage primaryStage;
    @Getter private final static Image icon = new Image(String.valueOf(getResource("images/mojang_logo.png")));
    @Getter private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    @SneakyThrows
    @Override
    public void start(Stage stage) {
        parent = FXMLLoader.load(getResource("app.fxml"));
        primaryStage = stage;

        primaryStage.setTitle("Mojang Status");
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(new Scene(parent, 800, 600));
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(e -> threadPool.shutdownNow());

        primaryStage.show();
    }

    /**
     * @see ClassLoader#getResource(String)
     */
    public static URL getResource(String path) {
        return Main.class.getClassLoader().getResource(path);
    }

    public static Image getImage(String path) {
        return new Image(String.valueOf(getResource(path)));
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() {
                return supplier.get();
            }
        };

        CompletableFuture<T> result = new CompletableFuture<>();

        task.setOnSucceeded(e -> result.complete(task.getValue()));
        task.setOnCancelled(e -> result.cancel(false));
        task.setOnFailed(e -> result.completeExceptionally(task.getException()));

        threadPool.execute(task);
        return result;
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                runnable.run();
                return null;
            }
        };

        CompletableFuture<Void> result = new CompletableFuture<>();

        task.setOnSucceeded(e -> result.complete(task.getValue()));
        task.setOnCancelled(e -> result.cancel(false));
        task.setOnFailed(e -> result.completeExceptionally(task.getException()));

        threadPool.execute(task);
        return result;
    }

    public static void main(String[] args) {
        Application.launch();
    }

}
