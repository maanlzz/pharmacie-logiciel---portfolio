package global;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import models.Personnel;
import utils.UtilAlert;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Cette classe permet de gérer les changements de fenêtre des rubriques de la barre de menu.
 *
 * @author : Hugo VITORINO PEREIRA
 * @author : Nicolas ADAMCZYK
 * @author : Victoria MASSAMBA
 * Date de création : 26/02/2025
 * Date de dernière modification : 22/04/2025
 */
public class BarreMenuController implements Initializable {

    @FXML
    private MenuItem userNameMenuItem;

    /**
     * Date de création : 23/04/25
     * Initialise le controller en remplacement la section NOM PRENOM par celle du personnel connecté au logiciel.
     *
     * @author Victoria MASSAMBA
     * @param url
     * @param resourceBundle
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Personnel user = SessionManager.getCurrentPersonnel();
        if (user != null) {
            String nom = user.getNom();
            String prenom = user.getPrenom();
            userNameMenuItem.setText(nom.toUpperCase() + " " + prenom);
        }


    }
// Les méthodes suivantes sont déclenchés lors d'un clic sur l'une des rubriques de la barre de menu

    @FXML
    private void handleHome() {
        System.out.println("Page home");
        NavigationManager.loadPage(Page.HOME.getFilePath());
    }

    @FXML
    private void handleVentesAvecOrdonnance() {
        System.out.println("Ventes avec ordonnance");
        NavigationManager.loadPage(Page.VENTES_AVEC_ORDONNANCE.getFilePath());
    }

    @FXML
    private void handleVentesSansOrdonnance() {
        System.out.println("Ventes sans ordonnance");
        NavigationManager.loadPage(Page.VENTES_SANS_ORDONNANCE.getFilePath());
    }

    @FXML
    private void handleNosMedicaments() {
        System.out.println("Liste des médicaments");
        NavigationManager.loadPage(Page.NOS_MEDICAMENTS_RECHERCHE.getFilePath());
    }

    @FXML
    private void handleFournisseursAchat() {
        System.out.println("Achats");
        NavigationManager.loadPage(Page.COMMANDE_AJOUT.getFilePath());
    }

    @FXML
    private void handleFournisseursSuiviDesCommandes() {
        System.out.println("Suivi des commandes");
        NavigationManager.loadPage(Page.COMMANDE_SUIVI.getFilePath());
    }

    @FXML
    private void handleFournisseursConsulter() {
        System.out.println("Consulter fournisseur");
        NavigationManager.loadPage(Page.FOURNISSEUR_RECHERCHE.getFilePath());
    }

    @FXML
    private void handleFournisseursPanier() {
        System.out.println("Panier");
        NavigationManager.loadPage(Page.COMMANDE_PANIER.getFilePath());
    }

    @FXML
    private void handleClientsConsulter() {
        System.out.println("Consulter un client");
        NavigationManager.loadPage(Page.CLIENT_RECHERCHE.getFilePath());
    }

    @FXML
    private void handleClientsReservation() {
        System.out.println("consulter une réservation");
        NavigationManager.loadPage(Page.RESERVATION_RECHERCHE.getFilePath());
    }

    @FXML
    private void handleFinancesPerformances() {
        System.out.println("Performances");
        NavigationManager.loadPage(Page.FINANCES_PERFORMANCES.getFilePath());
    }

    @FXML
    private void handleFinancesPrevisions() {
        System.out.println("Prévisions");
        //TODO A réfléchir
        //NavigationManager.loadPage("/views/finances/FinancesPrevisions");
    }

    @FXML
    private void handleAnalyses() {
        System.out.println("Analyses de données");
        NavigationManager.loadPage(Page.ANALYSES_LOG.getFilePath());
    }

    @FXML
    private void handleAdminPersonnel() {
        System.out.println("Page Personnel");
        NavigationManager.loadPage(Page.PERSONNEL_RECHERCHE.getFilePath());
    }

    @FXML
    private void handleAdminLogs() {
        System.out.println("Page Logs");
        //TODO A faire
        //NavigationManager.loadPage("/views/administration/AdminLogs");
    }

    @FXML
    private void handleAdminBdd() {
        System.out.println("Page Save BDD");
        NavigationManager.loadPage(Page.COMPTE_SAUVEGARDE_BDD.getFilePath());
    }

    @FXML
    private void handleNotifications() {
        System.out.println("Page Notifications");
        NavigationManager.loadPage(Page.NOTIFICATIONS.getFilePath());
    }

    @FXML
    private void handleCompteMessagerie() {
        System.out.println("Page Messagerie");
        //TODO A faire
        //NavigationManager.loadPage("/views/comptes/CompteMessagerie");
    }

    @FXML
    private void handleCompteMotDePasse() {
        System.out.println("Page Changement MDP");
        NavigationManager.loadPage(Page.COMPTE_CHANGER_MDP.getFilePath());
    }
    @FXML
    private void handlerReservationPharmacie() {
        System.out.println("Page Reservations de la Pharmacie");
        NavigationManager.loadPage(Page.RESERVATION_PHARMACIE.getFilePath());
    }

    /**
     * Date de création : 02/04/2025
     * Date de dernière modification : 26/04/2025
     *
     * <p>Permet la déconnexion du logiciel.</p>
     *
     * @author Hugo VITORINO PEREIRA
     */
    @FXML
    private void handleCompteDeconnexion() {
        // Création du type d'alerte
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // Définir le titre du bouton
        alert.setTitle("L301 - Déconnexion logiciel");
        // Définir le gros titre
        alert.setContentText("L3O1 - Voulez-vous vous déconnecter ?");
        alert.setHeaderText(null);
        Optional<ButtonType> resultat = alert.showAndWait();
        boolean choixUtilisateur = resultat.isPresent() && resultat.get() == ButtonType.OK;
        if(choixUtilisateur) {
            NavigationManager.loadPage(Page.AUTHENTIFICATION.getFilePath());
            NavigationManager.removeAllPage();
            SessionManager.logout();
        }
    }

    /**
     * Date : 02/04/2025
     * Date de dernière modification : 26/04/2025
     *
     *<p>Permet de quitter le logiciel.</p>
     *
     * @author Hugo VITORINO PEREIRA
     * @author Jason DARIUS
     */
    @FXML
    private void compteQuitterHandler() {
        // Création du type d'alerte
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // Définir le titre du bouton
        alert.setTitle("L301 - Quitter logiciel");
        // Définir le gros titre
        alert.setContentText("L3O1 - Voulez-vous quitter le logiciel ?");
        alert.setHeaderText(null);
        Optional<ButtonType> resultat = alert.showAndWait();
        boolean choixUtilisateur = resultat.isPresent() && resultat.get() == ButtonType.OK;
        if(choixUtilisateur)
            Platform.exit();
    }
}