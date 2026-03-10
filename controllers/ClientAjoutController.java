package controllers.clients;

import database.LogDAO;
import global.NavigationManager;
import database.ClientDAO;
import database.MedecinDAO;
import global.SessionManager;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Client;
import models.Log;
import models.Medecin;
import utils.UtilAlert;
import utils.UtilForm;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller de la fenêtre d'ajout de client.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ClientAjoutController implements Initializable {
    @FXML
    private HBox barreMenuContainer;
    @FXML
    private TextField nomClientTextField;
    @FXML
    private TextField prenomClientTextField;
    @FXML
    private ComboBox<String> sexeClientComboBox;
    @FXML
    private DatePicker dateNaissanceClientDatePicker;
    @FXML
    private TextField adresseClientTextField;
    @FXML
    private TextField telephoneClientTextField;
    @FXML
    private TextField emailClientTextField;
    @FXML
    private TextField mutuelleClientTextField;
    @FXML
    private TextField numeroSecuriteSocialClientTextField;
    @FXML
    private TextField specialisationMedecinTextField;
    @FXML
    private MenuButton choixSpecialisationMenuButton;
    @FXML
    private TextField nomMedecinTraitantTextField;
    @FXML
    private TextField prenomMedecinTraitantTextField;
    @FXML
    private TextField adresseMedecinTraitantTextField;
    @FXML
    private TextField telephoneMedecinTraitantTextField;
    @FXML
    private Button ajouterMedecinButton;
    @FXML
    private Button annulerButton;
    @FXML
    private Button ajouterClientButton;

    //Gestion des attributs relatifs à la fenêtre pop-up
    private Stage stage;
    private String mode;
    private Client clientCree;

    /**
     * Date de création : 24/02/2025
     * Date de dernière modification : 24/04/2025
     *
     * <p>Gère le retour à la fenêtre précédente et l'annulation de l’ajout du client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    @FXML
    private void annulerAjoutClientHandler() {
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir annuler l'ajout du client ?\nToute informations seront perdues", "Confirmation annulation ajout client", null, "OUI", "NON");
        if(choixUtilisateur){
            NavigationManager.goBack();
        }
    }

    /**
     * Date de création : 24/02/2025
     * Date de dernière modification : 24/04/2025
     *
     * <p>Gère l'ajout d'un client et de son médecin traitant dans la base de données.</p>
     *
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    @FXML
    private void ajoutClientHandler() {
        StringBuilder messageErreurs = new StringBuilder();

        verificationInformationClient(messageErreurs);
        verificationInformationMedecin(messageErreurs);

        // Si aucun problème n'a été rencontré
        if (messageErreurs.isEmpty()) {

            if (UtilAlert.confirmationAlert("Voulez vous ajouter le client?", "Confirmation ajout client", null, "Oui", "Non")) {
                try {
                    Medecin medecinTraitant = recupererInformationsMedecin();
                    boolean ajoutMedecin = false;

                    if (!MedecinDAO.medecinExiste(medecinTraitant)) {
                        ajoutMedecin = MedecinDAO.ajouterMedecin(medecinTraitant);
                        if (!ajoutMedecin) {
                            UtilAlert.erreurAlert("Échec de l'ajout du médecin dans la base de données.", "Erreur ajout médecin", null, "OK");
                            return;
                        }
                    }
                    // Ajout du 17/04/2025
                    else {
                        boolean modificationOk = MedecinDAO.updateMedecin(medecinTraitant);
                        if (!modificationOk) {
                            UtilAlert.erreurAlert("Échec de la mise à jour du médecin dans la base de données.", "Erreur mise à jour", null, "OK");
                            return;
                        }
                    }

                    Client client = recupererInformationsClient();
                    // Par défaut l'attribut vaut 0 mais afin d'éviter tout risque je lui passe explicitement la valeur -1
                    // Cette ajout d'id est nécessaire dans la méthode clientExiste()
                    client.setId(-1);
                    client.setMedecinTraitant(medecinTraitant);

                    if (!ClientDAO.clientExiste(client)) {
                        if (ClientDAO.ajouterClient(client)) {
                            // Bloc Victoria MASSAMBA
                            this.clientCree = client;
                            UtilAlert.informationAlert("Ajout du client réalisé avec succès", "Confirmation ajout client", null, "OK"); // ligne codée par Hugo VITORINO PEREIRA
                            int id_personnel = SessionManager.getCurrentPersonnel().getId();
                            Log log = new Log(Log.LogAction.AJOUTER_CLIENT, LocalDateTime.now(),id_personnel,client.getId());
                            LogDAO.ajouterLog(log);
                            //fermeture de la fenêtre pop-up s'il y'en a une
                            if(stage!=null){
                                stage.close();
                            }else {
                                //Fin du Bloc Victoria MASSAMBA
                                NavigationManager.goBack();
                            }
                        } else {
                            if(ajoutMedecin) {
                                MedecinDAO.supprimerMedecin(medecinTraitant.getId());
                            }
                            UtilAlert.erreurAlert("Échec de l'ajout du client dans la base de donnée (veuillez réessayer).", "Erreur ajout client", null, "OK");
                        }
                    } else {
                        UtilAlert.erreurAlert("Le client existe déjà.\nSi vous voulez modifier ses informations, il est nécessaire de se rendre sur sa page de consultation", "Erreur ajout client", null, "OK");
                    }
                } catch (Exception e) {
                    UtilAlert.erreurAlert("Une erreur est survenue :\n" + e.getMessage(), "Erreur interne", null, "OK");
                    e.printStackTrace();
                }
            }

        } else {
            UtilAlert.erreurAlert(messageErreurs.toString(), "Erreur ajout client", "Erreur ajout client !", "OK");
        }
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 08/03/2025
     *
     * <p>Permet l'ajout d'un médecin.</p>
     * <p>Affiche pour le moment un message de clique sur le bouton.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void ajouterMedecinHandler(){
        System.out.println("Clique sur le bouton pour ajouter un médecin.");
    }

    /**
     * Date de création : 08/02/2025
     * Date de dernière modification: 24/03/2025
     *
     * <p>Vérifie que toutes les informations saisies pour un client sont valides.</p>
     *
     * @param messageErreurs Le {@link StringBuilder} dans lequel seront ajoutés les messages d'erreur si un champ est invalide.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void verificationInformationClient(StringBuilder messageErreurs) {
        if (!UtilForm.champTexteFieldNonVide(nomClientTextField)) {
            messageErreurs.append("Le champ Nom est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(prenomClientTextField)) {
            messageErreurs.append("Le champ Prénom est vide.\n");
        }
        if (!UtilForm.comboBoxValide(sexeClientComboBox)) {
            messageErreurs.append("Le sexe du client n'a pas été défini.\n");
        }
        if (!UtilForm.datePickerValide(dateNaissanceClientDatePicker)) {
            messageErreurs.append("La date de naissance doit être antérieure à aujourd'hui.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(adresseClientTextField)) {
            messageErreurs.append("Le champ Adresse est vide.\n");
        }
        if (!UtilForm.telephoneValide(telephoneClientTextField.getText())) {
            messageErreurs.append("Le numéro de téléphone est invalide.\n");
        }
        if (!UtilForm.emailValide(emailClientTextField.getText())) {
            messageErreurs.append("L'adresse email est invalide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(mutuelleClientTextField)) {
            messageErreurs.append("Le champ Mutuelle est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(numeroSecuriteSocialClientTextField)) {
            messageErreurs.append("Le numéro de sécurité sociale est vide.\n");
        }
    }

    /**
     * Date de création : 18/03/2025
     * Date de dernière modification : 21/03/2025
     *
     * <p>Vérifie que toutes les informations saisies pour un médecin sont valides.</p>
     *
     * @param messageErreurs Le {@link StringBuilder} dans lequel seront ajoutés les messages d'erreur si un champ est invalide.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void verificationInformationMedecin(StringBuilder messageErreurs) {
        if (!UtilForm.champTexteFieldNonVide(specialisationMedecinTextField)) {
            messageErreurs.append("Le champ de spécifications du médecin traitant est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(nomMedecinTraitantTextField)) {
            messageErreurs.append("Le champ Nom du médecin traitant est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(prenomMedecinTraitantTextField)) {
            messageErreurs.append("Le champ Prénom du médecin traitant est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(adresseMedecinTraitantTextField)) {
            messageErreurs.append("Le champ Adresse du médecin traitant est vide.\n");
        }
        if (!UtilForm.telephoneValide(telephoneMedecinTraitantTextField.getText())) {
            messageErreurs.append("Le numéro de téléphone du médecin traitant est invalide.\n");
        }
    }

    /**
     * Date de création : 18/03/2025
     * Date de dernière modification : 18/03/2025
     *
     * <p>Récupère les informations saisies par l'utilisateur dans les champs du formulaire pour créer un client.</p>
     *
     * @return Un objet {@link Client} contenant les informations saisies par l'utilisateur dans le formulaire.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private Client recupererInformationsClient() {
        String nom = nomClientTextField.getText();
        String prenom = prenomClientTextField.getText();
        String sexe = sexeClientComboBox.getValue();
        LocalDate dateNaissance = dateNaissanceClientDatePicker.getValue();
        String adresse = adresseClientTextField.getText();
        String telephone = telephoneClientTextField.getText();
        String email = emailClientTextField.getText();
        String mutuelle = mutuelleClientTextField.getText();
        String numeroSecurite = numeroSecuriteSocialClientTextField.getText();
        Medecin medecin = recupererInformationsMedecin();

        return new Client(nom, prenom, sexe, dateNaissance, adresse, telephone, email, mutuelle, numeroSecurite, medecin);
    }

    /**
     * Date de création : 18/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Récupère les informations saisies par l'utilisateur dans les champs du formulaire pour créer un médecin.</p>
     *
     * @return Un objet {@link Medecin} contenant les informations saisies par l'utilisateur dans le formulaire.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private Medecin recupererInformationsMedecin() {
        // Pas de problèmes car la vérification a été réalisé avant
        List<String> specialisation = new ArrayList<>(Arrays.asList(specialisationMedecinTextField.getText().split(", ")));
        String nom = nomMedecinTraitantTextField.getText();
        String prenom = prenomMedecinTraitantTextField.getText();
        String adresse = adresseMedecinTraitantTextField.getText();
        String telephone = telephoneMedecinTraitantTextField.getText();

        return new Medecin(specialisation, nom, prenom, adresse, telephone);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 05/03/2025
     *
     * <p>Méthode d'initialisation du contrôleur.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        UtilClient.initialisationBoutonSexeClient(sexeClientComboBox);
        UtilClient.initialisationSpecialisationsMedecin(specialisationMedecinTextField, choixSpecialisationMenuButton);
    }

    /**
     * Date de création : 24/04/25
     * @author Victoria MASSAMBA
     * @param mode
     * @param stage
     */

    public void setMode(String mode, Stage stage) {
        this.stage = stage;
        this.mode = mode;

        if ("creation".equals(mode)) {
            // rend la barre de menu visible mais inopérante
            barreMenuContainer.setDisable(true);
            barreMenuContainer.setMouseTransparent(true);
            barreMenuContainer.setFocusTraversable(false);
        }
    }

    /**
     * Date de création : 24/04/25
     * @author Victoria MASSAMBA
     * @return clientCree le client ajouté par le formulaire pour la gestion de la fenêtre pop-up
     */
    public Client getClientCree() {
        return clientCree;
    }
}
