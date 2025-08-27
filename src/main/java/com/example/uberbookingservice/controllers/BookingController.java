package com.example.uberbookingservice.controllers;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.UpdateBookingDto;
import com.example.uberbookingservice.dto.UpdateBookingRequestDto;
import com.example.uberbookingservice.services.BookingService;
import com.example.uberprojectentityservice.models.Booking;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

     private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
         this.bookingService = bookingService;
     }
    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> createBooking(@RequestBody CreateBookingDto createBookingDto) {

       return new ResponseEntity<>(bookingService.createBooking(createBookingDto), HttpStatus.CREATED);
    }

    @PostMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingDto> updateBooking(@RequestBody UpdateBookingRequestDto updateBookingRequestDto, @PathVariable Long bookingId) {
        System.out.println("bookingId"+bookingId);
        return new ResponseEntity<>(bookingService.updateBooking(updateBookingRequestDto,bookingId),HttpStatus.OK);
    }
}
