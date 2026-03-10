package database;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import models.Medicament;
import models.Reservation;
import utils.UtilCrypto;

import static database.DataBaseConnection.*;
import static database.DataBaseConnection.getConnection;


/**
 * Date de dernière modification : 09/04/2025
 *
 * <p>Cette classe permet de gérer les opérations en lien avec la table Reservation
 * de la base de données postgre</p>
 *
 * @author Victoria MASSAMBA
 */
public class ReservationDAO {

//    /**
//     * Convertit un ResultSet en un objet Reservation.
//     * @param rs Le ResultSet contenant les données de la réservation
//     * @return Un objet Reservation rempli avec les données du ResultSet
//     * @throws SQLException Si une erreur SQL survient lors de la récupération des données
//     */
//    private static Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
//        Reservation reservation = new Reservation();
//        reservation.setIdReservation(rs.getInt("id_reservation"));
//        reservation.setIdClient(rs.getInt("id_client"));
//        reservation.setDateReservation(rs.getDate("date_reservation"));
//        return reservation;
//    }

    public static List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM Reservation";
        try (Connection connection = DataBaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getDate("date_reservation"),
                        rs.getInt("id_client")
                );
                reservations.add(reservation);
            }
        }
        return reservations;
    }

    /**
     * Ajoute dans la base de données la réservation
     *
     * @param id_client l'identifiant du client pour lequel on souhaite effectuer la résa'
     * @param connection la connection à la bdd
     * @throws SQLException
     */

    public static int ajouterReservation(Connection connection, int id_client) throws SQLException {
        String sql = "INSERT INTO Reservation (date_reservation, id_client) VALUES (CURRENT_DATE, ?) RETURNING id_reservation";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id_client);


            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_reservation");
                } else {
                    throw new SQLException("Pas d’ID renvoyé par RETURNING");
                }
            }
        }
    }

    /**
     * Met a jour dans la base de données la réservation
     *
     * @param conn la connection à la bdd
     * @param id_client le client pour lequel on souhaite modifier la réservation
     * @param id_resa la réservation à modifier
     * @author Victoria MASSAMBA
     * @throws SQLException
     */

    public static void updateReservation(Connection conn, int id_resa, int id_client) throws SQLException {
        String sql = "UPDATE Reservation SET date_reservation = CURRENT_DATE, id_client = ? WHERE id_reservation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_client);
            stmt.setInt(2, id_resa);
            stmt.executeUpdate();
        }
    }

    /**
     * Supprime la réservation de la base de données
     *
     * @param idReservation l'identifiant de la réservation
     * @author Victoria MASSAMBA
     */

    public static void supprimerReservation(int idReservation) throws SQLException {
        String query1 = "DELETE FROM public.reservemedicament WHERE id_reservation = ?";
        String query2 = "DELETE FROM Reservation WHERE id_reservation = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query1)) {
            pstmt.setInt(1, idReservation);
            pstmt.executeUpdate();
        }
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query2)) {
            pstmt.setInt(1, idReservation);
            pstmt.executeUpdate();
        }
    }

    public static void supprimerReservation(Connection connection, int idReservation) throws SQLException {
        String query1 = "DELETE FROM public.reservemedicament WHERE id_reservation = ?";
        String query2 = "DELETE FROM Reservation WHERE id_reservation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query1)) {
            pstmt.setInt(1, idReservation);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = connection.prepareStatement(query2)) {
            pstmt.setInt(1, idReservation);
            pstmt.executeUpdate();
        }
    }

    /**
     * Récupère dans une liste les réservation d'un client passé en paramètre
     * @param clientId l'identifiant du client
     * @return liste la liste des réservations du client
     * @throws SQLException
     * @author Victoria MASSAMBA
     */


    public static List<Reservation> getReservationsByClient(int clientId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM Reservation WHERE id_client = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation(
                            rs.getInt("id_reservation"),
                            rs.getDate("date_reservation"),
                            rs.getInt("id_client")
                    );
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    public static List<Reservation> getReservationsByClient(Connection connection,int clientId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM Reservation WHERE id_client = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation(
                            rs.getInt("id_reservation"),
                            rs.getDate("date_reservation"),
                            rs.getInt("id_client")
                    );
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }


    /**
     * <p>
     * Date de création : 23/04/25
     * </p>
     * <p> Date de dernière modification : 23/04/25 </p>
     * <p> Récupère les medicaments et leur quantité réservée d'une réservation </p>
     *
     * @param reservationID L'identifiant de la réservation
     * @return la map rempli des médicaments et de leur quantité réservés
     * @throws SQLException
     * @author Victoria MASSAMBA
     */
    public static Map<Medicament, Integer> getMedicamentByReservations(int reservationID) throws SQLException {
        Map<Medicament, Integer> reservations = new HashMap<>();
        String query = "SELECT  rm.id_reservation,rm.code_cip, rm.quantite, m.code_cip AS m_cip, m.denomination_medicament, m.forme_pharmaceutique,\n" +
                "                m.voies_administration, m.etat_commercialisation, m.libelle_presentation, m.taux_remboursement, m.prix_medicament, m.indication_remboursement, \n" +
                "                m.designation_element_pharmaceutique, m.denomination_substance, m.dosage_substance, m.condition_delivrance, m.libelle_statut, m.date_debut_statut, m.necessite_ordonnance, m.seuilRecommande\n" +
                ", rm.quantite FROM public.reservemedicament rm LEFT JOIN public.medicament m ON rm.code_cip = m.code_cip WHERE rm.id_reservation =?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reservationID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int codeCip = rs.getInt("code_cip");
                    int quantite = rs.getInt("quantite");
                    Medicament med;
                    if (rs.getObject("m_cip") != null) {
                        med = NosMedicamentsDAO.mapResultSetToMedicament(rs);
                    } else {
                        med = recupFromBDPM(codeCip);
                    }

                    reservations.put(med, rs.getInt("quantite"));
                }
            }
        }
        return reservations;
    }

    public static Map<Medicament,Integer> getMedicamentByReservations(Connection connection, int reservationID) throws SQLException {
        Map<Medicament, Integer> reservations = new HashMap<>();
        String query = "SELECT  rm.id_reservation,rm.code_cip, rm.quantite, m.code_cip AS m_cip, m.denomination_medicament, m.forme_pharmaceutique,\n" +
                "                m.voies_administration, m.etat_commercialisation, m.libelle_presentation, m.taux_remboursement, m.prix_medicament, m.indication_remboursement, \n" +
                "                m.designation_element_pharmaceutique, m.denomination_substance, m.dosage_substance, m.condition_delivrance, m.libelle_statut, m.date_debut_statut, m.necessite_ordonnance, m.seuilRecommande\n" +
                ", rm.quantite FROM public.reservemedicament rm LEFT JOIN public.medicament m ON rm.code_cip = m.code_cip WHERE rm.id_reservation =?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reservationID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int codeCip   = rs.getInt("code_cip");
                    int quantite  = rs.getInt("quantite");
                    Medicament med;
                    if(rs.getObject("m_cip") != null) {
                        med = NosMedicamentsDAO.mapResultSetToMedicament(rs);
                    }else{
                        med = recupFromBDPM(codeCip);
                    }

                    reservations.put(med, rs.getInt("quantite"));
                }
            }
        }
        return reservations;
    }


    /**
     * <p>Date de création : 23/04/25</p>
     * <p> Date de dernière modification : 23/04/25 </p>
     * <p>Retourne un médicament de la BDPM par son code_cip (ou code_cis)</p>
     * @param codeCip le code_cip (ou code_cis) du medicament
     * @return le medicament
     * @throws SQLException l'exception
     * @author Victoria MASSAMBA
     */
    private static Medicament recupFromBDPM(int codeCip) throws SQLException {
        String sql = "SELECT cis.code_cis, cis.denomination_medicament, cis.forme_pharmaceutique, " +
                "cis.voies_administration, cis.etat_commercialisation, " +
                "cip.libelle_presentation, cip.taux_remboursement, cip.prix_medicament, " +
                "cip.indication_remboursement, " +
                "compo.designation_element_pharmaceutique, compo.denomination_substance, compo.dosage_substance, " +
                "cpd.condition_prescription, " +
                "dispo_spec.libelle_statut, dispo_spec.date_debut " +
                "FROM cis " +
                "JOIN cis_cip AS cip ON cis.code_cis = cip.code_cis " +
                "JOIN cis_compo AS compo ON cis.code_cis = compo.code_cis " +
                "LEFT JOIN cis_cpd AS cpd ON cis.code_cis = cpd.code_cis " +
                "LEFT JOIN cis_cip_dispo_spec AS dispo_spec ON cis.code_cis = dispo_spec.code_cis " +
                "WHERE cis.code_cis = ?";

        try (Connection c = DataBaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, codeCip);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Medicament m = new Medicament();
                    m.setCodeCIP(rs.getInt("code_cis"));
                    m.setDenominationMedicament(rs.getString("denomination_medicament"));
                    m.setFormePharmaceutique(rs.getString("forme_pharmaceutique"));
                    m.setVoiesAdministration(rs.getString("voies_administration"));
                    m.setEtatCommercialisation(rs.getString("etat_commercialisation"));
                    m.setLibellePresentation(rs.getString("libelle_presentation"));
                    m.setTauxRemboursement(rs.getString("taux_remboursement"));
                    m.setPrixMedicament(rs.getDouble("prix_medicament"));
                    m.setIndicationRemboursement(rs.getString("indication_remboursement"));
                    m.setDesignationElementPharmaceutique(rs.getString("designation_element_pharmaceutique"));
                    m.setDenominationSubstance(rs.getString("denomination_substance"));
                    m.setDosageSubstance(rs.getString("dosage_substance"));
                    m.setConditionDelivrance(rs.getString("condition_prescription"));
                    m.setLibelleStatut(rs.getString("libelle_statut"));
                    m.setDateDebutStatut(rs.getString("date_debut"));
                    m.setQuantite(0); // pas de stock local
                    return m;
                } else {
                    throw new SQLException("Aucun médicament trouvé pour CIP " + codeCip);
                }
            }
        }
    }


    /**
     * <p>Date de création : 23/04/25</p>
     * <p> Date de dernière modification : 23/04/25 </p>
     * <p>Retourne une réservation de la bdd par son identifiant</p>
     * @param idReservation l'identifiant de la réservation
     * @return la réservation
     * @throws SQLException l'exception
     * @author Victoria MASSAMBA
     */

    public static Reservation getReservationById(int idReservation) throws SQLException {
            Reservation reservation = null;
            String query = "SELECT * FROM Reservation WHERE id_reservation = ?";
            try (Connection connection = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, idReservation);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        reservation = new Reservation(
                                rs.getInt("id_reservation"),
                                rs.getDate("date_reservation"),
                                rs.getInt("id_client")
                        );
                    }
                }
            }

            return reservation;
    }

    public static Reservation getReservationById(Connection connection, int idReservation) throws SQLException {
        Reservation reservation = null;
        String query = "SELECT * FROM Reservation WHERE id_reservation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idReservation);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservation = new Reservation(
                            rs.getInt("id_reservation"),
                            rs.getDate("date_reservation"),
                            rs.getInt("id_client")
                    );
                }
            }
        }

        return reservation;
    }


    /**
     * <p>Date de création : 24/04/25</p>
     * <p>Date de dernière modification : 4/05/25</p>
     * <p>Récupère les ruptures locales (de la pharmacie) et les médicaments non vendus en pharmacie</p>
     * @author Victoria MASSAMBA
     * @param prefixe le préfixe entré dans la barre de recherche par l'utilisateur
     * @return la liste des médicaments
     * @throws SQLException l'exception
     */

    public static List<Medicament> getMedicamentsPourReservation(String prefixe) throws SQLException {


        String like = prefixe.trim().toLowerCase() + "%";

        String sql = "/* Ruptures locales */" +
                "SELECT\n" +
                "    m.code_cip AS code_cis,\n" +
                "    m.denomination_medicament,\n" +
                "    m.forme_pharmaceutique,\n" +
                "    m.voies_administration,\n" +
                "    m.etat_commercialisation,\n" +
                "    m.libelle_presentation,\n" +
                "    m.taux_remboursement,\n" +
                "    m.prix_medicament,\n" +
                "    m.indication_remboursement,\n" +
                "    m.designation_element_pharmaceutique,\n" +
                "    m.denomination_substance,\n" +
                "    m.dosage_substance,\n" +
                "    m.condition_delivrance AS condition_prescription,\n" +
                "    m.libelle_statut,\n" +
                "    m.date_debut_statut  AS date_debut,\n" +
                "    0  AS quantite\n" +
                "    FROM   public.medicament m\n" +
                "    WHERE  m.quantite = 0\n" +
                "    AND  LOWER(m.denomination_medicament) LIKE ? \n" +
                "\n" +
                "    UNION ALL\n" +
                "\n" +
                "    /*  Medicaments jamais vendues */\n" +
                "    SELECT\n" +
                "    cis.code_cis,\n" +
                "    cis.denomination_medicament,\n" +
                "    cis.forme_pharmaceutique,\n" +
                "    cis.voies_administration,\n" +
                "    cis.etat_commercialisation,\n" +
                "    cip.libelle_presentation,\n" +
                "    CAST(cip.taux_remboursement AS TEXT)  AS taux_remboursement,\n" +
                "    cip.prix_medicament,\n" +
                "    cip.indication_remboursement,\n" +
                "    compo.designation_element_pharmaceutique,\n" +
                "    compo.denomination_substance,\n" +
                "    compo.dosage_substance,\n" +
                "    cpd.condition_prescription,\n" +
                "    dispo.libelle_statut,\n" +
                "    dispo.date_debut,\n" +
                "    -1 AS quantite\n" +
                "    FROM cis\n" +
                "    LEFT JOIN  cis_cip  AS cip   ON cip.code_cis   = cis.code_cis\n" +
                "    LEFT JOIN  cis_compo AS compo ON compo.code_cis = cis.code_cis\n" +
                "    LEFT JOIN  cis_cpd  AS cpd   ON cpd.code_cis   = cis.code_cis\n" +
                "    LEFT JOIN  cis_cip_dispo_spec AS dispo ON dispo.code_cis = cis.code_cis\n" +
                "    LEFT JOIN  public.medicament  AS m ON m.code_cip  = cis.code_cis\n" +
                "    WHERE  LOWER(cis.denomination_medicament) LIKE ? \n" +
                "    AND   m.code_cip IS NULL \n" +

                "    ORDER BY denomination_medicament;";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // On alimente les deux ? avec le même motif
            ps.setString(1, like);   // pour le bloc A
            ps.setString(2, like);   // pour le bloc B

            try (ResultSet rs = ps.executeQuery()) {

                List<Medicament> liste = new ArrayList<>();

                while (rs.next()) {
                    Medicament m = new Medicament();
                    m.setCodeCIP(rs.getInt("code_cis"));
                    m.setDenominationMedicament(rs.getString("denomination_medicament"));
                    m.setFormePharmaceutique(rs.getString("forme_pharmaceutique"));
                    m.setVoiesAdministration(rs.getString("voies_administration"));
                    m.setEtatCommercialisation(rs.getString("etat_commercialisation"));
                    m.setLibellePresentation(rs.getString("libelle_presentation"));
                    m.setTauxRemboursement(rs.getString("taux_remboursement"));
                    m.setPrixMedicament(rs.getDouble("prix_medicament"));
                    m.setIndicationRemboursement(rs.getString("indication_remboursement"));
                    m.setDesignationElementPharmaceutique(rs.getString("designation_element_pharmaceutique"));
                    m.setDenominationSubstance(rs.getString("denomination_substance"));
                    m.setDosageSubstance(rs.getString("dosage_substance"));
                    m.setConditionDelivrance(rs.getString("condition_prescription"));
                    m.setLibelleStatut(rs.getString("libelle_statut"));
                    m.setDateDebutStatut(rs.getString("date_debut"));
                    m.setQuantite(rs.getInt("quantite"));

                    liste.add(m);
                }
                return liste;
            }
        }
    }








}
