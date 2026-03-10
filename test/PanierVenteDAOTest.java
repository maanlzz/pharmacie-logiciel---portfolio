package databases;

import database.DataBaseConnection;
import database.PanierVenteDAO;
import models.Medicament;
import models.PanierVente;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe qui gère les tests unitaires associés à PanierventeDAO
 *
 * @author Victoria MASSAMBA
 */


public class PanierVenteDAOTest {

    private Connection connection;
    private int idVente;     // vente factice
    private int codeCIP;     // médicament factice

    @BeforeEach
    void setUp() throws Exception {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(false);

        // Médicament de test
        codeCIP = insererMedicament(connection);

        // Vente (montant arbitraire)
        idVente = insererVente(connection, 0.0);

        connection.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.rollback();    // efface tout ce qu’on a inséré
        connection.close();
    }


    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/25
     * <p>Vérifie qu’un panier ajouté est bien présent via getPanierVente.</p>
     * @author Victoria MASSAMBA
     */
    @Test
    void testAjouterPanierEtGetPanierVente() {
        PanierVenteDAO.ajouterPanier(idVente, codeCIP, 3);

        List<PanierVente> lignes = PanierVenteDAO.getPanierVente(idVente);
        assertEquals(1, lignes.size(), "On doit récupérer une seule ligne");

        PanierVente pv = lignes.get(0);
        assertAll(
                () -> assertEquals(idVente, pv.getIdVente()),
                () -> assertEquals(codeCIP, pv.getCode_cip()),
                () -> assertEquals(3, pv.getQuantite())
        );
    }



    /**
     * Date de création : 14/04/2025
     *  Date de dernière modification : 19/05/25
     * <p>
     * Insère un médicament factice et renvoie son code CIP pour tester l'ajout d'un panier.
     * </p>
     * @param conn la connection à la bdd
     * @author Victoria MASSAMBA
     * */
    private int insererMedicament(Connection conn) throws SQLException {
        int cip = (int) (System.nanoTime() % 100_000_000);   // CIP unique de test
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicament (code_cip, denomination_medicament, " +
                        "quantite, seuilrecommande, prix_medicament) " +
                        "VALUES (?, 'TestMed', 100, 10, 1.0)")) {
            ps.setInt(1, cip);
            ps.executeUpdate();
        }
        return cip;
    }

    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/25
     * <p>
     * Insère une vente factice et renvoie son id pour tester l'ajout du panier à la vente.
     * </p>
     * @param conn la connection à la bdd
     * @param montant le montant de la vente
     * @author Victoria MASSAMBA
     * */
    private int insererVente(Connection conn, double montant) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO vente (montant_total, date_vente) " +
                        "VALUES (?, CURRENT_TIMESTAMP) RETURNING id_vente")) {
            ps.setDouble(1, montant);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_vente");
            }
        }
    }
}
