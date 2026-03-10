package controllers.analyses;

import database.LogDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Log;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LogController implements Initializable {


    @FXML private TableView<Log> logTable;

    @FXML private TableColumn<Log, String> dateColumn;
    @FXML private TableColumn<Log, String> heureColumn;
    @FXML private TableColumn<Log, String> idPersonnelColumn;
    @FXML private TableColumn<Log, String> actionColumn;
    @FXML private TableColumn<Log, String> idConcerneColumn;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Configuration des colonnes
        dateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate().format(DATE_FMT))
        );

        heureColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate().format(HEURE_FMT))
        );

        idPersonnelColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getIdPersonnel()))
        );

        actionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAction().getValue())
        );

        idConcerneColumn.setCellValueFactory(cell -> {
            Integer idConc = cell.getValue().getIdConcerne();
            return new SimpleStringProperty(idConc != null ? String.valueOf(idConc) : "—");
        });

        logTable.setItems(
                FXCollections.observableArrayList(LogDAO.getAllLogs())
        );
    }
}