package com.mchouse.mcrassistant.config;

import com.mchouse.mcrassistant.model.Settings;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SettingsManager {
    private static final String NO_EMAIL="noemail@noemail.no";
    private static final String NO_TOKEN="notoken";
    private final String configFileBaseDir;
    private static final String CONFIG_FILE_DIR = "\\AppData\\Roaming\\MRCAssistant";
    private static final String FILE_NAME = "config.ser";
    private static SettingsManager configManager;

    @Setter
    @Getter
    private Settings settings;

    private SettingsManager() {
        this.configFileBaseDir = System.getProperty("user.home") + CONFIG_FILE_DIR;
    }

    public static SettingsManager getInstance() {
        if (configManager == null) {
            configManager = new SettingsManager();
        }
        return configManager;
    }

    public void loadConfig() {
        try {
            this.loadConfigImpl();
        } catch (IOException e) {
            getLogger().error("Error while loading configuration file", e);
            throw new RuntimeException(e);
        }
    }

    public void saveSettings() {
        try {
            this.saveConfigImpl();
        } catch (FileNotFoundException e) {
            getLogger().error("Error while saving configuration file", e);
            throw new RuntimeException(e);
        }
    }

    private String getFullFileName() {
        return configFileBaseDir + "\\" + FILE_NAME;
    }

    private void loadConfigImpl() throws IOException {
        getLogger().debug("Loading configuration file at: " + getFullFileName());
        File configFile = new File(getFullFileName());
        if (configFile.exists()) {
            loadConfigFile(configFile);
        } else {
            createNewConfigFile();
        }
    }

    private void createNewConfigFile() throws FileNotFoundException {
        getLogger().warn("Config file not found!");
        getLogger().warn("Trying to create a new file...");
        this.settings = buildNewConfiguration();
        this.saveConfigImpl();
    }

    private void loadConfigFile(File configFile) throws IOException {
        getLogger().debug("Config file found.");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile))) {
            this.settings = (Settings) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            getLogger().error(e);
            createNewConfigFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile))) {
                this.settings = (Settings) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                getLogger().error(ex);
                throw new IOException(ex);
            }
        }
    }

    private static Settings buildNewConfiguration() {
        return Settings.builder()
                .sourceEmail(NO_EMAIL)
                .altSourceEmail(NO_EMAIL)
                .sourceEmailToken(NO_TOKEN)
                .mailSubject("N/A")
                .mailBody("n/a")
                .mailSignature("NO_SIGNATURE")
                .build();
    }

    private void saveConfigImpl() throws FileNotFoundException {
        getLogger().debug("Saving configuration file at: " + getFullFileName());
        File configFile = new File(getFullFileName());
        deleteConfigFileIfExists(configFile);
        writeConfigFile(configFile);

    }

    private void deleteConfigFileIfExists(File configFile) {
        if (configFile.exists()) {
            boolean result = configFile.delete();
            if (!result) {
                getLogger().error("Config file could not be deleted at: " + getFullFileName());
                throw new RuntimeException("Config file could not be deleted");
            }
        }
    }

    private void writeConfigFile(File configFile) throws FileNotFoundException {
        try {
            Files.createDirectories(Paths.get(configFileBaseDir));
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
        } catch (IOException e) {
            throwFileCreationException(e);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(configFile))) {
            oos.writeObject(this.settings);
            getLogger().info("Config file saved!");
        } catch (IOException e) {
            throwFileCreationException(e);
        }
    }

    private void throwFileCreationException(Exception e) throws FileNotFoundException {
        String errorMsg = "Config file could not be created at: " + getFullFileName();
        getLogger().error(errorMsg, e);
        throw new FileNotFoundException(errorMsg);
    }

    private void throwFileCreationException() throws FileNotFoundException {
        String errorMsg = "Config file could not be created at: " + getFullFileName();
        getLogger().error(errorMsg);
        throw new FileNotFoundException(errorMsg);
    }

    public boolean isSettingsValid(){
        Settings s = this.getSettings();
        return s.getSourceEmail()!=null && !s.getSourceEmail().trim().isEmpty() && !NO_EMAIL.equalsIgnoreCase(s.getSourceEmail())
                && s.getSourceEmailToken()!=null && !s.getSourceEmailToken().trim().isEmpty() && !NO_TOKEN.equalsIgnoreCase(s.getSourceEmailToken())
                && s.getMailBody()!=null && !s.getMailBody().trim().isEmpty()
                && s.getMailSubject()!=null && !s.getMailSubject().trim().isEmpty();

    }

    private static Logger getLogger() {
        return LogManager.getLogger(SettingsManager.class);
    }
}
