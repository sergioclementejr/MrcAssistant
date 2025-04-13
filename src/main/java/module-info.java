module com.mchouse.mcrassistant {
    requires static lombok;

    requires javafx.controls;
    requires javafx.fxml;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    requires jakarta.mail;
    requires java.logging;

    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;

    opens com.mchouse.mcrassistant to javafx.fxml;
    opens com.mchouse.mcrassistant.controller to javafx.fxml;
    opens com.mchouse.mcrassistant.model to javafx.base, javafx.fxml;

    opens com.mchouse.mcrassistant.event  to javafx.base,javafx.fxml;
    opens com.mchouse.mcrassistant.config  to javafx.base,javafx.fxml;
    opens com.mchouse.mcrassistant.enums  to javafx.base,javafx.fxml;
    opens com.mchouse.mcrassistant.file.manager  to javafx.base,javafx.fxml;
    opens com.mchouse.mcrassistant.service  to javafx.base,javafx.fxml;

    exports com.mchouse.mcrassistant;
    exports com.mchouse.mcrassistant.controller;
    exports com.mchouse.mcrassistant.model;
    exports com.mchouse.mcrassistant.event;
    exports com.mchouse.mcrassistant.config;
    exports com.mchouse.mcrassistant.enums;
    exports com.mchouse.mcrassistant.file.manager;
    exports com.mchouse.mcrassistant.service;
}