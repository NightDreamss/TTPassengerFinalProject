package com.nightdream.ttpassenger;

import androidx.annotation.NonNull;

public class requestGetterSetter {
    String clat, clng, dlat, dlng, cLocation, dLocation, passsengerId, driverId, status, driverLat, driverLng;

    public requestGetterSetter() {
    }

    @NonNull
    @Override
    public String toString() {
        return "requestGetterSetter{" +
                "clat='" + clat + '\'' +
                ", clng='" + clng + '\'' +
                ", dlat='" + dlat + '\'' +
                ", dlng='" + dlng + '\'' +
                ", cLocation='" + cLocation + '\'' +
                ", dLocation='" + dLocation + '\'' +
                ", passsengerId='" + passsengerId + '\'' +
                ", driverId='" + driverId + '\'' +
                ", status='" + status + '\'' +
                ", driverLat='" + driverLat + '\'' +
                ", driverLng='" + driverLng + '\'' +
                '}';
    }

    public String getPasssengerId() {
        return passsengerId;
    }

    public void setPasssengerId(String passsengerId) {
        this.passsengerId = passsengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriverLat() {
        return driverLat;
    }

    public void setDriverLat(String driverLat) {
        this.driverLat = driverLat;
    }

    public String getDriverLng() {
        return driverLng;
    }

    public void setDriverLng(String driverLng) {
        this.driverLng = driverLng;
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
