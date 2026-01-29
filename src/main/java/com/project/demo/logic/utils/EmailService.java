package com.project.demo.logic.utils;

import com.project.demo.logic.entity.bid.TblBid;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String sender, String recipient, String subject, String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(sender);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(body, true);

        javaMailSender.send(message);
    }

    public String sendBillEmail(Long userId, String body) {
        Optional<TblUser> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        String recipient = foundUser.get().getEmail();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "Factura de compra";

        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return recipient;
    }

    public String notificateAuctionWinner(TblBid bid, TblPublication tblPublication) {
        Optional<TblUser> foundUser = userRepository.findById(bid.getUser().getId());
        if (foundUser.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        String recipient = foundUser.get().getEmail();
        String userName = foundUser.get().getName() + " " + foundUser.get().getLastName1();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "Has ganado la subasta";
        String paymentUrl = "http://localhost:4200/";
        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040;\">Felicidades " + userName + "!</h2>" +
                "        <p style=\"font-size: 16px;\">Usted ha sido el ganador de la subasta \"" + tblPublication.getTitle() + "\".</p>"+
                "        <p style=\"font-size: 16px;\">En el siguiente enlace podrás realizar el pago para continuar con la compra.</p>"+
                "        <br/>"+
                "        <a href=\"" + paymentUrl + "\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Realizar pago</button></a>"+
                "        <br/><br/>"+
                "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                "   </div>"+
                "</div/>";
        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return recipient;
    }

    public void notificatePublicationOwner(TblPublication foundPublication) {
        Optional<TblUser> foundUser = userRepository.findById(foundPublication.getUser().getId());
        if (foundUser.isEmpty()) {
            throw new IllegalArgumentException("Dueño de publicacion no encontrado no encontrado.");
        }

        String recipient = foundUser.get().getEmail();
        String userName = foundUser.get().getName() + " " + foundUser.get().getLastName1();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "Tu animal ha sido vendido";
        String baseUrl = "http://localhost:4200/";
        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040;\">Felicidades " + userName + "!</h2>" +
                "        <p style=\"font-size: 16px;\">Tu animal ha sido vendido, mediante el siguiente botón podrás actualizar el estado del envío.</p>"+
                "        <br/>"+
                "        <a href=\"" + baseUrl + "\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Actualizar envío</button></a>"+
                "        <br/><br/>"+
                "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                "   </div>"+
                "</div/>";
        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void notificateAuctionBidder(TblBid bid, TblPublication tblPublication) {
        Optional<TblUser> foundUser = userRepository.findById(bid.getUser().getId());
        if (foundUser.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        String recipient = foundUser.get().getEmail();
        String userName = foundUser.get().getName() + " " + foundUser.get().getLastName1();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "La subasta ha finalizado";
        String auctions = "http://localhost:4200/";
        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040;\">Felicidades " + userName + "!</h2>" +
                "        <p style=\"font-size: 16px;\">Lamentablemente la subasta \"" + tblPublication.getTitle() + "\" ha finalizado y no has sido el ganador.</p>"+
                "        <p style=\"font-size: 16px;\">Pero no te preocupes, mediante el siguiente botón podrás encontrar nuevas subastas. .</p>"+
                "        <br/>"+
                "        <a href=\"" + auctions + "\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Ir a subastas</button></a>"+
                "        <br/><br/>"+
                "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                "   </div>"+
                "</div/>";
        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void authenticateEmail(TblUser user) {
        String recipient = user.getEmail();
        String userName = user.getName() + " " + user.getLastName1();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "Confirmación de correo electrónico";
        String baseUrl = "http://localhost:4200/autenticate/";
        String urlConfirmation = baseUrl +  "?id=" + user.getId();

        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040;\">Estimado " + userName + "!</h2>" +
                "        <p style=\"font-size: 16px;\">Por favor haz click en el siguiente botón para confirmar tu correo electrónico:</p>"+
                "        <br/>"+
                "        <a href=\"" + urlConfirmation + "\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Confirmar</button></a>"+
                "        <br/><br/>"+
                "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                "   </div>"+
                "</div/>";

        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public String notificateAppointment(VeterinaryAppointmentDto createdAppointment, Long userId) {
        try {
            Optional<TblUser> foundUser = userRepository.findById(userId);
            if (foundUser.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado.");
            }
            String recipient = foundUser.get().getEmail();
            String userName = foundUser.get().getName() + " " + foundUser.get().getLastName1();

            String doctorFirstName = createdAppointment.getVeterinaryName();
            String doctorLastName1 = createdAppointment.getFirstSurname();
            String doctorLastName2 = createdAppointment.getSecondSurname();
            String patientFirstName = foundUser.get().getName();
            String patientLastName1 = foundUser.get().getLastName1();
            String patientLastName2 = foundUser.get().getLastName2();
            String specialty = createdAppointment.getSpeciality();
            String appointmentDate = FormatterUtil.formatDate(createdAppointment.getStartDate(), "dd/MM/yyyy");
            String appointmentStartTime = FormatterUtil.formatDate(createdAppointment.getStartDate(), "HH:mm");
            String appointmentEndTime = FormatterUtil.formatDate(createdAppointment.getEndDate(), "HH:mm");
            String sender = "ruralanimalcr@gmail.com";
            String subject = "Cita Agendada";

            String body =
                    "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                    "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                    "        <div style=\"margin-bottom: 20px;\">" +
                    "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                    "        </div>" +
                    "        <h2 style=\"color: #77c040;\">¡Hola  " + userName + "!</h2>" +
                    "        <p style=\"font-size: 16px;\">Te informamos los detalles de la cita agendada.</p>"+
                    "            <p style=\"font-size: 16px; line-height: 1.5; text-align: left; color: black;\">" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Nombre del veterinario/a:</span> " + doctorFirstName + " " + doctorLastName1 + " " + doctorLastName2 + "<br>" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Nombre del Paciente:</span> " + patientFirstName + " " + patientLastName1 + " " + patientLastName2 + "<br>" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Especialidad Médica:</span> " + specialty + "<br>" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Sede de Consulta:</span>Rural Animal<br>" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Fecha de inicio de la Cita:</span> " + appointmentDate + "<br>" +
                    "                <span style=\"color: #77C040; font-weight:bold;\">Hora de la Cita:</span> " + appointmentStartTime + " " +appointmentEndTime+
                    "            </p>" +
                    "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                    "   </div>"+
                    "</div/>";

            this.sendEmail(sender, recipient, subject, body);

            return recipient;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public String restorePassword(TblUser user, String tempPassword) {

        String recipient = user.getEmail();
        String userName = user.getName() + " " + user.getLastName1();
        String sender = "ruralanimalcr@gmail.com";
        String subject = "Restablecer contraseña";
        String logInURL = "http://localhost:4200/login/";

        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040;\">Estimado " + userName + "!</h2>" +
                "        <p style=\"font-size: 16px;\">Hemos recibido la solicitud de restablecer contraseña, a continuación encontrará su contraseña temporal.</p>"+
                "        <p style=\"font-size: 16px;\">" + tempPassword + "</p>"+
                "        <br/>"+
                "        <a href=\"" + logInURL + "\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Iniciar sesión</button></a>"+
                "        <br/><br/>"+
                "        <p style=\"font-size: 16px;\">Gracias!</p>"+
                "   </div>"+
                "</div/>";

        try {
            this.sendEmail(sender, recipient, subject, body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return recipient;
    }
}