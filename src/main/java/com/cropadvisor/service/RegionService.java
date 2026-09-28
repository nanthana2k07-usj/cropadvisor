package com.cropadvisor.service;

import com.cropadvisor.entity.Region;
import com.cropadvisor.exception.BusinessException;
import com.cropadvisor.repository.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public Region createRegion(Region region) {
        return regionRepository.save(region);
    }

    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }

    public Region getRegionById(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException("Region not found: " + regionId));
    }

    public Region updateRegion(Long regionId, Region updatedRegion) {
        Region region = getRegionById(regionId);
        region.setName(updatedRegion.getName());
        region.setDistrict(updatedRegion.getDistrict());
        region.setState(updatedRegion.getState());
        return regionRepository.save(region);
    }

    public void deleteRegion(Long regionId) {
        Region region = getRegionById(regionId);
        regionRepository.delete(region);
    }
}