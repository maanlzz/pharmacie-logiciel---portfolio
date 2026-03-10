package controllers.clients;

import controllers.clients.ordonnances.OrdonnanceRechercheController;
import controllers.medicaments.NosMedicamentsRechercherController;
import database.RechercheBDPMDAO;
import global.RechercheBDPMController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Client;
import models.Medicament;
import models.Ordonnance;
import utils.UtilAlert;

import java.io.IOException;
import java.sql.ClientInfoStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitaire pour la gestion de la prescription d'une ordonnance.
 *
 * @author Hugo VITORINO PEREIRA
 * @author Victoria MASSAMBA
 */
public class UtilOrdonnance {

    /**
     * Date : 10/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Crée et ajoute le bouton de suppression pour une ligne de la prescription.</p>
     *
     * @param itemsList La ListView contenant tous les éléments de la prescription
     *
     * @return Button Le bouton de suppresion d'une ligne de la prescription
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static Button creerBoutonSuppressionLigne(ObservableList<HBox> itemsList) {
        Button supprimerLigneButton = new Button("Supprimer");
        // Ajout de la classe CSS associée
        supprimerLigneButton.getStyleClass().add("bouton-rouge");
        // ChatGPT
        supprimerLigneButton.setOnAction(e -> {
            boolean choixUtilisateur = UtilAlert.confirmationAlert("Voulez vous supprimer le médicament ?", "L3O1 - Suppression médicament", null,"Oui", "Non");
            if (choixUtilisateur)
                itemsList.removeIf(hbox -> hbox.equals(supprimerLigneButton.getParent()));
        });
        // Fin ChatGPT
        return supprimerLigneButton;
    }

    /**
     * Date : 10/03/2025
     * Date : 29/03/2025
     *
     * <p>Crée et ajoute la zone d'inscription du nom du medicament pour une ligne de la prescription.</p>
     *
     * @param nomMedicament Le nom du medicament devant être présent dans la prescription
     *
     * @return TextField Le textField contenant le nom du médicament de la ligne de la prescription
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static TextField creerNomMedicamentLigne(String nomMedicament) {
        // Champ TextField affichant le nom du médicament (non éditable)
        TextField nomMedicamentTF = new TextField(nomMedicament);
        nomMedicamentTF.setEditable(false);
        // Empeche le textField de prendre le focus lorsque l'on appuie sur tab
        nomMedicamentTF.setFocusTraversable(false);
        // Pour que le TextField s'adapte à la taille de l'écran
        HBox.setHgrow(nomMedicamentTF, Priority.ALWAYS);

        return nomMedicamentTF;
    }

    /**
     * Date : 10/03/2025
     * Date : 29/03/2025
     *
     * <p>Crée et ajoute le bouton permettant de choisir la quantité du médicament de la prescription.</p>
     *
     * @param quantite La quantité pouvant être présent dans la zone de quantité
     *
     * @return Spinner<Integer> Un spinner permettant de choisir la quantité
     *
     * @author Hugo VITORINO PEREIRA
     */
    // ChatGPT
    private static Spinner<Integer> creerQuantiteSpinnerLigne(int quantite) {
        Spinner<Integer> quantiteS = new Spinner<>();
        quantiteS.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, quantite));
        quantiteS.setPrefWidth(60);
        quantiteS.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Vérifie que seuls des chiffres sont entrés
                quantiteS.getEditor().setText(oldValue);
            }
        });
        return quantiteS;
    }
    // Fin ChatGPT

    /**
     * Date : 10/03/2025
     * Date : 15/03/2025
     *
     * <p>Crée et ajoute un TextField contenant la quantité du médicament de l'ordonnance.</p>
     *
     * @param quantite La quantité pouvant être présent dans la zone de quantité
     *
     * @return TextField Contenant la quantité de médicament demandé.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static TextField creerQuantiteTextFieldLigne(int quantite) {
        TextField quantiteS = new TextField(String.valueOf(quantite));
        quantiteS.setEditable(false);
        quantiteS.setPrefWidth(50);
        return quantiteS;
    }

    /**
     * Date : 15/03/2025
     * Date : 29/03/2025
     *
     * <p>Crée et ajoute la zone d'inscription de posologie pour une ligne de la prescription.</p>
     *
     * @param posologieInitiale La posologie pouvant être chargée
     * @param typeAction Dans quelle situation la méthode est appelée
     *
     * @return TextField Le TextField pouvant contenir la posologie
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static TextField creerPosologieLigne(String posologieInitiale, String typeAction) {
        TextField posologieTF = new TextField(posologieInitiale);
        if (typeAction.equals("Consultation")) {
            posologieTF.setEditable(false);
        }
        else{
            posologieTF.setPromptText("Entrer une posologie");
        }
        HBox.setHgrow(posologieTF, Priority.ALWAYS);
        return posologieTF;
    }

    /**
     * Date : 10/03/2025
     * Date : 17/03/2025
     *
     * <p>Crée une ligne d'affichage pour un médicament sous forme d'un HBox, contenant les champs nécessaires pour afficher et/ou modifier ses informations.</p>
     *
     * @param typeAction Dans quelle situation la méthode est appelée
     * @param posologieInitiale La posologie pouvant être chargée
     * @param itemsList La ListView contenant tous les éléments de la prescription
     * @param nomMedicament Le nom du medicament devant être présent dans la prescription
     * @param quantiteInitiale La quantité pouvant être présent dans la zone de quantité
     *
     * @return HBox Le HBox correspond à la ligne de la prescription
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static HBox creerLigneMedicament(ObservableList<HBox> itemsList, String nomMedicament, int quantiteInitiale, String posologieInitiale, String typeAction) {
        TextField nomMedicamentTF = creerNomMedicamentLigne(nomMedicament);

        // Champ TextField pour entrer la posologie
        TextField posologieTF = creerPosologieLigne(posologieInitiale, typeAction);

        if (typeAction.equals("Ajout") || typeAction.equals("Modification")) {
            Button boutonSupprimerLigne = creerBoutonSuppressionLigne(itemsList);
            Spinner<Integer> quantite = creerQuantiteSpinnerLigne(quantiteInitiale);
            // Ajouter les éléments dans un HBox et le retourner
            return new HBox(10, boutonSupprimerLigne, nomMedicamentTF, quantite, posologieTF);
        }
        else {
            TextField quantite = creerQuantiteTextFieldLigne(quantiteInitiale);
            // Ajouter les éléments (sans le bouton supprimer) dans un HBox et le retourner
            return new HBox(10, nomMedicamentTF, quantite, posologieTF);
        }
    }

    /**
     * Date de création : 03/04/2025
     *
     * <p>Surcharge de la méthode.</p>
     *
     * @param itemsList
     * @param nomMedicament
     * @return
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static HBox creerLigneMedicament(ObservableList<HBox> itemsList, String nomMedicament) {
        HBox ligneMedicament = creerLigneMedicament(itemsList, nomMedicament, 1, "", "Ajout");
        return ligneMedicament;

    }

    /**
     * Date de création : 04/04/2025
     * Date de dernière modification : 05/04/2025
     *
     * <p>Affiche une fenêtre bloquante permettant de choisir un médicament à intégrer à la prescription.</p>
     *
     * @return Le Medicament choisi
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static Medicament afficherFenetreSelectionMedicament() {
        try {
            // Volontairement charger la fenêtre sans NavigationManager
            FXMLLoader loader = new FXMLLoader(NosMedicamentsRechercherController.class.getResource("/views/medicaments/NosMedicamentsRecherche.fxml"));
            //FXMLLoader loader = new FXMLLoader(RechercheBDPMDAO.class.getResource("/views/RechercheBDPM.fxml"));
            Parent root = loader.load();

            NosMedicamentsRechercherController controller = loader.getController();
            //RechercheBDPMController controller = loader.getController();

            Stage stage = new Stage();
            // Pour spécifier que la fenêtre est modale par rapport à l'application, ce qui est signifie que la fenêtre est "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sélectionner un Médicament");

            // Taille fixée à 1200 x 600
            stage.setScene(new Scene(root, 1200, 600));
            // Centre la fenêtre
            stage.centerOnScreen();

            // Définir le mode en SELECTION pour fermer la fenêtre après un double-clic
            controller.setMode("lecture", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture

            return controller.getMedicamentSelectionne();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Date de création : 09/04/2025
     * Date de dernière modification : 09/04/2025
     * Méthode qui affiche une fenêtre bloquante permettant de choisir un Client (à intégrer à une vente avec ordonnance).
     *
     * @return Le client choisi
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    public static Client afficherFenetreSelectionClient(){
        try {
            // Chargement de la fenêtre
            FXMLLoader loader = new FXMLLoader(ClientRechercheController.class.getResource("/views/clients/ClientRecherche.fxml"));
            Parent root = loader.load();

            ClientRechercheController controller = loader.getController();

            Stage stage = new Stage();
            // Fenêtre "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sélectionner un Client");

            // Taille fixée à 1200 x 600
            stage.setScene(new Scene(root, 1200, 600));
            // Centre la fenêtre
            stage.centerOnScreen();
            controller.setMode("lecture", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture

            return controller.getClientSelectionne();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Date de création : 09/04/2025
     * Date de dernière modification : 09/04/2025
     * Méthode qui affiche une fenêtre bloquante permettant de choisir une ordonnance (à intégrer à la vente).
     *
     * @return L'ordonnance choisie
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    public static Ordonnance afficherFenetreSelectionOrdonnance(Client slectedClient) {
        try {
            // Chargement de la fenêtre

            FXMLLoader loader = new FXMLLoader(OrdonnanceRechercheController.class.getResource("/views/clients/ordonnances/OrdonnanceRecherche.fxml"));
            Parent root = loader.load();

            OrdonnanceRechercheController controller = loader.getController();

            HashMap<String, Object> params = new HashMap<>();
            params.put("client", slectedClient);
            controller.initializeWithParameters(params);
            Stage stage = new Stage();
            // Fenêtre est "bloquante"
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sélectionner une Ordonnance");

            // Taille fixée à 1200 x 600
            stage.setScene(new Scene(root, 1200, 600));
            // Centre la fenêtre
            stage.centerOnScreen();
            
            controller.setMode("lecture", stage);

            stage.showAndWait(); // Bloque l’exécution jusqu’à la fermeture

            return controller.getOrdonnanceSelectionnee();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

}
