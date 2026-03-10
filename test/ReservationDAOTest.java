package databases;

import database.DataBaseConnection;
import database.ReservationDAO;
import database.ReserveMedicamentDAO;
import models.Medicament;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import models.Reservation;


import java.sql.*;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date de création : 14/04/2025
 * Classe qui gère les tests unitaires associés à ReservationDAO et à ReserveMedicamentDAO
 *
 * @author Victoria MASSAMBA
 */
public class ReservationDAOTest {
    Connection connection;

    /**
     *
     * Initialise la connection à la bdd avant chaque test
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @BeforeEach
    void setUp() throws SQLException {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(false);
        try (Statement st = connection.createStatement()) {
            // Remise à jour de la séquence des médecins
            st.execute(
                    "SELECT setval('medecin_id_medecin_seq', (SELECT COALESCE(MAX(id_medecin),0) FROM medecin))");
            // Remise à jour de la séquence des clients
            st.execute("SELECT setval('client_id_client_seq', (SELECT COALESCE(MAX(id_client), 0) FROM client))");
        }

    }



    /**
     * Nettoie les modifs faites à la table après les tests
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @AfterEach
    void cleanTables() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.rollback(); // on annule les changements faits par le test
            connection.close();
        }
    }

    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/2025
     * Teste l'ajout d'une réservation dans la bdd et la récupération de cette réservation par son id depuis la bdd
     * On commence par créer une médecin et un client factices pour pouvoir associer une réservation à un client
     * @throws SQLException
     * @author Victoria MASSAMBA
     */

