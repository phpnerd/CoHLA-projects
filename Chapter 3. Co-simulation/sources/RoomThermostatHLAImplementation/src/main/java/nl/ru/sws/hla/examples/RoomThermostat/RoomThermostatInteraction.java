package nl.ru.sws.hla.examples.RoomThermostat;

import hla.HLAInteraction;

/**
 * Created by thomas on 30-3-16.
 */
public enum RoomThermostatInteraction implements HLAInteraction {

    TARGET_TEMPERATURE_UP,
    TARGET_TEMPERATURE_DOWN,
    REQUEST_TEMPERATURE,
    PROVIDE_TEMPERATURE,
    TURN_HEATER_ON,
    TURN_HEATER_OFF

}
