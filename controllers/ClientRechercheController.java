package controllers.clients;

import global.NavigationManager;
import database.ClientDAO;
import global.Page;
import models.Client;
import models.Medicament;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.UtilRecherche;

import java.net.URL;
import java.util.*;

/**
 * Controller de la fenêtre permettant de rechercher un client.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ClientRechercheController implements Initializable {
    @FXML
    private ComboBox<String> rechercherParComboBox;
    @FXML
    private TextField zoneRechercherClientTextField;
    @FXML
    private ListView<Client> zoneAffichageClientListView;
    @FXML
    private Button retourFenetrePrecedenteButton;

    // Permet d'avoir une mise à jour automatique de l'interface en cas de modification de la ListView
    private ObservableList<Client> itemsList;

    private Client clientSelectionne;
    private String mode;
    private Stage stage;

    public void setMode(String mode, Stage stage) {
        this.mode = mode;
        this.stage = stage;
    }


    /**
     * Date : 05/03/2025
     * Date de dernière modification : 04/04/2025
     *
     * <p>Lors d'un double clique sur un client, l'utilisateur est redirigé vers la page de consultation des informations de ce dernier.</p>
     *
     * @param event L'événement de la souris capturé lors du clic
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void listViewClickHandler(MouseEvent event) {
        // Vérifie si un double clic a eu lieu
        if (event.getClickCount() == 2) {
            clientSelectionne = zoneAffichageClientListView.getSelectionModel().getSelectedItem();
            if (clientSelectionne != null) {
                if (!"lecture".equals(mode)) {
                    // Mode Consultation : Ouvrir la page de consultation
                    Map<String, Object> params = new HashMap<>();
                    params.put("client", clientSelectionne);
                    NavigationManager.loadPage(Page.CLIENT_CONSULTATION.getFilePath(), params);
                } else {
                    // Mode Sélection : Fermer la fenêtre et retourner le client
                    fermerFenetre();
                }
            }
        }
    }

    /**
     * Date : 05/03/2025
     * Date : 24/03/2025
     *
     * <p>Permet le retour à la fenêtre précédente du logiciel.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void retourHandler() {
        System.out.println("Clique sur le bouton pour revenir à la fenêtre précédente.");
        NavigationManager.goBack();
    }

    /**
     * Date : 08/03/2025
     * Date : 24/03/2025
     *
     * <p>Redirige vers la page d'ajout d'un client.</p>>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void ajoutClientHandler(){
        System.out.println("Clique sur le bouton d'jaout de client");
        NavigationManager.loadPage(Page.CLIENT_AJOUT.getFilePath());
    }

    /**
     * Date : 05/03/2025
     *
     * <p>Permet l'initialisation des informations du bouton rechercherPar contenant les différents types de recherches pouvant être effectués.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationBoutonRechercherPar(){
        // Remplit la liste déroulante par ces deux String (types de recherche)
        rechercherParComboBox.getItems().addAll("Nom", "Prenom", "N°sécurité sociale");
    }

    /**
     * Date : 24/02/2025
     * Date : 05/03/2025
     * Date : 31/03/2025
     *
     * <p>Permet d'ajouter les clients dans la ListView correspondant à la zone de résultat de recherche de client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationZoneAffichageClient() {
        // Récupérer les clients depuis la base de données
        List<Client> clients = ClientDAO.getTousLesClients();

        // Vérifier si la liste est vide et afficher un message
        if (clients == null || clients.isEmpty()) {
            clients = new ArrayList<>();
        }

        // ChatGPT
        Label placeholderLabel = new Label("Aucun client disponible");
        placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-text-alignment: center;");
        placeholderLabel.setWrapText(true);

        VBox placeholderBox = new VBox(placeholderLabel);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPrefHeight(zoneAffichageClientListView.getHeight());

        zoneAffichageClientListView.setPlaceholder(placeholderBox);
        // Fin ChatGPT

        // Créer un ObservableList pour la ListView
        itemsList = FXCollections.observableArrayList(clients);
        // Définir les items de la ListView
        zoneAffichageClientListView.setItems(itemsList);

        // Personnaliser l'affichage des éléments dans la ListView
        zoneAffichageClientListView.setCellFactory(param -> new ListCell<Client>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);
                if (empty || client == null) {
                    setText(null);
                } else {
                    //TODO améliorer l'affichage
                    setText(client.getNom() + "    " + client.getPrenom());
                }
            }
        });
    }


    /**
     * Date : 24/02/2025
     * Date : 02/04/2025
     *
     * <p>Méthode d'initialisation du contrôleur.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     *
     * @author Hugo VITORINO PEREIRA
     * @author Nicolas ADAMCZYK
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        initialisationBoutonRechercherPar();
        initialisationZoneAffichageClient();
        new UtilRecherche<>(
                rechercherParComboBox,
                zoneRechercherClientTextField,
                itemsList,
                (critere, valeur) -> ClientDAO.rechercherClientsParTexte(critere, valeur),
                (critere, valeur) -> new ArrayList<>() // Pas de recherche numérique pour l'instant
        );
    }


    /**
     * Date de dernière modification : 02/04/2025
     *
     * @return Client
     *
     * @author Hugo VITORINO PEREIRA
     */
    public Client getClientSelectionne() {
        return clientSelectionne;
    }

    /**
     * Méthode qui ferme la fenêtre
     * @author Hugo VITORINO PEREIRA
     */
    private void fermerFenetre() {
        if (stage != null) {
            stage.close();
        }
    }

}
