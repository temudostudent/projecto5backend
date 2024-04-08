package aor.paj.ctn.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@ApplicationScoped
public class EmailService {

    // Configurações para acesso ao servidor de e-mail
    private final String password = "3%!E^kPT*&4RP^7BF";

    @Resource (mappedName = "java:jboss/mail/Default")
    private Session mailSession;

    // Endereço de e-mail adicional para enviar todos os e-mails
    private static final String ADMIN_EMAIL = "cesartn27@gmail.com";

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Servidor SMTP do Gmail
        props.put("mail.smtp.port", "587"); // Porta SMTP padrão para o Gmail
        mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ADMIN_EMAIL, password);
            }
        });
    }

    public void sendPasswordResetEmail (String toEmail, String username, String resetURL) throws MessagingException {
        String subject = "Password Reset Request";
        String body = "Hello " + username + ",<br><br>" +
                "A request has been received to change the password for your Agileflow account. " +
                "<a href=\"" + resetURL + "\">Reset Password</a><br><br>" +
                "Thank You,<br>" +
                "The Agileflow Team";

        //sendEmail(toEmail, subject, body);
        sendEmail(ADMIN_EMAIL, subject, body);
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException{
        try {
            Message message = new MimeMessage(mailSession);
            message.setSubject(subject);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (AddressException e) {
            throw new MessagingException("Endereço de e-mail inválido", e);
        }
    }
}