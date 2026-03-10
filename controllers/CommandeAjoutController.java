    /* Code écrit par Jason DARIUS
     * Description : Controller permettant de gérer les évènements associés à la fenêtre "AjoutCommande"
     */

    package controllers.fournisseurscommandes.commandes;

    import database.CommandeDAO;
    import database.FournisseurDAO;
    import database.LogDAO;
    import global.InitializableWithParameters;
    import global.NavigationManager;
    import global.Page;
    import global.SessionManager;
    import javafx.beans.property.SimpleObjectProperty;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.event.EventHandler;
    import javafx.geometry.Pos;
    import javafx.scene.control.*;
    import javafx.scene.control.cell.PropertyValueFactory;
    import javafx.scene.control.cell.TextFieldTableCell;
    import javafx.scene.input.KeyEvent;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.HBox;
    import javafx.util.converter.IntegerStringConverter;
    import models.Commande;
    import models.Fournisseur;
    import models.Log;
    import models.Medicament;
    import utils.UtilAlert;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;


    import java.net.URL;
    import java.time.LocalDateTime;
    import java.util.*;

    /**
     * Date de création 27/02/2025
     * Date de dernière modification : 06/04/2025
     *
     * <p>Cette classe permet de gérer les évenement associés à la fen^tre
     * concernant la gestion d'ajout d'article au panier (faire une commande)</p>
     *
     * @author Jason DARIUS
     */
    public class CommandeAjoutController implements Initializable, InitializableWithParameters {

        @FXML
        private Button retourButton;

        @FXML
        private Button annulerButton;

        @FXML
        private Button ajouterPannierButton;

        @FXML
        private ComboBox<String> rechercherParComboBox;

        @FXML
        private TextField zoneRechercherMedicamentTextField;

        @FXML
        private TableView<Medicament> zoneAjoutMedicamentTableView;

        @FXML
        private TableColumn<Medicament, String> denomination1Column;

        @FXML
        private TableColumn<Medicament, Double> prix1Column;

        @FXML
        private TableView<Medicament> zoneInfoCommandeTableView;

        @FXML
        private TableColumn<Medicament, String> denomination2Column;

        @FXML
        private TableColumn<Medicament, Integer> quantiteColumn;

        @FXML
        private TableColumn<Medicament, Double> prix2Column;

        @FXML
        private MenuButton choisirFournisseurButton;

        @FXML
        private TableColumn<Medicament, Void> actionsPanierColumn;

        private ObservableList<Medicament> medicamentObservableList;

        private CommandeDAO commandeDAO = new CommandeDAO();

        private Fournisseur fournisseur;

        private FournisseurDAO fournisseurDAO = new FournisseurDAO();

        private ObservableList<Medicament> prePanierObservableList;




        /**
         * Date de création : 27/02/2025
         * Date de dernière modification : 03/04/2025
         *
         * <p>Méthode pour rechercher un médicament dans la bdd (sans bdd pour l'instant).
         * Elle permet de rentrer un médicament dans la barre de recherche et de l'ajouter dans la liste des articles</p>
         *
         * @author Jason DARIUS
         */
        @FXML
        private void rechercherMedicament() {
            String search = zoneRechercherMedicamentTextField.getText().trim();

            if(!search.isEmpty()){
                List<Medicament> resultats =  commandeDAO.rechercheMedicament(search);
                medicamentObservableList.clear();
                medicamentObservableList.addAll(resultats);
            }
            else{
                medicamentObservableList.clear();
            }
        }

        /**
         * Date de dernière modification : 27/02/2025
         *
         * <p>Méthode pour ajouter des options de recherche par nom, catégorie et code au ComboBox.</p>
         *
         *@author Jason DARIUS
         */
        @FXML
        private void boutonRechercherParHandler(){
            rechercherParComboBox.getItems().addAll("Nom", "Prix");
        }

        /**
         * Date de dernière modification : 27/02/2025
         *
         * <p>Méthode pour gérer l'événement de clic sur le bouton Retour.</p>
         *
         *@author Jason DARIUS
         */
        @FXML
        private void boutonRetourHandler(){
            NavigationManager.goBack();
        }

        /**
         * Date de dernière modification : 27/02/2025
         *
         * <p>Méthode pour réinitialiser les champs de recherche et les informations de commande.</p>
         *
         * @author Jason DARIUS
         */
        @FXML
        private void boutonAnnulerHandler(){
            boolean choix = UtilAlert.confirmationAlert("Voulez vous enlever ces articles du panier ?", "Passer les commandes", null, "Oui", "Non");
            if(choix){
                zoneRechercherMedicamentTextField.clear();
                zoneInfoCommandeTableView.getItems().clear();
                System.out.println("Annuler");
            }
        }

        /**
         * Date de création : 27/02/2025
         * Date de dernière modification : 05/04/2025
         *
         * <p>Méthode pour ajouter les médicament sélectionnés au panier.</p>
         *
         * @author Jason DARIUS
         */

        @FXML
        private void boutonAjouterPanierHandler(){
            boolean choix = UtilAlert.confirmationAlert("Voulez vous ajouter ces médicaments au panier?", "Ajouter au panier", null, "Oui", "Non");
            if (choix) {
                // Récupérer les médicaments du pré-panier
                List<Medicament> prePanier = new ArrayList<>(prePanierObservableList); // Copie de la liste actuelle

                if (fournisseur == null) {
                    fournisseur = new Fournisseur();
                }

                String nomEntreprise = choisirFournisseurButton.getText();
                int idFournisseur = fournisseurDAO.recupererIdFournisseurParNom(nomEntreprise);

                if (idFournisseur == 0) {
                    System.out.println("Fournisseur introuvable : " + nomEntreprise);
                    UtilAlert.erreurAlert("Erreur, vous n'avez pas sélectionné de fournisseur", "Le fournisseur n'existe pas dans la base de données.", null, "OK");
                    return;
                }

                fournisseur.setId(idFournisseur);
                fournisseur.setNomEntreprise(nomEntreprise);

                if (prePanier.isEmpty()) {
                    UtilAlert.erreurAlert("Le pré-panier est vide.", "Aucun médicament à ajouter.", null, "OK");
                    return;
                }
                if (fournisseur.getNomEntreprise() == null || fournisseur.getNomEntreprise().isEmpty()) {
                    UtilAlert.erreurAlert("Le fournisseur n'est pas sélectionné.", "Veuillez sélectionner un fournisseur.", null, "OK");
                }else{
                    double montantTotal = 0.0;
                    for (Medicament medicament : prePanier) {
                        montantTotal += medicament.getPrixMedicament() * medicament.getQuantite();
                    }

                    // Créer une commande
                    Commande nouvelleCommande = new Commande();
                    nouvelleCommande.setEtat("En attente");
                    nouvelleCommande.setMontantTotal(montantTotal);
                    nouvelleCommande.setDateCommande(LocalDateTime.now());
                    nouvelleCommande.setDateLivraison(null); // Livraison estimée
                    nouvelleCommande.setFournisseurId(fournisseur.getId());

                    // Enregistrer la commande et les médicaments associés dans la base de données
                    boolean isAdded = commandeDAO.ajouterCommandeAvecMedicaments(nouvelleCommande, prePanier);

                    if (nouvelleCommande.getId() == 0) {
                        System.out.println("Erreur : L'ID de la commande n'a pas été défini après l'ajout.");
                        UtilAlert.erreurAlert("Erreur système", "L'ID de la commande est incorrect.", null, "OK");
                        return;
                    }


                    if (!isAdded) {
                        UtilAlert.informationAlert("Erreur", "La commande n'a pas pu être ajoutée.", null, "OK");
                        return;
                    }else{
                        int id_personnel = SessionManager.getCurrentPersonnel().getId();
                        Log log = new Log(Log.LogAction.EFFECTUER_COMMANDE, LocalDateTime.now(),id_personnel,nouvelleCommande.getId());
                        LogDAO.ajouterLog(log);


                        // Préparer les paramètres à transmettre à la nouvelle page
                        Map<String, Object> params = new HashMap<>();
                        params.put("fournisseur", fournisseur);       // Ajouter le fournisseur
                        params.put("prePanier", prePanier); // Ajouter la liste des médicaments
                        params.put("commandeId", nouvelleCommande.getId());

                        // Charger la nouvelle page avec les paramètres
                        NavigationManager.loadPage(Page.COMMANDE_PANIER.getFilePath(), params);
                    }
                }

            }
        }

        /**
         * Date de création : 04/04/2025
         * Date de dernière modification : 04/04/2025
         *
         * <p>Cette méthode permet d'initialiser le menuItem permettant de choisir un fournisseur
         * auquel passer une commande. Il propose d'ajouter un fournisseur si aucun n'est présents
         * dans la base de données.</p>
         *
         * @author Jason DARIUS
         */
        @FXML
        private void boutonChoisirFournisseurHandler(){

            NavigationManager.loadPage(Page.FOURNISSEUR_AJOUT.getFilePath());
        }

        /**
         * Date de création : 03/04/2025
         * Date de dernière modification : 03/04/2025
         *
         * <p>Permet d'initialiser les fournisseurs de la base de données dans le menuButton
         * afin de choisir chez quel fournisseur passer la commande</p>
         *
         * @author Jason DARIUS
         */
        public void chargerMenuItem(){
            List<Fournisseur> fournisseurs = fournisseurDAO.getAllFournisseur();

            choisirFournisseurButton.getItems().clear();

            if(fournisseurs.isEmpty()){
                //Si aucun fournisseur n'est disponible, on ajoute un menuItem pour Ajouter une fournisseur
                MenuItem ajouterItem = new MenuItem("Ajouter un fournisseur");
                ajouterItem.setOnAction(event -> {
                    System.out.println("Option 'Ajouter un fournisseur' sélectionnée !");
                    NavigationManager.loadPage(Page.FOURNISSEUR_AJOUT.getFilePath());
                });

                choisirFournisseurButton.getItems().add(ajouterItem);
                choisirFournisseurButton.setText("Aucun fournisseur disponible");
            }
            else{
                for(Fournisseur fournisseurNom : fournisseurs){
                    MenuItem menuItem = new MenuItem(fournisseurNom.getNomEntreprise());
                    menuItem.setOnAction(event -> {
                        System.out.println("Fournisseur sélectionné : " + fournisseurNom.getNomEntreprise());
                        choisirFournisseurButton.setText(fournisseurNom.getNomEntreprise());
                    });

                    choisirFournisseurButton.getItems().add(menuItem);
                }
                choisirFournisseurButton.setText("Sélectionnez un fournisseur");
            }
        }


        /**
         * Date de création : 05/04/2025
         * Date de dernière modification : 05/04/2025
         *
         * <p>Cette méthode permet de choisir un médicament obtenu lors de la recherche et de l'ajouter dans le pré-panier
         * lors d'un double clique sur ce dernier</p>
         *
         * @param event
         *
         * @author Jason DARIUS
         */
        @FXML
        private void listViewClickHandler(MouseEvent event){
            // Verifie si il y a un double click
            if (event.getClickCount() == 2) {
                // Obtenir le médicament sélectionné dans la première ListView
                Medicament medicamentSelectionne = zoneAjoutMedicamentTableView.getSelectionModel().getSelectedItem();
                System.out.println("Médicament sélectionné");

                if (medicamentSelectionne != null) {
                    // Vérifier si le médicament existe déjà dans la deuxième ListView (pré-panier)
                    Optional<Medicament> medicamentExiste = prePanierObservableList.stream()
                            .filter(med -> med.getCodeCIP() == medicamentSelectionne.getCodeCIP())
                            .findFirst();

                    if (medicamentExiste.isPresent()) {
                        // Si le médicament est déjà présent, augmenter la quantité
                        Medicament quantiteMedicament = medicamentExiste.get();
                        quantiteMedicament.setQuantite(quantiteMedicament.getQuantite() + 1);
                        zoneInfoCommandeTableView.refresh();
                        System.out.println("Quantité augmentée");
                    } else {
                        // Sinon, ajouter le médicament dans le pré-panier avec une quantité initiale
                        medicamentSelectionne.setQuantite(1);
                        prePanierObservableList.add(medicamentSelectionne);
                    }
                }
            }
        }


        /**
         * Date de création : 27/02/2025
         * Date de dernière modification : 06/04/2025
         *
         * <p>Méthode d'initialisation du contrôleur. Elle permet également de mettre à jour en temps
         * réel la liste contenant les médicaments que l'on souhaite acheter, et de charger le menuItem contenant
         * la liste des fournisseurs présents en base de données</p>
         *
         * @param location L'emplacement utilisé pour initialiser le contrôleur.
         * @param resources Les ressources utilisées pour initialiser le contrôleur.
         *
         * @author Jason DARIUS
         */
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            chargerMenuItem();
            ajouterColonneActions();
            rechercherParComboBox.getItems().addAll("Nom", "Prix");

            // Initialisation de l'ObservableList
            medicamentObservableList = FXCollections.observableArrayList();
            prePanierObservableList =  FXCollections.observableArrayList();

            zoneAjoutMedicamentTableView.setItems(medicamentObservableList);
            zoneInfoCommandeTableView.setItems(prePanierObservableList);

            // Lier les colonnes de zoneAjoutMedicamentTableView
            denomination1Column.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
            prix1Column.setCellValueFactory(new PropertyValueFactory<>("prixMedicament"));

            // Lier les colonnes de zoneInfoCommandeTableView
            denomination2Column.setCellValueFactory(new PropertyValueFactory<>("denominationMedicament"));
            quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
            prix2Column.setCellValueFactory(cellData -> {
                Medicament medicament = cellData.getValue();
                return new SimpleObjectProperty<>(medicament.getPrixMedicament() * medicament.getQuantite());
            });

            // Rendre la colonne Quantité éditable
            zoneInfoCommandeTableView.setEditable(true);
            quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            quantiteColumn.setOnEditCommit(event -> {
                Medicament medicament = event.getRowValue();
                Integer nouvelleQuantite = event.getNewValue();

                // Valider la nouvelle quantité
                if (nouvelleQuantite != null && nouvelleQuantite >= 0) {
                    medicament.setQuantite(nouvelleQuantite);
                    System.out.println("Quantité mise à jour pour "+
                            medicament.getDenominationMedicament()+ " -> " + nouvelleQuantite);
                    // Rafraîchir le TableView pour mettre à jour prix2Column
                    zoneInfoCommandeTableView.refresh();
                } else {
                    System.out.println("Quantité invalide : " + nouvelleQuantite);
                    UtilAlert.erreurAlert("Erreur", "La quantité doit être un nombre positif.", null, "OK");
                    // Restaurer l'ancienne valeur
                    zoneInfoCommandeTableView.refresh();
                }
            });

            // Lier un événement de saisie à la méthode de recherche
            zoneRechercherMedicamentTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    // Appeler la méthode de recherche à chaque frappe
                    rechercherMedicament();
                }
            });
        }

        /**
         * Date de création : 29/03/2025
         * Date de dernière modification : 05/04/2025
         *
         * <p>Cette méthode permet de passer une Map contenant les informations
         * d'un fournisseur d'un controller à l'autre</p>
         *
         * @param parameters Map de paramètres à faire passer entre 2 pages
         *
         * @author Jason DARIUS
         */
        @Override
        public void initializeWithParameters(Map<String, Object> parameters) {

            // Récupérer l'objet Fournisseur
            if (parameters.containsKey("fournisseur")) {
                this.fournisseur = (Fournisseur) parameters.get("fournisseur");


                if (fournisseur == null) {
                    System.out.println("Fournisseur introuvable dans les paramètres !");
                    return;
                }
                System.out.println("Nom Entreprise : " + fournisseur.getNomEntreprise());
                System.out.println("Numéro SIRET : " + fournisseur.getNumeroSiret());

                if (fournisseur != null) {
                    choisirFournisseurButton.setText(fournisseur.getNomEntreprise());
                } else {
                    System.out.println("Aucun Fournisseur reçu");
                }
            }
        }

        /**
         * Date de création 11/04/2025
         * Date de dernière modification 11/04/2025
         *
         * <p>Cette méthode permet d'ajouter des boutons d'action afin de passer ou supprimer une commande
         * qui est dans le panier</p>
         *
         * @author Nicolas ADAMCZYK
         * @author Jason DARIUS
         */
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
                        med.setQuantite(med.getQuantite() + 1);
                        zoneInfoCommandeTableView.refresh();
                    });

                    moinsBtn.setOnAction(e -> {
                        Medicament med = getTableView().getItems().get(getIndex());
                        int qte = med.getQuantite();
                        if (qte > 1) {
                            med.setQuantite(qte - 1);
                        } else {
                            prePanierObservableList.remove(med);
                        }
                        zoneInfoCommandeTableView.refresh();
                    });

                    supprimerBtn.setOnAction(e -> {
                        Medicament med = getTableView().getItems().get(getIndex());
                        prePanierObservableList.remove(med);
                        zoneInfoCommandeTableView.refresh();
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

    }
