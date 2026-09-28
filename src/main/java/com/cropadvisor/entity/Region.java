package com.cropadvisor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regionId;

    @NotBlank(message = "Region name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "District is required")
    @Column(nullable = false)
    private String district;

    @NotBlank(message = "State is required")
    @Column(nullable = false)
    private String state;

    public Region() {
    }

    public Region(String name, String district, String state) {
        this.name = name;
        this.district = district;
        this.state = state;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}