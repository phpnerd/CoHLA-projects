package nl.ru.sws.hla.examples.roomthermostat.thermostatpooslwrapper;

import hla.tools.Util;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatHLAImplementation;
import nl.ru.sws.hla.examples.RoomThermostat.models.Thermostat;
import nl.ru.sws.hla.poosl.HLAAttribute;
import nl.ru.sws.hla.poosl.HLAMessage;
import nl.ru.sws.hla.poosl.HLAMessageType;
import nl.ru.sws.hla.poosl.POOSLSimulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Created by thomas on 5-4-16.
 */
public class ThermostatSimulator extends POOSLSimulator {

    private Thermostat thermostat;

    public ThermostatSimulator(String hostname, int port) {
        super(hostname, port);
        thermostat = new Thermostat();
        attributes.addAll(Arrays.asList(
                new HLAAttribute<>(RoomThermostatHLAImplementation.ATTRIBUTE_THERMOSTAT_TARGETTEMPERATURE, Double.class,
                        thermostat::setTargetTemperature),
                new HLAAttribute<>(RoomThermostatHLAImplementation.ATTRIBUTE_THERMOSTAT_HEATERSTATE, Boolean.class,
                        thermostat::setHeaterState)));

        addResponseListener(m -> {
            if (m.isAttributeUpdate()) {
                attributes.forEach(a -> {
                    if (m.getName().equals(a.getName()) && !a.isSynced()) {
                        a.getSetter().accept(m.getParameter(0, a.getClazz()));
                        a.setSynced(true);
                    }
                });
            }
        });
    }

    public boolean heaterState() {
        return thermostat.heaterState();
    }

    public double getTargetTemperature() {
        return thermostat.getTargetTemperature();
    }

    public void setTemperature(double temperature) {
        thermostat.setTemperature(temperature);
    }

    public Thermostat getThermostat() {
        return thermostat;
    }

}
