package models;

public class PanierVenteOrdonnance {
    private int idVenteOrdonnance;
    private int code_cip;
    private int quantite;
    private Integer idOrdonnance;
    //private int idClient;
    public PanierVenteOrdonnance(int id, int codeCip, int quantite, Integer idOrdonnance) {
        this.idVenteOrdonnance = id;
        this.code_cip = codeCip;
        this.quantite = quantite;
        this.idOrdonnance = idOrdonnance;
        //this.idClient = idClient;

    }
    public PanierVenteOrdonnance(){};
    public int getIdVenteOrdonnance() {
        return idVenteOrdonnance;
    }
    public void setIdVenteOrdonnance(int idVenteOrdonnance) {
        this.idVenteOrdonnance = idVenteOrdonnance;
    }
    public int getCodeCip() {
        return code_cip;
    }
    public void setCodeCip(int codeCip) {
        this.code_cip = codeCip;
    }
    public int getQuantite() {
        return quantite;
    }
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    public Integer getIdOrdonnance() {
        return idOrdonnance;
    }
    public void setIdOrdonnance(Integer idOrdonnance) {
        this.idOrdonnance = idOrdonnance;
    }
//    public int getIdClient() {
//        return idClient;
//    }
//    public void setIdClient(int idClient) {
//        this.idClient = idClient;
//    }
}
