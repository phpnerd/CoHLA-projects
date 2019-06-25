package nl.ru.sws.hla.examples.RoomThermostat.models;

import hla.HLAObject;

/**
 * Created by thomas on 31-3-16.
 */
public class Room implements HLAObject {

    private double temperature;
    private boolean heaterState;

    public Room() {
        temperature = 0d;
        heaterState = false;
    }

    public Room(double temperature, boolean heaterState) {
        this.temperature = temperature;
        this.heaterState = heaterState;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean heaterState() {
        return heaterState;
    }

    public void setHeaterState(boolean heaterState) {
        this.heaterState = heaterState;
    }

    @Override
    public String toString() {
        return "Room{temperature: " + temperature + ", heaterState: " + heaterState + "}";
    }
}
