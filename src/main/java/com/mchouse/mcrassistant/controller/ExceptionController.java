package com.mchouse.mcrassistant.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.Setter;

public class ExceptionController {
    @FXML
    public TextArea txaErro;
    @FXML
    public Button btnConfirmar;
    public Label lblCauseMsg;

    private String erroMsg;
    private String causeMsg;
    @Setter
    private Runnable onClose;

    public void setErroMessage(String erroMsg) {
        this.erroMsg = erroMsg;
        if(txaErro!=null){
            txaErro.setText(erroMsg);
        }
    }

    public void setCauseMessage(String causeMsg) {
        this.causeMsg = causeMsg;
        if(lblCauseMsg!=null){
            lblCauseMsg.setText(causeMsg);
        }
    }

    @FXML
    public void initialize() {
        txaErro.setText(erroMsg);
        lblCauseMsg.setText(causeMsg);
    }

    public void handleCloseAction(MouseEvent mouseEvent) {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        if(onClose!=null){
            onClose.run();
        }
        stage.close();
    }
}
