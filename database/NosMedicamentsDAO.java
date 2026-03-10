package database;

import models.Medicament;

import java.sql.*;
import java.util.List;


/**
 * Classe permettant d'effectuer des opérations sur la table Medicament de la base de données
 * @author Nicolas ADAMCZYK
 * @author Victoria MASSAMBA
 * Date de création : 14/03/2025
 * Date de dernière modification : 09/04/2025
 */
public class NosMedicamentsDAO {

    /**
     * Convertit un ResultSet en un objet Medicament.
     * @param rs Le ResultSet contenant les données du médicament
     * @return Un objet Medicament rempli avec les données du ResultSet
     * @throws SQLException Si une erreur SQL survient lors de la récupération des données
     * @author Nicolas ADAMCZYK
     */
    public static Medicament mapResultSetToMedicament(ResultSet rs) throws SQLException {
        Medicament medicament = new Medicament();
        medicament.setCodeCIP(rs.getInt("Code_CIP"));
        medicament.setDenominationMedicament(rs.getString("denomination_medicament"));
        medicament.setFormePharmaceutique(rs.getString("forme_pharmaceutique"));
        medicament.setVoiesAdministration(rs.getString("voies_administration"));
        medicament.setEtatCommercialisation(rs.getString("etat_commercialisation"));
        medicament.setLibellePresentation(rs.getString("libelle_presentation"));
        medicament.setTauxRemboursement(rs.getString("taux_remboursement"));
        medicament.setPrixMedicament(rs.getDouble("prix_medicament"));
        medicament.setIndicationRemboursement(rs.getString("indication_remboursement"));
        medicament.setDesignationElementPharmaceutique(rs.getString("designation_element_pharmaceutique"));
        medicament.setDenominationSubstance(rs.getString("denomination_substance"));
        medicament.setDosageSubstance(rs.getString("dosage_substance"));
        medicament.setConditionDelivrance(rs.getString("condition_delivrance"));
        medicament.setLibelleStatut(rs.getString("libelle_statut"));
        medicament.setDateDebutStatut(rs.getDate("date_debut_statut").toString());
        medicament.setQuantite(rs.getInt("quantite"));
        medicament.setSeuilRecommande(rs.getInt("seuilRecommande"));

        // Récupérer la valeur du champ necessite_ordonnance
        medicament.setNecessiteOrdonnance(rs.getBoolean("necessite_ordonnance"));

        return medicament;
    }

