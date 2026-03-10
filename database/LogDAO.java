package database;

import models.Log;
import models.Log.LogAction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    /**
     * Insère un log dans la base de données.
     *
     * @param log L'objet Log à insérer.
     * @return true si l'insertion s'est déroulée correctement, false sinon.
     */
    public static boolean ajouterLog(Log log) {

        String sql = "INSERT INTO public.log (action, date, id_personnel, id_concerne) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, log.getAction().getValue(), Types.OTHER);
            stmt.setTimestamp(2, Timestamp.valueOf(log.getDate()));
            stmt.setInt(3, log.getIdPersonnel());

            // Gestion de la valeur null pour id_concerne
            if (log.getIdConcerne() != null) {
                stmt.setInt(4, log.getIdConcerne());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setIdLog(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupère tous les logs.
     *
     * @return une liste de Log.
     */
    public static List<Log> getAllLogs() {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT id_log, action, date, id_personnel, id_concerne FROM public.log ORDER BY date DESC";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idLog = rs.getInt("id_log");
                String actionStr = rs.getString("action");
                LogAction action = LogAction.fromValue(actionStr);
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                int idPersonnel = rs.getInt("id_personnel");


                Integer idConcerne = rs.getInt("id_concerne");
                if (rs.wasNull()) {
                    idConcerne = null;
                }

                logs.add(new Log(idLog, action, date, idPersonnel, idConcerne));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Récupère les logs d'un personnel spécifique.
     *
     * @param idPersonnel L'identifiant du personnel.
     * @return une liste de Log pour ce personnel.
     */
    public static List<Log> getLogsByPersonnel(int idPersonnel) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT id_log, action, date, id_personnel, id_concerne FROM public.log " +
                "WHERE id_personnel = ? ORDER BY date DESC";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPersonnel);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idLog = rs.getInt("id_log");
                    String actionStr = rs.getString("action");
                    LogAction action = LogAction.fromValue(actionStr);
                    LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();


                    Integer idConcerne = rs.getInt("id_concerne");
                    if (rs.wasNull()) {
                        idConcerne = null;
                    }

                    logs.add(new Log(idLog, action, date, idPersonnel, idConcerne));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }


    /**
     * Récupère les logs pour une période spécifique.
     *
     * @param debut Date de début de la période.
     * @param fin Date de fin de la période.
     * @return une liste de Log pour cette période.
     */
    public static List<Log> getLogsByPeriod(LocalDateTime debut, LocalDateTime fin) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT id_log, action, date, id_personnel, id_concerne FROM public.log " +
                "WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idLog = rs.getInt("id_log");
                    String actionStr = rs.getString("action");
                    LogAction action = LogAction.fromValue(actionStr);
                    LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                    int idPersonnel = rs.getInt("id_personnel");


                    Integer idConcerne = rs.getInt("id_concerne");
                    if (rs.wasNull()) {
                        idConcerne = null;
                    }

                    logs.add(new Log(idLog, action, date, idPersonnel, idConcerne));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }


}