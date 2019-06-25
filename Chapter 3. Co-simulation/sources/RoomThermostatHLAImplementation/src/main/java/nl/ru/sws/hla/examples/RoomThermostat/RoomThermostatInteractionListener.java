package nl.ru.sws.hla.examples.RoomThermostat;

import hla.HLAInteractionListener;

/**
 * Created by thomas on 30-3-16.
 */
public interface RoomThermostatInteractionListener extends HLAInteractionListener {

    void targetTemperatureUp();

    void targetTemperatureDown();

    void requestTemperature();

    void provideTemperature(double temperature);

    void turnHeaterOn();

    void turnHeaterOff();

}
