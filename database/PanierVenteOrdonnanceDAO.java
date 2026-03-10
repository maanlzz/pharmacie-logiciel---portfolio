package database;

import models.Medicament;
import models.PanierVente;
import models.PanierVenteOrdonnance;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe permettant de gérer les paniers des ventes sans ordonnances dans la base de données
 * Date de création : 18/04/2025
 * @author Victoria MASSAMBA
 */

public class PanierVenteOrdonnanceDAO {

    public static void ajouterPanierOrdonnance(Connection conn, int idVente, int code_cip, int quantite, Integer id_ordonnance) throws SQLException {
        String sql = "INSERT INTO panierVenteOrdonnance "
                + "(id_vente, id_ordonnance, code_cip, quantite) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVente);
            if (id_ordonnance != null) {
                stmt.setInt(2, id_ordonnance);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, code_cip);
            stmt.setInt(4, quantite);
            stmt.executeUpdate();
        }
    }


    /**
     * Convertit un ResultSet en un objet PanierVente.
     * @param rs Le ResultSet contenant les données du panier d'une vente avec ordonnance
     * @return Un objet PanierVente, qui correspond à une ligne du panier, rempli avec les données du ResultSet
     */
    public static PanierVenteOrdonnance resulsetToPanierVenteOrdonnance(ResultSet rs)  {
        PanierVenteOrdonnance pvo = new PanierVenteOrdonnance();
        try{
            pvo.setIdVenteOrdonnance(rs.getInt("id_vente"));
            pvo.setCodeCip(rs.getInt("code_cip"));
            pvo.setQuantite(rs.getInt("quantite"));
            Integer idOrd = rs.getObject("id_ordonnance", Integer.class);
            pvo.setIdOrdonnance(idOrd);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return pvo;

    }

    /**
     * Date de création : 18/04/25
     * Date de dernière modification : 18/04/25
     * Récupère chaque panier des ventes avec ordonnance
     * @return la liste des paniers des ventes avec ordonnance
     * @throws SQLException L'erreur
     */

    public static List<PanierVenteOrdonnance> getTousPaniers() throws SQLException {
        String sql = "SELECT * FROM PanierVenteOrdonnance;";
        List<PanierVenteOrdonnance> paniersVentes = new ArrayList<PanierVenteOrdonnance>();
        try(Connection connection = DataBaseConnection.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PanierVenteOrdonnance pvo = new PanierVenteOrdonnance();
                pvo.setIdVenteOrdonnance(rs.getInt("id_vente"));
                pvo.setCodeCip(rs.getInt("code_cip"));
                pvo.setQuantite(rs.getInt("quantite"));
                paniersVentes.add(pvo);
            }



        }catch (SQLException e) {
            System.out.println("Erreur lors de la récupérations des tous les paniers des ventes : "+e);
        }
        return paniersVentes;


    }
    /**
     * Date de création : 18/04/25
     * Date de dernière modification : 18/04/25
     * Récupère le panier d'une vente avec ordonnances spécifié en argument
     * @param idVente L'identifiant de la vente avec ordonnances
     * @author Victoria MASSAMBA
     */
    public static List<PanierVenteOrdonnance> getPanierVenteOrdonnance(int idVente) {
        List<PanierVenteOrdonnance> panierVente = new ArrayList<PanierVenteOrdonnance>();

        String sql = "SELECT * FROM PanierVenteOrdonnance WHERE id_vente = ?;";
        try(Connection connection = DataBaseConnection.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, idVente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PanierVenteOrdonnance pvo = new PanierVenteOrdonnance();
                pvo.setIdVenteOrdonnance(rs.getInt("id_vente"));
                pvo.setCodeCip(rs.getInt("code_cip"));
                pvo.setQuantite(rs.getInt("quantite"));
                pvo.setIdOrdonnance(rs.getObject("id_ordonnance", Integer.class));

                panierVente.add(pvo);
            }

        }catch(SQLException e){
            System.err.println("Erreur "+e.getErrorCode()+" lors de la récupération du panier : "+e.getMessage());
        }
        return panierVente;

    }

    public static List<PanierVenteOrdonnance> getPanierVenteOrdonnance(Connection connection, int idVente) {
        List<PanierVenteOrdonnance> panierVente = new ArrayList<PanierVenteOrdonnance>();

        String sql = "SELECT * FROM PanierVenteOrdonnance WHERE id_vente = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, idVente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PanierVenteOrdonnance pvo = new PanierVenteOrdonnance();
                pvo.setIdVenteOrdonnance(rs.getInt("id_vente"));
                pvo.setCodeCip(rs.getInt("code_cip"));
                pvo.setQuantite(rs.getInt("quantite"));
                pvo.setIdOrdonnance(rs.getObject("id_ordonnance", Integer.class));

                panierVente.add(pvo);
            }

        }catch(SQLException e){
            System.err.println("Erreur "+e.getErrorCode()+" lors de la récupération du panier : "+e.getMessage());
        }
        return panierVente;

    }

    /**
     * Date de création : 18/04/2025
     * Date de dernière modification : 18/04/2025
     * Récupère les médicaments et leurs quantités achetés du panier d'une vente avec ordonnnace
     * @param idVente l'identifiant d'une vente avec ordonnance
     * @return la map des médicaments du panier d'une vente et leur quantité acheté
     * @author Victoria MASSAMBA
     */
    public static Map<Medicament,Integer> getMedicamentPanierVenteOrdonnance(int idVente) {
        Map<Medicament,Integer> medicamentsPanier = new HashMap<>();
        String sql = "SELECT pvo.code_cip, pvo.quantite, m.denomination_medicament FROM PanierVenteOrdonnance pvo INNER JOIN Medicament m ON pvo.code_cip = m.code_cip WHERE id_vente = ? ;";
        try(Connection connection = DataBaseConnection.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Medicament med = new Medicament();
                    int quantite = rs.getInt("quantite");
                    med.setCodeCIP(rs.getInt("code_cip"));
                    med.setDenominationMedicament(rs.getString("denomination_medicament"));
                    medicamentsPanier.put(med,quantite);
                }
            }

        }catch(SQLException e){
            System.err.println("Erreur lors de la récupération des médicaments du panier : "+e);
        }
        return medicamentsPanier;

    }

    public static Map<Medicament,Integer> getMedicamentPanierVenteOrdonnance(Connection connection,int idVente) {
        Map<Medicament,Integer> medicamentsPanier = new HashMap<>();
        String sql = "SELECT pvo.code_cip, pvo.quantite, m.denomination_medicament FROM PanierVenteOrdonnance pvo INNER JOIN Medicament m ON pvo.code_cip = m.code_cip WHERE id_vente = ? ;";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, idVente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Medicament med = new Medicament();
                    int quantite = rs.getInt("quantite");
                    med.setCodeCIP(rs.getInt("code_cip"));
                    med.setDenominationMedicament(rs.getString("denomination_medicament"));
                    medicamentsPanier.put(med,quantite);
                }
            }

        }catch(SQLException e){
            System.err.println("Erreur lors de la récupération des médicaments du panier : "+e);
        }
        return medicamentsPanier;

    }


}

