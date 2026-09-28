package com.cropadvisor.repository;

import com.cropadvisor.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    List<Farmer> findByRegionRegionId(Long regionId);
}