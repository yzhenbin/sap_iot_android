package com.example.sapiot.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {

    @SerializedName("capabilityAlternateId")
    @Expose
    private String capabilityAlternateId;
    @SerializedName("sensorAlternateId")
    @Expose
    private String sensorAlternateId;
    @SerializedName("measures")
    @Expose
    private List<Measure> measures = null;

    public String getCapabilityAlternateId() {
        return capabilityAlternateId;
    }

    public void setCapabilityAlternateId(String capabilityAlternateId) {
        this.capabilityAlternateId = capabilityAlternateId;
    }

    public String getSensorAlternateId() {
        return sensorAlternateId;
    }

    public void setSensorAlternateId(String sensorAlternateId) {
        this.sensorAlternateId = sensorAlternateId;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }
}
