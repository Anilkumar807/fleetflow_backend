
package com.fleetflow.fleetflowbackend.controller;

import com.fleetflow.fleetflowbackend.entity.Vehicle;
import com.fleetflow.fleetflowbackend.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:5173")
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // GET: Retrieve all vehicles
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // GET: Retrieve one vehicle by ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Create a vehicle
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle.getRegistrationNumber() == null
                || vehicle.getRegistrationNumber().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (vehicleRepository.existsByRegistrationNumber(
                vehicle.getRegistrationNumber().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        vehicle.setRegistrationNumber(
                vehicle.getRegistrationNumber().trim());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
    }

    // PUT: Update a vehicle
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @RequestBody Vehicle updatedVehicle) {

        return vehicleRepository.findById(id).map(vehicle -> {
            if (updatedVehicle.getRegistrationNumber() == null
                    || updatedVehicle.getRegistrationNumber().isBlank()) {
                return ResponseEntity.badRequest().<Vehicle>build();
            }

            String registration = updatedVehicle.getRegistrationNumber().trim();

            boolean duplicate = vehicleRepository
                    .existsByRegistrationNumber(registration)
                    && !registration.equalsIgnoreCase(
                    vehicle.getRegistrationNumber());

            if (duplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .<Vehicle>build();
            }

            vehicle.setRegistrationNumber(registration);
            vehicle.setType(updatedVehicle.getType());
            vehicle.setModel(updatedVehicle.getModel());
            vehicle.setCapacity(updatedVehicle.getCapacity());
            vehicle.setFuelType(updatedVehicle.getFuelType());
            vehicle.setStatus(updatedVehicle.getStatus());
            vehicle.setOdometer(updatedVehicle.getOdometer());
            vehicle.setDriver(updatedVehicle.getDriver());
            vehicle.setNextService(updatedVehicle.getNextService());
            vehicle.setInsuranceExpiry(updatedVehicle.getInsuranceExpiry());

            return ResponseEntity.ok(vehicleRepository.save(vehicle));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Delete a vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
