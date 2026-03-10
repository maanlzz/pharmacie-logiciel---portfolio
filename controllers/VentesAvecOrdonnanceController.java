package controllers.ventes;

import controllers.clients.ClientAjoutController;
import controllers.clients.UtilClient;
import controllers.clients.ordonnances.OrdonnanceAjoutController;
import controllers.clients.reservations.ReservationAjoutController;
import database.*;
import global.NavigationManager;
import global.Page;
import global.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.*;
import utils.UtilAlert;
import utils.UtilRecherche;
import controllers.clients.ordonnances.UtilOrdonnance;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des ventes avec ordonnances.
 * Permet de sélectionner un client, de choisir une ou plusieurs ordonnances,
 * d'ajouter leurs médicaments prescrits dans le panier, puis de valider la vente.
 *
 * Date de dernière modification : 12/04/25
 */
public class VentesAvecOrdonnanceController implements Initializable {

    @FXML
    public Button reserverButton;


    @FXML
    private Button annulerButton, vendreButton;
    @FXML
    private ComboBox<String> rechercherParButton;
    @FXML
    private TextField zoneRechercheMedicament;
    @FXML
    private Label totalLabel;

    // TableView des médicaments disponibles
    @FXML
    private TableView<Medicament> zoneAffichageMedicament;
    @FXML
    private TableColumn<Medicament, String> codeCisColumn;
    @FXML
    private TableColumn<Medicament, String> denominationColumn;
    @FXML
    private TableColumn<Medicament, Double> prixColumn;
    @FXML
    private TableColumn<Medicament, Integer> quantiteColumn;
    @FXML
    private TableColumn<Medicament, String> dosageColumn;

    //  TableView du panier
    @FXML
    private TableView<Medicament> zonePanierMedicament;
    @FXML
    private TableColumn<Medicament, String> denominationPanierColumn;
    @FXML
    private TableColumn<Medicament, Double> prixPanierColumn;
    @FXML
    private TableColumn<Medicament, Integer> quantitePanierColumn;
    @FXML
    private TableColumn<Medicament, Void> actionsPanierColumn;

    @FXML
    public TableColumn<Medicament,Void> indispoColumn;

    // TableView du client
    @FXML
    private TableView<Client> zoneAffichageClient;
    @FXML
    private TableColumn<Client, String> nomClient;
    @FXML
    private TableColumn<Client, String> prenomClient;
    @FXML
    private TableColumn<Client, Date> dateNaissanceClient;

    // TableView des ordonnances sélectionnées
    @FXML
    private TableView<Ordonnance> ZoneAffichageOrdonnance;
    @FXML
    private TableColumn<Ordonnance, Date> dateOrdonnance;
    @FXML
    private TableColumn<Ordonnance, String> nomOrdonnance;
    @FXML
    private TableColumn<Ordonnance, Void> actionsOrdonnanceColumn;
    public TableColumn<Ordonnance,String> medecinOrdonnance;


    //  Boutons pour ordonnances/clients
    @FXML
    private Button ajouterOrdonnanceBouton1; // Ajouter une ordonnance
    @FXML
    private Button selectionnerOrdonnanceBouton; // Sélectionner une ordonnance existante
    @FXML
    private Button selectionnerClientBouton;     // Sélectionner un client
    @FXML
    private Button ajouterClientBouton;          // Créer un nouveau client

    // Données internes
    private Client clientSelectionne;    // Le client sélectionné
    private ObservableList<Ordonnance> ordonnancesSelectionnees = FXCollections.observableArrayList();
    private ObservableList<Medicament> items;     // Liste des médicaments nécessitant une ordonnance
    private ObservableList<Medicament> medicamentsAVendre; // Médicaments ajoutés au panier
    private Map<Medicament, Integer> quantitesPanier;     // Map (Medicament -> quantité à vendre)

    // Pour la recherche
    private UtilRecherche<Medicament> utilRecherche;

    // Se remplira s'il y a des médicaments en rupture dans le panier

    private final Map<Medicament, Integer> medicamentsRupture = new LinkedHashMap<>();



    /**Date de dernière modification :03/05/2025
     * <p></p>
     * Méthode initiale (Initializable).
     * @author Victoria MASSAMBA
     * @param resources
     * @param url
     */
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        vendreButton.setDisable(true);
        // On désactive le bouton Reserver au préalable
        reserverButton.setDisable(true);

