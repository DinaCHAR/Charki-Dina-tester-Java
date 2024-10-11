package com.parkit.parkingsystem.service;

import java.util.ArrayList;
import java.util.List;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	//Désactivé la réduction
	public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Le prbl c'est qu'on utilise des entiers, pas le hours, mais Time
        //le calculer en long
        long inHour = ticket.getInTime().getTime() / (60*1000);
        long outHour = ticket.getOutTime().getTime() / (60*1000);

        //TODO: Some tests are failing here. Need to check if this logic is correct ok
        //duration 0,75
        float duration = outHour - inHour;
        duration = duration / 60;
        
        //Vérifier que la durée est inférieur à 30min, si c'est le cas alors le prix est = à 0 (0.5 correspond a 30min en heures (30/60)
        if (duration <= 0.5) {
        	ticket.setPrice(0);
        	return;
        }
        
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
        
        //Appliquer la reduc de 5%
        if(discount){
        	ticket.setPrice(ticket.getPrice() * 0.95);
        }
    }
}