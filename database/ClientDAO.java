package database;

import models.Client;
import models.Medecin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Cette classe permet la gestion des clients dans la base de données.
 *
 * @author Hugo VITORINO PEREIRA
 */
public class ClientDAO {

    /**
     * Date de création : 28/03/2025
     * Date de dernière modification : 05/04/2025
     *
     * <p>Récupère la liste de tous les clients présents dans la base de données.</p>
     *
     * @return List<Client>
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static List<Client> getTousLesClients() {
        List<Client> clients = new ArrayList<>();
        // Requête fournie par ChatGPT
        String sql = "SELECT c.id_client, c.nom, c.prenom, c.sexe, c.date_naissance, c.adresse, c.telephone, c.email, c.mutuelle, c.numero_securite_sociale, " +
                "m.id_medecin AS id_medecin, m.nom AS nom_medecin, m.prenom AS prenom_medecin, m.telephone AS telephone_medecin, m.adresse AS adresse_medecin " +
                "FROM Client c " +
                "LEFT JOIN Medecin m ON c.id_medecin = m.id_medecin";
        // Fin ChatGPT

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Récupérer les informations du client
                int clientId = rs.getInt("id_client");
                String clientNom = rs.getString("nom");
                String clientPrenom = rs.getString("prenom");
                String clientSexe = rs.getString("sexe");
                LocalDate clientDateNaissance = rs.getDate("date_naissance").toLocalDate();
                String clientAdresse = rs.getString("adresse");
                String clientTelephone = rs.getString("telephone");
                String clientEmail = rs.getString("email");
                String clientMutuelle = rs.getString("mutuelle");
                String clientNumeroSecuriteSociale = rs.getString("numero_securite_sociale");

                // Récupérer les informations du médecin
                int medecinId = rs.getInt("id_medecin");
                String medecinNom = rs.getString("nom_medecin");
                String medecinPrenom = rs.getString("prenom_medecin");
                String medecinTelephone = rs.getString("telephone_medecin");
                String medecinAdresse = rs.getString("adresse_medecin");

                // Créer un objet Medecin avec ses informations et ses spécialités
                Medecin medecinTraitant = new Medecin(medecinId, MedecinDAO.getSpecialisationsMedecin(medecinId), medecinNom, medecinPrenom, medecinAdresse, medecinTelephone);

                // Créer un client et l'ajouter à la liste
                Client client = new Client(clientId, clientNom, clientPrenom, clientSexe, clientDateNaissance, clientAdresse, clientTelephone, clientEmail, clientMutuelle, clientNumeroSecuriteSociale, medecinTraitant);
                clients.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des clients : " + e.getMessage());
        }

        return clients;
    }


    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 10/04/2025
     *
     * <p>Vérifie si le client passé en paramètre existe dans la bdd.</p>
     *
     * @param client Le client pour lequel on souhaite vérifier sa présence dans la bdd.
     *
     * @return True si le client est présent dans la bdd, False sinon
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean clientExiste(Client client) {
        String sql = "SELECT id_client FROM Client WHERE numero_securite_sociale = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getNumeroSecuriteSociale());

            try (ResultSet rs = stmt.executeQuery()) {
                // Si on ne rentre pas dans le if cela signifie que le client est unique (par rapport au NIR)
                if (rs.next()) {
                    // Récupérer l'ID du client depuis le résultat de la requête
                    int idClient = rs.getInt(1);
                    // Vérifier si l'ID du client trouvé est différent de celui du client actuel
                    // Dans le cas d'un ajout, client.getId() == -1 (car on la spécifié dans le code), donc forcément la méthode retournera true;
                    // D'une certaine façon, pour l'ajout une fois que l'on passe la condition on sait que le client existe déjà
                    return  idClient != client.getId();
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Ajoute dans la base de données le client passé en paramètre.</p>
     *
     * @param client Le client que l'on souhaite ajouter dans la bdd.
     *
     * @return True si le client a bien été ajouté, False sinon
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean ajouterClient(Client client) {
        String sql = "INSERT INTO Client (nom, prenom, sexe, date_naissance, adresse, telephone, email, mutuelle, numero_securite_sociale, id_medecin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getSexe());
            stmt.setDate(4, java.sql.Date.valueOf(client.getDateNaissance()));
            stmt.setString(5, client.getAdresse());
            stmt.setString(6, client.getTelephone());
            stmt.setString(7, client.getEmail());
            stmt.setString(8, client.getMutuelle());
            stmt.setString(9, client.getNumeroSecuriteSociale());
            stmt.setInt(10, client.getMedecinTraitant().getId());

            int rowsInserted = stmt.executeUpdate();

            // Récupérer l'ID généré automatiquement (optionnel)
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        client.setId(generatedKeys.getInt(1));
                    }
                }
            }

            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 29/03/2025
     *
     * <p>Modifie dans la base de données le client passé en paramètre.</p>
     *
     * @param client Le client pour lequel on souhaite modifier ses informations
     *
     * @return True si la modification a bien été effectuée, False sinon.
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean updateClient(Client client) {
        String sql = "UPDATE Client SET nom = ?, prenom = ?, sexe = ?, date_naissance = ?, adresse = ?, telephone = ?, email = ?, mutuelle = ?, numero_securite_sociale = ?, id_medecin = ? " + "WHERE id_client = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getSexe());
            stmt.setDate(4, java.sql.Date.valueOf(client.getDateNaissance()));
            stmt.setString(5, client.getAdresse());
            stmt.setString(6, client.getTelephone());
            stmt.setString(7, client.getEmail());
            stmt.setString(8, client.getMutuelle());
            stmt.setString(9, client.getNumeroSecuriteSociale());

            stmt.setInt(10, client.getMedecinTraitant().getId());

            stmt.setInt(11, client.getId());

            // Exécuter la mise à jour du client
            int rowsUpdated = stmt.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du client : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Date de création : 29/03/2025
     * Date de dernière modification : 02/04/2025
     *
     * <p>Supprime le client de la base de données en utilisant son id.</p>
     *
     * @param idClient L'id du client que l'on souhaite supprimer de la bdd.
     *
     * @return True si la suppression a bien été effectuée, False sinon.
     *
     * @author Hugo VITORINO PEREIRA
     */
    public static boolean supprimerClient(int idClient) {
        String sql = "DELETE FROM Client WHERE id_client = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClient);

            int rowDelete = stmt.executeUpdate();

            return rowDelete > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client : " + e.getMessage());
        }

