package models;

/**
 * Classe qui relie Reservation à Medicament
 * @author Victoria MASSAMBA
 * Date de dernière modification : 12/04/2025
 */
public class ReserveMedicament {
    private int idReservation;
    private int codeCip;
    private int quantite;


    public ReserveMedicament(int idReservation, int codeCip, int quantite) {
        this.idReservation = idReservation;
        this.codeCip = codeCip;
        this.quantite = quantite;
    }


    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getCodeCip() {
        return codeCip;
    }

    public void setCodeCip(int codeCip) {
        this.codeCip = codeCip;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }


    @Override
    public String toString() {
        return "ReserveMedicament{" +
                "idReservation=" + idReservation +
                ", codeCip=" + codeCip +
                ", quantite=" + quantite +
                '}';
    }
}
