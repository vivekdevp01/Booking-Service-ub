package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi locationServiceApi;
//    private static final String LOCATION_SERVICE="http://localhost:7878";

    public BookingServiceImpl(PassengerRepository passengerRepository,LocationServiceApi locationServiceApi, BookingRepository bookingRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
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
        processNearByDriverAsync(nearByDriverRequestDto);
////        make an api call to location service to find nearby drivers
//       ResponseEntity<DriverLocationDto[]> result= restTemplate.postForEntity(LOCATION_SERVICE+"/api/location/nearby/drivers",nearByDriverRequestDto,DriverLocationDto[].class);
//
////       DriverLocationDto[] result1=result.getBody();
//        if(result.getStatusCode().is2xxSuccessful() && result.getBody()!=null){
//
//        List<DriverLocationDto> driverLocationDtos= Arrays.asList(result.getBody());
//        driverLocationDtos.forEach(driverLocationDto->{
//            System.out.println(driverLocationDto.getDriverId()+" "+"lat:"+driverLocationDto.getLatitude()+" long:"+driverLocationDto.getLongitude());
//        });
//        }

     return CreateBookingResponseDto.builder()


             .bookingId(newBooking.getId())
             .bookingStatus(newBooking.getBookingStatus().toString())
//             .driver(Optional.of(newBooking.getDriver()))
             .build();

    }
    private void processNearByDriverAsync(NearByDriverRequestDto requestDto){
        Call<DriverLocationDto[]> call=locationServiceApi.getNearByDrivers(requestDto);

        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                try{
                    Thread.sleep(5000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if(response.isSuccessful()&& response.body()!=null){

        List<DriverLocationDto> driverLocationDtos= Arrays.asList(response.body());
        driverLocationDtos.forEach(driverLocationDto->{
            System.out.println(driverLocationDto.getDriverId()+" "+"lat:"+driverLocationDto.getLatitude()+" long:"+driverLocationDto.getLongitude());
        });
        }
                else{
                    System.out.println("Request failed"+response.message());
                }

                }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                t.printStackTrace();
                System.out.println("Error occurred while calling location service"+t.getMessage());
            }
        });
    }
}
