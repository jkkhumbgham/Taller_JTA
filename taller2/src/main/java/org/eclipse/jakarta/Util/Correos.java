package org.eclipse.jakarta.Util;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class Correos {
    @Resource(lookup = "java:jboss/mail/Default")
    private Session mailSession;

    public void enviarCorreo(String destinatario, String asunto, String contenido) {
    try {
        Message message = new MimeMessage(mailSession);
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(destinatario)
        );
        message.setSubject(asunto);
        message.setText(contenido);

        Transport.send(message);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
