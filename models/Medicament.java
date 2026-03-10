package models;

/**
 * Cette classe permet de définir l'ensemble des attributs d'un médicament utilisés
 * @author Nicolas ADAMCZYK
 * Date de création : 27/02/2025
 * Date de dernière modification : 06/04/2025
 */
public class Medicament {

    private int codeCIP;
    private String denominationMedicament;
    private String formePharmaceutique;
    private String voiesAdministration;
    private String etatCommercialisation;
    private String libellePresentation;
    private String tauxRemboursement;
    private double prixMedicament;
    private String indicationRemboursement;
    private String designationElementPharmaceutique;
    private String denominationSubstance;
    private String dosageSubstance;
    private String conditionDelivrance;
    private String libelleStatut;
    private String dateDebutStatut;
    private int quantite;
    private int seuilRecommande;
    private boolean necessiteOrdonnance; // Nouvel attribut

    /**
     * Date : 03/04/2025
     *
     * @author Hugo VITORINO PEREIRA
     */
    public Medicament(int codeCIP, String denominationMedicament, String formePharmaceutique, String voiesAdministration, String etatCommercialisation, String libellePresentation, String tauxRemboursement, double prixMedicament, String indicationRemboursement, String designationElementPharmaceutique, String denominationSubstance, String dosageSubstance, String conditionDelivrance, String libelleStatut, String dateDebutStatut, int quantite, int seuilRecommande, boolean necessiteOrdonnance) {
        this.codeCIP = codeCIP;
        this.denominationMedicament = denominationMedicament;
        this.formePharmaceutique = formePharmaceutique;
        this.voiesAdministration = voiesAdministration;
        this.etatCommercialisation = etatCommercialisation;
        this.libellePresentation = libellePresentation;
        this.tauxRemboursement = tauxRemboursement;
        this.prixMedicament = prixMedicament;
        this.indicationRemboursement = indicationRemboursement;
        this.designationElementPharmaceutique = designationElementPharmaceutique;
        this.denominationSubstance = denominationSubstance;
        this.dosageSubstance = dosageSubstance;
        this.conditionDelivrance = conditionDelivrance;
        this.libelleStatut = libelleStatut;
        this.dateDebutStatut = dateDebutStatut;
        this.quantite = quantite;
        this.seuilRecommande = seuilRecommande;
        this.necessiteOrdonnance = necessiteOrdonnance;
    }

    public Medicament(int codeCIP, String denominationMedicament) {
        this.codeCIP = codeCIP;
        this.denominationMedicament = denominationMedicament;
    }

    public Medicament(int codeCIP, String denominationMedicament, double prix) {
        this.codeCIP = codeCIP;
        this.denominationMedicament = denominationMedicament;
        this.prixMedicament = prix;
    }

    public Medicament() {
    }

    // Getters and Setters

    public int getCodeCIP() {
        return codeCIP;
    }

    public void setCodeCIP(int codeCIP) {
        this.codeCIP = codeCIP;
    }

    public String getDenominationMedicament() {
        return denominationMedicament;
    }

    public void setDenominationMedicament(String denominationMedicament) {
        this.denominationMedicament = denominationMedicament;
    }

    public String getFormePharmaceutique() {
        return formePharmaceutique;
    }

    public void setFormePharmaceutique(String formePharmaceutique) {
        this.formePharmaceutique = formePharmaceutique;
    }

    public String getVoiesAdministration() {
        return voiesAdministration;
    }

    public void setVoiesAdministration(String voiesAdministration) {
        this.voiesAdministration = voiesAdministration;
    }

    public String getEtatCommercialisation() {
        return etatCommercialisation;
    }

    public void setEtatCommercialisation(String etatCommercialisation) {
        this.etatCommercialisation = etatCommercialisation;
    }

    public String getLibellePresentation() {
        return libellePresentation;
    }

    public void setLibellePresentation(String libellePresentation) {
        this.libellePresentation = libellePresentation;
    }

    public String getTauxRemboursement() {
        return tauxRemboursement;
    }

    public void setTauxRemboursement(String tauxRemboursement) {
        this.tauxRemboursement = tauxRemboursement;
    }

    public double getPrixMedicament() {
        return prixMedicament;
    }

    public void setPrixMedicament(double prixMedicament) {
        this.prixMedicament = prixMedicament;
    }

    public String getIndicationRemboursement() {
        return indicationRemboursement;
    }

    public void setIndicationRemboursement(String indicationRemboursement) {
        this.indicationRemboursement = indicationRemboursement;
    }

    public String getDesignationElementPharmaceutique() {
        return designationElementPharmaceutique;
    }

    public void setDesignationElementPharmaceutique(String designationElementPharmaceutique) {
        this.designationElementPharmaceutique = designationElementPharmaceutique;
    }

    public String getDenominationSubstance() {
        return denominationSubstance;
    }

    public void setDenominationSubstance(String denominationSubstance) {
        this.denominationSubstance = denominationSubstance;
    }

    public String getDosageSubstance() {
        return dosageSubstance;
    }

    public void setDosageSubstance(String dosageSubstance) {
        this.dosageSubstance = dosageSubstance;
    }

    public String getConditionDelivrance() {
        return conditionDelivrance;
    }

    public void setConditionDelivrance(String conditionDelivrance) {
        this.conditionDelivrance = conditionDelivrance;
    }

    public String getLibelleStatut() {
        return libelleStatut;
    }

    public void setLibelleStatut(String libelleStatut) {
        this.libelleStatut = libelleStatut;
    }

    public String getDateDebutStatut() {
        return dateDebutStatut;
    }

    public void setDateDebutStatut(String dateDebutStatut) {
        this.dateDebutStatut = dateDebutStatut;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getSeuilRecommande() {
        return seuilRecommande;
    }

    public void setSeuilRecommande(int seuilRecommande) {
        this.seuilRecommande = seuilRecommande;
    }

    public boolean isNecessiteOrdonnance() {
        return necessiteOrdonnance;
    }

    public void setNecessiteOrdonnance(boolean necessiteOrdonnance) {
        this.necessiteOrdonnance = necessiteOrdonnance;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "codeCIP=" + codeCIP +
                ", denominationMedicament='" + denominationMedicament + '\'' +
                ", formePharmaceutique='" + formePharmaceutique + '\'' +
                ", voiesAdministration='" + voiesAdministration + '\'' +
                ", etatCommercialisation='" + etatCommercialisation + '\'' +
                ", libellePresentation='" + libellePresentation + '\'' +
                ", tauxRemboursement='" + tauxRemboursement + '\'' +
                ", prixMedicament=" + prixMedicament +
                ", indicationRemboursement='" + indicationRemboursement + '\'' +
                ", designationElementPharmaceutique='" + designationElementPharmaceutique + '\'' +
                ", denominationSubstance='" + denominationSubstance + '\'' +
                ", dosageSubstance='" + dosageSubstance + '\'' +
                ", conditionDelivrance='" + conditionDelivrance + '\'' +
                ", libelleStatut='" + libelleStatut + '\'' +
                ", dateDebutStatut='" + dateDebutStatut + '\'' +
                ", quantite=" + quantite +
                ", seuilRecommande=" + seuilRecommande +
                ", necessiteOrdonnance=" + necessiteOrdonnance +
                '}';
    }
}
