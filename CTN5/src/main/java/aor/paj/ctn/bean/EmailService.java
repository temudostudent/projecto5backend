package aor.paj.ctn.bean;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

@Singleton
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    // Mail SMTP credentials
    private final String username = "cesardev00@gmail.com";
    private final String password = "loqt hhig vbji ygql";

    // Mail SMTP host and port
    private final String host = "smtp.gmail.com";
    private final String port = "587";

    @Resource(name = "mail/session")
    private Session session;

    public EmailService() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host); // SMTP host
        props.put("mail.smtp.port", port); // SMTP port

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendPasswordResetEmail(String toEmail, String username, String resetURL) throws MessagingException {
        String subject = "Password Reset Request";
        String body = "Hello " + username + ",<br><br>" +
                "A request has been received to change the password for your Agileflow account. <br><br>" +
                "Please click on the link. <br><br>" +
                "<a href=\"" + resetURL + "\">Reset Password</a><br><br>" +
                "Thank You,<br>" +
                "The Agileflow Team";

        //sendEmail(toEmail, subject, body);
        sendEmail("cesardev00@gmail.com", subject, body);
    }

    public void sendAccountConfimationEmail(String toEmail, String username, String confirmURL) throws MessagingException {
        String subject = "Agileflow Confirmation Request";
        String body = "Hello " + username + ",<br><br>" +
                "It's time to verify your Agileflow account!<br><br>" +
                "Just confirm your account and we're officially friends."+
                "<a href=\"" + confirmURL + "\">Confirm your account</a><br><br>" +
                "Thank You,<br>" +
                "The Agileflow Team";

        //sendEmail(toEmail, subject, body);
        sendEmail("cesardev00@gmail.com", subject, body);
    }


    public void sendEmail(String to, String subject, String body) {

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            message.setSubject(subject);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, "text/html; charset=utf-8"); // Set content type as HTML

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("email send to " + to);
            System.out.println(body);
        } catch (MessagingException e) {
            logger.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }
    }
}