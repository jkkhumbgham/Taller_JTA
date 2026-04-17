package org.eclipse.jakarta.consumer;

import com.rabbitmq.client.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class CorreoConsumer {

    private static final String QUEUE_NAME    = "cola.correos";
    private static final String RABBITMQ_HOST = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
    private static final String MAIL_HOST     = System.getenv().getOrDefault("MAIL_HOST", "smtp.gmail.com");
    private static final String MAIL_PORT     = System.getenv().getOrDefault("MAIL_PORT", "587");
    private static final String MAIL_USER     = System.getenv().getOrDefault("MAIL_USER", "tucorreo@gmail.com");
    private static final String MAIL_PASS     = System.getenv().getOrDefault("MAIL_PASS", "tupassword");

    public static void main(String[] args) throws Exception {
        System.out.println("[Consumer] Conectando a RabbitMQ: " + RABBITMQ_HOST);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);

        Connection connection = null;
        for (int i = 0; i < 10; i++) {
            try {
                connection = factory.newConnection();
                break;
            } catch (Exception e) {
                System.out.println("[Consumer] RabbitMQ no listo, reintentando en 5s...");
                Thread.sleep(5000);
            }
        }
        if (connection == null) throw new RuntimeException("No se pudo conectar a RabbitMQ.");

        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(1);

        System.out.println("[Consumer] Escuchando cola: " + QUEUE_NAME);

        DeliverCallback callback = (tag, delivery) -> {
            String msg = new String(delivery.getBody(), "UTF-8");
            try {
                String[] p = msg.split("\\|", 3);
                enviarCorreo(p[0], p[1], p[2]);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                e.printStackTrace();
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, callback, t -> {});
        Thread.currentThread().join();
    }

    private static void enviarCorreo(String dest, String asunto, String body) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_USER, MAIL_PASS);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(MAIL_USER));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest));
        msg.setSubject(asunto);
        msg.setText(body);
        Transport.send(msg);
        System.out.println("[Consumer] Correo enviado a: " + dest);
    }
}