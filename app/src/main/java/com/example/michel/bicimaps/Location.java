package com.example.michel.bicimaps;

/**
 * Created by Michel on 21/04/2018.
 */

//Clase Location para contener los datos de localización que guardaremos en FireBase

public class Location {
    private double lat;
    private double lon;
    private int pm;
    private String dh;

    public Location(){

    }

    public Location(double lat, double lon, int pm, String dh)
    {
        this.lat = lat;
        this.lon = lon;
        this.pm  = pm;
        this.dh = dh;
    }

    public double getLat(){
        return lat;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public double getLon(){ return lon;}

    public void setLon(double lon){
                this.lon = lon;
    }

    public int getPm() { return pm;}

    public void setPm(int pm) { this.pm = pm; }

    public String getDh() { return dh;}

    public void setDh(String dh) {this.dh = dh;}


    @Override
    public String toString() {
        return "Localizacion{" +
                ", latitud='" + lat + '\'' +
                ", longitud=" + lon +
                '}';
    }


}

