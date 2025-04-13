package com.mchouse.mcrassistant.controller;

import com.mchouse.mcrassistant.config.SettingsManager;
import com.mchouse.mcrassistant.event.ChangeViewEvent;
import com.mchouse.mcrassistant.model.Settings;
import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SettingsController {
    @FXML
    public TextArea txaCorpoEmail;
    @FXML
    public TextField txtAssunto;
    @FXML
    public TextField txtToken;
    @FXML
    public TextField txtEmail;
    @FXML
    public VBox root;
    public HBox hBoxSaveConfirmation;
    public Button btnSave;
    public TextArea txaAssEmail;
    public TextField txtAltEmail;

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings(){
        Settings s = SettingsManager.getInstance().getSettings();
        txtEmail.setText(s.getSourceEmail());
        txtAltEmail.setText(s.getAltSourceEmail());
        txtToken.setText(s.getSourceEmailToken());
        txtAssunto.setText(s.getMailSubject());
        txaCorpoEmail.setText(s.getMailBody());
        txaAssEmail.setText(s.getMailSignature());
    }

    @FXML
    public void saveSettings(Event e){
        btnSave.setDisable(true);
        saveSettings();
        FadeTransition ft = new FadeTransition(Duration.seconds(.5), hBoxSaveConfirmation);
        ft.setFromValue(0);
        ft.setToValue(1);

        FadeTransition ftOut = new FadeTransition(Duration.seconds(.5), hBoxSaveConfirmation);
        ftOut.setFromValue(1);
        ftOut.setToValue(0);
        ftOut.setOnFinished((ev) -> {
            btnSave.setDisable(false);
        });

        ft.setOnFinished((ev) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            ftOut.play();
        });
        ft.play();
    }

    private void saveSettings(){
        Settings s = SettingsManager.getInstance().getSettings();
        s.setMailBody(txaCorpoEmail.getText());
        s.setMailSubject(txtAssunto.getText());
        s.setSourceEmailToken(txtToken.getText());
        s.setSourceEmail(txtEmail.getText());
        s.setMailSignature(txaAssEmail.getText());
        s.setAltSourceEmail(txtAltEmail.getText());

        SettingsManager.getInstance().setSettings(s);
        SettingsManager.getInstance().saveSettings();
        root.fireEvent(new ChangeViewEvent());
    }
}
