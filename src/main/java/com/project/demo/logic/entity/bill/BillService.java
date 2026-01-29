package com.project.demo.logic.entity.bill;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.FormatterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BillService {

    @Autowired
    private UserRepository userRepository;

    public static String generateHtmlContent(String body) {
        String htmlContent =
                "<html>" +
                "    <head>"+
                "        <style>" +
                "            p { font-size: 14px;}" +
                "       </style>" +
                "    </head>" +
                "    <body>" +
                body  +
                "    </body>" +
                "</html>";

        return htmlContent;
    }

    public String generateBillBody(TblTransaction transaction) {
        Optional<TblUser> foundUser = userRepository.findById(transaction.getUser().getId());
        if (foundUser.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        String userName = foundUser.get().getName() + " " + foundUser.get().getLastName1();
        LocalDateTime date = transaction.getCreationDate() != null ? transaction.getCreationDate() : null;
        String formattedDate = FormatterUtil.formatDate(date, "dd/MM/yyyy");
        String subTotal = transaction.getSubTotal().toString();
        String tax = transaction.getTax().toString();
        String total = transaction.getTotal().toString();

        StringBuilder publicationsRows = new StringBuilder();
        if (transaction.getPublications() != null && !transaction.getPublications().isEmpty()) {
            for (TblPublication publication : transaction.getPublications()) {
                publicationsRows.append(
                        "<tr>" +
                        "    <td style=\"padding: 8px; border: 1px solid #ddd;\">" + publication.getTitle() + "</td>" +
                        "    <td style=\"padding: 8px; border: 1px solid #ddd;\">" + publication.getSpecie() + "</td>" +
                        "    <td style=\"padding: 8px; border: 1px solid #ddd;\">" + publication.getPrice() + "</td>" +
                        "</tr>"
                );
            }
        }

        // Construcción del cuerpo del correo
        String body =
                "<div style=\"width: 100%; max-width: 800px; margin: auto; font-family: Arial, sans-serif;\">" +
                "    <div style=\"background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">" +
                "        <div style=\"text-align: center; margin-bottom: 20px;\">" +
                "            <p style=\"color: #77c040; display: inline; font-size: 22px;\">Rural <span style=\"color: #333333;\">Animal</span></p>" +
                "        </div>" +
                "        <h2 style=\"color: #77c040; text-align: center;\">Hola " + userName + "</h2>" +
                "        <p style=\"text-align: center; font-size: 16px;\">A continuación, le detallamos su factura:</p>" +
                "        <table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">" +
                "            <tr>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">Fecha:</td>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">" + formattedDate + "</td>" +
                "            </tr>" +
                "            <tr>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">Sub total:</td>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">₡ " + subTotal + "</td>" +
                "            </tr>" +
                "            <tr>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">Impuesto:</td>" +
                "                <td style=\"padding: 8px; border: 1px solid #ddd;\">₡ " + tax + "</td>" +
                "            </tr>" +
                "        </table>";

        if (publicationsRows.length() > 0) {
            body +=
                    "<h3 style=\"color: #77c040;\">Publicaciones adquiridas:</h3>" +
                    "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">" +
                    "    <tr>" +
                    "        <th style=\"padding: 8px; border: 1px solid #ddd; background-color: #f2f2f2;\">Título</th>" +
                    "        <th style=\"padding: 8px; border: 1px solid #ddd; background-color: #f2f2f2;\">Especie</th>" +
                    "        <th style=\"padding: 8px; border: 1px solid #ddd; background-color: #f2f2f2;\">Precio</th>" +
                    "    </tr>" +
                    publicationsRows +
                    "</table>";
        }

        body +=
                "<h3 style=\"text-align: right; color: #77c040;\">Total: ₡ " + total + "</h3>" +
                "    </div>" +
                "</div>";

        return body;
    }
}
