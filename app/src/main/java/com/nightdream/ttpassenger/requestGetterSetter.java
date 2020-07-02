package com.nightdream.ttpassenger;

public class requestGetterSetter {
    String clat, clng, dlat, dlng, cLocation, dLocation;

    public requestGetterSetter() {
    }

    @Override
    public String toString() {
        return "requestGetterSetter{" +
                "clat='" + clat + '\'' +
                ", clng='" + clng + '\'' +
                ", dlat='" + dlat + '\'' +
                ", dlng='" + dlng + '\'' +
                ", cLocation='" + cLocation + '\'' +
                ", dLocation='" + dLocation + '\'' +
                '}';
    }

    public String getClat() {
        return clat;
    }

    public String getClng() {
        return clng;
    }

    public String getDlat() {
        return dlat;
    }

    public String getDlng() {
        return dlng;
    }

    public String getcLocation() { return cLocation; }

    public String getdLocation() { return dLocation; }

    public void setClat(String clat) {
        this.clat = clat;
    }

    public void setClng(String clng) {
        this.clng = clng;
    }

    public void setDlat(String dlat) {
        this.dlat = dlat;
    }

    public void setDlng(String dlng) {
        this.dlng = dlng;
    }

    public void setcLocation(String cLocation) { this.cLocation = cLocation; }

    public void setdLocation(String dLocation) { this.dLocation = dLocation; }
}
