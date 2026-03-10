package controllers.clients.reservations;

import database.ClientDAO;
import database.ReservationDAO;
import global.NavigationManager;
import global.Page;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import models.Client;
import models.Reservation;
import utils.UtilAlert;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReservationsPharmacieController implements Initializable {
    @FXML
    private TableView<Reservation> reservations;

    @FXML
    private TableColumn<Reservation, Integer> numClientColumn;

    @FXML
    private TableColumn<Reservation, Date> dateColumn;

    @FXML
    private TableColumn<Reservation, String> numResaColumn;

    @FXML
    private ObservableList<Reservation> reservationsObservableList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Configuration du contenu du tableview
        reservationsObservableList = FXCollections.observableArrayList();
        try {
            System.out.println("Nombre de réservations trouvées : " + ReservationDAO.getAllReservations().size());
            reservationsObservableList.addAll(ReservationDAO.getAllReservations());

        } catch (SQLException e) {
            e.printStackTrace();
            UtilAlert.erreurAlert("Erreur lors du chargements des réservations de la pharmacie","Erreur","Chargement des réservations impossible","Ok");
        }
        reservations.setItems(reservationsObservableList);

        //Configuration des colonnes du tableview
        numResaColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Reservation n° " + cellData.getValue().getIdReservation()));
        numClientColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));


    }

    /**
     * Date de création : 27/04/25
     * <p>Date de dernière modification : 27/04/25</p>
     * <p>
     *     Gère le clic associé au tableview de réservation. Un double clic sur une ligne de la table charge la fenêtre de consultation de la réservation.
     * </p>
     * @param event Le double clic sur un élement du table view
     * @author Victoria MASSAMBA
     */

    public void tableViewClickHandler(MouseEvent event)  {
        //Le clic doit provenir du bouton principal de la souris
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        if (event.getClickCount() == 2 && event.getSource() == reservations) {
            Reservation reservationSelected = reservations.getSelectionModel().getSelectedItem();
            if (reservationSelected != null) {
                try {
                    Map<String, Object> params = new HashMap<>();
                    Client client = ClientDAO.getClientById(reservationSelected.getIdClient());
                    if(client!=null) {
                        params.put("client", client);
                        params.put("reservation", reservationSelected);
                        NavigationManager.loadPage(Page.RESERVATION_CONSULTATION.getFilePath(), params);
                    }else{
                        UtilAlert.erreurAlert("Le client n'a pas pu être trouvé","Erreur","Erreur lors de la récupération du client","Ok");
                    }

                }catch(SQLException e) {
                    e.printStackTrace();
                    UtilAlert.erreurAlert("Erreur lors du chargement de la fiche de réservation du client","Erreur","Chargement de la fiche du réservation impossible","Ok");
                }
            }


            }

    }
}
