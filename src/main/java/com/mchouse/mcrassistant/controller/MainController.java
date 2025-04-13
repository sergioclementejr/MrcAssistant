package com.mchouse.mcrassistant.controller;

import com.mchouse.mcrassistant.App;
import com.mchouse.mcrassistant.animation.StageResizeAnimation;
import com.mchouse.mcrassistant.config.SettingsManager;
import com.mchouse.mcrassistant.enums.ViewEnum;
import com.mchouse.mcrassistant.event.ChangeViewEvent;
import com.mchouse.mcrassistant.exceptions.ExceptionListener;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;

public class MainController {
    @FXML
    public HBox header;
    @FXML
    public Pane body;
    @FXML
    public VBox root;
    public ViewEnum currentView;
    public ImageView imgOpt;
    public VBox btnBack;
    public VBox vBoxOpt;
    public Tooltip optToolTip;

    private double offsetX, offsetY;


    @FXML
    public void initialize() {
        App.getApp().setRootSceneController(this);
        if (SettingsManager.getInstance().isSettingsValid()) {
            openSubScene(ViewEnum.EMAIL_EXCEL);
            showOptBtn();
            changeOptImage();
            addTooltip();
        } else {
            openSubScene(ViewEnum.SETTINGS);
        }
        body.addEventHandler(ChangeViewEvent.CHANGE_VIEW, this::onChangeView);

        header.setOnMousePressed(mouseEvent -> {
            offsetX = mouseEvent.getSceneX();
            offsetY = mouseEvent.getSceneY();
        });

        header.setOnMouseDragged(event -> {
            App.getApp().getMainStage().setX(event.getScreenX() - offsetX);
            App.getApp().getMainStage().setY(event.getScreenY() - offsetY);
        });

        ExceptionListener.sign(this::exceptionHandler);
    }

    private void addTooltip() {
        optToolTip = new Tooltip("Configurações");
        Tooltip.install(vBoxOpt, optToolTip);
    }

    private void showOptBtn() {
        vBoxOpt.setManaged(true);
        vBoxOpt.setVisible(true);
    }

    public void onChangeView(ChangeViewEvent e) {
        if (!vBoxOpt.isVisible()) {
            vBoxOpt.setDisable(true);
            showOptBtn();
            if (ViewEnum.SETTINGS.equals(currentView)) {
                openSubScene(ViewEnum.EMAIL_EXCEL);
            } else {
                openSubScene(ViewEnum.SETTINGS);
            }
            changeOptImage();
        }
    }

    public void handleCloseAction(MouseEvent actionEvent) {
        Thread loadingThread = App.getApp().getLoadingThread();
        if (loadingThread != null) {
            loadingThread.interrupt();
        }
        Platform.exit();
    }

    public void setDisable(boolean isDisable) {
        this.root.setDisable(isDisable);
    }

    @FXML
    public void handleOpenOptAction(MouseEvent mouseEvent) {
        vBoxOpt.setDisable(true);
        if (ViewEnum.SETTINGS.equals(currentView)) {
            openSubScene(ViewEnum.EMAIL_EXCEL);
        } else {
            openSubScene(ViewEnum.SETTINGS);
        }
        changeOptImage();
    }

    private void changeOptImage() {
        if (!ViewEnum.SETTINGS.equals(currentView)) {
            FadeTransition btnBackTransition = getExcelOptButtonTransition(new FadeTransition(Duration.seconds(.2), btnBack),
                    new FadeTransition(Duration.seconds(.2), imgOpt));
            btnBack.setVisible(false);
            btnBack.setManaged(false);
            imgOpt.setOpacity(0);
            imgOpt.setVisible(true);
            imgOpt.setManaged(true);
            updateToolTipText("Configurações");
            btnBackTransition.play();
        } else {
            FadeTransition btnBackTransition = getSettingsOptButtonTransition(new FadeTransition(Duration.seconds(.2), imgOpt),
                    new FadeTransition(Duration.seconds(.2), btnBack));
            btnBack.setVisible(true);
            btnBack.setManaged(true);
            btnBack.setOpacity(0);
            imgOpt.setVisible(false);
            imgOpt.setManaged(false);
            updateToolTipText("Voltar");
            btnBackTransition.play();
        }
    }

