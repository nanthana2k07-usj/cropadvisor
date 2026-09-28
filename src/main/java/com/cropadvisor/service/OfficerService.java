package com.cropadvisor.service;

import com.cropadvisor.entity.Officer;
import com.cropadvisor.entity.Region;
import com.cropadvisor.exception.BusinessException;
import com.cropadvisor.repository.OfficerRepository;
import com.cropadvisor.repository.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final RegionRepository regionRepository;

    public OfficerService(OfficerRepository officerRepository, RegionRepository regionRepository) {
        this.officerRepository = officerRepository;
        this.regionRepository = regionRepository;
    }

    public Officer createOfficer(Officer officer, Long regionId) {
        officer.setRegion(getRegion(regionId));
        return officerRepository.save(officer);
    }

    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    public Officer getOfficerById(Long officerId) {
        return officerRepository.findById(officerId)
                .orElseThrow(() -> new BusinessException("Officer not found: " + officerId));
    }

    public Officer updateOfficer(Long officerId, Officer updatedOfficer, Long regionId) {
        Officer officer = getOfficerById(officerId);
        officer.setName(updatedOfficer.getName());
        officer.setPhone(updatedOfficer.getPhone());
        officer.setEmail(updatedOfficer.getEmail());
        officer.setSpecialization(updatedOfficer.getSpecialization());
        officer.setRegion(getRegion(regionId));
        return officerRepository.save(officer);
    }

    public void deleteOfficer(Long officerId) {
        officerRepository.delete(getOfficerById(officerId));
    }

    public List<Officer> getOfficersByRegion(Long regionId) {
        getRegion(regionId);
        return officerRepository.findByRegionRegionId(regionId);
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException("Region not found: " + regionId));
    }
}