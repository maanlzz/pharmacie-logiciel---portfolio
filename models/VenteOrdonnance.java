package models;

import java.sql.Timestamp;

/**
 * Représente une vente avec ordonnance dans la base de données.
 * @author Victoria MASSAMBA
 * Date de création : 17/04/2025
 * Date de dernière modification : 19/04/2025
 */
public class VenteOrdonnance {
    private int idVente;
    private int idClient;
    //private int idOrdonnance;
    private double montantTotal;
    private Timestamp dateVente;



    // Getters et Setters
    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public Timestamp getDateVente() {
        return dateVente;
    }

    public void setDateVente(Timestamp dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
    public int getIdClient() {
        return idClient;
    }
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }
//    public int getIdOrdonnance() {
//        return idOrdonnance;
//    }
//    public void setIdOrdonnance(int idOrdonnance) {
//        this.idOrdonnance = idOrdonnance;
//    }



}
