package nl.ru.sws.hla.examples.RoomThermostat.models;

import hla.HLAObject;

/**
 * Created by thomas on 31-3-16.
 */
public class Thermostat implements HLAObject {

    private double temperature, targetTemperature;
    private boolean heaterState;

    public Thermostat() {
        temperature = 0d;
        heaterState = false;
        targetTemperature = 0d;
    }

    public Thermostat(double temperature, boolean heaterState, double targetTemperature) {
        this.temperature = temperature;
        this.heaterState = heaterState;
        this.targetTemperature = targetTemperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public boolean heaterState() {
        return heaterState;
    }

    public void setHeaterState(boolean heaterState) {
        this.heaterState = heaterState;
    }

    @Override
    public String toString() {
        return "{temperature: " + temperature + ", targetTemperature: " + targetTemperature + ", heaterState: " + heaterState + "};";
    }
}
