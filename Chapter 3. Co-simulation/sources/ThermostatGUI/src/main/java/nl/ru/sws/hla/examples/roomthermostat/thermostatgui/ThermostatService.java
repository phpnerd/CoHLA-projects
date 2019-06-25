package nl.ru.sws.hla.examples.roomthermostat.thermostatgui;

import hla.HLAEvent;
import hla.HLAObjectListener;
import hla.HLATimeAdvanceGrantListener;
import hla.tools.Util;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatHLAImplementation;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatInteraction;

import javax.annotation.PostConstruct;

/**
 * Created by thomas on 14-4-16.
 */
public class ThermostatService {

    private RoomThermostatHLAImplementation hla;
    private boolean isAdvancing = false;

    @PostConstruct
    public void init() {
        try {
            hla = new RoomThermostatHLAImplementation();
            hla.start(null, hla.getClass().getClassLoader().getResource("RoomThermostat.xml"), "RoomThermostat", "ThermostatGUI");

            hla.addInteractionPub(RoomThermostatInteraction.TARGET_TEMPERATURE_UP);
            hla.addInteractionPub(RoomThermostatInteraction.TARGET_TEMPERATURE_DOWN);

            hla.enableTimeConstrained();
            while (!hla.isConstrained())
                Thread.sleep(500);

            hla.addTimeAdvanceGrantListener(time -> {
                isAdvancing = false;
            });

            hla.registerFederationSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN, null);
            while(!hla.hasSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);
            Util.waitForUser();
            hla.synchronisationPointAchieved(RoomThermostatHLAImplementation.READY_TO_RUN);
            System.out.println("Achieved synchronisation point " + RoomThermostatHLAImplementation.READY_TO_RUN);
            while(!hla.isSynchronised(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);

            hla.subscribeThermostat();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        hla.stop();

    }

    public void addThermostatListener(HLAObjectListener listener) {
        if (hla != null)
            hla.addThermostatListener(listener);
    }

    public void timeAdvanceRequest(double step) {
        if (hla != null) {
            isAdvancing = true;
            hla.timeAdvanceRequest(step);
        }
    }

    public void addTimeAdvanceGrantListener(HLATimeAdvanceGrantListener listener) {
        if (hla != null)
            hla.addTimeAdvanceGrantListener(listener);
    }

    public void sendEvent(HLAEvent event) {
        hla.sendEvent(event);
    }

    public boolean isAdvancing() {
        return isAdvancing;
    }

}
