package com.mchouse.mcrassistant.controller;

import com.mchouse.mcrassistant.App;
import com.mchouse.mcrassistant.config.SettingsManager;
import com.mchouse.mcrassistant.file.manager.ExcelReader;
import com.mchouse.mcrassistant.model.ControleCondominio;
import com.mchouse.mcrassistant.model.Settings;
import com.mchouse.mcrassistant.service.EmailBroadcastService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EmailExcelController {
    @FXML
    public VBox root;
    @FXML
    public Label lbFilePath;
    @FXML
    public CheckBox cbEnviados;
    @FXML
    public CheckBox cbRetornados;
    @FXML
    public CheckBox cbValidados;
    @FXML
    public Button btnEscolherArquivo;
    @FXML
    public Button btnEnviarEmail;
    @FXML
    public Label lbQtdRegistros;
    @FXML
    public Label lbQtdRegistrosValue;
    public VBox vBoxProgressBar;
    public ProgressBar pbEmails;
    public Pane rootPane;
    public HBox hBoxSaveConfirmation;
    @FXML
    private TableView<ControleCondominio> condominioTable;
    @FXML
    private TableColumn<ControleCondominio, String> colImovelLocado;
    @FXML
    private TableColumn<ControleCondominio, String> colLocatario;
    @FXML
    private TableColumn<ControleCondominio, Boolean> colEnvio;
    @FXML
    private TableColumn<ControleCondominio, Boolean> colRetorno;
    @FXML
    private TableColumn<ControleCondominio, Boolean> colValidado;
    @FXML
    private ObservableList<ControleCondominio> controleCondominioObservableList;
    private LinkedList<ControleCondominio> sourceControleCondominioList;

    private EmailBroadcastService emailService;

    private Thread loadingThread;

    @FXML
    public void initialize() {
        colImovelLocado.setCellValueFactory(new PropertyValueFactory<>("imovelLocado"));
        colLocatario.setCellValueFactory(new PropertyValueFactory<>("locatario"));
        colEnvio.setCellValueFactory(new PropertyValueFactory<>("enviado"));
        colRetorno.setCellValueFactory(new PropertyValueFactory<>("retorno"));
        colValidado.setCellValueFactory(new PropertyValueFactory<>("validado"));
        sourceControleCondominioList = new LinkedList<>();

        controleCondominioObservableList = FXCollections.observableArrayList();

        condominioTable.setItems(controleCondominioObservableList);

        colEnvio.setCellFactory(this::getBooleanImageCellFactory);
        colRetorno.setCellFactory(this::getBooleanImageCellFactory);
        colValidado.setCellFactory(this::getBooleanImageCellFactory);

        this.emailService = new EmailBroadcastService(SettingsManager.getInstance().getSettings());

        vBoxProgressBar.setManaged(false);
        vBoxProgressBar.setVisible(false);
    }

    private TableCell<ControleCondominio, Boolean> getBooleanImageCellFactory(TableColumn<ControleCondominio, Boolean> controleCondominioBooleanTableColumn) {
        return new TableCell<ControleCondominio, Boolean>() {
            private ImageView OkImageView;
            private ImageView failImageView;

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                try(InputStream okIs = Objects.requireNonNull(App.class.getResource("img/ok-15.png")).openStream();
                    InputStream failIs = Objects.requireNonNull(App.class.getResource("img/fail-15.png")).openStream()) {
                    OkImageView = new ImageView(new Image(okIs));
                    OkImageView.setSmooth(true);
                    OkImageView.setFitHeight(15d);
                    OkImageView.setFitWidth(15d);
                    failImageView = new ImageView(new Image(failIs));
                    failImageView.setSmooth(true);
                    failImageView.setFitHeight(15d);
                    failImageView.setFitWidth(15d);
                } catch (IOException e) {
                    getLogger().error(e);
                }
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item) {
                        setGraphic(OkImageView);
                    } else {
                        setGraphic(failImageView);
                    }
                }
            }
        };
    }

    private void addEntry() {
        // Example of adding a new entry (you can replace this with a dialog to get user input)
        ControleCondominio newEntry = new ControleCondominio(
                "ImovelLocado1", "Imovel1", "Locatario1", "Locador1",
                "email@example.com", "Admin", true, false, true
        );
        controleCondominioObservableList.add(newEntry);
    }

    private void removeSelectedEntry() {
        ControleCondominio selectedEntry = condominioTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            controleCondominioObservableList.remove(selectedEntry);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an entry to remove.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public Logger getLogger() {
        return LogManager.getLogger(EmailExcelController.class);
    }

    @FXML
    public void escolherArquivo(){
        getLogger().info("Opening file chooser");
        FileChooser fileChooser = buildFileChooser();

        this.disable();
        File selectedFile = fileChooser.showOpenDialog(App.getApp().getMainStage());

        try {
            if (selectedFile != null) {
                getLogger().info("File selected.");
                getLogger().debug("Selected file: " + selectedFile);
                savePreferredFolder(selectedFile);
                loadFileContent(selectedFile);
                updateSearchBarStyle(selectedFile);
            } else {
                getLogger().info("No file selected");
                this.btnEscolherArquivo.setDisable(false);
            }
        }catch(Exception e){
            updateSearchBarStyle(null);
            throw e;
        }finally {
            this.setDisable(controleCondominioObservableList.isEmpty());
            this.btnEscolherArquivo.setDisable(false);
        }
    }

    private FileChooser buildFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo");

        String initialFolder = getInitialFolder();

        getLogger().debug("Opening initial folder at: "+initialFolder);
        fileChooser.setInitialDirectory(new File(initialFolder));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos de Excel", "*.xlsx")
        );
        return fileChooser;
    }

    private static String getInitialFolder() {
        String previousFolder = SettingsManager.getInstance().getSettings().getLastExcelFileFolder();
        String initialFolder;
        if(previousFolder == null || previousFolder.trim().isEmpty()) {
            initialFolder = System.getProperty("user.home");
        }else{
            initialFolder = previousFolder;
        }
        return initialFolder;
    }

    private void loadFileContent(File selectedFile) {
        ExcelReader reader = new ExcelReader();
        sourceControleCondominioList = reader.readFile(selectedFile.getAbsolutePath());
        getLogger().debug("Itens encontrados: "+sourceControleCondominioList.size());
        filterList();
    }

    private void setFilteredList(List<ControleCondominio> controleCondominios){
        controleCondominioObservableList.clear();
        controleCondominioObservableList.addAll(controleCondominios);
    }

    private void updateSearchBarStyle(File selectedFile) {
        if(selectedFile!=null) {
            lbFilePath.setText(selectedFile.getAbsolutePath());
            lbFilePath.setStyle("-fx-padding: 0 10 0 10; -fx-border-color: #BB3333; -fx-border-width: 1px; -fx-border-radius: 5px 0 0 5px; -fx-background-color: #E0E0E0");

            btnEscolherArquivo.getStyleClass().clear();
            btnEscolherArquivo.getStyleClass().add("button-r-round");
        }else{
            lbFilePath.setText("");
            lbFilePath.setStyle("");

            btnEscolherArquivo.getStyleClass().clear();
            btnEscolherArquivo.getStyleClass().add("button");
        }
    }

    private static void savePreferredFolder(File selectedFile) {
        SettingsManager.getInstance().getSettings().setLastExcelFileFolder(selectedFile.getParent());
        SettingsManager.getInstance().saveSettings();
    }

    private void enviarEmailsConcluidoTransition(){
        FadeTransition ft = new FadeTransition(Duration.seconds(.5), hBoxSaveConfirmation);
        ft.setFromValue(0);
        ft.setToValue(1);

        FadeTransition ftOut = new FadeTransition(Duration.seconds(.5), hBoxSaveConfirmation);
        ftOut.setFromValue(1);
        ftOut.setToValue(0);
        ftOut.setOnFinished((ev) -> {
            enable();
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

    private void enable(){
        this.setDisable(false);
    }

    private void disable(){
        this.setDisable(true);
    }

    private void setDisable(boolean value){
        cbEnviados.setDisable(value);
        cbValidados.setDisable(value);
        cbRetornados.setDisable(value);
        btnEnviarEmail.setDisable(value);
        lbQtdRegistros.setDisable(value);
        condominioTable.setDisable(value);
        btnEscolherArquivo.setDisable(value);
        lbQtdRegistrosValue.setDisable(value);
    }

    private void disableAll(){
        setDisableAll(true);
    }

    private void enableAll(){
        setDisableAll(false);
    }

    private void setDisableAll(boolean value){
        this.setDisable(value);
        App.getApp().getRootSceneController().setDisable(value);
    }
    @FXML
    public void filterList(){
        List<ControleCondominio> filteredList = sourceControleCondominioList.stream().filter(this::getFilter).toList();
        setFilteredList(filteredList);
        lbQtdRegistrosValue.setText(String.valueOf(controleCondominioObservableList.size()));
    }

    public boolean getFilter(ControleCondominio cc){
        boolean isValid = true;
        if(!cbEnviados.isSelected()){
            isValid = isValid && !cc.isEnviado() ;
        }
        if(!cbRetornados.isSelected()){
            isValid = isValid && !cc.isRetorno() ;
        }
        if(!cbValidados.isSelected()){
            isValid = isValid && cc.isValidado() ;
        }
        return isValid;
    }

    @FXML
    public void filterEnviados(){
        filterList();
    }

    @FXML
    public void filterRetornados(){
        filterList();
    }

    @FXML
    public void filterInvalidos(){
        filterList();
    }

    private int maxQtdProcessedEmail;
    private int currentValue;

    @FXML
    public void enviarEmail(ActionEvent actionEvent) {
        disable();
        currentValue = 0;
        pbEmails.setProgress(0);
        vBoxProgressBar.setManaged(true);
        vBoxProgressBar.setVisible(true);

        Map<String,List<String>> groupByEmail = new HashMap<>();
        populateListGroupingImoveisByEmails(groupByEmail);
        try {
            Thread send = buildEmailDeliverThread(groupByEmail);
            send.start();
        }catch (Exception e){
            getLogger().error(e);
            throw e;
        }
        maxQtdProcessedEmail = groupByEmail.size();
    }

    private Thread buildEmailDeliverThread(Map<String, List<String>> groupByEmail) {
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String, List<String>> e : groupByEmail.entrySet()){
                    emailService.sendSimpleEmail(e.getKey(), buildEmailMessage(e.getValue()), SettingsManager.getInstance().getSettings().getMailSignature());
                    currentValue++;
                    Platform.runLater(() -> pbEmails.setProgress((double) currentValue/maxQtdProcessedEmail));
                }

                vBoxProgressBar.setVisible(false);
                Platform.runLater(() -> enviarEmailsConcluidoTransition());
            }
        });
        send.setName("Email Deliver");
        App.getApp().setLoadingThread(send);
        return send;
    }

    private void populateListGroupingImoveisByEmails(Map<String, List<String>> groupByEmail) {
        for(ControleCondominio cc:controleCondominioObservableList){
            List<String> imoveis = groupByEmail.get(cc.getEmail());
            if(CollectionUtils.isEmpty(imoveis)){
                imoveis = new ArrayList<>();
                imoveis.add(cc.getImovel());
                groupByEmail.put(cc.getEmail(),imoveis);
            }else{
                imoveis.add(cc.getImovel());
            }
        }
    }

    private String buildEmailMessage(List<String> imoveis) {
        String imoveisStr = imoveisToString(imoveis);

        String message = SettingsManager.getInstance().getSettings().getMailBody();
        message = message.replaceAll("\\{imoveis\\}", imoveisStr);
        return message;
    }

    private String imoveisToString(List<String> imoveis) {
        StringBuilder sb =new StringBuilder();
        for (String imovel : imoveis) {
            sb.append("<li>").append(imovel).append("</li>");
        }
        return "</ul>"+sb+"</ul>";
    }
}
