package controllers.ventes;

import database.LogDAO;
import database.NosMedicamentsDAO;
import database.PanierVenteDAO;
import database.VenteDAO;
import global.NavigationManager;
import global.Page;
import global.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import models.Log;
import models.Medicament;
import utils.UtilAlert;
import utils.UtilRecherche;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des ventes sans ordonnances.
 * Permet d'afficher les médicaments disponibles, de constituer un panier de vente
 * et d'enregistrer les transactions.
 * @author Victoria Massamba
 * @author Nicolas Adamczyk
 * Date de première modification : 01/03/25
 * Date de dernière modification : 26/03/25
 */
public class VentesSansOrdonnanceController implements Initializable {

    @FXML private ComboBox<String> rechercherParButton;
    @FXML private Label totalLabel;

    // TableView pour l'affichage des médicaments disponibles
    @FXML private TableView<Medicament> zoneAffichageMedicament;
    @FXML private TableColumn<Medicament, String> codeCisColumn;
    @FXML private TableColumn<Medicament, String> denominationColumn;
    @FXML private TableColumn<Medicament, Double> prixColumn;
    @FXML private TableColumn<Medicament, Integer> quantiteColumn;
    @FXML private TableColumn<Medicament, String> dosageColumn;

    // TableView pour le panier
    @FXML private TableView<Medicament> zonePanierMedicament;
    @FXML private TableColumn<Medicament, String> denominationPanierColumn;
    @FXML private TableColumn<Medicament, Double> prixPanierColumn;
    @FXML private TableColumn<Medicament, Integer> quantitePanierColumn;

    @FXML private TableColumn<Medicament, Void> actionsPanierColumn;

    @FXML private TextField zoneRechercheMedicament;

    private ObservableList<Medicament> medicamentsAVendre;
    private ObservableList<Medicament> items;
    private Map<Medicament, Integer> quantitesPanier; // Panier de la vente : Medicament + quantité acheté
    private UtilRecherche<Medicament> utilRecherche;

    /**
     * Gère l'action d'annulation et retourne à la page d'accueil.
     * @author Nicolas Adamczyk
     * @param event L'événement de clic de souris
     */
    @FXML
    void annulerHandler(MouseEvent event) {
        NavigationManager.loadPage(Page.HOME.getFilePath());
    }

