package com.directdash.backend.model;

import jakarta.persistence.Entity;

@Entity
public class Location extends DBObject {
    public Double latitude;
    public Double longitude;

		public Location() {
		}

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
}
