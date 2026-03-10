package database;

import models.Medicament;
import models.PanierVente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe permettant de gérer les paniers des ventes sans ordonnances dans la base de données
 * Date de création : 18/04/2025
 *
 */

public class PanierVenteDAO {

    /**
     * Date de création : 19/04/25
     * Date de dernière modification : 19/04/25
     * Ajouter un panier d'une vente dans la bbd
     * @param idVente l'identifiant de la vente
     * @param code_cip l'identifiant du médicament
     * @param quantite la quantité du médicament acheté
     * @author Victoria MASSAMBA
     */
    public static void ajouterPanier(int idVente, int code_cip, int quantite) {
        String sql = "INSERT INTO panierVente (id_vente, code_cip, quantite) VALUES(?,?,?)";
        try {
            DataBaseUtil.executeUpdate(sql, idVente, code_cip, quantite);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du panier :" + e.getMessage());
        }
    }

    public static void ajouterPanier(Connection conn, int idVente, int code_cip, int quantite)
            throws SQLException {
        String sql = "INSERT INTO panierVente (id_vente, code_cip, quantite) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVente);
            ps.setInt(2, code_cip);
            ps.setInt(3, quantite);
            ps.executeUpdate();
        }
    }



    /**
     * Convertit un ResultSet en un objet PanierVente.
     * @param rs Le ResultSet contenant les données du panier d'une vente
     * @return Un objet PanierVente, qui correspond à une ligne du panier, rempli avec les données du ResultSet
     */
    public static PanierVente resulsetToPanierVente(ResultSet rs) {
        PanierVente pv = new PanierVente();
        try {
            pv.setIdVente(rs.getInt("id_vente"));
            pv.setCode_cip(rs.getInt("code_cip"));
            pv.setQuantite(rs.getInt("quantite"));
        } catch (SQLException e) {
            System.out.println(e);
        }
        return pv;
    }



    /**
     * Date de création : 18/04/25
     * Date de dernière modification : 18/04/25
     * Récupère le panier d'une vente sans ordonnances spécifié en argument
     * @param idVente L'identifiant de la vente sans ordonnances
     * @author Victoria MASSAMBA
     */
    public static List<PanierVente> getPanierVente(int idVente) {
        List<PanierVente> panierVente = new ArrayList<>();
        String sql = "SELECT * FROM PanierVente WHERE id_vente = ?;";
        try {
            panierVente = DataBaseUtil.executeQuery(sql, new ResultSetMapper<PanierVente>() {
                @Override
                public PanierVente map(ResultSet rs) throws SQLException {
                    return resulsetToPanierVente(rs);
                }
            }, idVente);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du panier : " + e);
        }
        return panierVente;
    }


}