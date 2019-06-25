package nl.ru.sws.hla.examples.roomthermostat.roomfmiwrapper;

import hla.HLAFMUSimulator;

/**
 * Created by thomas on 31-3-16.
 */
public class RoomSimulator extends HLAFMUSimulator {

//    private static final String TEMPERATURE = "temperature";
//    private static final String HEATER_STATE = "heaterState.k";
    private static final String TEMPERATURE = "RoomTemperature";
    private static final String HEATER_STATE = "HeaterState";

    public RoomSimulator(double startTime, String fmuFile) {
        super(startTime, fmuFile);
    }

    public double getTemperature() {
        return simulation.read(TEMPERATURE).asDouble();
    }

    public void setHeaterState(boolean heaterState) {
        simulation.write(HEATER_STATE).with(heaterState);
    }

}
