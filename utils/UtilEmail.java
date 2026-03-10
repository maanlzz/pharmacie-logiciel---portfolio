package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe utilitaire pour envoyer des emails via le protocole SMTP en utilisant JavaMail (Jakarta Mail).
 * Cette classe permet d'envoyer des messages textes simples à un destinataire donné, depuis une adresse email.
 * Elle utilise une session SMTP sécurisée (TLS) avec authentification.
 *
 * @author Victoria MASSAMBA
 * Date de première modification : 31/03/25
 * Date de dernière modification : 02/04/25
 */

public class UtilEmail {

    /**
     * Méthode qui envoie un email simple en texte à un destinataire donné.
     *
     * @param destinataire  Adresse email du destinataire
     * @param sujet         Objet de l'email
     * @param messageTexte  Contenu du message à envoyer
     *
     * @author Victoria MASSAMBA
     * Date de première modification : 31/03/25
     * Date de dernière modification : 02/04/25
     */
    public static void envoyerEmail(String destinataire, String sujet, String messageTexte) {
        Properties config = new Properties();

        try (InputStream input = UtilEmail.class.getClassLoader().getResourceAsStream("config.properties")) {

            config.load(input);

            String emailExpediteur = config.getProperty("email");
            String motDePasse = config.getProperty("password_email");

            if (emailExpediteur == null || motDePasse == null) {
                throw new IllegalStateException("L'adresse email ou le mot de passe est manquant");
            }

            // Configuration de la session SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailExpediteur, motDePasse);
                }
            });

            // Création et envoi du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailExpediteur));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(messageTexte);

            Transport.send(message);
            System.out.println("Email envoyé à " + destinataire);

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier config.properties : " + e.getMessage());
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}