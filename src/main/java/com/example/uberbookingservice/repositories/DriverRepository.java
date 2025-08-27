package com.example.uberbookingservice.repositories;

import com.example.uberprojectentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository  extends JpaRepository<Driver,Long> {
    Optional<Object> findById(Optional<Long> driverId);
}
