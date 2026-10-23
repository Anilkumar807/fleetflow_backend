package com.fleetflow.fleetflowbackend.repository;

import com.fleetflow.fleetflowbackend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
