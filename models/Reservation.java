package models;

import java.sql.Date;

public class Reservation {
    private int idReservation;
    private Date dateReservation;
    private int idClient;

    public Reservation() {
    }

    public Reservation(int idReservation, Date dateReservation, int idClient) {
        this.idReservation = idReservation;
        this.dateReservation = dateReservation;
        this.idClient = idClient;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", dateReservation=" + dateReservation +
                ", idClient=" + idClient +
                '}';
    }
}