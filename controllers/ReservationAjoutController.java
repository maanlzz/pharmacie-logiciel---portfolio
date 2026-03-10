package controllers.clients.reservations;

import database.*;
import global.InitializableWithParameters;
import global.NavigationManager;
import global.SessionManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import models.*;
import utils.UtilAlert;


import utils.UtilEmail;
import utils.UtilRecherche;

import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller de la fenêtre permettant l'ajout d'une réservation de médicaments pour un client.
 *
 * @author Victoria MASSAMBA
 */
public class ReservationAjoutController implements Initializable, InitializableWithParameters {

    private Stage stage;
    @FXML private HBox barreMenuContainer;

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


    @FXML
    private Button enregistrerReservationButton;
    @FXML
    private Button retourButton;

    private boolean reservationEnregistree = false;


    // Champs client
    @FXML
    private TextField nomClientTextField;
    @FXML
    private TextField prenomClientTextField;
    @FXML
    private ChoiceBox<String> sexeChoiceBox;
    @FXML
    private DatePicker dateNaissanceDatePicker;
    @FXML
    private TextField adresseClientTextField;
    @FXML
    private TextField telephoneClientTextField;
    @FXML
    private TextField emailClientTextField;
    @FXML
    private TextField secuTextField;
    @FXML
    private TextField mutuelleTextField;

    @FXML
    private TableView<ReservationItem> reservationTableView;


    @FXML
    private TableColumn<ReservationItem, String> nomMedicamentColumn;
    @FXML
    private TableColumn<ReservationItem, Number> quantiteColumn;
    @FXML
    private TableColumn<ReservationItem, Void> actionsReservationColumn;


    private ObservableList<Medicament> medicamentsEnRupture;
    private ObservableList<ReservationItem> reservationItems;

    private Client client;


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
     * Date de création :31/03/2025
     * Date de dernière modification : 12/04/2025
     *
     * Méthode appelée quand on clique sur un médicament dans la ListView.
     * On ajoute ce médicament à la table de réservation s'il n'existe pas,
     * ou on incrémente la quantité s'il est déjà présent.
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
     * Date de création :01/04/2025
     * Date de dernière modification : 25/04/2025
     * <p>
     * Méthode appelée quand on clique sur "Enregistrer la réservation".
     * Elle enregistre dans la reservation dans la bdd (table 'Reservation' + table 'ReserveMedicament')
     * puis envoie un email de confirmation.
     *
     * @param event l'évenement de clic sur le bouton "enregistrer la réservation"
     *
     * @author Victoria MASSAMBA
     */
    @FXML
    private void enregistrerReservationHandler(ActionEvent event) {
        if(reservationItems.isEmpty()) {
            UtilAlert.erreurAlert("La table de réservation est vide","Enregistrement Impossible","Vous ne pouvez pas enregistrer la réservation","Ok");
            return;
        }

        int id_client = client.getId();
        int id_resa;
        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            // Ajout de la réservation dans la bdd
            id_resa = ReservationDAO.ajouterReservation(conn,id_client);

            // Insérer chacun des médicaments réservés dans la bdd
            for (ReservationItem item : reservationItems) {
                //ReserveMedicamentDAO.addReservationWithImport(conn, id_resa, item.getMedicament().getCodeCIP(), item.getQuantite());
                ReserveMedicamentDAO.addReservation(conn,
                        id_resa,
                        item.getMedicament().getCodeCIP(),
                        item.getQuantite()
                );
            }

            conn.commit();
            reservationEnregistree = true;
        }catch (SQLException e) {

            UtilAlert.erreurAlert("Erreur lors de l'enregistrement de la réservation", "Erreur", "Réservation impossible", "OK");
            System.err.println("Erreur lors de l'enregistrement de la réservation : "+e.getMessage());
            e.printStackTrace();
            return;

        }
        // envoyer mail de confirmation
        envoyerEmailConfirmation();
        int id_personnel = SessionManager.getCurrentPersonnel().getId();
        Log log = new Log(Log.LogAction.AJOUTER_RESERVATION, LocalDateTime.now(),id_personnel,id_resa);
        LogDAO.ajouterLog(log);