    /**
     * Gère les clics sur les TableView pour ajouter ou supprimer des médicaments du panier.
     * @author Nicolas Adamczyk
     * @param event L'événement de clic de souris
     */
    @FXML
    void listViewClickHandler(MouseEvent event) {
        // Ignorer les clics droits (qui sont pour le menu contextuel)
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (event.getClickCount() == 2) {
            if (event.getSource() == zoneAffichageMedicament) {
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
                    } else {
                        UtilAlert.informationAlert("Stock insuffisant", "Information",
                                "Stock épuisé pour " + selected.getDenominationMedicament(), "OK");
                    }
                }
            }
        }
        else if (event.getSource() == zonePanierMedicament) {
            // Gestion du clic sur le panier (décrémentation)
            Medicament selected = zonePanierMedicament.getSelectionModel().getSelectedItem();
            if (selected != null && quantitesPanier.containsKey(selected)) {
                int nouvelleQuantite = quantitesPanier.get(selected) - 1;

                if (nouvelleQuantite <= 0) {
                    // Supprimer du panier si quantité <= 0
                    quantitesPanier.remove(selected);
                    medicamentsAVendre.remove(selected);
                } else {
                    // Décrémenter la quantité
                    quantitesPanier.put(selected, nouvelleQuantite);
                }

                calculerEtAfficherTotal();
                zonePanierMedicament.refresh();
            }
        }
    }

    /**
     * Gère la validation et l'enregistrement de la vente après confirmation.
     * @author Nicolas Adamczyk
     * @param event L'événement de clic de souris
     */
    /**
     * Gère la validation et l'enregistrement de la vente après confirmation.
     * @author Nicolas Adamczyk
     * @param event L'événement de clic de souris
     */
    @FXML
    void vendreHandler(MouseEvent event) {
        if (!medicamentsAVendre.isEmpty()) {
            // Afficher la confirmation avant de procéder à la vente
            boolean confirmation = UtilAlert.confirmationAlert(
                    "Êtes-vous sûr de vouloir valider cette vente ?",
                    "Confirmation de vente",
                    "Montant total: " + String.format("%.2f€", calculerMontantTotal()),
                    "Confirmer",
                    "Annuler"
            );
            // Vérification finale des stocks avant validation
            boolean stockOk = true;
            StringBuilder messageErreur = new StringBuilder();

            for (Map.Entry<Medicament, Integer> entry : quantitesPanier.entrySet()) {
                if (entry.getKey().getQuantite() < entry.getValue()) {
                    stockOk = false;
                    messageErreur.append("- ")
                            .append(entry.getKey().getDenominationMedicament())
                            .append(": stock insuffisant (")
                            .append(entry.getKey().getQuantite())
                            .append(" disponible, ")
                            .append(entry.getValue())
                            .append(" demandé)\n");
                }
            }
            if (!stockOk) {
                UtilAlert.erreurAlert("Stocks insuffisants", "Erreur",
                        "Impossible de valider la vente:\n" + messageErreur.toString(), "OK");
                return;
            }
            int idVente=0;
            if (confirmation) {

                try {
                    // Enregistrer la vente
                    double montantTotal = calculerMontantTotal();
                    idVente = VenteDAO.ajouterVente(montantTotal);

                    // Enregistrer le panier de la vente dans la bdd
                    for (Map.Entry<Medicament, Integer> entry : quantitesPanier.entrySet()) {
                        Medicament med = entry.getKey();
                        int quantite = entry.getValue();

                        PanierVenteDAO.ajouterPanier(idVente, med.getCodeCIP(),quantite);


                        NosMedicamentsDAO.diminuerQuantiteMedicament(
                                med.getCodeCIP(), quantite
                        );
                    }

                    // Afficher confirmation de succès
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
                } catch (SQLException e) {
                    UtilAlert.erreurAlert(
                            "Erreur lors de l'enregistrement",
                            "Erreur",
                            e.getMessage(),
                            "OK"
                    );
                    return;
                }

                int id_personnel = SessionManager.getCurrentPersonnel().getId();
                Log log = new Log(Log.LogAction.EFFECTUER_VENTE_SANS_ORDONNANCE, LocalDateTime.now(),id_personnel,idVente);
                LogDAO.ajouterLog(log);
            }
        } else {
            UtilAlert.informationAlert(
                    "Le panier est vide",
                    "Information",
                    "Ajoutez des médicaments avant de valider",
                    "OK"
            );
        }
    }
    /**
     * Initialise le contrôleur après le chargement de son élément racine.
     * @author Nicolas Adamczyk
     * @param url L'emplacement utilisé pour résoudre les chemins relatifs
     * @param resources Les ressources utilisées pour localiser l'objet racine
     */
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Initialisation des listes
        medicamentsAVendre = FXCollections.observableArrayList();
        items = FXCollections.observableArrayList();
        quantitesPanier = new HashMap<>();

        // Configuration des TableView
        configurerColonnesMedicaments();
        configurerColonnesPanier();

        ajouterColonneActions(); // pour afficher les boutons dans le panier

        // Configuration de la recherche
        configurerRecherche();

        // Chargement initial
        chargerMedicaments();

        // configurer le clic droit
        configurerMenusContextuels();
    }

    /**
     * Configure les colonnes de la TableView des médicaments disponibles.
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
     * @author Nicolas Adamczyk
     */
    private void configurerColonnesPanier() {
        denominationPanierColumn.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
        prixPanierColumn.setCellValueFactory(new PropertyValueFactory<>("prixMedicament"));
        quantitePanierColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(quantitesPanier.getOrDefault(cellData.getValue(), 0)).asObject());

        zonePanierMedicament.setItems(medicamentsAVendre);
    }

    /**
     * Configure le système de recherche des médicaments.
     * @author Nicolas Adamczyk
     */
    private void configurerRecherche() {
        rechercherParButton.getItems().addAll("Dénomination", "Prix");
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
     * Récupère tous les médicaments disponibles depuis la base de données.
     * @author Nicolas Adamczyk
     * @return La liste des médicaments disponibles
     */
    private List<Medicament> getTousLesMedicaments() {
        try {
            return NosMedicamentsDAO.getAllMedicaments();
        } catch (SQLException e) {
            UtilAlert.erreurAlert("Erreur de chargement", "Erreur", e.getMessage(), "OK");
            return List.of();
        }
    }

    /**
     * Charge les médicaments nécessitant une ordonnance dans la TableView.
     * @author Nicolas Adamczyk
     */
    private void chargerMedicaments() {
        // Récupérer tous les médicaments disponibles
        List<Medicament> tousLesMedicaments = getTousLesMedicaments();

        // Filtrer uniquement ceux qui ne nécessitent PAS d'ordonnance
        List<Medicament> medicamentsSansOrdonnance = tousLesMedicaments.stream()
                .filter(med -> !med.isNecessiteOrdonnance()) // Notez le ! pour inverser la condition
                .collect(Collectors.toList());

        // Mettre à jour la TableView avec les médicaments filtrés
        items.setAll(medicamentsSansOrdonnance);
    }


    /**
     * Date de dernière modification : 12/04/25
     * Effectue une recherche de médicaments par critère textuel.
     * @author Victoria Massamba
     * @author Nicolas Adamczyk
     * @param critere Le critère de recherche (Dénomination)
     * @param valeur La valeur à rechercher
     * @return La liste des médicaments correspondants
     */
    private List<Medicament> rechercherParCritereTexte(String critere, String valeur) {
        try {
            if ("Dénomination".equals(critere)) {
                List<Medicament> CritereMedicaments= NosMedicamentsDAO.rechercherParNom(valeur);
                List<Medicament> medicamentsCritereMedicamentsSansOrdonnance = CritereMedicaments.stream()
                .filter(med -> !med.isNecessiteOrdonnance())
                .collect(Collectors.toList());
                return medicamentsCritereMedicamentsSansOrdonnance;
            }
            return List.of();
        } catch (SQLException e) {
            UtilAlert.erreurAlert("Erreur de recherche", "Erreur", e.getMessage(), "OK");
            return List.of();
        }
    }

    /**
     * Date de dernière modification : 12/04/25
     * Effectue une recherche de médicaments par critère numérique.
     * @author Nicolas Adamczyk
     * @author Victoria MASSAMBA
     * @param critere Le critère de recherche (Prix)
     * @param valeur La valeur à rechercher
     * @return La liste des médicaments correspondants
     */
    private List<Medicament> rechercherParCritereNumerique(String critere, Double valeur) {
        try {
            if ("Prix".equals(critere)) {
                List<Medicament> CritereMedicaments=  NosMedicamentsDAO.rechercherParPrix(valeur);
                List<Medicament> medicamentsCritereMedicamentsSansOrdonnance = CritereMedicaments.stream()
                .filter(med -> !med.isNecessiteOrdonnance())
                .collect(Collectors.toList());
                return medicamentsCritereMedicamentsSansOrdonnance;                
            }
            return List.of();
        } catch (SQLException e) {
            UtilAlert.erreurAlert("Erreur de recherche", "Erreur", e.getMessage(), "OK");
            return List.of();
        }
    }

    /**
     * Calcule le montant total de la vente en cours.
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
     * @author Nicolas Adamczyk
     */
    private void calculerEtAfficherTotal() {
        totalLabel.setText(String.format("Total: %.2f€", calculerMontantTotal()));
    }

    /**
     * Configure les menus contextuels pour les TableView
     * @author Nicolas Adamczyk
     */
    private void configurerMenusContextuels() {
        UtilQuantiteCustom.configurerMenusContextuels(
                zoneAffichageMedicament,
                zonePanierMedicament,
                quantitesPanier,
                medicamentsAVendre
        );
    }

    private void ajouterColonneActions() {
        actionsPanierColumn.setCellFactory(col -> new TableCell<>() {
            private final Button plusBtn = new Button("+");
            private final Button moinsBtn = new Button("−");
            private final Button supprimerBtn = new Button("x");
            private final HBox hbox = new HBox(5, plusBtn, moinsBtn, supprimerBtn);

            {
                plusBtn.getStyleClass().add("bouton-vert");
                moinsBtn.getStyleClass().add("bouton-rouge");
                supprimerBtn.getStyleClass().add("bouton-rouge");

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

    private void incrementerQuantite(Medicament med) {
        int stockDisponible = med.getQuantite() - quantitesPanier.getOrDefault(med, 0);

        if (stockDisponible > 0) {
            quantitesPanier.put(med, quantitesPanier.getOrDefault(med, 0) + 1);
            calculerEtAfficherTotal();
            zonePanierMedicament.refresh();
        } else {
            UtilAlert.informationAlert("Stock insuffisant", "Information",
                    "Stock épuisé pour " + med.getDenominationMedicament(), "OK");
        }
    }

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
        }
    }

    private void supprimerDuPanier(Medicament med) {
        quantitesPanier.remove(med);
        medicamentsAVendre.remove(med);
        calculerEtAfficherTotal();
        zonePanierMedicament.refresh();
    }
}