package database;


import models.VenteOrdonnance;
import java.sql.*;
import java.util.*;

/**
 * Date de création : X/X/25
 * Date de dernière modification : 17/04/25
 *
 * <p>Cette classe permet la gestion des ventes avec ordonnance dans la base de données.
 * Elle propose diverses méthodes pour vérifier l'existence d'une vente, insérer, mettre à jour et supprimer
 * une vente avec ordonnance.</p>
 *
 * @author Victoria MASSAMBA
 */
public class VenteOrdonnanceDAO {

    /**
     * Date de création : 17/04/25
     * Date de dernière modification : 17/04/25
     *
     * <p>Vérifie si la vente passée en paramètre est déjà présente dans la base de données.</p>
     *
     * @param vente La vente à vérifier.
     * @return true si la vente existe dans la base, false sinon.
     * @author Victoria MASSAMBA
     */
    public static boolean venteExistante(VenteOrdonnance vente) {
        String sql = "SELECT id_vente FROM VenteOrdonnance WHERE id_vente = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vente.getIdVente());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Date de création : 17/04/25
     * Date de dernière modification : 19/04/25
     *
     * <p>Ajoute une vente avec ordonnance dans la base de données.</p>
     *
     * @return id_vente l'identifiant de la vente
     * @author Victoria MASSAMBA
     */
    public static int ajouterVenteOrdonnance(int id_client, double montant_total) throws SQLException {
        String sql = "INSERT INTO VenteOrdonnance (id_client, montant_total, date_vente) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP)"+
                "RETURNING id_vente";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, id_client);
            stmt.setDouble(2, montant_total);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vente");
                } else {
                    throw new SQLException("Aucun ID renvoyé par le RETURNING.");
                }
            }
        }
    }

    public static int ajouterVenteOrdonnance(Connection conn, int id_client, double montant_total) throws SQLException {
        String sql = "INSERT INTO VenteOrdonnance (id_client, montant_total, date_vente) "
                + "VALUES (?, ?, CURRENT_TIMESTAMP) RETURNING id_vente";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_client);
            stmt.setDouble(2, montant_total);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vente");
                } else {
                    throw new SQLException("Pas d’ID renvoyé par RETURNING");
                }
            }
        }
    }



    /**
     * 17/04/25
     * Récupère toutes les ventes avec ordonnance de la base de données.
     * @return Une liste de ventes avec ordonnance.
     * @throws SQLException Si une erreur SQL survient.
     */
    public static List<VenteOrdonnance> getAllVentesOrdonnance() throws SQLException {
        String sql = "SELECT * FROM VenteOrdonnance";
        return DataBaseUtil.executeQuery(sql, VenteOrdonnanceDAO::mapResultSetToVente);
    }

    /**
     * Convertit un ResultSet en un objet VenteOrdonnance.
     * @param rs Le ResultSet contenant les données de la vente
     * @return Un objet Vente rempli avec les données du ResultSet
     * @throws SQLException Si une erreur SQL survient lors de la récupération des données
     */
    private static VenteOrdonnance mapResultSetToVente(ResultSet rs) throws SQLException {
        VenteOrdonnance vente = new VenteOrdonnance();
        vente.setIdVente(rs.getInt("id_vente"));
        vente.setIdClient(rs.getInt("id_client"));
        //vente.setIdOrdonnance(rs.getInt("id_ordonnance"));
        vente.setMontantTotal(rs.getDouble("montant_total"));
        vente.setDateVente(rs.getTimestamp("date_vente"));
        return vente;
    }


}

