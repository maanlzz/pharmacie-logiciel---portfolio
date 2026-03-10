package controllers.clients.reservations;

import database.LogDAO;
import database.ReservationDAO;
import global.InitializableWithParameters;
import global.NavigationManager;
import global.Page;

import global.SessionManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import models.*;
import utils.UtilAlert;
//import utils.UtilEmail;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller de la fenêtre de consultation d'une réservation
 * @author Victoria MASSAMBA
 * Date de première modification : 01/04/25
 *
 */
public class ReservationConsultationController implements Initializable, InitializableWithParameters {

    //Champs FXML relatifs au client
    @FXML private TextField nomClientTextField;
    @FXML private TextField prenomClientTextField;
    @FXML private ChoiceBox<String> sexeChoiceBox;
    @FXML private DatePicker dateNaissanceClient;
    @FXML private TextField adresseClient;
    @FXML private TextField telephoneClientTextField;
    @FXML private TextField emailClientTextField;
    @FXML private TextField secuTextField;
    @FXML private TextField mutuelleTextField;

    @FXML private CheckBox emailCheckBox;
    @FXML private CheckBox appelCheckBox;

    @FXML private TableView<ReservationItem> reservationTableView;
    @FXML private TableColumn<ReservationItem, String> nomMedicamentColumn;
    @FXML private TableColumn<ReservationItem, Number> quantiteColumn;


    @FXML private Button modifierReservationButton;
    @FXML private Button supprimerReservationButton;
    @FXML private Button retourButton;

    private ObservableList<ReservationItem> reservationItems; 
    private Client client;
    private Integer reservationid;
    private String reservationString;  // Exemple: "N°Reservation : 6901061"

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
//        Map<String, Object> params = new HashMap<>();
//        params.put("client", client);
//        NavigationManager.loadPage(Page.CLIENT_CONSULTATION.getFilePath(), params);
        NavigationManager.goBack();
    }

    /**
     * Date de première modification : 1/04/25
     * Date de dernière modification : 12/04/25
     * Méthode de clic sur "Modifier la réservation".
     * On passe les paramètres (client, etc.) à la page de modification.
     * @param event clic sur le bouton MODIFIER LA RESERVATION
     * @author Victoria MASSAMBA
     */
    @FXML
    private void modifierReservationhandler(ActionEvent event) {
        Map<String, Object> params = new HashMap<>();
        params.put("client", this.client);
        params.put("reservation", this.reservationString);

        NavigationManager.loadPage(Page.RESERVATION_MODIFICATION.getFilePath(), params);
    }

    /**
     * Date de première modification : 1/04/25
     * Date de dernière modification : 12/04/25
     * Méthode qui supprime la réservation de la BDD via ReservationDAO,
     * puis revient à l'écran précédent.
     * @param event clic sur le bouton SUPPRIMER LA RESERVATION
     * @author Victoria MASSAMBA
     */
    @FXML
    private void supprimerReservationHandler(ActionEvent event) {
        if (reservationid != null) {
            try {
                ReservationDAO.supprimerReservation(reservationid);
                NavigationManager.goBack();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int id_personnel = SessionManager.getCurrentPersonnel().getId();
            Log log = new Log(Log.LogAction.SUPPRIMER_RESERVATION, LocalDateTime.now(),id_personnel,reservationid);
            LogDAO.ajouterLog(log);
        } else {
            UtilAlert.erreurAlert(
                "Erreur", "Réservation invalide", 
                "Impossible de supprimer la réservation, ID introuvable", 
                "OK"
            );
        }
    }

    /**
     * Méthode qui remplit les champs du client pour la réservation
     * @param client Le client pour lequel on crée la réservation
     * @author Victoria MASSAMBA
     * Date de première modification : 26/03/25
     * Date de dernière modification : 26/03/25
     */
    private void remplirInformationsClient(Client client) {
        if (client != null) {
            nomClientTextField.setText(client.getNom());
            prenomClientTextField.setText(client.getPrenom());
            sexeChoiceBox.setValue(client.getSexe());
            dateNaissanceClient.setValue(client.getDateNaissance());
            adresseClient.setText(client.getAdresse());
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

    /**
     * Date de création : 11/04/25
     * Date de dernière modification : 11/04/25
     * Extrait l'ID de réservation depuis la chaine "N°Reservation : 12345|..."
     */
    private Integer extraireIdReservation(String reservationString) {
        if (reservationString != null && reservationString.startsWith("N°Reservation : ")) {
            try {
                String[] parts = reservationString.split("\\|");
                if (parts.length > 0) {
                    // substring(16) => enlève "N°Reservation : "
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
     * Date de création : 11/04/25
     * Date de dernière modification : 12/04/25
     * Méthode qui initialise les paramètres "client" et "reservation".
     * @author Victoria MASSAMBA
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {

        // Récupérer le client
        this.client = (Client) parameters.get("client");
        if (this.client != null) {
            remplirInformationsClient(this.client);
        }

        Object obj = parameters.get("reservation");
        if (obj instanceof Reservation res) {
            this.reservationid = res.getIdReservation();
            this.reservationString = "N°Reservation : " + reservationid;
        } else if (obj instanceof String s) {
            this.reservationString = s;
            this.reservationid = extraireIdReservation(s);
        } else {
            throw new IllegalArgumentException("Paramètre 'reservation' invalide");
        }


        // Charger les médicaments + quantités de la réservation
        Map<Medicament, Integer> resTab = new HashMap<>();
        if (reservationid != null) {
            try {

                resTab = ReservationDAO.getMedicamentByReservations(reservationid);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Convertir chaque (Medicament, quantite) en ReservationItem
        for (Map.Entry<Medicament, Integer> entry : resTab.entrySet()) {
            Medicament med = entry.getKey();
            Integer quantite = entry.getValue();
            ReservationItem item = new ReservationItem(med, quantite);
            reservationItems.add(item);
        }
        reservationTableView.refresh();
    }

    /**
     * Date de création : 1/04/25
     * Date de dernière modification : 12/04/25
     * Méthode d'initialisation du contrôleur (Initializable).
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        reservationItems = FXCollections.observableArrayList();
        reservationTableView.setItems(reservationItems);


        nomMedicamentColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(
                cellData.getValue().getMedicament().getDenominationMedicament()
            )
        );


        quantiteColumn.setCellValueFactory(cellData -> 
            cellData.getValue().quantiteProperty()
        );

        System.out.println("init  -> reservationItems = " + reservationItems);
    }
}
