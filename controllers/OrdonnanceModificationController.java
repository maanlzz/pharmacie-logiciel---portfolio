package controllers.clients.ordonnances;

import controllers.clients.UtilClient;
import controllers.clients.UtilOrdonnance;
import database.LogDAO;
import database.MedecinDAO;
import database.OrdonnanceDAO;
import global.InitializableWithParameters;
import global.NavigationManager;
import global.Page;
import global.SessionManager;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;

import utils.UtilAlert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import utils.UtilForm;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Controller de la fenêtre de modification des informations d'une ordonnance.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class OrdonnanceModificationController implements Initializable, InitializableWithParameters {
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
    private Button supprimerOrdonanceButton;
    @FXML
    private Button annulationModificationButton;
    @FXML
    private Button enregistrerModificationButton;
    @FXML
    private Button ajouterMedecinButton;
    @FXML
    private Button ajouterMedicamentButton;
    @FXML
    private ListView<HBox> zoneConsultationPrescriptionListView;

    // Permet d'avoir une mise à jour automatique de l'interface en cas de modification de la ListView
    private ObservableList<HBox> itemsList;

    private Map<HBox, Medicament> medicamentMap = new HashMap<>();

    private Ordonnance ordonnance;

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 11/04/2025
     *
     * <p>Gère l'événement de suppression d'une ordonnance.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void supprimerOrdonnanceHandler() {
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir supprimer l'ordonannce ?", "Confirmation suppression ordonnance", null, "OUI", "NON");
        if(choixUtilisateur){
            boolean suppressionReussi = OrdonnanceDAO.supprimerOrdonnance(ordonnance.getId());
            if (suppressionReussi) {
                int id_personnel = SessionManager.getCurrentPersonnel().getId();
                Log log = new Log(Log.LogAction.SUPPRIMER_ORDONNANCE, LocalDateTime.now(),id_personnel, ordonnance.getId());
                LogDAO.ajouterLog(log);
                UtilAlert.informationAlert("Suppression de l'ordonnance réalisé avec succès", "Confirmation suppression ordonnance", null,"OK");
                Map<String, Object> params = new HashMap<>();
                params.put("client", ordonnance.getClient());
                NavigationManager.loadPage(Page.ORDONNANCE_RECHERCHE.getFilePath(), params);
                NavigationManager.removeLastPage();
                NavigationManager.removeLastPage();
                NavigationManager.removeLastPage();
            } else {
                UtilAlert.erreurAlert("Échec de la suppression de l'ordonannce de la base de donnée (veuillez réessayer).", "Erreur suppression ordonnance", null,"OK");
            }
        }
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 12/04/2025
     *
     * <p>Permet l'annulation des modifications de l'ordonnance.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void annulerModificationHandler(){
        boolean choixUtilisateur = UtilAlert.confirmationAlert("Êtes-vous sur de vouloir annuler les modifications de l'ordonnance ?", "Confirmation annulation modification ordonnance", null, "OUI", "NON");
        if(choixUtilisateur){
            NavigationManager.goBack();
        }
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 16/04/2025
     *
     * <p>Gère la validation et la mise à jour des informations de l'ordonnance.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void validerModificationHandler(){
        StringBuilder messageErreurs = new StringBuilder();

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

            if (UtilAlert.confirmationAlert("Voulez vous modifier le client?", "Confirmation modificatoin client", null, "Oui", "Non")) {
                try {
                    Medecin medecin = recupererInformationsMedecin();
                    medecin.setId(ordonnance.getClient().getMedecinTraitant().getId());
                    if (!MedecinDAO.medecinExiste(medecin)) {
                        boolean ajoutMedecinReussi = MedecinDAO.ajouterMedecin(medecin);
                        if (!ajoutMedecinReussi) {
                            UtilAlert.erreurAlert("Échec de l'ajout du médecin dans la base de données.", "Erreur ajout médecin", null, "OK");
                            return;
                        }
                    }

                    List<Prescription> prescriptions = recupererInformationsPrescription();
                    Ordonnance ordonnanceModifie = new Ordonnance(ordonnance.getClient(), medecin, dateOrdonnanceDatePicker.getValue(), prescriptions);
                    ordonnanceModifie.setId(ordonnance.getId());

                    if (!OrdonnanceDAO.ordonnanceExiste(ordonnanceModifie)) {
                        if (OrdonnanceDAO.updateOrdonnance(ordonnanceModifie)) {
                            int id_personnel = SessionManager.getCurrentPersonnel().getId();
                            Log log = new Log(Log.LogAction.MODIFIER_ORDONNANCE, LocalDateTime.now(),id_personnel, ordonnance.getId());
                            LogDAO.ajouterLog(log);
                            UtilAlert.informationAlert("Mise à jour de l'ordonnance réalisé avec succès", "Confirmation ajout ordonnance", null, "OK");
                            Map<String, Object> params = new HashMap<>();
                            params.put("ordonnance", ordonnanceModifie);
                            NavigationManager.loadPage(Page.ORDONNANCE_CONSULTATION.getFilePath(), params);
                            NavigationManager.removeLastPage();
                            NavigationManager.removeLastPage();
                        } else {
                            UtilAlert.erreurAlert("Échec de la mise à jour de l'ordonnance dans la base de donnée (veuillez réessayer).", "Erreur ajout ordonnance", null, "OK");
                        }
                    } else {
                        UtilAlert.erreurAlert("L'ordonnance existe déjà.\nSi vous voulez modifier ses informations, il est nécessaire de se rendre sur sa page de consultation", "Erreur modification ordonnance", null, "OK");
                    }
                } catch (Exception e) {
                    UtilAlert.erreurAlert("Une erreur est survenue lors de la modification de l'ordonnance\n" + e.getMessage(), "Erreur interne", null, "OK");
                    e.printStackTrace();
                }
            }

        }
        else{ // Affichage des erreurs lors de la complétion du formulaire
            UtilAlert.erreurAlert(messageErreurs.toString(), "Erreur modification ordonnance", "Erreur modification ordonnance","OK");
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
     * Date de dernière modification : 17/04/2025
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
            System.out.println(medicaments.entrySet().size());
            // Itérer sur la liste des médicaments sélectionnés
            for (Map.Entry<Medicament, Integer> entry : medicaments.entrySet()) {
                medicamentTemp = entry.getKey();
                quantiteTemp = entry.getValue();

                HBox ligneExistante = null;

                // Vérifie si le médicament est déjà présent
                for (Map.Entry<HBox, Medicament> mapEntry : medicamentMap.entrySet()) {
                    if (mapEntry.getValue().getCodeCIP() == medicamentTemp.getCodeCIP()) {
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
                    itemsList.add(lignePrescription);
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
     * Date de dernière modification : 16/04/2025
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
            System.out.println(medicament);
            // Récupérer les éléments de chaque HBox
            // L'odre des éléments est le suivant :
            // 2) nomMedicamentTF
            // 3) quantite
            // 4) posologie
            TextField nomMedicamentTF = (TextField) ligne.getChildren().get(1);
            Spinner<Integer> quantite = (Spinner<Integer>) ligne.getChildren().get(2);
            TextField posologieTF = (TextField) ligne.getChildren().get(3);

            // Récupérer les valeurs
            String nomMedicament = nomMedicamentTF.getText();
            Integer q = quantite.getValue();
            String posologie = posologieTF.getText();
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
     * Date de création : 10/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Ajoute des informations de prescription à la liste des prescriptions affichées dans l'interface utilisateur.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void ajouterInformationsPrescription() {
        // Recuperer les prescriptions depuis la bdd
        List<Prescription> listePrescription = OrdonnanceDAO.getPrescriptionsOrdonnance(ordonnance.getId());
        System.out.println("Liste de prescription : "+listePrescription);
        for(Prescription ligne : listePrescription){
            System.out.println("Information de la prescription : "+ligne);
            HBox lignePrescription = UtilOrdonnance.creerLigneMedicament(itemsList, ligne.getMedicament().getDenominationMedicament(), ligne.getQuantite(), ligne.getPosologie(), "Modification");
            itemsList.add(lignePrescription);
            // On garde une trace du médicament
            medicamentMap.put(lignePrescription, ligne.getMedicament());
        }
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 02/04/2025
     *
     * <p>Initialise les champs de l'inteface avec les informations d'une ordonannce.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param ordonnance L'objet {@link Ordonnance} contenant les informations à afficher sur la fenêtre.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationInformationsOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        nomClientTextField.setText(ordonnance.getClient().getNom());
        prenomClientTextField.setText(ordonnance.getClient().getPrenom());
        sexeClientComboBox.setValue(ordonnance.getClient().getSexe());
        dateNaissanceClientDatePicker.setValue(ordonnance.getClient().getDateNaissance());
        adresseClientTextField.setText(ordonnance.getClient().getAdresse());
        telephoneClientTextField.setText(ordonnance.getClient().getTelephone());
        emailClientTextField.setText(ordonnance.getClient().getEmail());
        mutuelleClientTextField.setText(ordonnance.getClient().getMutuelle());
        numeroSecuriteSocialClientTextField.setText(ordonnance.getClient().getNumeroSecuriteSociale());
        nomMedecinTextField.setText(ordonnance.getMedecin().getNom());
        prenomMedecinTextField.setText(ordonnance.getMedecin().getPrenom());
        adresseMedecinTextField.setText(ordonnance.getMedecin().getAdresse());
        telephoneMedecinTextField.setText(ordonnance.getMedecin().getTelephone());
        dateOrdonnanceDatePicker.setValue(ordonnance.getDateOrdonnance());
        specialisationMedecinTextField.setText(String.join(", ", MedecinDAO.getSpecialisationsMedecin(ordonnance.getMedecin().getId())));
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 30/03/2025
     *
     * <p>Initialise les informations de prescription et configure l'interface de consultation des prescriptions.</p>
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
        ajouterInformationsPrescription();
    }

    /**
     * Date de création : 03/04/2025
     * Date de dernière modification : 12/04/2025
     *
     * <p>Initialise l'ordonnance</p>
     *
     * @param parameters Un {@link Map} contenant les paramètres nécessaires à l'initialisation. La clé "client" doit être associée à un objet {@link Client}.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        initialisationInformationsOrdonnance((Ordonnance) parameters.get("ordonnance"));
        UtilClient.initialisationSpecialisationsMedecin(specialisationMedecinTextField, choixSpecialisationMenuButton);
        initialisationInformationsPrescription();
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 05/03/2025
     *
     * <p>Méthode d'initialisation du controller.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
    }
}
