package databases;

import database.DataBaseConnection;
import database.LogDAO;
import models.Log;
import models.Log.LogAction;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe qui gère les tests unitaires associés à logDAO
 *
 * @author Victoria MASSAMBA
 */
public class LogDAOTest {

    private Connection connection;
    private int idPersonnel;



    @BeforeEach
    void setUp() throws Exception {
        connection = DataBaseConnection.getConnection();
        connection.setAutoCommit(false);

        //ajout d'un faux personnel et commit pour le rendre visible
        idPersonnel = insererPersonnel(connection,"Test","Utilisateur");
        connection.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        // supprimer les logs créés dans le test
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM public.log WHERE id_personnel = ?")) {
            ps.setInt(1,idPersonnel);
            ps.executeUpdate();
        }
        // supprimer le personnel factice
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM personnel WHERE id = ?")) {
            ps.setInt(1,idPersonnel);
            ps.executeUpdate();
        }

        // remettre les séquences
        try (Statement st = connection.createStatement()) {
            st.execute("""
            SELECT setval('log_id_log_seq',
                          GREATEST(1,(SELECT COALESCE(MAX(id_log),0) FROM public.log)));
        """);
            st.execute("""
            SELECT setval('personnel_id_seq',
                          GREATEST(1,(SELECT COALESCE(MAX(id),0) FROM personnel)));
        """);
        }

        connection.commit();
        connection.close();
    }



    /**
     * Date de dernière modification : 19/05/2025
     * <p>Teste l'ajout d'un log à la bdd et sa récupération depuis la bdd par le moyen de l'id du personnel</p>
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    void testAjouterLogEtGetLogsByPersonnel() throws SQLException {
        Log log = new Log(LogAction.CONNEXION, LocalDateTime.now(), idPersonnel);
        assertTrue(LogDAO.ajouterLog(log), "L’insertion doit retourner true");
        //assertTrue(log.getIdLog() > idLogMaxAvant, "L’id généré doit être > ancien MAX");

        List<Log> liste = LogDAO.getLogsByPersonnel(idPersonnel);
        assertFalse(liste.isEmpty(), "On doit récupérer au moins le log inséré");
        assertEquals(log.getIdLog(), liste.get(0).getIdLog(), "Même id_log qu’à l’insertion");
    }


    /**
     * Date de dernière modification : 19/05/2025
     * <p>Teste la récupération de logs selon une plage temporelle</p>
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    @Test
    void testGetLogsByPeriod() throws SQLException {

        LocalDateTime avant   = LocalDateTime.now().minusMinutes(1);
        Log l = new Log(LogAction.MODIFIER_MOT_DE_PASSE, LocalDateTime.now(), idPersonnel);
        LogDAO.ajouterLog(l);
        LocalDateTime apres   = LocalDateTime.now().plusMinutes(1);

        List<Log> periode = LogDAO.getLogsByPeriod(avant, apres);
        assertTrue(
                periode.stream().anyMatch(log -> log.getIdLog() == l.getIdLog()),
                "Le log doit être trouvé dans la plage temporelle");
    }

    /**
     * Date de dernière modification : 19/05/2025
     * <p>Teste la récupération de tous les log depuis la bdd</p>
     * @throws SQLException
     * @author Victoria MASSAMBA
     */


    @Test
    void testGetAllLogs() throws SQLException {
        int avant = LogDAO.getAllLogs().size();

        LogDAO.ajouterLog(new Log(LogAction.AJOUTER_RESERVATION, LocalDateTime.now(), idPersonnel));
        LogDAO.ajouterLog(new Log(LogAction.SUPPRIMER_RESERVATION, LocalDateTime.now(), idPersonnel));

        int apres = LogDAO.getAllLogs().size();
        assertEquals(avant + 2, apres, "Le nombre total doit avoir augmenté de 2");
    }


    /**
     * Date de création : 19/05/2025
     * <p>Date de dernière modification : 19/05/25</p>
     * Insère un personnel fictif pour tester les logs
     * @param conn la connection à la bdd
     * @param nom le nom du personnel
     * @param prenom le prenom du personnel
     * @return l'identifiant du personnel
     * @author Victoria MASSAMBA
     */
    private int insererPersonnel(Connection conn, String nom, String prenom) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO personnel (nom, prenom) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
