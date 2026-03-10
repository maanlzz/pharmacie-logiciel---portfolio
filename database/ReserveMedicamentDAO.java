package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.ReserveMedicament;
import utils.UtilAlert;

/**
 * Date de dernière modification : 09/04/2025
 *
 * <p>Cette classe permet de gérer les opérations en lien avec la table ReserveMedicament
 * de la base de données postgre</p>
 *
 * @author Victoria MASSAMBA
 */
public class ReserveMedicamentDAO {


    public static void addReservation(Connection connection,int idReservation, int codeCip, int quantite) throws SQLException {
        String sql = "INSERT INTO reservemedicament (id_reservation, code_cip, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, idReservation);
            stmt.setInt(2, codeCip);
            stmt.setInt(3, quantite);
            stmt.executeUpdate();
        }


    }


    public static void deleteReservation(Connection conn, int idReservation) throws SQLException {
        String sql = "DELETE FROM reservemedicament WHERE id_reservation = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql))  {
            stmt.setInt(1, idReservation);
            stmt.executeUpdate();
        }
    }

    public static List<Integer> getMedicamentCodesByReservationId(int idReservation) throws SQLException {
        String query = "SELECT code_cip FROM reservemedicament WHERE id_reservation = ?";
        List<Integer> medicamentCodes = new ArrayList<>();
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idReservation);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicamentCodes.add(resultSet.getInt("code_cip"));
                }
            }
        }
        return medicamentCodes;
    }


}
