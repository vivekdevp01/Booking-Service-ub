package com.example.uberbookingservice.dto;

import com.example.uberprojectentityservice.models.BookingStatus;
import com.example.uberprojectentityservice.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingDto {
    private BookingStatus status;
    private Long bookingId;
    private Optional<Driver> driver;
}
