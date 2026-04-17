package org.eclipse.jakarta.Util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.ejb.Stateless;

@Stateless
public class Correos {

    private static final String QUEUE_NAME = "cola.correos";
    private static final String RABBITMQ_HOST = "rabbitmq";

    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(5672);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                String mensaje = destinatario + "|" + asunto + "|" + contenido;
                channel.basicPublish("", QUEUE_NAME, null, mensaje.getBytes("UTF-8"));
                System.out.println("[RabbitMQ] Publicado para: " + destinatario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}