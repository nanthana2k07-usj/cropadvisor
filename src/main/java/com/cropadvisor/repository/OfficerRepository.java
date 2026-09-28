package com.cropadvisor.repository;

import com.cropadvisor.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfficerRepository extends JpaRepository<Officer, Long> {

    List<Officer> findByRegionRegionId(Long regionId);
}