package com.cropadvisor.service;

import com.cropadvisor.entity.Farmer;
import com.cropadvisor.entity.Region;
import com.cropadvisor.exception.BusinessException;
import com.cropadvisor.repository.FarmerRepository;
import com.cropadvisor.repository.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final RegionRepository regionRepository;

    public FarmerService(FarmerRepository farmerRepository, RegionRepository regionRepository) {
        this.farmerRepository = farmerRepository;
        this.regionRepository = regionRepository;
    }

    public Farmer createFarmer(Farmer farmer, Long regionId) {
        farmer.setRegion(getRegion(regionId));
        return farmerRepository.save(farmer);
    }

    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }

    public Farmer getFarmerById(Long farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new BusinessException("Farmer not found: " + farmerId));
    }

    public Farmer updateFarmer(Long farmerId, Farmer updatedFarmer, Long regionId) {
        Farmer farmer = getFarmerById(farmerId);
        farmer.setName(updatedFarmer.getName());
        farmer.setPhone(updatedFarmer.getPhone());
        farmer.setEmail(updatedFarmer.getEmail());
        farmer.setAddress(updatedFarmer.getAddress());
        farmer.setRegion(getRegion(regionId));
        return farmerRepository.save(farmer);
    }

    public void deleteFarmer(Long farmerId) {
        farmerRepository.delete(getFarmerById(farmerId));
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException("Region not found: " + regionId));
    }
}