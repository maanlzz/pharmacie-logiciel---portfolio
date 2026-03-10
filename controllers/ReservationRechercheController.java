package controllers.clients.reservations;

import global.InitializableWithParameters;
import global.NavigationManager;
import global.Page;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import models.Client;
import models.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import database.NosMedicamentsDAO;
import database.ReservationDAO;
import database.ReserveMedicamentDAO;
import utils.UtilAlert;

/**
 * Controller de la fenêtre de recherche des réservations d'un client.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ReservationRechercheController implements Initializable,InitializableWithParameters {
    private final ObservableList<Reservation> itemsList =
            FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> rechercherParComboBox;
    @FXML
    private TextField zoneRechercherReservationTextField;
    @FXML
    private TableView<Reservation> resaTableView;
    @FXML
    private TableColumn<Reservation, String> numResaColumn;
    @FXML
    private TableColumn<Reservation, Date> dateResaColumn;

    @FXML
    private Button retourFenetrePrecedenteButton;

    private Client client;

    /**
     * Date : 05/03/2025
     *
     * <p>Méthode permettant d'afficher la réservation séléctionnée lors d'un double clique sur la tableView.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    // ChatGPT
    @FXML
    public void tableViewClickHandler(MouseEvent event){
        // Verifie si il y a un double click
        if (event.getClickCount() == 2) {
            Reservation reservationSelectionne = resaTableView.getSelectionModel().getSelectedItem();
            if (reservationSelectionne != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("client", client);
                params.put("reservation", reservationSelectionne);
                NavigationManager.loadPage(Page.RESERVATION_CONSULTATION.getFilePath(), params);
            }
        }
    }
    // Fin ChatGPT

    /**
     * Date : 05/03/2025
     * Date : 24/03/2025
     *
     * <p>Permet le retour à la fenêtre précédente du logiciel.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void retourHandler(){
        System.out.println("Clique sur le bouton pour revenir à la fenêtre précédente.");
        NavigationManager.goBack();
    }

    /**
     * Date : 03/05/2025
     *
     * <p>Permet l'initialisation des informations du bouton rechercherPar contenant les différents types de recherches pouvant être effectués.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationBoutonRechercherPar(){
        // Remplis la liste déroulante par ces quatres String (types de recherche)
        rechercherParComboBox.getItems().addAll("N°Réservation", "Date");
    }


    /**
     * Date de création : 11/04/25
     * Date de dernière modification : 03/05/25
     * Méthode d'initialisation avec paramètres.
     * @author Victoria MASSAMBA
     * @param parameters Paramètres nécessaires à l'initialisation.
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {


        this.client = (Client) parameters.get("client");

        if (itemsList == null) { // sécurité si l’ordre d’appel change

            resaTableView.setItems(itemsList);
        }

        if (this.client != null) {
            try {
                itemsList.clear();
                List<Reservation> reservations = ReservationDAO.getReservationsByClient(this.client.getId());
                System.out.println("Réservations trouvées = " + itemsList.size());
                itemsList.addAll(reservations);
            } catch (SQLException e) {
                e.printStackTrace();
                UtilAlert.erreurAlert("Chargement de la liste de réservations impossible","Erreur","Erreur lors du chargement des réservations du client","Ok");
            }
        }

    }



    /**
     * Date : 24/02/2025
     * Date : 03/05/2025
     *
     * <p>Méthode d'initialisation du contrôleur.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     * @author Victoria MASSAMBA
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources){

        resaTableView.setItems(itemsList);
        //configuration des colonnes du tableau
        dateResaColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getDateReservation()
                )
        );
        numResaColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Reservation n° " + cellData.getValue().getIdReservation()));
        initialisationBoutonRechercherPar();
        resaTableView.refresh();
    }
}