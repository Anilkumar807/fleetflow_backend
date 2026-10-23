
package com.fleetflow.fleetflowbackend.controller;
import com.fleetflow.fleetflowbackend.entity.Trip;
import com.fleetflow.fleetflowbackend.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {

    private final TripRepository tripRepository;

    public TripController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    // GET all trips
    @GetMapping
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    // GET trip by ID
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        return tripRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE trip
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        trip.setId(null);
        Trip savedTrip = tripRepository.save(trip);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedTrip);
    }

    // UPDATE trip
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(
            @PathVariable Long id,
            @RequestBody Trip updatedTrip) {

        return tripRepository.findById(id)
                .map(trip -> {
                    trip.setVehicleId(updatedTrip.getVehicleId());
                    trip.setDriverId(updatedTrip.getDriverId());
                    trip.setVehicle(updatedTrip.getVehicle());
                    trip.setDriver(updatedTrip.getDriver());
                    trip.setOrigin(updatedTrip.getOrigin());
                    trip.setDestination(updatedTrip.getDestination());
                    trip.setPlannedStart(updatedTrip.getPlannedStart());
                    trip.setActualStart(updatedTrip.getActualStart());
                    trip.setCargoLoad(updatedTrip.getCargoLoad());
                    trip.setNotes(updatedTrip.getNotes());
                    trip.setStatus(updatedTrip.getStatus());
                    trip.setStatusHistoryJson(updatedTrip.getStatusHistoryJson());
                    trip.setDeliveriesJson(updatedTrip.getDeliveriesJson());
                    trip.setDistance(updatedTrip.getDistance());
                    trip.setEta(updatedTrip.getEta());

                    return ResponseEntity.ok(tripRepository.save(trip));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE trip
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        if (!tripRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tripRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
