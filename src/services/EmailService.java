package services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final String fromEmail;

    public EmailService(String smtpHost, String smtpPort,
          String username, String password, String fromEmail) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
    }

    public EmailService() {
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = "587";
        this.username = "vladmun329@gmail.com";
        this.password = "buoywknqkvwsecje";
        this.fromEmail = "vladmun329@gmail.com";
    }

    public void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO,
              InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    public void sendWelcomeEmailToClient(String toEmail, String nickname) {
        String subject = "Вітаємо в Комп'ютерному Клубі!";

        String body = String.format("""
              Вітаємо, %s!
              
              Дякуємо за реєстрацію в нашому Комп'ютерному Клубі!
              
              Ваш обліковий запис успішно створено.
              Email: %s
              
              Тепер ви можете:
              • Поповнити баланс в адміністратора
              • Почати гру на будь-якому вільному комп'ютері
              • Отримувати знижки за лояльність:
                - 10 відвідувань → 5%% знижка
                - 25 відвідувань → 10%% знижка
                - 50 відвідувань → 15%% знижка
                - 100 відвідувань → 20%% знижка
              
              З повагою,
              Команда Комп'ютерного Клубу
              """, nickname, toEmail);

        try {
            sendEmail(toEmail, subject, body);
            System.out.println("✓ Привітальний лист надіслано на " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Помилка надсилання email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmailToAdmin(String toEmail, String login) {
        String subject = "Ваш обліковий запис адміністратора створено";

        String body = String.format("""
              Вітаємо, %s!
              
              Ваш обліковий запис адміністратора успішно створено.
              
              Логін: %s
              Email: %s
              
              Тепер ви маєте доступ до системи адміністрування 
              Комп'ютерного Клубу.
              
              Доступні функції:
              • Управління комп'ютерами та сесіями
              • Управління клієнтами
              • Налаштування тарифів
              • Перегляд статистики
              
              З повагою,
              Система Комп'ютерного Клубу
              """, login, login, toEmail);

        try {
            sendEmail(toEmail, subject, body);
            System.out.println("✓ Привітальний лист надіслано на " + toEmail);
        } catch (MessagingException e) {
            System.err.println("✗ Помилка надсилання email: " + e.getMessage());
        }
    }
}