    /**
     * Ajoute un médicament à la base de données
     * @param codeCIP le code CIP du médicament
     * @param denomination la dénomination du médicament
     * @param formePharmaceutique la forme pharmaceutique du médicament
     * @param voiesAdministration les voies d'administration du médicament
     * @param etatCommercialisation l'état de commercialisation du médicament
     * @param libellePresentation le libellé de présentation du médicament
     * @param tauxRemboursement le taux de remboursement du médicament
     * @param prix le prix du médicament
     * @param indicationRemboursement l'indication de remboursement du médicament
     * @param designationElement la désignation de l'élément pharmaceutique
     * @param denominationSubstance la dénomination de la substance active
     * @param dosageSubstance le dosage de la substance active
     * @param conditionDelivrance la condition de délivrance du médicament
     * @param libelleStatut le libellé du statut du médicament
     * @param dateDebutStatut la date de début du statut du médicament
     * @param necessiteOrdonnance Indique si le médicament nécessite une ordonnance
     * @param quantite la quantité du médicament
     * @param seuilRecommande le seuil recommandé du médicament
     * @author Nicolas ADAMCZYK
     */
    public static void ajouterMedicament(int codeCIP, String denomination, String formePharmaceutique,
                                         String voiesAdministration, String etatCommercialisation,
                                         String libellePresentation, String tauxRemboursement,
                                         double prix, String indicationRemboursement,
                                         String designationElement, String denominationSubstance,
                                         String dosageSubstance, String conditionDelivrance,
                                         String libelleStatut, Date dateDebutStatut,
                                         boolean necessiteOrdonnance, int quantite, int seuilRecommande) throws SQLException {

        String sql = "INSERT INTO Medicament (code_cip, denomination_medicament, forme_pharmaceutique, " +
                "voies_administration, etat_commercialisation, libelle_presentation, " +
                "taux_remboursement, prix_medicament, indication_remboursement, " +
                "designation_element_pharmaceutique, denomination_substance, dosage_substance, " +
                "condition_delivrance, libelle_statut, date_debut_statut, necessite_ordonnance, quantite, seuilRecommande) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        DataBaseUtil.executeUpdate(sql, codeCIP, denomination, formePharmaceutique, voiesAdministration, etatCommercialisation,
                libellePresentation, tauxRemboursement, prix, indicationRemboursement, designationElement,
                denominationSubstance, dosageSubstance, conditionDelivrance, libelleStatut, dateDebutStatut,
                necessiteOrdonnance, quantite, seuilRecommande);
    }

    /**
     * Récupère tous les médicaments de la base de données
     * @return Une liste contenant tous les médicaments
     * @author Nicolas ADAMCZYK
     */
    public static List<Medicament> getAllMedicaments() throws SQLException {
        String sql = "SELECT * FROM Medicament";
        return DataBaseUtil.executeQuery(sql, NosMedicamentsDAO::mapResultSetToMedicament);
    }

    /**
     * Recherche les médicaments par leur nom.
     * @param prefixe Le préfixe du nom du médicament recherché
     * @return Une liste de médicaments dont le nom commence par le préfixe donné
     * @author Nicolas ADAMCZYK
     */
    public static List<Medicament> rechercherParNom(String prefixe) throws SQLException {
        String sql = "SELECT * FROM Medicament WHERE LOWER(denomination_medicament) LIKE ?";
        return DataBaseUtil.executeQuery(sql, NosMedicamentsDAO::mapResultSetToMedicament, prefixe.toLowerCase() + "%");
    }

    /**
     * Recherche les médicaments par leur prix.
     * @param prix Le prix du médicament recherché
     * @return Une liste de médicaments ayant le prix spécifié
     * @author Nicolas ADAMCZYK
     */
    public static List<Medicament> rechercherParPrix(Double prix) throws SQLException {
        String sql = "SELECT * FROM Medicament WHERE prix_medicament = ?";
        return DataBaseUtil.executeQuery(sql, NosMedicamentsDAO::mapResultSetToMedicament, prix);
    }

    /**
     * Permet de supprimer un médicament de la base de donnée de la pharmacie
     * @param codeCIP Le code CIP ou CIS du médicament
     * @author Nicolas ADAMCZYK
     */
    public static void supprimerMedicament(int codeCIP) throws SQLException {
        String sql = "DELETE FROM Medicament WHERE code_cip = ?";
        DataBaseUtil.executeUpdate(sql, codeCIP);
    }

    /**
     * Diminue la quantité d'un médicament dans la base de données.
     * @param codeCIP Le code CIP du médicament à mettre à jour.
     * @param quantite La quantité à soustraire (dans ce cas, 1).
     * @throws SQLException Si une erreur SQL survient.
     * @author Nicolas ADAMCZYK
     */
    public static void diminuerQuantiteMedicament(int codeCIP, int quantite) throws SQLException {
        String sql = "UPDATE Medicament SET quantite = quantite - ? WHERE code_cip = ?";
        DataBaseUtil.executeUpdate(sql, quantite, codeCIP);
    }

    public static void diminuerQuantiteMedicament(Connection conn, int codeCIP, int quantite) throws SQLException {
        String sql = "UPDATE Medicament SET quantite = quantite - ? WHERE code_cip = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, codeCIP);
            stmt.executeUpdate();
        }
    }



    /**
     * Date de première modification : 13/04/2025
     * Date de dernière modification : 13/04/2025
     *
     * Récupère la quantité d'un médicament à partir de son code CIP.
     * @param codeCIP Le code CIP du médicament.
     * @return La quantité du médicament, ou -1 si aucune quantité n'est trouvé.
     * @throws SQLException Si une erreur SQL survient.
     * @author Victoria MASSAMBA
     */

    public static int getQuantiteMedicament(int codeCIP) throws SQLException {
        String sql = "SELECT quantite FROM medicament WHERE code_cip = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setInt(1, codeCIP);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantite");
                } else {
                    return -1;
                }
            }
        }
    }
    

    /**
     * Date de première modification : 09/04/2025
     * Date de dernière modification : 09/04/2025
     *
     * Récupère le nom d'un médicament à partir de son code CIP.
     * @param codeCIP Le code CIP du médicament.
     * @return Le nom du médicament, ou null si aucun médicament n'est trouvé.
     * @throws SQLException Si une erreur SQL survient.
     * @author Victoria MASSAMBA
     */
    public static String getMedicamentNomParId(int codeCIP) throws SQLException {
        String sql = "SELECT denomination_medicament FROM Medicament WHERE code_cip = ?";
        List<String> result = DataBaseUtil.executeQuery(sql, rs -> rs.getString("denomination_medicament"), codeCIP);
        if (result.isEmpty()) {
            return null; // Aucun médicament trouvé avec ce code CIP
        } else {
            return result.get(0); // Retourne le nom du médicament
        }
    }
}
