package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author Victoria MASSAMBA
 * Date de dernière modification : 12/04/2025
 */

public class ReservationItem {
    private Medicament medicament;
    private IntegerProperty quantite; // valeur qu'on peut modifier avec un Observable

    public ReservationItem(Medicament medicament, int initialQuantity) {
        this.medicament = medicament;
        this.quantite = new SimpleIntegerProperty(initialQuantity);
    }

    public Medicament getMedicament() {
        return medicament;
    }

    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
    }


    public int getQuantite() {
        return quantite.get();
    }


    public void setQuantite(int value) {
        this.quantite.set(value);
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }
}