        return false;
    }

    /**
     *
     * @param critere
     * @param valeur
     * @return
     *
     * @author Nicolas ADAMCZYK
     */
    public static List<Client> rechercherClientsParTexte(String critere, String valeur) {
        List<Client> clients = new ArrayList<>();

        if (valeur == null || valeur.trim().isEmpty()) {
            return getTousLesClients(); // On retourne tous les clients si aucun filtre
        }

        String sql = null;
        switch (critere) {
            case "Nom":
                sql = "SELECT * FROM Client WHERE LOWER(nom) LIKE LOWER(?)";
                break;
            case "Prenom":
                sql = "SELECT * FROM Client WHERE LOWER(prenom) LIKE LOWER(?)";
                break;
            case "N°sécurité sociale":
                sql = "SELECT * FROM Client WHERE numero_securite_sociale LIKE ?";
                break;
            default:
                return clients;
        }

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + valeur + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("id_client"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("sexe"),
                        rs.getDate("date_naissance").toLocalDate(),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("mutuelle"),
                        rs.getString("numero_securite_sociale"),
                        null // Pas besoin de médecin ici pour une recherche simple
                );
                clients.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    /**
     * Date de création : 27/04/25
     * <p>Date de dernière modification : 27/04/24</p>
     * <p>Récupère le client depuis la bdd par son identifiant</p>
     * @param idClient l'identifiant du client
     * @return le client (et son médecin traitant)
     * @throws SQLException
     * @author Victoria MASSAMBA
     */

    public static Client getClientById(int idClient) throws SQLException{
        String sql = "SELECT c.*, m.* FROM Client c JOIN Medecin m ON c.id_medecin=m.id_medecin WHERE id_client = ?";
        Client client = null;
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idClient);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    client = new Client(rs.getInt("id_client"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("sexe"),
                            rs.getDate("date_naissance").toLocalDate(),
                            rs.getString("adresse"),
                            rs.getString("telephone"),
                            rs.getString("email"),
                            rs.getString("mutuelle"),
                            rs.getString("numero_securite_sociale"),
                            new Medecin(rs.getInt("id_medecin"),rs.getString(13),rs.getString(14), rs.getString(15), rs.getString(16))

                    );
                }
            }
        }
        return client;



    }

}
