package models;

import java.sql.Timestamp;

/**
 * Représente une vente dans la base de données.
 * @author Nicolas ADAMCZYK
 * @author Victoria MASSAMBA
 * Date de création : 19/03/2025
 * Date de dernière modification : 17/04/2025
 */
public class Vente {
    private int idVente;
    private Timestamp dateVente;
    private double montantTotal;


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


}
