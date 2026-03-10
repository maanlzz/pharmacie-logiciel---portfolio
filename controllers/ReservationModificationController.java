package controllers.clients.reservations;

import database.*;
import global.InitializableWithParameters;
import global.NavigationManager;

import global.Page;
import global.SessionManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import models.*;
import utils.UtilAlert;
import utils.UtilEmail;
import utils.UtilRecherche;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de la fenêtre de modification d'une réservation de médicaments pour un client.
 * @author Victoria MASSAMBA
 */
public class ReservationModificationController implements Initializable, InitializableWithParameters {


    //Zone de recherche de médicament

    @FXML
    private ComboBox<String> rechercherParButton;
    @FXML
    private TextField zoneRechercheMedicament;
    @FXML
    private TableView<Medicament> zoneAffichageMedicament; // TableView des médicaments en rupture
    @FXML
    private TableColumn<Medicament, String> codeCisColumn;
    @FXML
    private TableColumn<Medicament, String> denominationColumn;
    @FXML
    private TableColumn<Medicament, Double> prixColumn;

    @FXML
    private TableColumn<Medicament, String> dosageColumn;


    @FXML private TextField nomClientTextField;
    @FXML private TextField prenomClientTextField;
    @FXML private ChoiceBox<String> sexeChoiceBox;
    @FXML private DatePicker dateNaissanceClient;
    @FXML private TextField adresseClientTextField;
    @FXML private TextField telephoneClientTextField;
    @FXML private TextField emailClientTextField;
    @FXML private TextField secuTextField;
    @FXML private TextField mutuelleTextField;


    @FXML private TableView<ReservationItem> reservationTableView; 
    @FXML private TableColumn<ReservationItem, String> nomMedicamentColumn;
    @FXML private TableColumn<ReservationItem, Number> quantiteColumn;
    @FXML
    private TableColumn<ReservationItem, Void> actionsReservationColumn;




    @FXML private Button sauvegarderModifButton;

    @FXML private Button retourButton;


    private ObservableList<Medicament> medicamentsEnRupture;
    private ObservableList<ReservationItem> reservationItems;
    private UtilRecherche<Medicament> utilRecherche;

    private Client client;
    private Integer reservationid; // ID de la réservation qu'on modifie


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Préparation de la liste des médicaments en rupture
        medicamentsEnRupture = FXCollections.observableArrayList();
        // Préparer la liste d'items
        reservationItems = FXCollections.observableArrayList();
        reservationTableView.setItems(reservationItems);

        // Configurer la colonne "nom du médicament"
        nomMedicamentColumn.setCellValueFactory(
            cellData -> new ReadOnlyObjectWrapper<>(
                cellData.getValue().getMedicament().getDenominationMedicament()
            )
        );

        // Configurer la colonne "quantité"
        quantiteColumn.setCellValueFactory(
            cellData -> cellData.getValue().quantiteProperty()
        );

        double size = 270;

        actionsReservationColumn.setMinWidth(size);
        actionsReservationColumn.setPrefWidth(size);
        actionsReservationColumn.setMaxWidth(size);
        actionsReservationColumn.setResizable(false);

        reservationTableView.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        //Configuration de la colonne action du tableview des réservations
        ajouterColonneActions();

