package controllers.comptes;

import database.LogDAO;
import database.PersonnelDAO;
import global.NavigationManager;
import global.SessionManager;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import models.Log;
import models.Personnel;
import utils.UtilAlert;
import javafx.fxml.FXML;

import javafx.scene.control.PasswordField;
import utils.UtilPasswordVisibility;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;


/**
 * Contrôleur JavaFX de la vue de changement de mot de passe.
 * Cette classe gère les interactions entre l'utilisateur et l'interface graphique dédiée
 * à la modification de mot de passe. Elle permet notamment de valider le changement de mot de passe,
 * de vérifier les champs saisis et d'afficher ou masquer les champs de mot de passe.
 *
 * @author Nicolas ADAMCZYK
 * @author Jason DARIUS
 * @since 24/02/2025
 * @version 03/04/2025
 */
public class CompteChangerMdpController implements Initializable {

    @FXML
    private PasswordField nouveauMdpPasswordField;

    @FXML
    private TextField nouveauMdpTextField;

    @FXML
    private PasswordField confirmerMdpPasswordField;

    @FXML
    private TextField confirmerMdpTextField;

    @FXML
    private PasswordField AncienMdpPasswordField;

    @FXML
    private TextField AncienMdpTextField;

    @FXML
    private ImageView oeilAncienMdp;

    @FXML
    private ImageView oeilNouveauMdp;

    @FXML
    private ImageView oeilConfirmerMdp;

    private UtilPasswordVisibility ancienMdpVisibility;
    private UtilPasswordVisibility nouveauMdpVisibility;
    private UtilPasswordVisibility confirmerMdpVisibility;

    /**
     * Initialise les comportements de visibilité pour chaque champ de mot de passe.
     *
     * @param location  l'URL de localisation pour les ressources
     * @param resources les ressources pour l'internationalisation
     * @author Nicolas ADAMCZYK
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ancienMdpVisibility = new UtilPasswordVisibility(AncienMdpPasswordField, AncienMdpTextField, oeilAncienMdp);
        nouveauMdpVisibility = new UtilPasswordVisibility(nouveauMdpPasswordField, nouveauMdpTextField, oeilNouveauMdp);
        confirmerMdpVisibility = new UtilPasswordVisibility(confirmerMdpPasswordField, confirmerMdpTextField, oeilConfirmerMdp);
        descativerCopierColler();
    }

    /**
     * Gère l'affichage ou le masquage du champ ancien mot de passe.
     *
     * @param event événement de clic sur l'icône
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void toggleAncienMdpVisibility(MouseEvent event) {
        ancienMdpVisibility.toggleVisibility();
    }

    /**
     * Gère l'affichage ou le masquage du champ nouveau mot de passe.
     *
     * @param event événement de clic sur l'icône
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void toggleNouveauMdpVisibility(MouseEvent event) {
        nouveauMdpVisibility.toggleVisibility();
    }

    /**
     * Gère l'affichage ou le masquage du champ confirmation du mot de passe.
     *
     * @param event événement de clic sur l'icône
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void toggleConfirmerMdpVisibility(MouseEvent event) {
        confirmerMdpVisibility.toggleVisibility();
    }

    /**
     * Vérifie les champs saisis, valide l'ancien mot de passe,
     * et effectue la mise à jour du mot de passe si tout est correct.
     * Affiche des alertes en cas d’erreur ou de succès.
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void boutonValiderHandler() {
        String ancien = AncienMdpPasswordField.getText();
        String nouveau = nouveauMdpPasswordField.getText();
        String confirmer = confirmerMdpPasswordField.getText();

        Personnel utilisateur = SessionManager.getCurrentPersonnel();

        if (utilisateur == null) {
            UtilAlert.erreurAlert("Aucun utilisateur connecté", "Erreur", null, "Fermer");
            return;
        }

        try {
            Personnel verif = PersonnelDAO.authentifier(utilisateur.getLogin(), ancien);
            if (verif == null) {
                UtilAlert.erreurAlert("L'ancien mot de passe est incorrect", "Erreur d'identification", null, "Ok");
                return;
            }

            if (ancien.equals(nouveau)) {
                UtilAlert.erreurAlert("Le nouvel mot de passe ne peut pas être identique à l'ancien", "Erreur de saisie", null, "Ok");
                return;
            }

            if (!nouveau.equals(confirmer)) {
                UtilAlert.erreurAlert("Les mots de passe ne correspondent pas", "Erreur de saisie", null, "Ok");
                return;
            }

            PersonnelDAO.modifierMotDePasse(utilisateur.getId(), nouveau);
            UtilAlert.informationAlert("Votre mot de passe a bien été modifié", "Succès", null, "Ok");

            AncienMdpPasswordField.clear();
            nouveauMdpPasswordField.clear();
            confirmerMdpPasswordField.clear();

            Log log = new Log(Log.LogAction.MODIFIER_MOT_DE_PASSE, LocalDateTime.now(),utilisateur.getId());
            LogDAO.ajouterLog(log);

            NavigationManager.goBack();

        } catch (SQLException e) {
            UtilAlert.erreurAlert("Une erreur est survenue lors de la modification du mot de passe.", "Erreur SQL", e.getMessage(), "Fermer");
            e.printStackTrace();
        }
    }

    /**
     * Annule la saisie en cours et nettoie les champs de mot de passe.
     * Affiche une confirmation avant d'annuler.
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void boutonAnnulerHandler() {
        UtilAlert.confirmationAlert("Voulez vous annuler la modification", "Changement du mot de passe", null, "Oui", "Non");
        confirmerMdpPasswordField.clear();
        nouveauMdpPasswordField.clear();
        NavigationManager.goBack();
    }

    private void descativerCopierColler() {
        TextField[] allFields = {
                nouveauMdpTextField,
                confirmerMdpTextField,
                AncienMdpTextField,
                nouveauMdpPasswordField,
                confirmerMdpPasswordField,
                AncienMdpPasswordField
        };

        for (TextField field : allFields) {
            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case C:
                    case V:
                    case X:
                        if (event.isControlDown()) {
                            event.consume(); // bloque l'action
                        }
                        break;
                }
            });

            // Optionnel : empêche le clic droit (menu contextuel)
            field.setContextMenu(null);
        }
    }

}
