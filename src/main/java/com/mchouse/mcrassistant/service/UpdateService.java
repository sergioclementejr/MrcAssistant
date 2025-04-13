package com.mchouse.mcrassistant.service;

import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@AllArgsConstructor
public class UpdateService {
    private final static String POM_GITHUB_PATH = "https://github.com/sergioclementejr/MrcAssistant/blob/main/pom.xml"; //TODO: remove literal

    public boolean hasToUpdate() {
        String appCurrentVersion = getCurrentVersion();
        String appGitHubVersion = getAppGitHubVersion(POM_GITHUB_PATH);
        getLogger().info("appCurrentVersion: " + appCurrentVersion);
        getLogger().info("appGitHubVersion: " + appGitHubVersion);

        try {
            long appVersion = Long.parseLong(appCurrentVersion.replaceAll("\\.", ""));
            long gitHubVersion = Long.parseLong(appGitHubVersion.replaceAll("\\.", ""));
            return appVersion<gitHubVersion;
        }catch(NumberFormatException e){
            getLogger().error("Error while trying to check versions match for update");
            return false;
        }
    }

    private String getCurrentVersion() {
        return loadProjectInfo().get("project.version").toString();
    }

    private Properties loadProjectInfo() {
        Properties props = new Properties();

        try (InputStream input = UpdateService.class.getClassLoader()
                .getResourceAsStream("project-info.properties")) {

            if (input == null) {
                System.out.println("Arquivo project-info.properties não encontrado.");
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            getLogger().error(e);
            throw new RuntimeException("Error while trying to read property \"project.version\".");
        }

        return props;
    }

    private String getAppGitHubVersion(String UrlPath) {
        getLogger().trace("Getting app remote version from url: " + UrlPath);
        HttpURLConnection conn = null;
        try {
            conn = buildAndOpenConnection(UrlPath);

            String pomContent = getAppPomFromGitHub();
            Document pomXmlDocument = extractDocumentFromPomContent(pomContent);

            return extractProjectVersionNumber(pomXmlDocument);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            getLogger().error("It was not possible to check for updates because it wasn't possible to extract GitHub app version");
            getLogger().error(e);
            return "NoVersionFound";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static Document extractDocumentFromPomContent(String pomContent) throws ParserConfigurationException, SAXException, IOException {
        InputSource is = new InputSource(new StringReader(pomContent));
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document pomXmlDocument = dBuilder.parse(is);
        pomXmlDocument.getDocumentElement().normalize();
        return pomXmlDocument;
    }

    private static String extractProjectVersionNumber(Document pomXmlDocument) {
        Element rootElement = pomXmlDocument.getDocumentElement();
        String version = getElementValue(rootElement, "version");
        if (version == null) {
            Element parent = (Element) rootElement.getElementsByTagName("parent").item(0);
            if (parent != null) {
                version = getElementValue(parent, "version");
            }
        }
        return version;
    }

    private static HttpURLConnection buildAndOpenConnection(String UrlPath) throws IOException {
        URL url = new URL(UrlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return conn;
    }

    private static String getElementValue(Element parent, String tagName) {
        if (parent.getElementsByTagName(tagName).getLength() == 0) return null;
        return parent.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private Logger getLogger() {
        return LogManager.getLogger(EmailBroadcastService.class);
    }

    public String getAppPomFromGitHub() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/sergioclementejr/MrcAssistant/master/pom.xml")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                getLogger().warn("Failed to download: " + response.code());
                return "[ERRO] code="+ response.code();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
