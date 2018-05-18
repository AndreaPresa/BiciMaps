package com.example.michel.bicimaps;

/**
 * Created by Michel on 21/04/2018.
 */

//Clase Location para contener los datos de localización que guardaremos en FireBase

public class Location {
    private double lat;
    private double lon;

    public Location(){

    }

    public Location(  double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
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

    @Override
    public String toString() {
        return "Localizacion{" +
                ", latitud='" + lat + '\'' +
                ", longitud=" + lon +
                '}';
    }


}

