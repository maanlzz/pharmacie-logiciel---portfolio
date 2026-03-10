package controllers.clients;

import database.LogDAO;
import global.InitializableWithParameters;
import global.NavigationManager;
import database.ClientDAO;
import database.MedecinDAO;
import global.Page;
import global.SessionManager;
import models.Client;
import models.Log;
import models.Medecin;
import utils.UtilAlert;
import utils.UtilForm;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller de la fenêtre de modification des informations d'un client.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ClientModificationController implements Initializable, InitializableWithParameters {
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
    private Button supprimerClientButton;
    @FXML
    private Button annulationModificationButton;
    @FXML
    private Button enregistrerModificationButton;

    private Client client;

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 04/04/2025
     *
     * <p>Gère la suppression d'un client après confirmation de l'utilisateur. Reviens à la fenêtre de recherche de client après.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void supressionClientHandler(){
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir supprimer le client ?", "Confirmation suppression client", null, "OUI", "NON");
        if(choixUtilisateur){
            boolean suppressionReussi = ClientDAO.supprimerClient(client.getId());
            if (suppressionReussi) {
                int id_personnel = SessionManager.getCurrentPersonnel().getId();
                Log log = new Log(Log.LogAction.SUPPRIMER_CLIENT, LocalDateTime.now(),id_personnel,client.getId());
                LogDAO.ajouterLog(log);

                UtilAlert.informationAlert("Suppression du client réalisé avec succès", "Confirmation suppression client", null,"OK");

                NavigationManager.loadPage(Page.CLIENT_RECHERCHE.getFilePath());
                NavigationManager.removeLastPage();
                NavigationManager.removeLastPage();
                NavigationManager.removeLastPage();
            } else {
                UtilAlert.erreurAlert("Échec de la suppression du client de la base de donnée (veuillez réessayer).", "Erreur suppression client", null,"OK");
            }
        }
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 24/03/2025
     *
     * <p>Permet l'annulation des modifications du client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void annulerModificationHandler(){
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir annuler les modifications du client ?", "Confirmation annulation modification client", null, "OUI", "NON");
        if(choixUtilisateur){
            NavigationManager.goBack();
        }
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 17/04/2025
     *
     * <p>Gère la validation et la mise à jour des informations d'un client et de son médecin traitant.</p>
     * <p>
     * Cette méthode vérifie si les informations saisies par l'utilisateur pour le client et le médecin sont valides.
     * Si les informations sont valides, elle tente de mettre à jour les données du client et du médecin dans la base de données.
     * Si le médecin existe déjà dans la base de données, il peut être mis à jour ou remplacé si nécessaire. Si la mise à jour
     * est réussie, une alerte de confirmation est affichée. En cas d'échec de la modification ou si les données sont invalides,
     * des alertes d'erreur sont affichées.
     * </p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void validerModificationHandler() {
        StringBuilder messageErreurs = new StringBuilder();

        verificationInformationClient(messageErreurs);
        verificationInformationMedecin(messageErreurs);

        // Si aucun problème n'a été rencontré
        if(messageErreurs.isEmpty()) {

            if (UtilAlert.confirmationAlert("Voulez vous modifier le client ?", "Confirmation modification client", null, "Oui", "Non")) {
                try {
                    Medecin medecinTraitant = recupererInformationsMedecin();
                    medecinTraitant.setId(client.getMedecinTraitant().getId());
                    if (!MedecinDAO.medecinExiste(medecinTraitant)) {
                        boolean modificationMedecinReussi = MedecinDAO.ajouterMedecin(medecinTraitant);
                        if (!modificationMedecinReussi) {
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

                    Client clientModifie = recupererInformationsClient();
                    clientModifie.setId(client.getId());
                    clientModifie.setMedecinTraitant(medecinTraitant);

                    // Vérifier si le client existe déjà dans la base de données
                    if (!ClientDAO.clientExiste(clientModifie)) {
                        boolean modificationClientReussi = ClientDAO.updateClient(clientModifie);
                        if (modificationClientReussi) {
                            int id_personnel = SessionManager.getCurrentPersonnel().getId();
                            Log log = new Log(Log.LogAction.MODIFIER_CLIENT, LocalDateTime.now(),id_personnel,client.getId());
                            LogDAO.ajouterLog(log);
                            UtilAlert.informationAlert("Modification du client réalisé avec succès", "Confirmation modification client", null, "OK");
                            Map<String, Object> params = new HashMap<>();
                            params.put("client", clientModifie);
                            NavigationManager.loadPage(Page.CLIENT_CONSULTATION.getFilePath(), params);
                            NavigationManager.removeLastPage();
                            NavigationManager.removeLastPage();
                        } else {
                            UtilAlert.erreurAlert("Échec de la modification du client", "Erreur modification client", null, "OK");
                        }
                    } else {
                        UtilAlert.erreurAlert("Le client existe déjà.", "Erreur modification client", null, "OK");
                    }
                } catch(Exception e) {
                    UtilAlert.erreurAlert("Une erreur est survenue :\n" + e.getMessage(), "Erreur interne", null, "OK");
                    e.printStackTrace();
                }
            }

        }
        else {
            // Affichage des erreurs lors de la complétion du formulaire
            UtilAlert.erreurAlert(messageErreurs.toString(), "Erreur modification ordonnnance", "Erreur mofification ordonnance !","OK");
        }
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 24/03/2025
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
     * Date de dernière modification: 21/03/2025
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
     * Date de dernière modifcation : 18/03/2025
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
     * Date de création : 08/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Méthode d'initialisation des informations du client.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param client Le client pour lequel nous allons récupérer ces informations.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationInformationsClient(Client client){
        this.client = client;
        nomClientTextField.setText(client.getNom());
        prenomClientTextField.setText(client.getPrenom());
        sexeClientComboBox.setValue(client.getSexe());
        dateNaissanceClientDatePicker.setValue(client.getDateNaissance());
        adresseClientTextField.setText(client.getAdresse());
        telephoneClientTextField.setText(client.getTelephone());
        emailClientTextField.setText(client.getEmail());
        mutuelleClientTextField.setText(client.getMutuelle());
        numeroSecuriteSocialClientTextField.setText(client.getNumeroSecuriteSociale());
        nomMedecinTraitantTextField.setText(client.getMedecinTraitant().getNom());
        prenomMedecinTraitantTextField.setText(client.getMedecinTraitant().getPrenom());
        adresseMedecinTraitantTextField.setText(client.getMedecinTraitant().getAdresse());
        telephoneMedecinTraitantTextField.setText(client.getMedecinTraitant().getTelephone());
        specialisationMedecinTextField.setText(String.join(", ", MedecinDAO.getSpecialisationsMedecin(client.getMedecinTraitant().getId())));
    }

    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Cette méthode prend un {@link Map} de paramètres et récupère l'objet {@link Client} à partir de la clé
     * "client". Les informations du client sont ensuite utilisées pour initialiser les champs du formulaire via
     * la méthode {@link #initialisationInformationsClient(Client)}.</p>
     *
     * @param parameters Un {@link Map} contenant les paramètres nécessaires à l'initialisation. La clé "client" doit être associée à un objet {@link Client}.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        initialisationInformationsClient((Client) parameters.get("client"));
        // Nécessaire de le mettre après afin de ne pas rencontrer de problèmes d'incohérences
        UtilClient.initialisationSpecialisationsMedecin(specialisationMedecinTextField, choixSpecialisationMenuButton);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 29/03/2025
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
    }
}
