package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static final String String = null;

	private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    
    @Test
    public void testProcessIncomingVehicle() throws Exception {
//        ParkingSpotDAO parkingSpotDAO = mock(ParkingSpotDAO.class);
//        TicketDAO ticketDAO = mock(TicketDAO.class);
//        InputReaderUtil inputReaderUtil = mock(InputReaderUtil.class);

        ParkingSpot availableSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABC123");
        //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		//APPEL DE LA METHODE
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(availableSpot);
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }
    
    @Test
    public void processExitingVehicleWithoutDiscount() throws Exception {

        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABC123");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -60);
        ticket.setInTime(c.getTime());
        
        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);
        
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABC123");

        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ParkingService parkingService = new ParkingService(inputReaderUtil, null, ticketDAO);

		//APPEL DE LA METHODE
        parkingService.processExitingVehicle();

        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }
    
    @Test
    public void processExitingVehicleWithDiscount() throws Exception {

        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABC123");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -60);
        ticket.setInTime(c.getTime());
        
        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);
        
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(5);

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABC123");

        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ParkingService parkingService = new ParkingService(inputReaderUtil, null, ticketDAO);

		//APPEL DE LA METHODE
        parkingService.processExitingVehicle();

        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR* 0.95);
    }

    
    @Test
    public void testGetNextParkingNumberIfAvailable() {

        when(inputReaderUtil.readSelection()).thenReturn(1);
    	
        ParkingSpot availableSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, null);

		//APPEL
        ParkingSpot parkingSpot;
		try {

			parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		
	        //Vérifie que l'objet fourni en paramètre ne soit pas null
	        assertNotNull(parkingSpot);
	        //Vérifie l'égalité de deux valeurs de type primitif ou objet
	        assertEquals(availableSpot, parkingSpot);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }

    //on teste quand y a pas de disponibilité dans le parking
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

    	
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, null);

        //APPEL METHODE
        Exception exception = assertThrows(Exception.class, () -> {
        	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        	//check quele parking spot est null
        	assertNull(parkingSpot);
        });

        //Bonne exception avec le bon message
        assertEquals("Error fetching parking number from DB. Parking slots might be full", exception.getMessage());
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingService parkingService = new ParkingService(inputReaderUtil, null, null);

        ParkingSpot parkingSpot;
		try {
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();
	        assertNull(parkingSpot);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // Premier passage sans remise
        // Simuler un nouvel utilisateur première fois, 0 ticket enregistré
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0); // Aucun ticket avant
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1); // Type de parking CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DEF123");

        // Créer une instance de ParkingService
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        FareCalculatorService fareCalculatorService = new FareCalculatorService();

        // Simuler l'arrivée du véhicule pour la première fois
        parkingService.processIncomingVehicle();
        System.out.println("JE PASSE DANS LE INCOMING");

        // Vérifier que le ticket est enregistré
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

        // Configurer l'heure d'entrée pour 1 heure avant la sortie
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("DEF123");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -60); // Entrée il y a 1 heure
        ticket.setInTime(c.getTime());
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        // Simuler la sortie sans remise
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Calculer le tarif sans remise
        Date outTime = new Date();
        ticket.setOutTime(outTime);
        fareCalculatorService.calculateFare(ticket, false);

        // Appel de la méthode pour la sortie
        parkingService.processExitingVehicle();

        // Vérifier que le prix est plein (sans remise)
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
        System.out.println("AUCUNE REMISE  " + ticket.getPrice());

        // Deuxième passage avec remise de 5%
        // Simuler que l'utilisateur revient et qu'il est désormais récurrent + d'un ticket
        when(ticketDAO.getNbTicket(anyString())).thenReturn(2); // L'utilisateur a déjà utilisé le parking

        // Simuler une deuxième arrivée du même véhicule
        parkingService.processIncomingVehicle();

        // Configurer une nouvelle heure d'entrée pour 1 heure avant la deuxième sortie
        ticket = new Ticket();
        ticket.setVehicleRegNumber("DEF123");
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -60); //Entrée il y a 1 heure pour le deuxième passage
        ticket.setInTime(c.getTime());
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        // Simuler la sortie avec remise
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        // Calculer le tarif avec remise
        outTime = new Date();
        ticket.setOutTime(outTime);
        fareCalculatorService.calculateFare(ticket, true); // Applique la remise de 5%
        
        // Appel de la méthode pour la sortie avec remise
        parkingService.processExitingVehicle();
        System.out.println("JE PASSE DANS LE EXIT");
        // Vérifier que le prix a été réduit de 5%
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
        System.out.println("AVEC REMISE" + ticket.getPrice());
    }
}
 