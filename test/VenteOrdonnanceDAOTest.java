package databases;

import database.DataBaseConnection;
import database.VenteOrdonnanceDAO;
import models.VenteOrdonnance;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Classe qui gère les tests unitaires associés à venteDAO
 *
 * @author Victoria MASSAMBA
 */


public class VenteOrdonnanceDAOTest {

    private Connection connection;
    private int idMedecin;
    private int idClient;



    @BeforeEach
    void setUp() throws Exception {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(true);
        idMedecin = insererMedecin(connection, "NomMedecinTest", "PrenomMedecinTest" );
        idClient  = insererClient(connection, idMedecin,
                "Test", "Client", "F");
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id_vente),0) FROM vente")) {
            rs.next();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement st = connection.createStatement()) {


            //Suppresion des ventes factices crées pour le test
            st.executeUpdate(
                    "DELETE FROM venteordonnance WHERE id_client = " + idClient);

            //Remise à jour de la séquence
            st.execute(
                    "SELECT setval('venteordonnance_id_vente_seq', " +
                            "       (SELECT COALESCE(MAX(id_vente),0) FROM venteordonnance))");

            //Suppresion du medecin et du client factices crées pour le test
            st.executeUpdate("DELETE FROM client  WHERE id_client  = " + idClient);
            st.executeUpdate("DELETE FROM medecin WHERE id_medecin = " + idMedecin);
        }


        connection.close();
    }



    /**
     * Date de dernière modification : 19/05/25
     * <p>Test l'ajout d'une vente avec un montant et client factices dans la bdd. Verifie egalement que la vente existe bien dans la bdd</p>
     * author Victoria MASSAMBA
     * @throws Exception
     */

    @Test
    void testAjouterVenteOrdonnanceEtExistence() throws Exception {
        double montant = 88.90;


        int idCree = VenteOrdonnanceDAO.ajouterVenteOrdonnance(connection,idClient, montant);


        VenteOrdonnance vo = new VenteOrdonnance();
        vo.setIdVente(idCree);
        assertTrue(VenteOrdonnanceDAO.venteExistante(vo), "La vente doit exister");


        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT montant_total FROM venteordonnance WHERE id_vente = ?")) {
            ps.setInt(1, idCree);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "La ligne doit être présente");
                assertEquals(montant, rs.getDouble("montant_total"), 0.001,
                        "Montant correctement enregistré");
            }
        }
    }

    /**
     * Date de dernière modification : 19/05/25
     * <p>Test la récupération de toutes les ventes de la bdd avec l'ajout de 2 ventes (et même client) factices au préalable</p>
     * @author Victoria MASSAMBA
     * @throws Exception
     */

    @Test

    void testGetAllVentesOrdonnance() throws Exception {
        int avant = VenteOrdonnanceDAO.getAllVentesOrdonnance().size();

        VenteOrdonnanceDAO.ajouterVenteOrdonnance(connection, idClient, 10.0);
        VenteOrdonnanceDAO.ajouterVenteOrdonnance(connection,idClient, 20.0);


        List<VenteOrdonnance> toutes = VenteOrdonnanceDAO.getAllVentesOrdonnance();
        assertEquals(avant + 2, toutes.size(), "Le total doit augmenter de 2");
    }


    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Insere un medecin dans la base de données pour le test d'une vente avec ordonnance
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
     * <p>Insère un client dans la base de données pour tester la vente avec ordonance</p>
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
