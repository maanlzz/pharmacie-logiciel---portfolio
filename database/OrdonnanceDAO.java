package database;

import models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet la gestion des ordonnances dans la base de données.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class OrdonnanceDAO {

    /**
     * Date : 28/03/2025
     * Date : 04/04/2025
     *
     * <p>Récupère la liste de tous les ordonnances associées à un client.</p>
     *
     * @return List<Client>
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static List<Ordonnance> getToutesLesOrdonnances(Client clientSelectionne) {
        List<Ordonnance> ordonnances = new ArrayList<>();
        // Requête fournie par ChatGPT
        String sql = "SELECT o.id_ordonnance, o.date_ordonnance, " +
                "c.id_client, c.nom AS client_nom, c.prenom AS client_prenom, c.sexe, c.date_naissance, " +
                "c.adresse AS client_adresse, c.telephone AS client_telephone, c.email, c.mutuelle, c.numero_securite_sociale, " +
                "m.id_medecin, m.nom AS medecin_nom, m.prenom AS medecin_prenom, m.telephone AS medecin_telephone, m.adresse AS medecin_adresse " +
                "FROM Ordonnance o " +
                "JOIN Client c ON o.id_client = c.id_client " +
                "JOIN Medecin m ON o.id_medecin = m.id_medecin " +
                "WHERE c.id_client = ?";
        // Fin ChatGPT

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            stmt.setInt(1, clientSelectionne.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Récupérer les informations du client
                Client client = new Client(
                        rs.getInt("id_client"),
                        rs.getString("client_nom"),
                        rs.getString("client_prenom"),
                        rs.getString("sexe"),
                        rs.getDate("date_naissance").toLocalDate(),
                        rs.getString("client_adresse"),
                        rs.getString("client_telephone"),
                        rs.getString("email"),
                        rs.getString("mutuelle"),
                        rs.getString("numero_securite_sociale"),
                        null  // Temporairement null, le médecin est ajouté après
                );

                // Récupérer les informations du médecin
                Medecin medecin = new Medecin(
                        rs.getInt("id_medecin"),
                        MedecinDAO.getSpecialisationsMedecin(rs.getInt("id_medecin")), // Charger les spécialités du médecin
                        rs.getString("medecin_nom"),
                        rs.getString("medecin_prenom"),
                        rs.getString("medecin_adresse"),
                        rs.getString("medecin_telephone")
                );

                // Associer le médecin au client
                client.setMedecinTraitant(clientSelectionne.getMedecinTraitant());

                // Créer l'ordonnance
                Ordonnance ordonnance = new Ordonnance(rs.getInt("id_ordonnance"), client, medecin, rs.getDate("date_ordonnance").toLocalDate(), getToutesLesPrescriptionsOrdonnance(rs.getInt("id_ordonnance")));

                // Ajouter l'ordonnance à la liste
                ordonnances.add(ordonnance);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des ordonannces : " + e.getMessage());
        }

        return ordonnances;
    }

    /**
     * Date de création : 03/04/2025
     * Date de dernière modification : 03/04/2025
     *
     * @param idOrdonnance
     *
     * @return List de prescriptionb
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static List<Prescription> getToutesLesPrescriptionsOrdonnance(int idOrdonnance) {
        List<Prescription> prescriptions = new ArrayList<>();

        String sql = "SELECT p.code_cip, m.denomination_medicament, p.quantite, p.posologie FROM Prescription p " +
                "JOIN Medicament m ON p.code_cip = m.code_cip WHERE p.id_ordonnance = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, idOrdonnance);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Medicament medicament = new Medicament(rs.getInt("code_cip"), rs.getString("denomination_medicament"));
                Prescription prescription = new Prescription(medicament, rs.getInt("quantite"), rs.getString("posologie"));
                prescriptions.add(prescription);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des prescriptions : " + e.getMessage());
        }

        return prescriptions;
    }

    /**
     * Date de dernière modification : 03/04/2025
     *
     * <p>Permet d'obtenir toutes les prescriptions d'une ordonnance.</p>
     *
     * @param idOrdonnance L'id de l'ordonnance pour lequel l'on souhaite récupérer ses spécialisations
     *
     * @return List de String
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static List<Prescription> getPrescriptionsOrdonnance(int idOrdonnance) {
        List<Prescription> prescriptions= new ArrayList<>();

        // Requête ChatGPT
        String sql = "SELECT p.id_ordonnance, p.code_cip, p.quantite, p.posologie, m.denomination_medicament, m.prix_medicament FROM Prescription p JOIN Medicament m ON p.code_cip = m.code_cip WHERE p.id_ordonnance = ?;";
        // Fin ChatGPT

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrdonnance);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Récupérer les valeurs de la requête*
                    int idPrec = rs.getInt("id_ordonnance");
                    int codeCip = rs.getInt("code_cip");
                    int quantite = rs.getInt("quantite");
                    String posologie = rs.getString("posologie");
                    String nomMedicament = rs.getString("denomination_medicament");
                    double prix = rs.getDouble("prix_medicament");

                    // Créer un objet Medicament à partir du code CIP
                    Medicament medicament = new Medicament(codeCip, nomMedicament, prix);

                    Prescription prescription = new Prescription(idPrec, medicament, quantite, posologie);
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return prescriptions;
    }

    /**
     *
     *
     * @param ordonnance
     * @param conn
     *
     * @author Hugo VITORINO PEREIRA
     */
    private static void ajouterPrecriptions(Ordonnance ordonnance, Connection conn) {
        String sql = "INSERT INTO Prescription (id_ordonnance, code_cip, quantite, posologie) VALUES (?, ?, ?, ?)";
        // Ajouter chaque spécialité du médecin
        for (Prescription prescription : ordonnance.getElementsPrescription()) {
            if (prescription.getMedicament() == null || prescription.getMedicament().getCodeCIP() <= 0) {
                System.out.println(prescription);
                System.err.println("Erreur : Le médicament n'a pas de code CIP valide.");
                return ;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Associer l'ordonnance
                stmt.setInt(1, ordonnance.getId());
                // Code CIP du médicament
                stmt.setInt(2, prescription.getMedicament().getCodeCIP());
                // Quantité
                stmt.setInt(3, prescription.getQuantite());
                // Posologie
                stmt.setString(4, prescription.getPosologie());

                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Date : 03/04/2025
     *
     * @param ordonnance
     * @return
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean ajouterOrdonnance(Ordonnance ordonnance) {
        // Étape 1 : Insérer l'ordonnance
        String sql = "INSERT INTO Ordonnance (id_client, id_medecin, date_ordonnance) VALUES (?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ordonnance.getClient().getId());
            stmt.setInt(2, ordonnance.getMedecin().getId());
            stmt.setDate(3, java.sql.Date.valueOf(ordonnance.getDateOrdonnance()));

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ordonnance.setId(generatedKeys.getInt(1)); // Récupérer l'ID généré
                    }
                }

                // Étape 2 : Ajouter les prescriptions
                ajouterPrecriptions(ordonnance, conn);

                return true;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }

    public static boolean updateOrdonnance(Ordonnance ordonnance) {
        String updateOrdonnanceSQL = "UPDATE Ordonnance SET id_client = ?, id_medecin = ?, date_ordonnance = ? WHERE id_ordonnance = ?";
        String deletePrescriptionsSQL = "DELETE FROM Prescription WHERE id_ordonnance = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateOrdonnanceSQL);
             PreparedStatement deletePrescriptionsStmt = conn.prepareStatement(deletePrescriptionsSQL)) {

            updateStmt.setInt(1, ordonnance.getClient().getId());
            updateStmt.setInt(2, ordonnance.getMedecin().getId());
            updateStmt.setDate(3, java.sql.Date.valueOf(ordonnance.getDateOrdonnance()));
            updateStmt.setInt(4, ordonnance.getId());

            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Supprimer les anciennes prescriptions
                deletePrescriptionsStmt.setInt(1, ordonnance.getId());
                deletePrescriptionsStmt.executeUpdate();

                // Étape 2 : Ajouter les prescriptions
                ajouterPrecriptions(ordonnance, conn);

                return true;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Date : 29/03/2025
     * Date de dernière modification : 03/04/2025
     *
     * <p>Supprime l'ordonnance de la base de données en utilisant son id.</p>
     *
     * @param idOrdonnance L'id de l'ordonannce que l'on souhaite supprimer de la bdd.
     *
     * @return True si la suppression a bien été effectuée, False sinon.
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean supprimerOrdonnance(int idOrdonnance) {
        String sql = "DELETE FROM Ordonnance WHERE id_ordonnance = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idOrdonnance);

            int rowDelete = stmt.executeUpdate();

            return rowDelete > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'ordonnance : " + e.getMessage());
        }

        return false;
    }

}
