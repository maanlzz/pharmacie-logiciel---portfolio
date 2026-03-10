package controllers.clients.ordonnances;

import controllers.clients.UtilClient;
import controllers.clients.UtilOrdonnance;
import database.LogDAO;
import database.MedecinDAO;
import database.OrdonnanceDAO;
import global.InitializableWithParameters;
import global.NavigationManager;
import global.SessionManager;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;

import utils.UtilAlert;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import utils.UtilForm;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller de la fenêtre d'ajout d'une ordonnance.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class OrdonnanceAjoutController implements Initializable, InitializableWithParameters {
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
    private TextField nomMedecinTextField;
    @FXML
    private TextField prenomMedecinTextField;
    @FXML
    private TextField adresseMedecinTextField;
    @FXML
    private TextField telephoneMedecinTextField;
    @FXML
    private DatePicker dateOrdonnanceDatePicker;
    @FXML
    private Button ajouterMedecinButton;
    @FXML
    private Button enregistrerOrdonnanceButton;
    @FXML
    private Button ajouterMedicamentButton;
    @FXML
    private ListView<HBox> zoneConsultationPrescriptionListView;

    // Permet d'avoir une mise à jour automatique de l'interface en cas de modification de la ListView
    private ObservableList<HBox> itemsList;
    private Map<HBox, Medicament> medicamentMap = new HashMap<>();
    private TextArea conseilsMedicauxTextArea;
    private Client client;

    //Gestion des attributs relatifs à la fenêtre pop-up
    private Stage stage;
    private String mode;
    private Ordonnance ordonannceCree;

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 24/03/2025
     *
     * <p>Gère l'annulation de l'ajout d'une ordonnance et le retour à la fenêtre précédente.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void annulerAjoutOrdonnanceHandler(){
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir annuler l'ajout de l'ordonnance ?\nToute informations seront perdues", "Confirmation annulation ajout ordonannce", null, "OUI", "NON");
        if(choixUtilisateur){
            NavigationManager.goBack();
        }
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 24/04/2025
     *
     * <p>Gère l'enregistrement de l'ordonnance dans la base de données.</p>
     *
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    @FXML
    private void enregistrerOrdonnanceHandler() {
        System.out.println("Clique sur le bouton de l'ordonnance");
        StringBuilder messageErreurs = new StringBuilder();

        recupererInformationsPrescription();

        verificationInformationMedecin(messageErreurs);

        // Vérifier que la date a bien été rentrée
        if (dateOrdonnanceDatePicker.getValue() == null) {
            messageErreurs.append("La date de l'ordonnance ne doit pas être nulle.\n");
        } else {
            // Si elle n'est pas nulle, on peut comparer
            if (dateOrdonnanceDatePicker.getValue().isAfter(LocalDate.now())) {
                messageErreurs.append("La date de l'ordonnance ne doit pas être supérieure à aujourd'hui.\n");
            }
        }

        if (zoneConsultationPrescriptionListView.getItems().isEmpty()) {
            messageErreurs.append("La liste de prescriptions est vide.");
        }

        // Si aucun problème n'a été rencontré
        if(messageErreurs.isEmpty()) {

            if (UtilAlert.confirmationAlert("Voulez vous ajouter l'ordonnance'?", "Confirmation ajout ordonnance", null, "Oui", "Non")) {
                try {
                    Medecin medecin = recupererInformationsMedecin();

                    if (!MedecinDAO.medecinExiste(medecin)) {
                        if (!MedecinDAO.ajouterMedecin(medecin)) {
                            UtilAlert.erreurAlert("Échec de l'ajout du médecin dans la base de données.", "Erreur ajout médecin", null, "OK");
                            return ;
                        }
                    }

                    Ordonnance ordonnance = new Ordonnance(client, medecin, dateOrdonnanceDatePicker.getValue(), recupererInformationsPrescription());

                    if (!OrdonnanceDAO.ordonnanceExiste(ordonnance)) {
                        if (OrdonnanceDAO.ajouterOrdonnance(ordonnance)) {
                            // Bloc par Victoria MASSAMBA
                            this.ordonannceCree = ordonnance;
                            int id_personnel = SessionManager.getCurrentPersonnel().getId();
                            Log log = new Log(Log.LogAction.AJOUTER_ORDONNANCE, LocalDateTime.now(),id_personnel, ordonnance.getId());
                            LogDAO.ajouterLog(log);
                            UtilAlert.informationAlert("Ajout de l'ordonnance réalisé avec succès", "Confirmation ajout ordonnance", null, "OK"); //Ligne codée par Hugo VITORINO PEREIRA
                            if(stage!=null){
                                stage.close();
                            }else {
                                //Fin du bloc par Victoria MASSAMBA
                                NavigationManager.goBack();
                            }
                        } else {
                            UtilAlert.erreurAlert("Échec de l'ajout de l'ordonnance dans la base de donnée (veuillez réessayer).", "Erreur ajout ordonnance", null, "OK");
                        }
                    } else {
                        UtilAlert.erreurAlert("Cette ordonnance existe déjà pour ce client et ce médecin", "Erreur ajout ordonnance", null, "OK");
                    }
                } catch (Exception e) {
                    UtilAlert.erreurAlert("Une erreur est survenue lors de l'ajout de l'ordonnance\n" + e.getMessage(), "Erreur interne", null, "OK");
                    e.printStackTrace();
                }
            }

        }
        else{ // Affichage des erreurs lors de la complétion du formulaire
            UtilAlert.erreurAlert(messageErreurs.toString(), "Erreur ajout ordonnance", "Erreur ajout ordonnance","OK");
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
     * Date de création : 05/03/2025
     * Date de dernière modification : 19/04/2025
     *
     * <p>Gère l'ajout d'un médicament à la liste des prescriptions.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void ajouterMedicamentHandler() {
        // Ouvrir la fenêtre de pop-up afin de choisir des médicaments
        // Pour afficher la fenêtre sur le même écran
        Stage currentStage = (Stage) ajouterMedicamentButton.getScene().getWindow();
        Map<Medicament, Integer> medicaments = UtilOrdonnance.afficherFenetreSelectionMedicament(currentStage);
        Medicament medicamentTemp = null;
        int quantiteTemp = 0;

        // Vérification si la liste de médicaments n'est pas vide
        if (medicaments != null && !medicaments.isEmpty()) {
            // Itérer sur la liste des médicaments sélectionnés
            for (Map.Entry<Medicament, Integer> entry : medicaments.entrySet()) {
                medicamentTemp = entry.getKey();
                quantiteTemp = entry.getValue();

                HBox ligneExistante = null;

                // Vérifie si le médicament est déjà présent
                for (Map.Entry<HBox, Medicament> mapEntry : medicamentMap.entrySet()) {
                    if (mapEntry.getValue().getCodeCIP() == medicamentTemp.getCodeCIP()){
                        ligneExistante = mapEntry.getKey();
                        break;
                    }
                }

                if (ligneExistante != null) {
                    // Le médicament existe déjà, on remplace la quantité
                    Spinner<Integer> quantiteSpinner = (Spinner<Integer>) ligneExistante.getChildren().get(2);
                    quantiteSpinner.getValueFactory().setValue(quantiteTemp);
                } else {
                    // Créer une ligne pour chaque médicament et l'ajouter à la liste
                    HBox lignePrescription = UtilOrdonnance.creerLigneMedicament(itemsList, medicamentTemp.getDenominationMedicament(), quantiteTemp, null, "Ajout");
                    itemsList.add(0, lignePrescription);
                    // Garder une trace du médicament dans la map
                    medicamentMap.put(lignePrescription, medicamentTemp);
                }
            }
            UtilAlert.informationAlert(medicaments.size()+" médicament(s) ont été ajoutés.", "Nombre médicament ajouté", null, "OK");
        } else {
            // Afficher un message si aucun médicament n'a été choisi
            UtilAlert.informationAlert("Aucun médicament n'a été choisi.", "Aucun médicament n'a été choisi", null, "OK");
        }
    }

    /**
     * Date de création : 10/03/2025
     * Date de dernière modification : 20/04/2025
     *
     * <p>Récupère les informations des prescriptions saisies par l'utilisateur.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private List<Prescription> recupererInformationsPrescription(){
        List<Prescription> listePrescription = new ArrayList<>();
        // Parcours tous les éléments de la ListView
        for (HBox ligne : zoneConsultationPrescriptionListView.getItems()) {
            // Recuperer le médicament de la ligne
            Medicament medicament = medicamentMap.get(ligne);
            // Récupérer les éléments de chaque HBox
            // L'odre des éléments est le suivant :
            // 2) nomMedicamentTF
            // 3) quantite
            // 4) posologie
            TextField nomMedicamentTF = (TextField) ligne.getChildren().get(1);
            Spinner<Integer> quantite = (Spinner<Integer>) ligne.getChildren().get(2);
            TextField posologieTF = (TextField) ligne.getChildren().get(3);
            //TextArea posologieTA = (TextArea) ligne.getChildren().get(3);

            // Récupérer les valeurs
            String nomMedicament = nomMedicamentTF.getText();
            Integer q = quantite.getValue();
            String posologie = posologieTF.getText();
            //String posologie = posologieTA.getText();
            if(posologie == null || posologie.trim().isEmpty()){
                posologie = "Aucune posologie / Informations";
            }

            // Ajouter à la liste des prescriptions avec le vrai objet Medicament
            listePrescription.add(new Prescription(medicament, q, posologie));
        }

        return listePrescription;
    }

    /**
     * Date de création : 18/03/2025
     * Date de dernière modification : 02/04/2025
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
        String nom = nomMedecinTextField.getText();
        String prenom = prenomMedecinTextField.getText();
        String adresse = adresseMedecinTextField.getText();
        String telephone = telephoneMedecinTextField.getText();

        return new Medecin(specialisation, nom, prenom, adresse, telephone);
    }

    /**
     * Date de création : 21/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Vérifie que toutes les informations saisies pour un médecin sont valides.</p>
     *
     * @param messageErreurs Le {@link StringBuilder} dans lequel seront ajoutés les messages d'erreur si un champ est invalide.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void verificationInformationMedecin(StringBuilder messageErreurs) {
        if (!UtilForm.champTexteFieldNonVide(specialisationMedecinTextField)) {
            messageErreurs.append("Le champ de spécifications du médecin est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(nomMedecinTextField)) {
            messageErreurs.append("Le champ Nom du médecin est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(prenomMedecinTextField)) {
            messageErreurs.append("Le champ Prénom du médecin est vide.\n");
        }
        if (!UtilForm.champTexteFieldNonVide(adresseMedecinTextField)) {
            messageErreurs.append("Le champ Adresse du médecin  est vide.\n");
        }
        if (!UtilForm.telephoneValide(telephoneMedecinTextField.getText())) {
            messageErreurs.append("Le numéro de téléphone du médecin est invalide.\n");
        }
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 08/03/2025
     *
     * <p>Permet l'initialisation des informations du client associées à l'ordonnance.</p>
     * <p>Pour le moment insère des valeurs d'exemples.</p>
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
        numeroSecuriteSocialClientTextField.setText(client.getTelephone());
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 20/04/2025
     *
     * <p>Permet d'initialiser la zone d'affichage des médicaments de la prescription.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationInformationsPrescription() {
        // Desactiver la séléction des lignes
        zoneConsultationPrescriptionListView.setSelectionModel(null);
        // ChatGPT
        itemsList = FXCollections.observableArrayList();
        zoneConsultationPrescriptionListView.setItems(itemsList);
        // Fin ChatGPT

        // Creer la ligne permettant de saisir les potentiels conseils médicaux
        //UtilOrdonnance.creerLigneConseilsMedicaux(itemsList, conseilsMedicauxTextArea);
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
        initialisationInformationsPrescription();
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
    public Ordonnance getOrdonannceCree(){
        return ordonannceCree;
    }
}
