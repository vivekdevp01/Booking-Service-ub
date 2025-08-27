package com.example.uberbookingservice.dto;

//import com.example.clientsocketservice.model.ExactLocation;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDto {

    private Long passengerId;

//    private ExactLocation startLocation;
//    private ExactLocation endLocation;

    private List<Long> driverIds;
    private Long bookingId;
}
