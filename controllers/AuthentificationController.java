package global;

import database.LogDAO;
import database.PersonnelDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Log;
import models.Personnel;
import utils.UtilAlert;
import utils.UtilPasswordVisibility;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Controller de la fenêtre d'authentification.
 * @author Hugo VITORINO PEREIRA
 * @author Nicolas ADAMCZYK
 * Date de création : 26/03/2025
 * Date de dernière modification : 05/03/2025
 */
public class AuthentificationController {
    @FXML
    private HBox rectangleVert;
    @FXML
    private Label nomProjet;
    @FXML
    private HBox espaceurHB;
    @FXML
    private Label anneeProjet;
    @FXML
    BorderPane pageAuthentification;
    @FXML
    private VBox vb;
    @FXML
    private ImageView imageCompte;
    @FXML
    private TextField champLoginTextField;
    @FXML
    private PasswordField champMotDePassePasswordField;
    @FXML
    private TextField champMotDePasseVisible;
    @FXML
    private Button connexionButton;
    @FXML
    private HBox nomsEtudiantsHB;
    @FXML
    private Label nomsEtudiants;

    @FXML
    private ImageView oeilMotDePasse;

    private boolean motDePasseVisible = false;

    private UtilPasswordVisibility motDePasseVisibility;

    /**
     * Date : 05/03/2025
     * Date de dernière modification : 29/03/2025
     * Permet de se connecter lorsqu'on appuie sur Entrée
     *
     * @author Nicolas ADAMCZYK
     */
    @FXML
    public void initialize() {
        // Initialiser la gestion de la visibilité du mot de passe
        motDePasseVisibility = new UtilPasswordVisibility(champMotDePassePasswordField, champMotDePasseVisible, oeilMotDePasse);

        // Gestion de la touche Entrée
        champLoginTextField.setOnKeyPressed(this::handleKeyPressed);
        champMotDePassePasswordField.setOnKeyPressed(this::handleKeyPressed);
        champMotDePasseVisible.setOnKeyPressed(this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            connexionHandler();
        }
    }

    /**
     * Date : 05/03/2025
     * Date de dernière modification : 29/03/2025
     * Gère la connexion (et donc l'accès au logiciel).
     *
     * @author Hugo VITORINO PEREIRA
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void connexionHandler() {
        String username = champLoginTextField.getText();
        String password = champMotDePassePasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            UtilAlert.erreurAlert(
                    "Veuillez remplir tous les champs",
                    "Erreur de connexion",
                    "Champs manquants",
                    "OK"
            );
            return;
        }

        try {
            // Authentifier en récupérant un objet Personnel
            Personnel personnel = PersonnelDAO.authentifier(username, password);

            if (personnel != null) {
                // Stocker le personnel connecté
                SessionManager.setCurrentPersonnel(personnel);
                // Ajouter un log de connexion
                Log logConnexion = new Log(
                        Log.LogAction.CONNEXION,
                        LocalDateTime.now(),
                        personnel.getId()
                );
                LogDAO.ajouterLog(logConnexion);
                // Rediriger vers la page d'accueil
                NavigationManager.loadPage(Page.HOME.getFilePath());
            } else {
                UtilAlert.erreurAlert(
                        "Le nom d'utilisateur ou le mot de passe est incorrect",
                        "Erreur de connexion",
                        "Identifiants invalides",
                        "OK"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            UtilAlert.erreurAlert(
                    "Une erreur est survenue lors de la connexion: " + e.getMessage(),
                    "Erreur système",
                    "Problème de connexion",
                    "OK"
            );
        }
    }

    /**
     * Date : 29/03/2025
     * Permet d'afficher ou masquer le mot de passe sur le champ de texte
     *
     * @author Hugo VITORINO PEREIRA
     * @author Nicolas ADAMCZYK
     */
    @FXML
    private void toggleMotDePasseVisible(MouseEvent event) {
        motDePasseVisibility.toggleVisibility();
    }

    // Classe utilisée pendant le développement
    //TODO : Classe à supprimer pour le déploiement

    /**
     * Classe permettant à l'utilisateur de se connecter directement en titulaire pour les tests pendant le développement du logiciel.
     * author Nicolas ADAMCZYK
     */
    @FXML
    private void connexionTitulaireHandler() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            String username = prop.getProperty("titulaire.username");
            String password = prop.getProperty("titulaire.password");

            champLoginTextField.setText(username);
            champMotDePassePasswordField.setText(password);

            // Déclencher la connexion automatique
            connexionHandler();
        } catch (IOException ex) {
            ex.printStackTrace();
            UtilAlert.erreurAlert(
                    "Erreur de chargement des identifiants titulaire",
                    "Erreur système",
                    "Problème de configuration",
                    "OK"
            );
        }
    }
}
