package ch.so.agi.parcelinfoservice;

import org.locationtech.jts.geom.Geometry;

public class Grundstueck {
    private String egrid;
    private String nummer;
    private String nbident;
    private String art;
    private String gbSubKreis;
    private double flaechenmass;
    private Geometry geometrie;
    
    public String getEgrid() {
        return egrid;
    }
    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }
    public String getNummer() {
        return nummer;
    }
    public void setNummer(String nummer) {
        this.nummer = nummer;
    }
    public String getNbident() {
        return nbident;
    }
    public void setNbident(String nbident) {
        this.nbident = nbident;
    }
    public String getArt() {
        return art;
    }
    public void setArt(String art) {
        this.art = art;
    }
    public String getGbSubKreis() {
        return gbSubKreis;
    }
    public void setGbSubKreis(String gbSubKreis) {
        this.gbSubKreis = gbSubKreis;
    }
    public double getFlaechenmass() {
        return flaechenmass;
    }
    public void setFlaechenmass(double flaechenmass) {
        this.flaechenmass = flaechenmass;
    }
    public Geometry getGeometrie() {
        return geometrie;
    }
    public void setGeometrie(Geometry geometrie) {
        this.geometrie = geometrie;
    }
}
