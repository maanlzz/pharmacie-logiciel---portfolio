package controllers.clients;

import global.InitializableWithParameters;
import global.NavigationManager;
import database.MedecinDAO;
import global.Page;
import models.Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller de la fenêtre de consultation des informations d'un client.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ClientConsultationController implements Initializable, InitializableWithParameters {
    @FXML
    private TextField nomClientTextField;
    @FXML
    private TextField prenomClientTextField;
    @FXML
    private ComboBox<String> sexeClientComboBox;
    @FXML
    private DatePicker dateNaissanceClientDatePicker;
    @FXML
    private TextField adresseClientTextField;
    @FXML
    private TextField telephoneClientTextField;
    @FXML
    private TextField emailClientTextField;
    @FXML
    private TextField mutuelleClientTextField;
    @FXML
    private TextField numeroSecuriteSocialClientTextField;
    @FXML
    private TextField specialisationMedecinTextField;
    @FXML
    private TextField nomMedecinTraitantTextField;
    @FXML
    private TextField prenomMedecinTraitantTextField;
    @FXML
    private TextField adresseMedecinTraitantTextField;
    @FXML
    private TextField telephoneMedecinTraitantTextField;
    @FXML
    private Button consulterOrdonnanceButton;
    @FXML
    private Button ajouterOrdonnanceButton;
    @FXML
    private Button consulterReservationButton;
    @FXML
    private Button effectuerModificationButton;
    @FXML
    private Button retourFenetrePrecedenteButton;

    private Client client;

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 04/04/2025
     *
     * <p>Redirige vers la fenêtre de consultations des réservations du client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     * @author Victoria MASSAMBA
     */
    @FXML
    private void consultationReservationHandler(){
        Map<String, Object> params = new HashMap<>();
        params.put("client", client);
        NavigationManager.loadPage(Page.RESERVATION_RECHERCHE.getFilePath(),params);
    }

    /**
     * Date : 09/04/2025
     *
     * <p>Méthode qui redirige vers la fenêtre d'ajout d'une réservation pour le client.</p>
     *
     * @author Victoria MASSAMBA
     */
    @FXML
    private void ajouterReservationHandler() {
        Map<String, Object> params = new HashMap<>();
        params.put("client", client);
        NavigationManager.loadPage(Page.RESERVATION_AJOUT.getFilePath(), params);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 04/04/2025
     *
     * <p>Redirige vers la fenêtre de consultations des ordonnances du client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void consultationOrdonnanceHandler() {
        Map<String, Object> params = new HashMap<>();
        params.put("client", client);
        NavigationManager.loadPage(Page.ORDONNANCE_RECHERCHE.getFilePath(), params);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Redirige vers la fenêtre d'ajout d'une ordonnance au client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void ajoutOrdonnanceHandler() {
        Map<String, Object> params = new HashMap<>();
        params.put("client", client);
        NavigationManager.loadPage(Page.ORDONNANCE_AJOUT.getFilePath(), params);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification: 04/04/2025
     *
     * <p>Redirige vers la fenêtre de modifications des informations du client.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void effectuerModificationHandler() {
        Map<String, Object> params = new HashMap<>();
        params.put("client", client);
        NavigationManager.loadPage(Page.CLIENT_MODIFICATION.getFilePath(), params);
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 05/04/2025
     *
     * <p>Permet le retour à la fenêtre précédente du logiciel.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void retourHandler() {
        NavigationManager.goBack();
    }

    /**
     * Date de création : 08/03/2025
     * Date de dernière modification : 30/03/2025
     *
     * <p>Initialise les champs du formulaire avec les informations d'un client existant.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param client L'objet {@link Client} contenant les informations à afficher dans le formulaire.
     *
     * @author Hugo VITORINO PEREIRA
     */
    private void initialisationInformationsClient(Client client){
        this.client = client;
        nomClientTextField.setText(client.getNom());
        prenomClientTextField.setText(client.getPrenom());
        sexeClientComboBox.setValue(client.getSexe());
        dateNaissanceClientDatePicker.setValue(client.getDateNaissance());
        adresseClientTextField.setText(client.getAdresse());
        telephoneClientTextField.setText(client.getTelephone());
        emailClientTextField.setText(client.getEmail());
        mutuelleClientTextField.setText(client.getMutuelle());
        numeroSecuriteSocialClientTextField.setText(client.getNumeroSecuriteSociale());
        nomMedecinTraitantTextField.setText(client.getMedecinTraitant().getNom());
        prenomMedecinTraitantTextField.setText(client.getMedecinTraitant().getPrenom());
        adresseMedecinTraitantTextField.setText(client.getMedecinTraitant().getAdresse());
        telephoneMedecinTraitantTextField.setText(client.getMedecinTraitant().getTelephone());
        specialisationMedecinTextField.setText(String.join(", ", MedecinDAO.getSpecialisationsMedecin(client.getMedecinTraitant().getId())));
    }

    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Cette méthode prend un {@link Map} de paramètres et récupère l'objet {@link Client} à partir de la clé
     * "client". Les informations du client sont ensuite utilisées pour initialiser les champs du formulaire via
     * la méthode {@link #initialisationInformationsClient(Client)}.</p>
     *
     * @param parameters Un {@link Map} contenant les paramètres nécessaires à l'initialisation. La clé "client" doit être associée à un objet {@link Client}.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @Override
    public void initializeWithParameters(Map<String, Object> parameters) {
        initialisationInformationsClient((Client) parameters.get("client"));
    }

    /**
     * Date de création : 05/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Méthode d'initialisation du contrôleur.</p>
     * <p>Cette méthode est appelée automatiquement au chargement de la fenêtre JavaFX.</p>
     *
     * @param location L'emplacement de la ressource de l'interface graphique.
     * @param resources Les ressources utilisées pour initialiser le contrôleur.
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
    }
}
