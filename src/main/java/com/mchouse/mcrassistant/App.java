package com.mchouse.mcrassistant;

import com.mchouse.mcrassistant.config.SettingsManager;
import com.mchouse.mcrassistant.controller.MainController;
import com.mchouse.mcrassistant.exceptions.ExceptionNotifier;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


public class App extends Application {
    @Getter
    private static App app;
    @Getter
    private Stage mainStage;
    @Getter
    private Parent mainParent;
    @Getter
    private MainController rootSceneController;

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static final LocalDateTime startTime = LocalDateTime.now();

    @Getter
    @Setter
    private Thread loadingThread;

    @Override
    public void start(Stage stage) throws IOException {
        app = this;
        mainStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        stage.setResizable(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("img/logo-rsm.png"))));

        Parent parent = fxmlLoader.load();
        mainParent = parent;
        Scene scene = new Scene(parent);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        scene.widthProperty().addListener((observable, oldValue, newValue) -> parent.setClip(getClipArea(scene)));
        scene.heightProperty().addListener((observable, oldValue, newValue) -> parent.setClip(getClipArea(scene)));

        stage.setTitle("MCR Assistant");
        stage.setScene(scene);
        stage.show();

        parent.setClip(getClipArea(scene));

        FadeTransition fadeTransition = new FadeTransition(javafx.util.Duration.seconds(.5), parent);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        finishLoading();
    }
    public static Rectangle getClipArea(Scene scene){
        Rectangle clip = new Rectangle(scene.getWidth(), scene.getHeight());
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        clip.setSmooth(true);
        return clip;
    }

    public static void main(String[] args){
        LOGGER.info("Starting application.");
        setupExceptionNotifier();
        SettingsManager.getInstance().loadConfig();
        launch();
    }

    private static void setupExceptionNotifier() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.info("Uncaught exception intercepted");
            LOGGER.error(throwable);
            ExceptionNotifier.notifyException(throwable);
        });
    }

    public static void finishLoading(){
        Duration d = Duration.between(startTime,LocalDateTime.now());
        LOGGER.info("Started "+App.class.getName()+" in "+d.toMillis()/1000f+" seconds");
    }

    public void setRootSceneController(MainController rootSceneController) {
        if(this.rootSceneController ==null){
            this.rootSceneController = rootSceneController;
        }
    }
}