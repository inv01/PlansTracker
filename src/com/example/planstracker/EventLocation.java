package com.example.planstracker;

public class EventLocation{

    private double loc_longitude;
    private double loc_latitude;
    private String loc_str;
    
    public EventLocation (double loc_longitude,
            double loc_latitude,String loc_str){
        this.loc_latitude = loc_latitude;
        this.loc_longitude = loc_longitude;
        this.loc_str = loc_str;
    }
    
    public double getLoc_longitude() {
        return loc_longitude;
    }
    public void setLoc_longitude(double loc_longitude) {
        this.loc_longitude = loc_longitude;
    }
    public double getLoc_latitude() {
        return loc_latitude;
    }
    public void setLoc_latitude(double loc_latitude) {
        this.loc_latitude = loc_latitude;
    }
    public String getLoc_str() {
        return (loc_str != null) ? loc_str : "";
    }
    public void setLoc_str(String loc_str) {
        this.loc_str = loc_str;
    }
    
}
