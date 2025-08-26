package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.UpdateBookingDto;
import com.example.uberbookingservice.dto.UpdateBookingRequestDto;
import com.example.uberprojectentityservice.models.Booking;

public interface BookingService {
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetail);
     UpdateBookingDto updateBooking(UpdateBookingRequestDto  updateBookingRequestDto, Long bookingId);
}
