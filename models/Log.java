package models;
import java.time.LocalDateTime;

/**
 * Date de création : 22/04/2025
 * <br>
 * Date de dernière modification : 05/05/2025
 *
 * <p>Classe représentant un log, mesure de sécurité du logiciel.</p>
 *
 * @author Victoria MASSAMBA
 */
public class Log {
    private int idLog;
    private models.Log.LogAction action;
    private LocalDateTime date;
    private int idPersonnel;
    private Integer idConcerne;

    // Enum pour représenter le type log_action
    public enum LogAction {
        AJOUTER_CLIENT("ajouter un client"),
        MODIFIER_CLIENT("modifier un client"),
        SUPPRIMER_CLIENT("supprimer un client"),
        MODIFIER_MOT_DE_PASSE("modifier mot de passe"),
        CONNEXION("connexion"),
        EFFECTUER_COMMANDE("effectuer une commande"),
        EFFECTUER_VENTE_SANS_ORDONNANCE("effectuer une  vente sans ordonnance"),
        EFFECTUER_VENTE_AVEC_ORDONNANCE("effectuer une vente avec ordonnance"),
        AJOUTER_RESERVATION("ajouter une reservation"),
        MODIFIER_RESERVATION("modifier une reservation"),
        SUPPRIMER_RESERVATION("supprimer une reservation"),
        AJOUTER_ORDONNANCE("ajouter une ordonnance"),
        MODIFIER_ORDONNANCE("modifier une ordonnance"),
        SUPPRIMER_ORDONNANCE("supprimer une ordonnance");

        private final String value;

        LogAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static models.Log.LogAction fromValue(String value) {
            for (models.Log.LogAction action : models.Log.LogAction.values()) {
                if (action.value.equals(value)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Valeur du logAction inconnue: " + value);
        }
    }

    public Log() {}

    // Constructeur complet avec idConcerne
    public Log(int idLog, models.Log.LogAction action, LocalDateTime date, int idPersonnel, Integer idConcerne) {
        this.idLog = idLog;
        this.action = action;
        this.date = date;
        this.idPersonnel = idPersonnel;
        this.idConcerne = idConcerne;
    }

    // Constructeur sans idLog mais avec idConcerne
    public Log(models.Log.LogAction action, LocalDateTime date, int idPersonnel, Integer idConcerne) {
        this.action = action;
        this.date = date;
        this.idPersonnel = idPersonnel;
        this.idConcerne = idConcerne;
    }

    // Constructeur sans idConcerne pour les actions qui n'ont pas d'identifiant concerné
    public Log(models.Log.LogAction action, LocalDateTime date, int idPersonnel) {
        this.action = action;
        this.date = date;
        this.idPersonnel = idPersonnel;
        this.idConcerne = null;
    }

    // Constructeur complet sans idConcerne
    public Log(int idLog, models.Log.LogAction action, LocalDateTime date, int idPersonnel) {
        this.idLog = idLog;
        this.action = action;
        this.date = date;
        this.idPersonnel = idPersonnel;
        this.idConcerne = null;
    }

    // Getters et Setters
    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public models.Log.LogAction getAction() {
        return action;
    }

    public void setAction(models.Log.LogAction action) {
        this.action = action;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getIdPersonnel() {
        return idPersonnel;
    }

    public void setIdPersonnel(int idPersonnel) {
        this.idPersonnel = idPersonnel;
    }

    // Getter et Setter pour idConcerne
    public Integer getIdConcerne() {
        return idConcerne;
    }

    public void setIdConcerne(Integer idConcerne) {
        this.idConcerne = idConcerne;
    }

    @Override
    public String toString() {
        return "Log{" +
                "idLog=" + idLog +
                ", action='" + action.getValue() + '\'' +
                ", date=" + date +
                ", idPersonnel=" + idPersonnel +
                ", idConcerne=" + (idConcerne != null ? idConcerne : "N/A") +
                '}';
    }
}