        // Configuration des colonnes du client
        nomClient.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom())
        );
        prenomClient.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom())
        );
        dateNaissanceClient.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(Date.valueOf(cellData.getValue().getDateNaissance()))
        );



        // Configuration de la liste d'ordonnances et de la tableView "ZoneAffichageOrdonnance"
        ZoneAffichageOrdonnance.setItems(ordonnancesSelectionnees);
        dateOrdonnance.setCellValueFactory(new PropertyValueFactory<>("dateOrdonnance"));
        nomOrdonnance.setCellValueFactory(cellData ->
                new SimpleStringProperty("Ordonnance n° " + cellData.getValue().getId())
        );
        medecinOrdonnance.setCellValueFactory(cellData ->new SimpleStringProperty(cellData.getValue().getMedecin().getNom()+" "+cellData.getValue().getMedecin().getPrenom()));
        ajouterColonneActionsOrdonnance();


        // Initialisation du panier et de la liste "items"
        medicamentsAVendre = FXCollections.observableArrayList();
        items = FXCollections.observableArrayList();
        quantitesPanier = new HashMap<>();


        configurerColonnesMedicaments();
        configurerColonnesPanier();
        configurerColonneIndisponibilite();

        chargerMedicaments();

        // Configuration de la colonne d'actions dans le panier
        ajouterColonneActions();


        configurerRecherche();


        configurerMenusContextuels();
    }

    /**
     * Configure les colonnes de la TableView des médicaments disponibles.
     *
     * @author Nicolas Adamczyk
     */
    private void configurerColonnesMedicaments() {
        codeCisColumn.setCellValueFactory(new PropertyValueFactory<>("codeCIP"));
        denominationColumn.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prixMedicament"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosageSubstance"));

        zoneAffichageMedicament.setItems(items);
    }

    /**
     * Configure les colonnes de la TableView du panier.
     *
     * @author Nicolas Adamczyk
     */
    private void configurerColonnesPanier() {
        denominationPanierColumn.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
        prixPanierColumn.setCellValueFactory(new PropertyValueFactory<>("prixMedicament"));
        quantitePanierColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        quantitesPanier.getOrDefault(cellData.getValue(), 0)
                ).asObject()
        );

        zonePanierMedicament.setItems(medicamentsAVendre);
    }

    /**
     * Méhtode qui charge les médicaments
     *
     * @author Victoria MASSAMBA
     */


    private void chargerMedicaments() {
        // On récupère tous les médicaments
            List<Medicament> tous = NosMedicamentsDAO.getAllMedicaments();
            if(tous==null) {
                UtilAlert.erreurAlert("Erreur de chargement", "Erreur","Chargement des medicaments impossible", "OK");

            }else{
                items.setAll(tous);
            }




    }

    /**
     * Méhtode qui configure le système de recherches de médicaments
     *
     * @author Victoria MASSAMBA
     */

    private void configurerRecherche() {
        rechercherParButton.getItems().addAll("Dénomination", "Prix");
        // Par défaut, Dénomination
        rechercherParButton.getSelectionModel().select("Dénomination");

        utilRecherche = new UtilRecherche<>(
                rechercherParButton,
                zoneRechercheMedicament,
                items,
                this::rechercherParCritereTexte,
                this::rechercherParCritereNumerique
        );
    }

    /**
     * Méthode qui effectue une recherche de médicaments par critère textuel.
     *
     * @param critere Le critère de recherche (Dénomination)
     * @param valeur  La valeur à rechercher
     * @return La liste des médicaments correspondants
     * @author Victoria Massamba
     */
    private List<Medicament> rechercherParCritereTexte(String critere, String valeur) {
        if ("Dénomination".equals(critere)) {
            return new ArrayList<>(NosMedicamentsDAO.rechercherParNom(valeur));
        }
        return List.of();
    }

    /**
     * Méthode qui effectue une recherche de médicaments par critère numérique.
     *
     * @param critere Le critère de recherche (Prix)
     * @param valeur  La valeur à rechercher
     * @return La liste des médicaments correspondants
     * @author Victoria MASSAMBA
     */
    private List<Medicament> rechercherParCritereNumerique(String critere, Double valeur) {
        try {
            if ("Prix".equals(critere)) {
                return new ArrayList<>(NosMedicamentsDAO.rechercherParPrix(valeur));
            }
            return List.of();
        } catch (SQLException e) {
            UtilAlert.erreurAlert("Erreur de recherche", "Erreur", "Chargement des médicaments par prix impossible", "OK");
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Calcule le montant total de la vente en cours.
     *
     * @return Le montant total calculé
     * @author Nicolas Adamczyk
     */
    private double calculerMontantTotal() {
        return quantitesPanier.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrixMedicament() * e.getValue())
                .sum();
    }

    /**
     * Calcule et affiche le montant total dans le label dédié.
     *
     * @author Victoria MASSAMBA
     */
    private void calculerEtAfficherTotal() {
        totalLabel.setText(String.format("Total: %.2f€", calculerMontantTotal()));
    }

    /**
     * Gère les clics sur les TableView pour ajouter ou supprimer des médicaments du panier.
     *
     * @param event L'événement de clic de souris
     * @author Nicolas Adamczyk
     */
    @FXML
    private void listViewClickHandler(MouseEvent event) {
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        // Double-clic sur la liste => ajouter un exemplaire
        if (event.getClickCount() == 2 && event.getSource() == zoneAffichageMedicament) {
            Medicament selected = zoneAffichageMedicament.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int stockDisponible = selected.getQuantite() - quantitesPanier.getOrDefault(selected, 0);
                if (stockDisponible > 0) {
                    if (quantitesPanier.containsKey(selected)) {
                        quantitesPanier.put(selected, quantitesPanier.get(selected) + 1);
                    } else {
                        medicamentsAVendre.add(selected);
                        quantitesPanier.put(selected, 1);
                    }
                    calculerEtAfficherTotal();
                    zonePanierMedicament.refresh();
                    updateVendreButton();
                } else {
                    UtilAlert.informationAlert(
                            "Stock insuffisant",
                            "Information",
                            "Stock épuisé pour " + selected.getDenominationMedicament(),
                            "OK"
                    );
                }
            }
        }
    }

    /**
     * Date de création : 13/04/25
     * Date de dernière modif : 13/04/25
     * Méthode qui ajoute un bouton "supprimer" et gère l'évènement associé au clic de ce bouton
     *
     * @author Victoria MASSAMBA
     */

    private void ajouterColonneActionsOrdonnance() {
        actionsOrdonnanceColumn.setCellFactory(col -> new TableCell<>() {

            private final Button supprimerBtn = new Button("Supprimer");

            {
                supprimerBtn.getStyleClass().add("bouton-rouge");
                supprimerBtn.setOnAction(e -> {
                    // Récupère l'ordonnance sur la ligne courante
                    Ordonnance ordonnance = getTableRow().getItem();
                    if (ordonnance != null) {
                        supprimerOrdonnance(ordonnance);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Afficher le bouton
                    setGraphic(supprimerBtn);
                }
            }
        });
    }


    /**
     * Date de création : 13/04/25
     * Date de dernière modification : 20/04/25
     * Retire l'ordonnance de la liste des ordonnances sélectionnées et les prescriptions associées.
     *
     * @param ordonnance L'ordonnance à supprimer
     * @author Victoria MASSAMBA
     */
    private void supprimerOrdonnance(Ordonnance ordonnance) {
        ordonnancesSelectionnees.remove(ordonnance);
        ZoneAffichageOrdonnance.refresh();

        // pour chaque prescription de l’ordonnance supprimée
        for (Prescription p : ordonnance.getElementsPrescription()) {
            Medicament med = p.getMedicament();
            int qtePrescrite = p.getQuantite();

            // décremente la quantité du médicament s'il est présent dans une autre ordonnance selectionnée ou sinon le supprimer du panier
            int restant = quantitesPanier.getOrDefault(med, 0) - qtePrescrite;
            if (restant <= 0) {
                quantitesPanier.remove(med);
                medicamentsAVendre.remove(med);
            } else {
                quantitesPanier.put(med, restant);
            }
        }

        //prix et panier mis à jours
        calculerEtAfficherTotal();
        zonePanierMedicament.refresh();
        updateVendreButton();
        updateReserverButton();
    }


    /**
     * @author Nicolas ADAMCYZK
     */
    private void ajouterColonneActions() {
        actionsPanierColumn.setCellFactory(col -> new TableCell<>() {
            private final Button plusBtn = new Button("+");
            private final Button moinsBtn = new Button("−");
            private final Button supprimerBtn = new Button("x");
            private final HBox hbox = new HBox(5, plusBtn, moinsBtn, supprimerBtn);

            {
                plusBtn.getStyleClass().add("bouton-panier-vert");
                moinsBtn.getStyleClass().add("bouton-panier-jaune");
                supprimerBtn.getStyleClass().add("bouton-panier-rouge");

                plusBtn.setOnAction(e -> {
                    Medicament med = getTableView().getItems().get(getIndex());
                    incrementerQuantite(med);
                });

                moinsBtn.setOnAction(e -> {
                    Medicament med = getTableView().getItems().get(getIndex());
                    decrementerQuantite(med);
                });

                supprimerBtn.setOnAction(e -> {
                    Medicament med = getTableView().getItems().get(getIndex());
                    supprimerDuPanier(med);
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
     * @author Nicolas ADAMCYZK
     */

    private void incrementerQuantite(Medicament med) {
        int stockDisponible = med.getQuantite() - quantitesPanier.getOrDefault(med, 0);

        if (stockDisponible > 0) {
            quantitesPanier.put(med, quantitesPanier.getOrDefault(med, 0) + 1);
            calculerEtAfficherTotal();
            zonePanierMedicament.refresh();
            updateVendreButton();
        } else {
            UtilAlert.informationAlert("Stock insuffisant", "Information",
                    "Stock épuisé pour " + med.getDenominationMedicament(), "OK");
        }
    }

    /**
     * @author Nicolas ADAMCYZK
     */

    private void decrementerQuantite(Medicament med) {
        if (quantitesPanier.containsKey(med)) {
            int nouvelleQuantite = quantitesPanier.get(med) - 1;

            if (nouvelleQuantite <= 0) {
                quantitesPanier.remove(med);
                medicamentsAVendre.remove(med);
            } else {
                quantitesPanier.put(med, nouvelleQuantite);
            }

            calculerEtAfficherTotal();
            zonePanierMedicament.refresh();
            updateVendreButton();
        }
    }

    /**
     * @author Nicolas ADAMCYZK
     */

    private void supprimerDuPanier(Medicament med) {
        quantitesPanier.remove(med);
        medicamentsAVendre.remove(med);
        calculerEtAfficherTotal();
        zonePanierMedicament.refresh();
        updateVendreButton();
    }

    /**
     * Configuration des menus contextuels
     *
     * @author Victoria MASSAMBA
     */
    private void configurerMenusContextuels() {
        ContextMenu menuAjout = new ContextMenu();
        UtilQuantiteCustom.configurerMenusContextuels(
                menuAjout,
                zoneAffichageMedicament,
                zonePanierMedicament,
                quantitesPanier,
                medicamentsAVendre,
                this::calculerEtAfficherTotal
        );
        // Ajout de l'option "Afficher les détails"
        MenuItem itemAfficherDetails = new MenuItem("Afficher les détails");
        itemAfficherDetails.setOnAction(e -> {
            Medicament selectedMedicament = zoneAffichageMedicament.getSelectionModel().getSelectedItem();
            if (selectedMedicament != null) {
                System.out.println("Medicament sélectionné : " + selectedMedicament.getDenominationMedicament());
                UtilOrdonnance.afficherDetailsMedicament(selectedMedicament);
            } else {
                UtilAlert.erreurAlert("Aucun médicament sélectionné", "Erreur", "Veuillez sélectionner un médicament", "OK");
            }
        });

        menuAjout.getItems().add(itemAfficherDetails);
    }

    /**
     * Gère la sélection d'un client.
     *
     * @param event L'événement de clic de souris
     * @author Victoria MASSAMBA
     * @author Hugo VITORINO PEREIRA
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 09/04/2025
     */
    @FXML
    private void selectionnerClientHandler(ActionEvent event) {
        // Pour afficher la fenêtre sur le même écran
        // Hugo VITORINO PEREIRA
        Stage currentStage = (Stage) ajouterClientBouton.getScene().getWindow();
        Client nouveauClient = UtilClient.afficherFenetreSelectionClient(currentStage);
        // Fin Hugo VITORINO PEREIRA
        if (nouveauClient != null) {
            clientSelectionne = nouveauClient;
            zoneAffichageClient.setItems(FXCollections.observableArrayList(clientSelectionne));

            // On vide la liste des ordonnances précédemment sélectionnées
            ordonnancesSelectionnees.clear();
            ZoneAffichageOrdonnance.refresh();

            // On vide également le panier
            quantitesPanier.clear();
            medicamentsAVendre.clear();
            zonePanierMedicament.refresh();
            calculerEtAfficherTotal();

            // Activer les boutons
            ajouterOrdonnanceBouton1.setDisable(false);
            selectionnerOrdonnanceBouton.setDisable(false);

        } else {
            UtilAlert.informationAlert("Aucun client n'a été choisi.", "Aucun client choisi", null, "OK");
            ajouterOrdonnanceBouton1.setDisable(true);
            selectionnerOrdonnanceBouton.setDisable(true);
        }
    }

    /**
     * Gère l'ajout d'un client.
     *
     * @author Victoria MASSAMBA
     * @author Hugo VITORINO PEREIRA
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 01/05/2025
     */

    @FXML
    private void ajouterClientHandler(ActionEvent event) {
        try {
            // Chargement de la fenêtre

            Stage currentStage = (Stage) ajouterClientBouton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clients/ClientAjout.fxml"));
            Parent root = loader.load();

            ClientAjoutController controller = loader.getController();



            Stage stage = new Stage();
            stage.initOwner(currentStage);
            // Fenêtre "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un nouveau client");

            // Obtenir les dimensions de la fenêtre parente
            double parentWidth = currentStage.getWidth();
            double parentHeight = currentStage.getHeight();

            // Calculer 85% de la taille de la fenêtre parente
            double width = parentWidth * 0.85;
            double height = parentHeight * 0.85;

            // Taille de la fenêtre
            stage.setScene(new Scene(root, width, height));
            // Centre la fenêtre
            stage.centerOnScreen();

            controller.setMode("creation", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture


            Client nouveauClient = controller.getClientCree();
            if (nouveauClient != null) {

                clientSelectionne = nouveauClient;

                // Placement du client dans la zone client
                zoneAffichageClient.setItems(FXCollections.observableArrayList(clientSelectionne));

                // Réinitialisation de la zone ordonnance et de la zone panier
                ordonnancesSelectionnees.clear();
                ZoneAffichageOrdonnance.refresh();
                quantitesPanier.clear();
                medicamentsAVendre.clear();
                zonePanierMedicament.refresh();
                calculerEtAfficherTotal();

                //Activation des boutons d'ordonnance
                ajouterOrdonnanceBouton1.setDisable(false);
                selectionnerOrdonnanceBouton.setDisable(false);
            } else {
                UtilAlert.informationAlert("Aucun client n'a été ajouté.", "Ajout de nouveau client", "Ajout du client impossible", "OK");
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            UtilAlert.erreurAlert("Impossible d'ouvrir la fenêtre d'ajout client", "Ouverture de la fenêtre impossible",null, "OK");
        }
    }


    /**
     * Gère l'ajout d'une ordonnance.
     *
     * @author Victoria MASSAMBA
     * @author Hugo VITORINO PEREIRA
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 01/05/2025
     */
    @FXML
    private void ajouterOrdonnanceHandler(ActionEvent event) {
        if (clientSelectionne == null) {
            UtilAlert.informationAlert("Veuillez d'abord sélectionner un client.", "Aucun client", "Aucun client selectionné", "OK");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("client", clientSelectionne);

        try {
            // Chargement de la fenêtre

            Stage currentStage = (Stage) ajouterClientBouton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clients/ordonnances/OrdonnanceAjout.fxml"));
            Parent root = loader.load();

            OrdonnanceAjoutController controller = loader.getController();
            controller.initializeWithParameters(params);

            Stage stage = new Stage();
            stage.initOwner(currentStage);
            // Fenêtre "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une nouvelle ordonnance");

            // Obtenir les dimensions de la fenêtre parente
            double parentWidth = currentStage.getWidth();
            double parentHeight = currentStage.getHeight();

            // Calculer 85% de la taille de la fenêtre parente
            double width = parentWidth * 0.85;
            double height = parentHeight * 0.85;

            // Taille de la fenêtre
            stage.setScene(new Scene(root, width, height));
            // Centre la fenêtre
            stage.centerOnScreen();

            controller.setMode("creation", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture

            Ordonnance nouvelleOrdonnance = controller.getOrdonannceCree();
            if (nouvelleOrdonnance != null) {

                if(!ordonnancesSelectionnees.contains(nouvelleOrdonnance)) {
                    // On l'ajoute à la liste (sans écraser les autres ordonnances)
                    ordonnancesSelectionnees.add(nouvelleOrdonnance);

                    // Ajouter également les prescriptions de cette ordonnance au panier
                    prescriptionsPanier(nouvelleOrdonnance.getId());
                }else{
                    UtilAlert.erreurAlert("L'ordonannce a déjà été ajoutée","Erreur","Impossible d'ajouter l'ordonannce","Ok");
                }

            } else {
                UtilAlert.erreurAlert("Aucune ordonnance n'a été ajoutée.", "Information","Ajout d'ordonnance impossible",  "OK");
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            UtilAlert.erreurAlert("Impossible d'ouvrir la fenêtre d'ajout d'ordonnance","Information", "Ouverture de la fenêtre impossible", "OK");
        }

    }

    /**
     * Gère la sélection d'une ordonnance.
     *
     * @param event L'événement de clic de souris
     * @author Victoria MASSAMBA
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 09/04/2025
     */

    @FXML
    private void selectionnerOrdonnanceHandler(ActionEvent event) {
        if (clientSelectionne == null) {
            UtilAlert.informationAlert("Veuillez d'abord sélectionner un client.", "Erreur", "Aucun client n'a été selectionné", "OK");
            return;
        }
        // Pour afficher la fenêtre sur le même écran
        Stage currentStage = (Stage) ajouterOrdonnanceBouton1.getScene().getWindow();
        Ordonnance ord = UtilOrdonnance.afficherFenetreSelectionOrdonnance(clientSelectionne, currentStage);
        if (ord != null) {
            if(!ordonnancesSelectionnees.contains(ord)) {
                // On l'ajoute à la liste (sans écraser les autres ordonnances)
                ordonnancesSelectionnees.add(ord);

                // Ajouter également les prescriptions de cette ordonnance au panier
                prescriptionsPanier(ord.getId());
            } else{
                UtilAlert.informationAlert("L'ordonannce a déjà été ajoutée","Erreur","Impossible d'ajouter l'ordonannce","Ok");
            }

        } else {
            UtilAlert.informationAlert("Aucune ordonnance n'a été choisie.", "Pas d'ordonnance", "Aucune ordonnance choisie", "OK");
        }
    }

    /**
     * Méthode qui ajoute les prescriptions d'une ordonnance directement au panier
     * Si les prescriptions sont en ruptures de stock, elles sont ajoutées également à la liste des médicaments en rupture pour pouvoir ensuite les réserver
     * <p></p>
     * @param id l'identifiant de l'ordonnance
     * @author Victoria MASSAMBA
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 04/05/2025
     */

    private void prescriptionsPanier(int id) {

        List<Prescription> prescriptions = OrdonnanceDAO.getPrescriptionsOrdonnance(id);

        for (Prescription p : prescriptions) {

            Medicament med = p.getMedicament();
            int qtePrescrite  = p.getQuantite();         // quantité inscrite sur l’ordonnance

            /* 1. Renseigner le stock réel du médicament (0 si non vendu) */
            int stockReel = 0;
            try {
                int s = NosMedicamentsDAO.getQuantiteMedicament(med.getCodeCIP());
                stockReel = (s < 0) ? 0 : s; // s sera egale à -1 si le medicament n'est pas vendu en pharmacie, on place donc sa quantité à 0 pour pouvoir znsuite aisément calculer la bonne quantité à reserver
            } catch (SQLException e) {
                System.err.println("Stock introuvable pour le medicament de code_cip : " + med.getCodeCIP());
                e.printStackTrace();
            }
            med.setQuantite(stockReel);


            // on rajoute la quantité prescrite dans le panier de vente
            quantitesPanier.merge(med, qtePrescrite, Integer::sum);

            int qteDansPanier = quantitesPanier.get(med);
            int manque = qteDansPanier - stockReel;

            //s'il manque des exemplaires
            if (manque > 0) { // rupture partielle ou totale
                medicamentsRupture.put(med, manque); // on réserve la différence (s'il y en a une)
            } else {
                medicamentsRupture.remove(med);
            }


            if (!medicamentsAVendre.contains(med)) {
                medicamentsAVendre.add(med);
            }
        }

        // on raffraichit l’interface
        calculerEtAfficherTotal();      // prix total du panier
        zonePanierMedicament.refresh(); // met à jour la TableView
        updateVendreButton();           // désactivé si une rupture est encore présente
        updateReserverButton();         // activé si medicamentsRupture n’est pas vide
    }

    /**
     * <p>Date de création : 2/05/25 </p>
     * <p>Date de dernière modification : 2/05/25</p>
     * Ajoute une pastille jaune dans la colonne Indisponible si le médicament est en rupture locale ou non vendu en pharmacie
     * @author Victoria MASSMABA
     */

    private void configurerColonneIndisponibilite() {
        indispoColumn.setCellFactory(col -> new TableCell<Medicament, Void>() {

            private final Button alertBtn = new Button();
            {
                alertBtn.getStyleClass().add("bouton-panier-jaune");
                alertBtn.setTooltip(new Tooltip("Rupture locale ou produit non vendu en pharmacie."));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Medicament med = getTableView().getItems().get(getIndex());

                int stockReel = med.getQuantite(); //quantité en rayon


                int qteDemandee = quantitesPanier.getOrDefault(med, 0);

                boolean indisponible = stockReel - qteDemandee < 0; // résultat négatif si medicament en rupture ou non vendu

                setGraphic(indisponible ? alertBtn : null);
            }
        });
    }



    /**
     * Date de dernière modification : 19/04/2025
     * Gère la validation et l'enregistrement de la vente après confirmation.
     *
     * @param event L'événement de clic de souris
     * @author Nicolas Adamczyk
     * @author Victoria MASSAMBA
     */
    @FXML
    void vendreHandler(MouseEvent event) throws SQLException {
        if (!medicamentsAVendre.isEmpty()) {
            // Confirmation
            boolean confirmation = UtilAlert.confirmationAlert(
                    "Êtes-vous sûr de vouloir valider cette vente ?",
                    "Confirmation de vente",
                    "Montant total: " + String.format("%.2f€", calculerMontantTotal()),
                    "Confirmer",
                    "Annuler"
            );
            // Vérifier stocks
            boolean stockOk = true;
            StringBuilder messageErreur = new StringBuilder();

            for (Map.Entry<Medicament, Integer> entry : quantitesPanier.entrySet()) {
                Medicament med = entry.getKey();
                int quantiteDemandee = entry.getValue();

                // Récupérer la quantité réelle en base
                int quantiteEnBase = 0;
                try {
                    quantiteEnBase = NosMedicamentsDAO.getQuantiteMedicament(med.getCodeCIP());
                } catch (SQLException e) {
                    System.out.println("Erreur lors de la récupération de la quantité de stock du médicament : " + e.getMessage());
                }

                if (quantiteEnBase < quantiteDemandee) {
                    stockOk = false;
                    messageErreur.append("- ")
                            .append(med.getDenominationMedicament())
                            .append(": stock insuffisant (")
                            .append(quantiteEnBase)
                            .append(" disponible, ")
                            .append(quantiteDemandee)
                            .append(" demandé)\n");
                }
            }

            if (!stockOk) {
                UtilAlert.erreurAlert("Stocks insuffisants", "Erreur",
                        "Impossible de valider la vente:\n" + messageErreur, "OK");
                return;
            }
            int idVente = 0;


            if (confirmation) {

                try (Connection conn = DataBaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    double montantTotal = calculerMontantTotal();
                    // On ajoute la vente a la bdd
                    idVente = VenteOrdonnanceDAO.ajouterVenteOrdonnance(
                            conn, clientSelectionne.getId(), montantTotal
                    );

                    // on ajoute chaque ligne du panier dans la bdd
                    int finalIdVente = idVente;
                    quantitesPanier.entrySet().stream().forEach(entry -> {
                        Medicament med = entry.getKey();
                        int qt = entry.getValue();

                        // Récupère la liste des id d'ordonnances qui contiennent med
                        List<Integer> ordonnanceIds = ordonnancesSelectionnees.stream()
                                .filter(ord -> ord.getElementsPrescription()
                                        .stream()
                                        .anyMatch(p -> p.getMedicament().equals(med)))
                                .map(Ordonnance::getId)
                                .collect(Collectors.toList());

                        // si aucune ordonnance trouvé pour un médicament du panier on passe la valeur null à l'id de l'ordonnance
                        if (ordonnanceIds.isEmpty()) {
                            ordonnanceIds = Collections.singletonList(null);
                        }

                        //pour chaque id (réel ou null), on insère dans la bdd
                        ordonnanceIds.forEach(idOrd -> {
                            try {
                                PanierVenteOrdonnanceDAO.ajouterPanierOrdonnance(
                                        conn, finalIdVente, med.getCodeCIP(), qt, idOrd
                                );
                            } catch (SQLException e) {
                                throw new RuntimeException(
                                        "Erreur insert panier pour ordonnance " + idOrd, e
                                );
                            }
                        });

                        //  on met à jour le stock
                        try {
                            NosMedicamentsDAO.diminuerQuantiteMedicament(conn, med.getCodeCIP(), qt);
                        } catch (SQLException e) {
                            throw new RuntimeException(
                                    "Erreur update stock pour med " + med.getCodeCIP(), e
                            );
                        }
                    });
                    // On commit dans la bdd les insertions ajoutées
                    conn.commit();


                    UtilAlert.informationAlert(
                            "Vente enregistrée avec succès!",
                            "Succès",
                            "Montant total: " + String.format("%.2f€", montantTotal),
                            "OK"
                    );
                    // Réinitialiser
                    medicamentsAVendre.clear();
                    quantitesPanier.clear();
                    calculerEtAfficherTotal();
                    chargerMedicaments();
                    zoneAffichageClient.getItems().clear();
                    ZoneAffichageOrdonnance.getItems().clear();
                    updateVendreButton();


                } catch (SQLException e) {

                    UtilAlert.erreurAlert("Erreur lors de l'enregistrement de la vente", "Erreur", "Vente impossible", "OK");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    return;
                }

                int id_personnel = SessionManager.getCurrentPersonnel().getId();
                Log log = new Log(Log.LogAction.EFFECTUER_VENTE_AVEC_ORDONNANCE, LocalDateTime.now(), id_personnel, idVente);
                LogDAO.ajouterLog(log);
            }
        } else {
            UtilAlert.informationAlert("Le panier est vide", "Information", "Ajoutez des médicaments avant de valider", "OK");
        }
    }

    /**
     * <p>Date de dernière modification : 02/05/25</p>
     * Gère l'action d'annulation et retourne à la page d'accueil.
     *
     * @param event L'événement de clic de souris
     * @author Nicolas Adamczyk
     * @author Victoria MASSAMBA
     */
    @FXML
    void annulerHandler(MouseEvent event) {
        boolean choix = UtilAlert.confirmationAlert("Le panier sera vidé.","Annulation","Êtes-vous sûr de vouloir annuler la vente ?","Oui","Non");
        if (choix) {
            NavigationManager.loadPage(Page.HOME.getFilePath());
        }

    }

    /**
     * Date de création :23/04/25
     * Date de dernière modification : 23/04/25
     * <p></p>
     * Gère la réservation de médicament en rupture de stock
     *
     * @param event L'évènement de clic sur le bouton "Réserver"
     *
     * @author Victoria MASSAMBA
     */
    @FXML

    public void reserverHandler(MouseEvent event) {
        try {
            // Chargement de la fenêtre

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clients/reservations/ReservationAjout.fxml"));
            Parent root = loader.load();

            ReservationAjoutController controller = loader.getController();

            //Mise en place des paramètres : le client et les médicaments en rupture

            HashMap<String, Object> params = new HashMap<>();
            params.put("client", clientSelectionne);
            params.put("medicamentsRupture",medicamentsRupture);
            controller.initializeWithParameters(params);
            Stage stage = new Stage();
            // Fenêtre "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une nouvelle réservation");
            // Taille de la fenêtre
            stage.setScene(new Scene(root, 1200, 600));
            // Centre la fenêtre
            stage.centerOnScreen();

            controller.setMode("creation", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture

            if (controller.isReservationEnregistree()) {

                // retirer du panier chaque médicament qui vient d’être réservé
                for (Medicament med : medicamentsRupture.keySet()) {
                    quantitesPanier.remove(med);
                    medicamentsAVendre.remove(med);
                }
                medicamentsRupture.clear();      //on met la map à vide car il n'y a plus rien à réserver

                // on rafraichit le panier
                zonePanierMedicament.refresh();
                calculerEtAfficherTotal();
                updateReserverButton();
                updateVendreButton();
            }


        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            UtilAlert.erreurAlert("Impossible d'ouvrir la fenêtre de réservation","Réservation impossible","Erreur de réservation","Ok");

        }


    }

    /**
     * Active ou désactive le bouton Reserver selon s'il y a des médicaments en rupture dans le panier de vente.
     */
    private void updateReserverButton() {
        // le bouton est actif si on a au moins un médicament en rupture
        reserverButton.setDisable(medicamentsRupture.isEmpty());
    }
    /**
     * <p>Date de création : 02/05/2025</p>
     * <p>Date de dernière modification : 02/05/2025</p>
     * Désactive Vendre si le panier est vide ou si un médicament du panier est en rupture (stock‑réel – qté demandée < 0)
     * @author Victoria MASSAMBA
     */
    private void updateVendreButton() {
        boolean panierVide      = quantitesPanier.isEmpty();
        boolean ruptureDansPanier = quantitesPanier.entrySet().stream()
                .anyMatch(e -> e.getKey().getQuantite() - e.getValue() < 0);

        vendreButton.setDisable(panierVide || ruptureDansPanier);
    }

}