    private void updateToolTipText(String text) {
        if (optToolTip != null) {
            optToolTip.setText(text);
        }
    }

    private void configOptButton(boolean value) {
        btnBack.setVisible(!value);
        btnBack.setManaged(!value);
        if (value) {
            imgOpt.setOpacity(0);
        } else {
            btnBack.setOpacity(0);
        }
        imgOpt.setVisible(value);
        imgOpt.setManaged(value);
    }

    private FadeTransition getSettingsOptButtonTransition(FadeTransition imgOpt, FadeTransition btnBack) {
        imgOpt.setFromValue(1);
        imgOpt.setToValue(0);
        btnBack.setFromValue(0);
        btnBack.setToValue(1);
        btnBack.setOnFinished((e) -> vBoxOpt.setDisable(false));
        imgOpt.setOnFinished((e) -> btnBack.play());
        return imgOpt;
    }

    private FadeTransition getExcelOptButtonTransition(FadeTransition btnBack, FadeTransition imgOpt) {
        return getSettingsOptButtonTransition(btnBack, imgOpt);
    }

    private void openSubScene(ViewEnum view) {
        currentView = view;
        FXMLLoader loader = new FXMLLoader(App.class.getResource(view.getFileName()));
        try {
            Pane newPanel = loader.load();
            if (!body.getChildren().isEmpty()) {
                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.2), body);
                fadeTransition.setFromValue(1);
                fadeTransition.setToValue(0);
                StageResizeAnimation stageAnimation;
                stageAnimation = new StageResizeAnimation(
                        App.getApp().getMainStage(),
                        newPanel.getPrefWidth(),
                        newPanel.getPrefHeight() + 45);
                FadeTransition fadeInTransition = new FadeTransition(Duration.seconds(.2), body);
                fadeInTransition.setFromValue(0);
                fadeInTransition.setToValue(1);

                FadeTransition fadeFakeTransition = new FadeTransition(Duration.seconds(stageAnimation.getAnimationTimeInMilli()), body);
                fadeFakeTransition.setFromValue(0);
                fadeFakeTransition.setToValue(0);
                fadeFakeTransition.setOnFinished((e) -> {
                    fadeInTransition.play();
                });
                fadeTransition.setOnFinished((e) -> {
                    body.getChildren().remove(0);
                    body.getChildren().add(newPanel);
                    stageAnimation.start();
                    fadeFakeTransition.play();
                });
                fadeTransition.play();
            } else {
                body.requestLayout();
                body.getChildren().add(newPanel);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exceptionHandler(Throwable t) {
        root.setOpacity(.75);
        getLogger().debug("Opening erro window");
        FXMLLoader loader = new FXMLLoader(App.class.getResource(ViewEnum.ERRO.getFileName()));
        try {
            Parent erroView = loader.load();
            ExceptionController controller = loader.getController();
            controller.setErroMessage(getStackTraceAsString(t));
            controller.setCauseMessage(getCauseMessage(t));
            controller.setOnClose(() -> root.setOpacity(1));

            Stage newWindow = new Stage();
            newWindow.setTitle("Erro!");
            Scene exceptionScene = new Scene(erroView);
            exceptionScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            newWindow.setScene(exceptionScene);
            newWindow.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("img/logo-rsm.png"))));
            newWindow.setResizable(false);
            newWindow.initStyle(StageStyle.TRANSPARENT);

            newWindow.initModality(Modality.APPLICATION_MODAL);

            newWindow.show();

            erroView.setClip(App.getClipArea(exceptionScene));
        } catch (IOException e) {
            getLogger().error("Erro ao processar exception: " + e.getCause(), e);
        }
    }

    private String getCauseMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public Logger getLogger() {
        return LogManager.getLogger(MainController.class);
    }
}