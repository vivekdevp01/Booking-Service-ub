package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.example.uberbookingservice.dto.*;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.DriverRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberprojectentityservice.models.Booking;
import com.example.uberprojectentityservice.models.BookingStatus;
import com.example.uberprojectentityservice.models.Driver;
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
    private final DriverRepository driverRepository;
    private final UberSocketApi uberSocketApi;
//    private static final String LOCATION_SERVICE="http://localhost:7878";

    public BookingServiceImpl(PassengerRepository passengerRepository,UberSocketApi uberSocketApi, LocationServiceApi locationServiceApi, BookingRepository bookingRepository, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.uberSocketApi=uberSocketApi;
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
        processNearByDriverAsync(nearByDriverRequestDto,bookingDetail.getPassengerId(),newBooking.getId());
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

    @Override
    public UpdateBookingDto updateBooking(UpdateBookingRequestDto updateBookingRequestDto, Long bookingId) {
        System.out.println(updateBookingRequestDto.getDriverId().get());
        System.out.println(updateBookingRequestDto.getStatus());
        System.out.println("bookingId"+bookingId);
      Driver driver= (Driver) driverRepository.findById(updateBookingRequestDto.getDriverId()).orElseThrow(()->new IllegalArgumentException("driver not found"));

//      todo to check ifdriver.ispresent and driver.get.isAvailbal
        if(!driver.isAvailable()){
            throw new IllegalArgumentException("driver not available");
        }
        bookingRepository.updateBookingById(bookingId,updateBookingRequestDto.getStatus(),driver);
        driver.setAvailable(false);
        driverRepository.save(driver);
//        driverRepository.update-> unavaible
        Optional<Booking> booking=bookingRepository.findById(bookingId);
        return UpdateBookingDto.builder()
                .bookingId(bookingId)
                .status(booking.get().getBookingStatus())
                .driver(Optional.ofNullable(booking.get().getDriver()))
                .build();
    }

    private void processNearByDriverAsync(NearByDriverRequestDto requestDto,Long passengerId,Long bookingId){
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
        raiseRideRequestAsync(RideRequestDto.builder().passengerId(passengerId).bookingId(bookingId).build());

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
    private void raiseRideRequestAsync(RideRequestDto requestDto){
        Call<Boolean> call=uberSocketApi.raiseRideRequest(requestDto);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful()&& response.body()!=null){
                    Boolean result=response.body();
                    System.out.println("Driver response is"+result.toString());
                    System.out.println("Ride request sent successfully");
                }
                else{
                    System.out.println("Request failed"+response.message());
                }

            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                t.printStackTrace();
                System.out.println("Error occurred while calling uber socket service"+t.getMessage());
            }
        });
    }
}
