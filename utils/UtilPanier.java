package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import models.Medicament;

/**
 * Classe utilitaire permettant la gestion de paniers d'achats sur le logiciel
 *
 * @author Victoria MASSAMBA
 * Date de 1ère modification : 14/03/24
 * Date de dernière modification : 14/03/24
 */

public class UtilPanier {

    /**
     * Méthode qui ajoute un médicament au panier
     * @author Victoria MASSAMBA
     * @param medicament Le medicament à ajouter
     * @param panier Liste observable du panier contenant les médicaments
     * Date de 1ère modification : 14/03/24
     * Date de dernière modification : 14/03/24
     */
    public static void ajouterAuPanier(ObservableList<Medicament> panier, Medicament medicament) {
        if(medicament!=null){
            for (Medicament article : panier) {
                if (article.getNom().equals(medicament.getNom())) {
                    // Le médicament est déjà dans le panier, on ne fait rien et on empêche d'ajouter à nouveau
                    return;
                }
            }
            panier.add(medicament);
        }



    }

    /**
     * Méthode qui met à jour le total du montant du panier
     * @author Victoria MASSAMBA
     * @param panier Liste observable du panier contenant les médicaments
     * @param totalLabel Label de la variable du total d'achat
     * Date de 1ère modification : 14/03/24
     * Date de dernière modification : 16/03/24
     */
    public static void mettreAJourTotal(Label totalLabel,ObservableList<Medicament> panier) {
        double total = 0;
        for (Medicament article : panier) {
            total += article.getPrix()*article.getQuantite();
        }

        totalLabel.setText("Total : " + String.format("%.2f", total) + "€");
    }

    /**
     * Méthode qui filtre la recherche de médicaments selon le nom ou la molécule.
     * @author Victoria MASSAMBA
     * @param medicaments une liste avec des medicaments
     * @param listeMedicaments la listeView des medicaments
     * @param champRecherche le champ de recherche des médicaments
     * @param critereComboBox le comboBox des critères de recherche de médicaments
     */

    public static void filtrerMedicaments(ObservableList<Medicament> medicaments, ComboBox<String> critereComboBox, ListView<Medicament> listeMedicaments, TextField champRecherche) {
        String critere = critereComboBox.getValue();
        String recherche = champRecherche.getText().toLowerCase();

        ObservableList<Medicament> medicamentsFiltres = FXCollections.observableArrayList();

        for (Medicament medicament : medicaments) {
            if (critere.equals("Nom") && medicament.getNom().toLowerCase().contains(recherche)) {
                medicamentsFiltres.add(medicament);
            } else if (critere.equals("Molecule") && medicament.getDose().toLowerCase().contains(recherche)) {
                medicamentsFiltres.add(medicament);
            }
        }

        listeMedicaments.setItems(medicamentsFiltres);
    }
}
