package models;

import java.util.List;

/**
 * Date de création : 17/03/2025
 * Date de dernière modification : 26/03/2025
 *
 * <p>Classe représentant un médecin, acteur secondaire du logiciel.</p>
 *
 * @author Hugo VITORINO PEREIRA
 */
public class Medecin {
    private int id;
    private List<String> specialisations;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;

    public Medecin(List<String> specialisations, String nom, String prenom, String adresse, String telephone) {
        this.specialisations = specialisations;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    // Constructeur avec ID (utilisé lors de la récupération depuis la BDD)
    public Medecin(int id, List<String> specialisations, String nom, String prenom, String adresse, String telephone) {
        this(specialisations, nom, prenom, adresse, telephone);
        this.id = id;
    }
    public Medecin(int id, String nom, String prenom, String adresse, String telephone) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(List<String> specialisations) {
        this.specialisations = specialisations;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getListesSpecialisations() {
        StringBuilder sb = new StringBuilder();
        for(String specialisation : specialisations) {
            sb.append(specialisation).append(" ");
        }
        return sb.toString();
    }

    public void afficher(){
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Medecin{" +
                "id=" + id +
                ", specialisations=" + specialisations +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
