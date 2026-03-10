package databases;

import database.*;
import models.PanierVenteOrdonnance;
import models.Medicament;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe qui gère les tests unitaires associés à PanierVenteOrdonnanceDAO.
 *
 * @author Victoria MASSAMBA
 *
 */
class PanierVenteOrdonnanceDAOTest {

    private Connection connection;

    private int idMedecin;
    private int idClient;
    private int idOrdonnance;
    private int idVente;
    private int codeCIP;

    @BeforeEach
    void setUp() throws Exception {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(false);                // 1 seule transaction

        //ajout d'un medecin, d'un client et d'une ordonnance factice pour tester un panier d'une vente avec ordonnance
        idMedecin   = insererMedecin(connection, "Doc-Test", "PrenomMedecinTest");
        idClient    = insererClient(connection, idMedecin,
                "Client", "Test", "F");
        idOrdonnance= insererOrdonnance(connection, idClient, idMedecin);

        //insertion d'une vente avec ordonnance factice pour tester le panier
        idVente = VenteOrdonnanceDAO.ajouterVenteOrdonnance(connection,
                idClient,0.0);


        codeCIP = insererMedicament(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.rollback();   // annule tout ce qu’on a créé
        connection.close();
    }


    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Teste l'ajout d'un panier dans la bdd et la récupération du panier à l'aide de son id
     * @throws Exception
     * @author Victoria MASSAMBA
     */
    @Test
    void testAjouterPanierEtGetPanier() throws Exception {
        // Ajout de 5 unités du médicament dans le panier
        PanierVenteOrdonnanceDAO.ajouterPanierOrdonnance(
                connection, idVente, codeCIP, 5, idOrdonnance);

        //Vérification avec getPanierVenteOrdonnance
        List<PanierVenteOrdonnance> lignes =
                PanierVenteOrdonnanceDAO.getPanierVenteOrdonnance(connection,idVente);

        assertEquals(1, lignes.size(), "Une seule ligne attendue");

        PanierVenteOrdonnance pvo = lignes.get(0);
        assertAll(
                () -> assertEquals(idVente,   pvo.getIdVenteOrdonnance()),
                () -> assertEquals(codeCIP,   pvo.getCodeCip()),
                () -> assertEquals(5,         pvo.getQuantite()),
                () -> assertEquals(idOrdonnance, pvo.getIdOrdonnance())
        );


        Map<Medicament,Integer> map =
                PanierVenteOrdonnanceDAO.getMedicamentPanierVenteOrdonnance(connection,idVente);

        assertEquals(1, map.size());
        assertTrue(
                map.entrySet().stream()
                        .anyMatch(e -> e.getKey().getCodeCIP() == codeCIP && e.getValue() == 5),
                "La map doit contenir le médicament avec la bonne quantité"
        );
    }

    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Insere un medecin dans la base de données pour le test d'un panier d'une vente avec ordonnance
     * @param conn la connection à la bdd
     * @param nom le nom du medecin
     * @param prenom le prenom du medecin
     * @return l'identifiant du médecin
     * @author Victoria MASSAMBA
     * @throws SQLException
     */
    private int insererMedecin(Connection conn, String nom, String prenom) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medecin(nom, prenom) VALUES (?, ?) RETURNING id_medecin")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
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
    private int insererClient(Connection conn, int idMedecin,
                              String nom, String prenom, String sexe) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO client(nom, prenom, sexe, date_naissance, adresse," +
                        " telephone, email, mutuelle, numero_securite_sociale, id_medecin) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?) RETURNING id_client")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, sexe);
            ps.setDate  (4, Date.valueOf("2000-01-01"));
            ps.setString(5, "1 rue Test");
            ps.setString(6, "060000" + System.nanoTime()%1_000_000);
            ps.setString(7, "test@example.com");
            ps.setString(8, "Mutuelle");
            ps.setString(9, String.valueOf(System.nanoTime()%1_000_000_000_000_000L));
            ps.setInt   (10, idMedecin);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Insere une ordonnance dans la base de données pour le test d'un panier d'une vente avec ordonnance
     * @param conn la connection à la bdd
     * @param idClient l'identifiant du client
     * @param idMedecin l'identifiant du medecin
     * @return l'identifiant de l'ordonnance
     * @author Victoria MASSAMBA
     * @throws SQLException
     */
    private int insererOrdonnance(Connection conn, int idClient, int idMedecin) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ordonnance(id_client, id_medecin) VALUES (?, ?) RETURNING id_ordonnance")) {
            ps.setInt(1, idClient);
            ps.setInt(2, idMedecin);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /**
     *  Date de dernière modification : 19/05/25
     * <p>
     * Insère un médicament factice et renvoie son code CIP pour tester l'ajout d'un panier.
     * </p>
     * @param conn la connection à la bdd
     * @author Victoria MASSAMBA
     * */
    private int insererMedicament(Connection conn) throws SQLException {
        int cip = (int)(System.nanoTime() % 100_000_000);     // code unique
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicament(code_cip, denomination_medicament," +
                        " quantite, seuilrecommande, prix_medicament, necessite_ordonnance)" +
                        " VALUES (?, 'TestMed', 100, 10, 1.0, TRUE)")) {
            ps.setInt(1, cip);
            ps.executeUpdate();
            return cip;
        }
    }
}