    @Test
    public void testAjouterReservationEtGetReservationById() throws SQLException {
        // création d'un médecin
        int idMedecin = insererMedecin(connection,"testNomMedecin","testPrenomMedecin");

        // création d'un client lié à ce médecin
        int idClient = insererClient(connection,idMedecin,"Test1","Client1Test","F");

        // ajout d'une réservation pour ce client
        int idReservation = ReservationDAO.ajouterReservation(connection, idClient);

        // verification que la réservation a bien été enregistrée
        Reservation r = ReservationDAO.getReservationById(connection,idReservation);
        assertNotNull(r,"La réservation devrait être correctement récupéré depuis la bdd par l'id de la résa");
        assertEquals(idClient, r.getIdClient(),"Les identifiants du client correspondent");
        assertEquals(Date.valueOf(LocalDate.now()), r.getDateReservation(),"Les dates de réservation correspondent");



    }
    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/2025
     * Teste la modification d'une réservation dans la bdd
     * On commence par créer une médecin et deux client factices pour pouvoir associer une réservation au premier client puis pour pouvoir modifier la réservation en l'associant à au 2e client.
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    public void testUpdateReservation() throws SQLException {
        // création d'un médecin
        int idMedecin = insererMedecin(connection,"testNomMedecin","testPrenomMedecin");

        // création d'un client lié à ce médecin
        int idClient = insererClient(connection,idMedecin,"Test1","Client1Test","F");

        // ajout d'une réservation pour ce client
        int idReservation = ReservationDAO.ajouterReservation(connection, idClient);

        // création d'un deuxième client lié au même médecin
        int idClient2 = insererClient(connection,idMedecin,"Test2","Client2Test","H");

        //modification de la réservation par le changement du client
        ReservationDAO.updateReservation(connection, idReservation,idClient2);
        //verification de la modification
        assertEquals(idClient2, ReservationDAO.getReservationById(connection, idReservation).getIdClient(),"L'id du client a bien été modifié");



    }
    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/2025
     * Teste la suppresion des medicaments d'une réservation dans la bdd
     * On commence par créer une médecin et un client factices pour pouvoir associer une réservation à un client
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    void testDeleteReservation() throws Exception {
        // création d'un médecin
        int idMedecin = insererMedecin(connection,"testNomMedecin","testPrenomMedecin");

        // création d'un client lié à ce médecin
        int idClient = insererClient(connection,idMedecin,"Test1","Client1Test","F");

        // ajout d'une réservation pour ce client
        int idReservation = ReservationDAO.ajouterReservation(connection, idClient);
        // pré-insertion
        ReserveMedicamentDAO.addReservation(connection, idReservation, 65196479, 1);

        ReserveMedicamentDAO.deleteReservation(connection, idReservation);

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM reservemedicament WHERE id_reservation = ?")) {
            ps.setInt(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                assertFalse(rs.next(), "Aucune ligne ne doit exister");
            }
        }
    }
    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/2025
     * Teste la suppression d'une réservation dans la bdd.
     * On commence par créer une médecin et un client factices pour pouvoir associer une réservation à un client
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    public void testSupprimerReservation() throws SQLException {
        // création d'un médecin
        int idMedecin = insererMedecin(connection,"testNomMedecin","testPrenomMedecin");

        // création d'un client lié à ce médecin
        int idClient = insererClient(connection,idMedecin,"Test1","Client1Test","F");
        // ajout d'une réservation pour ce client
        int idReservation = ReservationDAO.ajouterReservation(connection, idClient);
        ReserveMedicamentDAO.addReservation(connection, idReservation, 65196479, 3);

        //suppresion de la réservation du cleint
        ReservationDAO.supprimerReservation(connection,idReservation);
        //verification de la suppresion
        Map<Medicament, Integer> map = ReservationDAO.getMedicamentByReservations(connection,idReservation);
        assertTrue(map.isEmpty(), "Il ne devrait plus y avoir de médicament associé à cette réservation.");
        Reservation r = ReservationDAO.getReservationById(connection,idReservation);
        assertNull(r, "La réservation devrait avoir été supprimée de la base de données.");

    }
    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/2025
     * Teste la récupération des médicaments d'une réservation dans la bdd
     * On commence par créer une médecin et un client factices pour pouvoir associer une réservation à un client puis on ajoute un medicament de quantité 3 à la réservation.
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    public void testGetMedicamentByReservations() throws SQLException {
        // création d'un médecin
        int idMedecin = insererMedecin(connection,"testNomMedecin","testPrenomMedecin");

        // création d'un client lié à ce médecin
        int idClient = insererClient(connection,idMedecin,"Test1","Client1Test","F");


        // ajout d'une réservation pour ce client
        int idReservation = ReservationDAO.ajouterReservation(connection, idClient);

        //ajout de médicaments à la réservation avec le code cip d'un médicament choisi au préalable
        ReserveMedicamentDAO.addReservation(connection,idReservation,65196479,3 );
        Map<Medicament, Integer> map = ReservationDAO.getMedicamentByReservations(connection,idReservation);

        boolean medicamentTrouve = false;
        boolean quantiteTrouvee=false;

        for (Map.Entry<Medicament, Integer> entry : map.entrySet()) {
            Medicament med = entry.getKey();
            int quantite = entry.getValue();


            if (med.getCodeCIP() == 65196479) {
                medicamentTrouve = true;
                if(quantite==3){
                    quantiteTrouvee=true;
                    break;
                }

            }

        }

        assertTrue(medicamentTrouve, "Le médicament avec le code CIP 65196479 devrait être présent.");
        assertTrue(quantiteTrouvee,"La quantité du medicament est la bonne");
        assertEquals(1, map.size(), "La réservation contient bien un seul médicament");


    }

    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Insere un medecin dans la base de données pour le test d'une réservation
     * @param conn la connection à la bdd
     * @param nom le nom du medecin
     * @param prenom le prenom du medecin
     * @return l'identifiant du médecin
     * @throws SQLException
     */
    private int insererMedecin(Connection conn, String nom, String prenom) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Medecin (nom, prenom) VALUES (?, ?) RETURNING id_medecin")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_medecin");
            }
        }
    }

    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * <p>Insère un client dans la base de données pour tester la réservation</p>
     * @param conn la connection à la bdd
     * @param idMedecin l'identifiant du médecin
     * @param nom le nom du client
     * @param prenom le prénom du client
     * @param sexe le sexe du client
     * @return l'identifiant du client
     * @author Victoria MASSAMBA
     * @throws SQLException
     */
    private int insererClient(Connection conn, int idMedecin, String nom, String prenom, String sexe) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Client (nom, prenom, sexe, date_naissance, adresse, " +
                        "telephone, email, mutuelle, numero_securite_sociale, id_medecin) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_client")) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, sexe);
            ps.setDate(4, Date.valueOf("2000-01-01"));
            ps.setString(5, "123 Rue Test");
            ps.setString(6, "06" + System.nanoTime() % 1_000_000_00);
            ps.setString(7, nom.toLowerCase() + "@exemple.com");
            ps.setString(8, "MutuelleTest");
            ps.setString(9, String.valueOf(System.nanoTime() % 1_000_000_000_000_000L));
            ps.setInt(10, idMedecin);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_client");
            }
        }
    }


}