        UtilAlert.informationAlert(
                "Succès",
                "Réservation enregistrée",
                "La réservation a été enregistrée avec succès.",
                "OK"
        );
        //Si la fenetre est un pop up on la ferme sinon on retourne en arrière
        if (stage != null) {
            stage.close();
        } else {
            NavigationManager.goBack();
        }
    }


    /**
     * Date de création : 01/04/2025
     * Date de dernière modification : 07/04/2025
     * Méthode qui envoie au client un email pour lui confirmer sa réservation et lui récapituler l'ensemble
     * du contenu de la réservation
     * @author Victoria MASSAMBA
     */
    private void envoyerEmailConfirmation() {
        StringBuilder contenuMail = new StringBuilder("Bonjour,\n\nVotre réservation a bien été prise en compte.\n");
        contenuMail.append("Médicaments réservés :\n");
        for (ReservationItem item : reservationItems) {
            contenuMail.append("- ")
                    .append(item.getMedicament().getDenominationMedicament())
                    .append(" x ")
                    .append(item.getQuantite())
                    .append("\n");
        }
        contenuMail.append("\n\nMerci de votre confiance.");

        if (client != null && client.getEmail() != null && !client.getEmail().isEmpty()) {

            UtilEmail.envoyerEmail(client.getEmail(), "Confirmation de réservation", contenuMail.toString());
            System.out.println("Email envoyé à l'adresse : " + client.getEmail()); //pour déboccage
        } else {
            UtilAlert.erreurAlert(
                    "Erreur",
                    "Email manquant",
                    "Impossible d'envoyer un email au client (adresse email absente).",
                    "OK"
            );
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
     * Supprime le médicament réservé de la table de réservations
     * @author Victoria MASSAMABA
     */

    private void supprimerDesReservations(ReservationItem med) {
        reservationItems.remove(med);
        reservationTableView.refresh();
    }

    /**
     * Date de création :X/X/2025
     * Date de dernière modification : 12/04/2025
     * Méthode appelée lors de l'initialisation du contrôleur.
     * @author Victoria MASSAMBA
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Préparation de la liste des médicaments en rupture
        medicamentsEnRupture = FXCollections.observableArrayList();
        // Préparation de la liste d'items (chaque item = un médicament + la quantité)
        reservationItems = FXCollections.observableArrayList();
        reservationTableView.setItems(reservationItems);

        double size = 270;

        actionsReservationColumn.setMinWidth(size);
        actionsReservationColumn.setPrefWidth(size);
        actionsReservationColumn.setMaxWidth(size);
        actionsReservationColumn.setResizable(false);

        // désactive le redimensionnement automatique
        reservationTableView.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );


        // Lier la colonne "nom du médicament"
        nomMedicamentColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getMedicament().getDenominationMedicament()
                )
        );

        // Lier la colonne "quantité"
        // asObject() convertit la propriété NumberProperty en ObservableValue<Number>
        quantiteColumn.setCellValueFactory(cellData ->
                cellData.getValue().quantiteProperty()
        );

        //Configuration de la colonne action du tableview des réservations
        ajouterColonneActions();

        //CHargement des médicaments
        //chargerMedicaments();

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
     * Date de création :12/04/2025
     * Date de dernière modification : 23/04/2025
     * Méthode qui initialise la page à son chargement
     * On récupère le client, on affiche ses infos, puis on initialise la liste de médicaments.
     * On récupère potentiellement la map des médicaments en rupture et on la met dans la table de réservation
     * @param parameters la map qui contient les paramètres
     * @author Victoria MASSAMBA
     *
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        // Récupération et affichage du client
        this.client = (Client) parameters.get("client");
        if (this.client != null) {
            remplirInformationsClient(this.client);
        }

        // Récupération de la map des médicaments en rupture avec quantités prescrites
        if (parameters.containsKey("medicamentsRupture")) {
            @SuppressWarnings("unchecked")
            Map<Medicament, Integer> ruptures = (Map<Medicament, Integer>) parameters.get("medicamentsRupture");

            //on remplit la table de réservation avec la quantité prescrite
            for (Map.Entry<Medicament, Integer> entry : ruptures.entrySet()) {
                Medicament med       = entry.getKey();
                int qtePrescrite      = entry.getValue();
                reservationItems.add(new ReservationItem(med, qtePrescrite));
            }

            // on met à jour l’affichage de la table
            reservationTableView.refresh();
        }
    }


    /**
     * Date de création : 26/03/25
     * Date de dernière modification : 26/03/25
     * Méthode permettant de remplir les informations du client dans les champs correspondants.
     *
     * @param client Le client dont les informations doivent être affichées.
     * @author Victoria MASSAMBA
     */
    private void remplirInformationsClient(Client client) {
        if (client != null) {
            nomClientTextField.setText(client.getNom());
            prenomClientTextField.setText(client.getPrenom());
            sexeChoiceBox.setValue(client.getSexe());
            dateNaissanceDatePicker.setValue(client.getDateNaissance());
            adresseClientTextField.setText(client.getAdresse());
            telephoneClientTextField.setText(client.getTelephone());
            emailClientTextField.setText(client.getEmail());
            secuTextField.setText(client.getNumeroSecuriteSociale());
            mutuelleTextField.setText(client.getMutuelle());
        } else {
            UtilAlert.erreurAlert(
                    "Erreur",
                    "Client introuvable",
                    "Impossible de charger les informations du client.",
                    "OK"
            );
        }
    }


    private void fermerFenetre() {

        if (stage != null) {
            stage.close();
        }
    }

    public void setMode(String mode, Stage stage) {
        this.stage = stage;
        if ("creation".equals(mode)) {
            // rend la barre de menu visible mais inopérante

            barreMenuContainer.setDisable(true);
            barreMenuContainer.setMouseTransparent(true);
            barreMenuContainer.setFocusTraversable(false);
        }
    }

    /**
     * Date de première modification : 25/03/25
     * Date de dernière modification : 27/04/25
     * Méthode qui retourne à la page précédente au clic sur "Retour".
     * @param event clic sur le bouton Retour
     * @author Victoria MASSAMBA
     *
     */
    @FXML
    private void retourHandler(ActionEvent event) {
        NavigationManager.goBack();
    }

    public boolean isReservationEnregistree() { return reservationEnregistree; }
}
