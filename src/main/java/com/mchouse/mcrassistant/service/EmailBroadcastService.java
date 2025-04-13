package com.mchouse.mcrassistant.service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.mchouse.mcrassistant.config.SettingsManager;
import com.mchouse.mcrassistant.controller.EmailExcelController;
import com.mchouse.mcrassistant.model.Settings;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.soap.MimeHeader;

@AllArgsConstructor
public class EmailBroadcastService {
    private final String mainMail;
    private final String altMail;
    private final String token;
    private final String emailSubject;

    public EmailBroadcastService() {
        this(SettingsManager.getInstance().getSettings());
    }

    public EmailBroadcastService(Settings settings){
        super();
        this.mainMail = settings.getSourceEmail();
        this.altMail = settings.getAltSourceEmail();
        this.token = settings.getSourceEmailToken();
        this.emailSubject = settings.getMailSubject();
    }

    public void sendSimpleEmail(String recipient, String emailMessage, String ass) {
        getLogger().debug("Sending email to: "+recipient);
        getLogger().info("Opening session...");
        Session session = null;
        session = getSession();
        getLogger().info("Session open");
//        System.setProperty("mail.mime.address.map", "");
        try {
            getLogger().info("Building message instance...");
            MimeMessage message = getMessageInstance(recipient, session);
            getLogger().info("Message instance build");
            getLogger().info("Moisturising message data...");
            message.setSubject(emailSubject);
            message.setText(complementMessage(emailMessage, ass), "utf-8", "html");
            getLogger().info("Message data completed");
            getLogger().info("Sending email...");
            Transport.send(message);
            getLogger().info("Email sent");
        } catch (MessagingException|UnsupportedEncodingException e) {
            getLogger().error(e);
        }
    }

    private String complementMessage(String emailMessage, String ass) {
        return "<html>" +
                "<body>" +
                emailMessage.replaceAll("\n", "<br/>") +
                "<br/><br/><br/>" +
                addSignature(ass) +
                "</body>" +
                "</html>";
    }

    private String addSignature(String ass) {
        return "<p style='color: red; display:inline;'><b>" +
                ass.replaceAll("\n","<br/>") +
                "</b><br/>"+
                "<b href=\"https://www.mrcimoveis.com.br\" sytle=\"color:#0000FF\">www.mrcimoveis.com.br</b></p>";
    }

    private MimeMessage getMessageInstance(String recipient, Session session) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(mainMail, "Administrativo - MRC Imóveis"));
        message.setReplyTo(getReplyTo());
        message.setFrom(new InternetAddress(altMail));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipient)
        );
        return message;
    }

    private Address[] getReplyTo() throws AddressException {
        Address[] addresses = new Address[1];
        addresses[0] = new InternetAddress(altMail);
        return addresses;
    }

    private Logger getLogger(){
        return LogManager.getLogger(EmailBroadcastService.class);
    }

    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Ativar TLS
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", 587);
        try {
            return Session.getInstance(props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(mainMail, token);
                        }
                    });
        }catch(Exception ex){
            getLogger().error(ex);
            throw new RuntimeException(ex);
        }
    }
}