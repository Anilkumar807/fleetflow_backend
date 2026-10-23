package com.fleetflow.fleetflowbackend.controller;

import com.fleetflow.fleetflowbackend.entity.Driver;
import com.fleetflow.fleetflowbackend.repository.DriverRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:5173")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // GET: Retrieve all drivers
    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    // GET: Retrieve a driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        return driverRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Create a driver
    @PostMapping
    public ResponseEntity<?> createDriver(@RequestBody Driver driver) {
        if (driver.getName() == null || driver.getName().isBlank()
                || driver.getLicenseNumber() == null
                || driver.getLicenseNumber().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Driver name and license number are required.");
        }

        String license = driver.getLicenseNumber().trim();

        if (driverRepository.existsByLicenseNumber(license)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("License number already exists.");
        }

        driver.setName(driver.getName().trim());
        driver.setLicenseNumber(license);

        if (driver.getStatus() == null || driver.getStatus().isBlank()) {
            driver.setStatus("Available");
        }

        Driver savedDriver = driverRepository.save(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDriver);
    }

    // PUT: Update a driver
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriver(
            @PathVariable Long id,
            @RequestBody Driver updatedDriver) {

        return driverRepository.findById(id).map(driver -> {
            if (updatedDriver.getName() == null
                    || updatedDriver.getName().isBlank()
                    || updatedDriver.getLicenseNumber() == null
                    || updatedDriver.getLicenseNumber().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Driver name and license number are required.");
            }

            String license = updatedDriver.getLicenseNumber().trim();

            boolean duplicate = driverRepository
                    .findByLicenseNumber(license)
                    .filter(existing -> !existing.getId().equals(id))
                    .isPresent();

            if (duplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("License number already exists.");
            }

            driver.setName(updatedDriver.getName().trim());
            driver.setLicenseNumber(license);
            driver.setLicenseExpiry(updatedDriver.getLicenseExpiry());
            driver.setPhone(updatedDriver.getPhone());
            driver.setEmail(updatedDriver.getEmail());
            driver.setStatus(updatedDriver.getStatus());
            driver.setExperience(updatedDriver.getExperience());

            return ResponseEntity.ok(driverRepository.save(driver));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Delete a driver
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        if (!driverRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        driverRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