        //Configuration de la zone de recherche
        configurerColonnesMedicaments();
        zoneRechercheMedicament.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                // Appeler la méthode de recherche à chaque frappe
                chargerMedicaments();
            }
        });
    }

    /**
     * Date de création : 11/04/25
     * Date de dernière modification : 12/04/25
     * Méthode qui initialise les paramètres "client" et "reservation".
     * @param parameters La map contenant les paramètres
     * @author Victoria MASSAMBA
     */


    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        // Récupérer le paramètre client
        this.client = (Client) parameters.get("client");
        if (this.client != null) {
            remplirInformationsClient(this.client);
        }

        // Récupérer l'ID de la réservation à partir de la chaîne
        String reservationStr = (String) parameters.get("reservation");
        this.reservationid = extraireIdReservation(reservationStr);

        // Charger la réservation : on récupère la liste (Medicament -> quantite)
        Map<Medicament, Integer> resTab = new HashMap<>();
        if (reservationid != null) {
            try {
                resTab = ReservationDAO.getMedicamentByReservations(reservationid);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Convertir en "ReservationItem" et ajouter dans le TableView
        for (Map.Entry<Medicament, Integer> entry : resTab.entrySet()) {
            Medicament med = entry.getKey();
            Integer qty = entry.getValue();
            ReservationItem item = new ReservationItem(med, qty);
            reservationItems.add(item);
        }
        reservationTableView.refresh();

    }

    /**
     * Date de création :31/03/2025
     * Date de dernière modification : 12/04/2025
     *
     * <p> Méthode appelée quand on clique sur un médicament dans le tableView.
     * On ajoute ce médicament à la table de réservation s'il n'existe pas,
     * ou on incrémente la quantité s'il est déjà présent. </p>
     *
     * @param event le clic sur le medicament
     *
     * @author Victoria MASSAMBA
     */
    @FXML
    private void medicamentSelectionHandler(MouseEvent event) {
        if (event.getClickCount() == 2 && event.getSource() == zoneAffichageMedicament) {
            Medicament medicament = zoneAffichageMedicament.getSelectionModel().getSelectedItem();

            if (medicament == null) {

                UtilAlert.erreurAlert(
                        "Le médicament n'a pas pu être ajouté",
                        "Médicament indisponible",
                        "Médicament indisponible",
                        "Ok."
                );
                return;
            }

            // Vérifier si ce médicament est déjà dans la table
            ReservationItem existingItem = null;

            for (ReservationItem item : reservationItems) {
                if (item.getMedicament().getCodeCIP() == medicament.getCodeCIP()) {
                    existingItem = item;
                    break;
                }
            }

            // On incrémente la quantité du medicament comme il existe
            if (existingItem != null) {
                existingItem.setQuantite(existingItem.getQuantite() + 1);
                reservationTableView.refresh();
            } else {
                ReservationItem newItem = new ReservationItem(medicament, 1);
                reservationItems.add(newItem);
            }
        }
    }

    /**
     * Date de création : X/04/25
     * Date de dernière modification : 13/04/25
     * Méthode qui gère la selection de quantité pour un médicament reservé
     * @author Victoria MASSAMBA
     * @param event double-clic sur la table de réservation
     */
    private void reservationTableClickHandler(MouseEvent event) {

        if (event.getClickCount() == 2) {
            ReservationItem selectedItem = reservationTableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Si quantité > 1, on décrémente
                if (selectedItem.getQuantite() > 1) {
                    selectedItem.setQuantite(selectedItem.getQuantite() - 1);
                } 
                else {
                    // Si c'était 1, on supprime la ligne
                    reservationItems.remove(selectedItem);
                }
                reservationTableView.refresh();
            }
        }
    }

    /**
     * Date de création : 01/04/2025
     * Date de dernière modif : 25/04/25
     *
     * Méthode qui sauvegarde les modifications apportés à la réservation
     * @author Victoria MASSAMBA
     * @param event clic sur le bouton SAUVEGARDER LES MODIFICATIONS
     */
    @FXML
    private void sauvegarderModifHandler(ActionEvent event) {
        if(reservationItems.isEmpty()) {
            UtilAlert.erreurAlert("La table de réservation est vide","Enregistrement Impossible","Vous ne pouvez pas enregistrer la réservation","Ok");
            return;
        }
        // Vérifier qu'on a bien un ID de réservation
        if (reservationid == null) {
            UtilAlert.erreurAlert("Erreur", "Réservation Invalide", "Impossible de modifier la réservation.", "OK");
            return;
        }
        //Vérifier qu'on a un client
        if (client == null) {
            UtilAlert.erreurAlert("Erreur", "Client Invalide", "Aucun client associé à la réservation.", "OK");
            return;
        }
        // Mettre à jour la réservation
        try(Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            // On met à jour la date si besoin (ou on la garde)
            ReservationDAO.updateReservation(conn,reservationid,client.getId());

            // Supprimer les anciennes lignes dans "reservemedicament"
            ReserveMedicamentDAO.deleteReservation(conn,reservationid);

            // Réinsérer selon la nouvelle quantité
            for (ReservationItem item : reservationItems) {
                ReserveMedicamentDAO.addReservation(conn,reservationid,item.getMedicament().getCodeCIP(),item.getQuantite());

            }

            // envoyer un email de confirmation
            envoyerEmailConfirmation();

            UtilAlert.informationAlert(
                "Succès",
                "Réservation modifiée",
                "La réservation a été mise à jour avec succès.",
                "OK"
            );
            conn.commit();

            // Retour à l'écran précédent
//            NavigationManager.goBack();
            Map<String, Object> params = new HashMap<>();
            params.put("client", client);
            Reservation reservation = ReservationDAO.getReservationById(this.reservationid);
            if (reservation == null) {
                throw new NullPointerException();
            }
            params.put("reservation", reservation);
            NavigationManager.loadPage(Page.RESERVATION_CONSULTATION.getFilePath(), params);

        } catch (SQLException e) {
            UtilAlert.erreurAlert(
                "Erreur",
                "Mise à jour échouée",
                "Impossible de mettre à jour la réservation dans la base de données.",
                "OK"
            );
            e.printStackTrace();
        }

        int id_personnel = SessionManager.getCurrentPersonnel().getId();
        Log log = new Log(Log.LogAction.MODIFIER_RESERVATION, LocalDateTime.now(),id_personnel,this.reservationid);
        LogDAO.ajouterLog(log);
    }


    /**
     * Date de première modification : 25/03/25
     * Date de dernière modification : 04/05/25
     * Méthode qui retourne à la page précédente au clic sur "Retour".
     * @param event clic sur le bouton Retour
     * @author Victoria MASSAMBA
     *
     */
    @FXML
    private void retourHandler(ActionEvent event) {
        boolean choix = UtilAlert.confirmationAlert("Toutes modifications seront perdues","Annuler les modifications","Êtes-vous sûr de vouloir annuler les modifications ?","Oui","Annuler");
        if (choix) {
            NavigationManager.goBack();
        }

    }



    /**
     * Date de création : 26/03/25
     * Date de dernière modification : 07/04/25
     * Méthode permettant de remplir les informations du client dans les champs correspondants.
     *
     * @param client Le client dont les informations doivent être affichées.
     * @author Victoria MASSAMBA
     */

    private void remplirInformationsClient(Client client) {
        nomClientTextField.setText(client.getNom());
        prenomClientTextField.setText(client.getPrenom());
        sexeChoiceBox.setValue(client.getSexe());
        dateNaissanceClient.setValue(client.getDateNaissance());
        adresseClientTextField.setText(client.getAdresse());
        telephoneClientTextField.setText(client.getTelephone());
        emailClientTextField.setText(client.getEmail());
        secuTextField.setText(client.getNumeroSecuriteSociale());
        mutuelleTextField.setText(client.getMutuelle());
    }

    /**
     * Date de création : 11/04/25
     * Date de dernière modification : 11/04/25
     * @author Victoria MASSAMBA
     * Extrait l'ID de réservation depuis la chaine "N°Reservation : 12345|..."
     */

    private Integer extraireIdReservation(String reservationString) {
        if (reservationString != null && reservationString.startsWith("N°Reservation : ")) {
            try {
                String[] parts = reservationString.split("\\|");
                if (parts.length > 0) {
                    return Integer.parseInt(parts[0].substring(16).trim());
                }
            } catch (NumberFormatException e) {
                UtilAlert.erreurAlert(
                    "Erreur", 
                    "Format invalide", 
                    "Impossible d'extraire l'ID de la réservation.", 
                    "OK"
                );
            }
        }
        return null;
    }

    /**
     * Date de création :
     * Date de dernière modification :
     * Méthode qui envoie au client un email pour lui confirmer sa réservation et lui récapituler l'ensemble
     * du contenu de la réservation
     * @author Victoria MASSAMBA
     */


    private void envoyerEmailConfirmation() {
        StringBuilder contenuMail = new StringBuilder("Bonjour,\n\nVotre réservation a bien été mise à jour.\n\n");
        contenuMail.append("Médicaments réservés :\n");
        for (ReservationItem item : reservationItems) {
            contenuMail.append("- ")
                       .append(item.getMedicament().getDenominationMedicament())
                       .append(" x ")
                       .append(item.getQuantite())
                       .append("\n");
        }
        contenuMail.append("\n\nMerci de votre confiance.");

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            UtilEmail.envoyerEmail(client.getEmail(), "Confirmation de réservation", contenuMail.toString());
        } else {
            UtilAlert.erreurAlert(
                "Erreur", 
                "Email manquant", 
                "Impossible d'envoyer un email au client.",
                "OK"
            );
        }
    }

    /**
     * Configure les colonnes de la TableView des médicaments disponibles.
     * @author Victoria MASSAMBA
     */
    private void configurerColonnesMedicaments() {
        codeCisColumn.setCellValueFactory(new PropertyValueFactory<>("codeCIP"));
        denominationColumn.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prixMedicament"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosageSubstance"));

        zoneAffichageMedicament.setItems(medicamentsEnRupture);
    }


    /**
     * Date de création : 20/04/25
     * Date de dernière modification : 20/04/25
     * Méhtode qui charge les médicaments en rupture dans la zone de recherche
     * @author Victoria MASSAMBA
     */
    @FXML
    private void chargerMedicaments() {

        // On récupère tous les médicaments
        try {

            String search = zoneRechercheMedicament.getText().trim();
            if(!search.isEmpty()) {

                List<Medicament> enRupture = ReservationDAO.getMedicamentsPourReservation(search);
                medicamentsEnRupture.setAll(enRupture);
            }else{
                medicamentsEnRupture.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            UtilAlert.erreurAlert("Erreur de chargement", "Erreur", "Erreur lors du chargement des médicaments", "OK");
        }
    }

    /**
     * @author Nicolas ADAMCYZK
     */
    private void ajouterColonneActions() {
        actionsReservationColumn.setCellFactory(col -> new TableCell<>() {
            private final Button plusBtn = new Button("+");
            private final Button moinsBtn = new Button("−");
            private final Button supprimerBtn = new Button("x");
            private final HBox hbox = new HBox(5, plusBtn, moinsBtn, supprimerBtn);

            {
                plusBtn.getStyleClass().add("bouton-vert");
                moinsBtn.getStyleClass().add("bouton-rouge");
                supprimerBtn.getStyleClass().add("bouton-rouge");

                hbox.setAlignment(Pos.CENTER);
                hbox.setMaxWidth(Region.USE_PREF_SIZE);

                plusBtn.setOnAction(e -> {
                    ReservationItem med = getTableView().getItems().get(getIndex());
                    incrementerQuantite(med);
                });

                moinsBtn.setOnAction(e -> {
                    ReservationItem med = getTableView().getItems().get(getIndex());
                    decrementerQuantite(med);
                });

                supprimerBtn.setOnAction(e -> {
                    ReservationItem med = getTableView().getItems().get(getIndex());
                    supprimerDesReservations(med);
                });

                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

        /**
         * Date de création : 20/04/25
         * Date de dernière modification : 20/04/25
         * Augmente la quantité du médicament réservé
         * @author Victoria MASSAMBA
         */

        private void incrementerQuantite(ReservationItem med) {
            int quantite = med.getQuantite();
            med.setQuantite(quantite + 1);
        }

        /**
         * Date de création : 20/04/25
         * Date de dernière modification : 20/04/25
         * Décrémente ou supprime le médicament réservé du tableview.
         * @author Victoria MASSAMBA
         */

        private void decrementerQuantite(ReservationItem med) {
            int quantiteDepart = med.getQuantite();
            if (reservationItems.contains(med)) {
                med.setQuantite(quantiteDepart - 1);

                if (med.getQuantite() <= 0) {
                    reservationItems.remove(med);
                }

                reservationTableView.refresh();
            }
        }

        /**
         * Supprime le médicament réservée de la table de réservations
         * @author Victoria MASSAMABA
         */

        private void supprimerDesReservations(ReservationItem med) {
            reservationItems.remove(med);
            reservationTableView.refresh();
        }


}
