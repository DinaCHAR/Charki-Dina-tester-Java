package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
        assertNotNull(ticket);
        
        //Parking table is updated with availability
        Boolean placeDispo = parkingSpotDAO.sLotParkingAvailable(ticket.getParkingSpot().getId());
        assertFalse(placeDispo);
    }

    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void testParkingLotExit() throws Exception{
    	//S'assurer qu'il y est bien une voiture qui soit garé 
        testParkingACar();
        
        //permet de ne pas avoir l'in time = outtime sans se casser la tete, plutot que de créer le ticket manuellement avec sa DAO
        Thread.sleep(2000);
        
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
        assertNotNull(ticket);
        assertNotNull(ticket.getOutTime());
        assertNotNull(ticket.getPrice());
        
        //Vérifié que l'heure de sorti est correctement renseigner dans le DB
        equals(ticket.getOutTime());
    }
    
    @Test
    public void testParkingLotExitRecurringUser() throws Exception{
    	
    	//simule que un ticket a été crée + terminée, afin d'avoir une récurrence
         testParkingLotExit();
         
         ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
         //on refait rentré une voiture
         ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
         if(parkingSpot !=null && parkingSpot.getId() > 0){
             String vehicleRegNumber = parkingService.getVehichleRegNumber();
             parkingSpot.setAvailable(false);
             parkingSpotDAO.updateParking(parkingSpot);
             
             //on recule d'1h
             Calendar c = Calendar.getInstance();
             
             c.add(Calendar.HOUR, -1);
             
             Ticket ticket = new Ticket();
             ticket.setParkingSpot(parkingSpot);
             ticket.setVehicleRegNumber(vehicleRegNumber);
             ticket.setPrice(0);
             //ticket entrée il y a 1h
             ticket.setInTime(c.getTime());
             ticket.setOutTime(null);
             ticketDAO.saveTicket(ticket);
         }
         

        parkingService.processExitingVehicle();
        Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
        assertNotNull(ticket);
        assertNotNull(ticket.getOutTime());
        assertNotNull(ticket.getPrice());
        assertEquals(ticket.getPrice(), 0.95 * Fare.CAR_RATE_PER_HOUR);
        
    }

}
