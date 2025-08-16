package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearByDriverRequestDto;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberprojectentityservice.models.Booking;
import com.example.uberprojectentityservice.models.BookingStatus;
import com.example.uberprojectentityservice.models.Passenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private static final String LOCATION_SERVICE="http://localhost:7878";

    public BookingServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetail) {
       Optional<Passenger> passenger= passengerRepository.findById(bookingDetail.getPassengerId());
        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetail.getStartLocation())
                .endLocation(bookingDetail.getEndLocation())
                .passenger(passenger.get())
                .build();
        Booking newBooking=bookingRepository.save(booking);
        NearByDriverRequestDto nearByDriverRequestDto=NearByDriverRequestDto.builder()
                .latitude(bookingDetail.getStartLocation().getLatitude())
                .longitude(bookingDetail.getStartLocation().getLongitude())
                .build();
//        make an api call to location service to find nearby drivers
       ResponseEntity<DriverLocationDto[]> result= restTemplate.postForEntity(LOCATION_SERVICE+"/api/location/nearby/drivers",nearByDriverRequestDto,DriverLocationDto[].class);

//       DriverLocationDto[] result1=result.getBody();
        if(result.getStatusCode().is2xxSuccessful() && result.getBody()!=null){

        List<DriverLocationDto> driverLocationDtos= Arrays.asList(result.getBody());
        driverLocationDtos.forEach(driverLocationDto->{
            System.out.println(driverLocationDto.getDriverId()+" "+"lat:"+driverLocationDto.getLatitude()+" long:"+driverLocationDto.getLongitude());
        });
        }

     return CreateBookingResponseDto.builder()


             .bookingId(newBooking.getId())
             .bookingStatus(newBooking.getBookingStatus().toString())
//             .driver(Optional.of(newBooking.getDriver()))
             .build();

    }
}
