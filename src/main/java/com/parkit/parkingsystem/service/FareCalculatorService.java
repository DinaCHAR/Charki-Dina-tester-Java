package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Le prbl c'est qu'on utilise des entiers, pas le hours, mais Time
        //le calculer en long
        long inHour = ticket.getInTime().getTime() / (60*1000);
        long outHour = ticket.getOutTime().getTime() / (60*1000);

        //TODO: Some tests are failing here. Need to check if this logic is correct
        //duration 0,75
        float duration = outHour - inHour;
        duration = duration / 60;
        
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}