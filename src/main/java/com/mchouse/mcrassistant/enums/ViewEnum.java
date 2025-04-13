package com.mchouse.mcrassistant.enums;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public enum ViewEnum {
    EMAIL_EXCEL("emailExcel-view.fxml"),
    SETTINGS("settings-view.fxml"),
    ERRO("exception-view.fxml");

    private final String fileName;
}
