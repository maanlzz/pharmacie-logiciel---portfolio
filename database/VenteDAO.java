package database;

import models.Vente;
import java.sql.*;
import java.util.List;
import java.util.UUID;

/**
 * Date de création : 19/03/2025
 * Date de dernière modification : 19/04/2025
 *
 * <p>Cette classe gère les opérations sur la table 'Vente' dans la base de données.</p>
 * <p>Elle permet de récupérer, ajouter et manipuler les ventes dans la base de données.</p>
 *
 * @author Victoria MASSAMBA
 * @author Nicolas ADAMCZYK
 */
public class VenteDAO {

    /**
     * Date de dernière modification : 17/04/2025
     *
     * <p>Cette méthode convertit un ResultSet en un objet Vente.</p>
     * <p>Elle récupère les données de la vente depuis un ResultSet et remplit un objet Vente avec ces informations.</p>
     *
     * @param rs Le ResultSet contenant les données de la vente.
     *
     * @return Un objet Vente rempli avec les données du ResultSet.
     *
     * @throws SQLException Si une erreur SQL survient lors de la récupération des données.
     *
     * @author Nicolas ADAMCZYK
     */
    private static Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        Vente vente = new Vente();
        vente.setIdVente(rs.getInt("id_vente"));
        vente.setMontantTotal(rs.getDouble("montant_total"));
        vente.setDateVente(rs.getTimestamp("date_vente"));
        return vente;
    }

    /**
     * Date de dernière modification : 19/04/2025
     *
     * <p>Cette méthode ajoute une nouvelle vente dans la base de données et retourne son identifiant.</p>
     * <p>Elle insère une vente avec le montant total spécifié et renvoie l'ID de la vente enregistrée.</p>
     *
     * @param montantTotal Le montant total de la vente.
     *
     * @return idvente L'identifiant de la vente tout juste enregistrée.
     *
     * @throws SQLException Si une erreur SQL survient lors de l'insertion.
     *
     * @author Victoria MASSAMBA
     */
    public static int ajouterVente(double montantTotal) throws SQLException {
        String sql = "INSERT INTO Vente (date_vente, montant_total) " +
                "VALUES (CURRENT_TIMESTAMP, ?) " +
                "RETURNING id_vente";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, montantTotal);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vente");
                } else {
                    throw new SQLException("Aucun ID renvoyé par le RETURNING.");
                }
            }
        }
    }


    /**
     * Date de dernière modification : 17/04/2025
     *
     * <p>Cette méthode récupère toutes les ventes de la base de données.</p>
     * <p>Elle exécute une requête SQL pour obtenir toutes les ventes et les retourne sous forme de liste.</p>
     *
     * @return Une liste de ventes.
     *
     * @throws SQLException Si une erreur SQL survient lors de la récupération des données.
     *
     * @author Nicolas ADAMCZYK
     */
    public static List<Vente> getAllVentes() throws SQLException {
        String sql = "SELECT * FROM Vente";
        return DataBaseUtil.executeQuery(sql, VenteDAO::mapResultSetToVente);
    }

}
