package com.example.myapplication;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSenderUtil {

    // Replace with your actual sender email and Google App Password
    // Must be the exact Gmail address you used to generate the App Password
    private static final String SENDER_EMAIL = "saisanjaypatil01@gmail.com";

    // Paste the 16-character App Password here (no spaces between letters)
    private static final String SENDER_PASSWORD = "iohjjvenbqbsioqj";

    public static void sendOtpEmail(String recipientEmail, String otpCode) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Your Vision Triage Security Code");

        String emailBody = "Hello,\n\n"
                + "Your OTP for secure access is: " + otpCode + "\n\n"
                + "Please enter this code in the app to continue.\n\n"
                + "Securely,\nVision Triage App";

        message.setText(emailBody);

        Transport.send(message);
    }
}