package databases;

import database.DataBaseConnection;
import database.VenteDAO;
import models.Vente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Classe qui gère les tests unitaires associés à venteDAO
 *
 * @author Victoria MASSAMBA
 */


public class VenteDAOTest {

    private Connection connection;
    private int idMaxAvant;// dernier id_vente existant avant le test

    @BeforeEach
    void setUp() throws Exception {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(true);

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id_vente),0) FROM vente")) {
            rs.next();
            idMaxAvant = rs.getInt(1);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // suppression de toutes les ventes créées durant le test

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM vente WHERE id_vente > ?")) {
            ps.setInt(1, idMaxAvant);
            ps.executeUpdate();
        }

        // Mise a jour de la séquence au bon numéro
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    SELECT setval(
                        'vente_id_vente_seq',
                        COALESCE((SELECT MAX(id_vente) FROM vente),0)
                    )
                    """);
        }

        connection.close();
    }


    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/25
     * <p>Test l'ajout d'une vente avec un montant factice dans la bdd</p>
     * @author Victoria MASSAMBA
     * @throws Exception
     */

    @Test

    void testAjouterVente() throws Exception {
        double montant = 123.45;

        int idCree = VenteDAO.ajouterVente(montant);

        assertTrue(idCree > idMaxAvant, "L’id renvoyé doit être > ancien MAX");

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT montant_total FROM vente WHERE id_vente = ?")) {
            ps.setInt(1, idCree);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "La vente doit exister dans la bdd");
                assertEquals(montant, rs.getDouble("montant_total"), 0.001);
            }
        }
    }

    /**
     * Date de création : 14/04/2025
     * Date de dernière modification : 19/05/25
     * <p>Test la récupération de toutes les ventes de la bdd avec l'ajout de 2 ventes factices au préalable</p>
     * @author Victoria MASSAMBA
     * @throws Exception
     */
    @Test
    void testGetAllVentes() throws Exception {
        int avant = VenteDAO.getAllVentes().size();

        VenteDAO.ajouterVente(50.0);
        VenteDAO.ajouterVente(75.0);

        List<Vente> apres = VenteDAO.getAllVentes();
        assertEquals(avant + 2, apres.size(),
                "Le nombre total de ventes doit avoir augmenté de 2");
    }
}