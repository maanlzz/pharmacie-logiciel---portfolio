package models;

/**
 * Classe gérant le panier d'une vente sans ordonnance
 */

public class PanierVente {
    private int idVente;
    private int code_cip;
    private int quantite;

    public PanierVente(int idVente, int code_cip, int quantite) {
        this.idVente = idVente;
        this.code_cip = code_cip;
        this.quantite = quantite;
    }

    public PanierVente() {}
    public int getIdVente() {
        return idVente;
    }
    public int getCode_cip() {
        return code_cip;
    }
    public int getQuantite() {
        return quantite;
    }
    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }
    public void setCode_cip(int code_cip) {
        this.code_cip = code_cip;
    }
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

}
