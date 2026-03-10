package controllers.clients.ordonnances;

import database.OrdonnanceDAO;
import global.InitializableWithParameters;
import global.NavigationManager;
import global.Page;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Client;
import models.Ordonnance;

import java.net.URL;
import java.util.*;

/**
 * Controller de la fenêtre permettant de rechercher une ordonnance.
 *
 * @author Hugo VITORINO PEREIRA
 * @author Victoria MASSAMBA
 */
public class OrdonnanceRechercheController implements Initializable, InitializableWithParameters {

    @FXML
    private ComboBox<String> rechercherParComboBox;
    @FXML
    private TextField zoneRechercherOrdonnanceTextField;
    @FXML
    private ListView<Ordonnance> zoneAffichageOrdonnanceListView;
    @FXML
    private Button retourFenetrePrecedenteButton;

    // Permet d'avoir une mise à jour automatique de l'interface en cas de modification de la ListView
    private ObservableList<Ordonnance> itemsList;

    private Client client;
    private Ordonnance ordonnanceSelectionne;    
    private String mode;
    private Stage stage;

    public void setMode(String mode, Stage stage) {
        this.mode = mode;
        this.stage = stage;
    }
    /**
     * Date : 05/03/2025
     * Date de dernière modificiation : 04/04/2025
     * Méthode permettant d'afficher l'ordonnance séléctionné lors d'un double clique sur la ListView.
     *
     * @author Hugo VITORINO PEREIRA
     */
    // ChatGPT
    @FXML
    public void listViewClickHandler(MouseEvent event){
        // Vérifie si un double clic a eu lieu
        if (event.getClickCount() == 2) {
            ordonnanceSelectionne = zoneAffichageOrdonnanceListView.getSelectionModel().getSelectedItem();
            if (ordonnanceSelectionne != null) {
                if (!"lecture".equals(mode)) {
                    // Mode Consultation : Ouvrir la page de consultation
                    System.out.println("L'ordonnance choisie est : " + ordonnanceSelectionne);
                    Map<String, Object> params = new HashMap<>();
                    params.put("ordonnance", ordonnanceSelectionne);
                    NavigationManager.loadPage(Page.ORDONNANCE_CONSULTATION.getFilePath(),  params);
                } else {
                    // Mode Sélection : Fermer la fenêtre et retourner le client
                    fermerFenetre();
                }
            }
        }
    }
    // Fin ChatGPT

    /**
     * Date : 05/03/2025
     * Permet le retour à la fenêtre précédente du logiciel.
     * Affiche pour le moment un message de clique sur le bouton.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void retourHandler() {
        System.out.println("Clique sur le bouton pour revenir à la fenêtre précédente.");
        NavigationManager.goBack();
    }

    /**
     * Date : 05/03/2025
     * Permet l'initialisation des informations du bouton rechercherPar contenant les différents types de recherches pouvant être effectués.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationBoutonRechercherPar(){
        // Remplis la liste déroulante par ces quatre String (types de recherche)
        rechercherParComboBox.getItems().addAll("Nom", "Prenom", "Médecin", "Date");
    }

    /**
     * Date : 05/03/2025
     * <p>Date de dernière modification : 04/04/2025</p>
     *
     * <p>Permet l'ajout d'ordonances dans la ListView.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationZoneAffichageOrdonnance(){
        // Récupérer les ordonnances depuis la base de données
        List<Ordonnance> ordonnances = OrdonnanceDAO.getToutesLesOrdonnances(client);

        // Vérifier si la liste est vide et afficher un message
        if (ordonnances == null || ordonnances.isEmpty()) {
            ordonnances = new ArrayList<>();
        }

        // ChatGPT
        Label placeholderLabel = new Label("Aucune ordonannce disponible pour ce client");
        placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-text-alignment: center;");
        placeholderLabel.setWrapText(true);

        VBox placeholderBox = new VBox(placeholderLabel);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPrefHeight(zoneAffichageOrdonnanceListView.getHeight());

        zoneAffichageOrdonnanceListView.setPlaceholder(placeholderBox);
        // Fin ChatGPT

        // Créer un ObservableList pour la ListView
        itemsList = FXCollections.observableArrayList(ordonnances);
        // Définir les items de la ListView
        zoneAffichageOrdonnanceListView.setItems(itemsList);

        // Personnaliser l'affichage des éléments dans la ListView
        zoneAffichageOrdonnanceListView.setCellFactory(param -> new ListCell<Ordonnance>() {
            protected void updateItem(Ordonnance ordonnance, boolean empty) {
                super.updateItem(ordonnance, empty);
                if (empty || ordonnance == null) {
                    setText(null);
                } else {
                    //TODO améliorer l'affichage
                    setText(ordonnance.getClient().getNom() + "    " + ordonnance.getMedecin().getNom() + "    " + ordonnance.getDateOrdonnance());
                }
            }
        });
    }

    /**
     * Date de création : 04/04/2025
     *
     * @param parameters Un {@link Map} contenant les paramètres nécessaires à l'initialisation. La clé "client" doit être associée à un objet {@link Client}.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        this.client = (Client) parameters.get("client");
        initialisationZoneAffichageOrdonnance();
    }

    /**
     * Date : 05/03/2025
     * Méthode d'initialisation du contrôleur.
     * Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources){
        initialisationBoutonRechercherPar();
    }




    /**
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 09/04/2025
     * Méthode qui selectionne une ordonnance
     *
     * @return Ordonnance L'ordonnance selectionnée
     *
     * @author Victoria MASSAMBA
     */
    public Ordonnance getOrdonnanceSelectionnee() {
        return ordonnanceSelectionne;
    }

    /**
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 09/04/2025
     * Méthode qui ferme la fenêtre du controller
     *
     *
     * @author Victoria MASSAMBA
     */
    private void fermerFenetre() {
        if (stage != null) {
            stage.close();
        }
    }
}
