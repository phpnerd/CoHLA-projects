package nl.ru.sws.hla.examples.roomthermostat.roomfmiwrapper;

import hla.HLAEvent;
import hla.tools.DataCollector;
import hla.tools.Util;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatHLAImplementation;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatInteraction;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatInteractionListener;
import nl.ru.sws.hla.examples.RoomThermostat.models.Room;
import nl.ru.sws.hla.examples.RoomThermostat.models.Thermostat;

import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by thomas on 31-3-16.
 */
public class RoomFMIWrapper {

    private double stepSize;

    private RoomSimulator sim;
    private RoomThermostatHLAImplementation hla;
    private Room room;

    private DataCollector roomData;
    private double measureEndTime;
    private Date startTime;
    private double targetTemperature;

    public RoomFMIWrapper(double startTime, double stepSize, double lookahead, String fmuFile, String logConfig, double measureEndTime) {
        this.stepSize = stepSize;
        sim = new RoomSimulator(startTime, fmuFile);
        hla = new RoomThermostatHLAImplementation(lookahead, logConfig);
        room = new Room();
        roomData = new DataCollector("Time (s)", "Simu Time (s)", "Temperature (C)", "Target Temperature (C)");
        this.measureEndTime = measureEndTime;
        System.out.println(String.format("Created RoomFMIWrapper: %f, %f, %f, %f, %s", startTime, stepSize, lookahead, measureEndTime, fmuFile));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hla.stop();
            sim.stop();
        }));
    }

    public void start() {
        try {
            hla.start("Tutorial", hla.getClass().getClassLoader().getResource("RoomThermostat.xml"), "RoomThermostat",
                    RoomThermostatHLAImplementation.OBJECT_ROOM);
            hla.addInteractionSub(RoomThermostatInteraction.REQUEST_TEMPERATURE);
            hla.addInteractionSub(RoomThermostatInteraction.TURN_HEATER_ON);
            hla.addInteractionSub(RoomThermostatInteraction.TURN_HEATER_OFF);
            hla.addInteractionPub(RoomThermostatInteraction.PROVIDE_TEMPERATURE);

            hla.createRoom(room);

            hla.enableTimeConstrained();
            while(!hla.isConstrained())
                Thread.sleep(500);

            hla.addTimeAdvanceGrantListener(time -> {
                sim.advanceTime(time);
                room.setTemperature(sim.getTemperature());
                hla.updateRoom(room);
                System.out.print("\r[" + time + "]: " + room);
                if (roomData.isEnabled()) {
                    if (time > measureEndTime) {
                        if (roomData.export("room.csv"))
                            System.out.println("Written data to room.csv");
                        else
                            System.err.println("Failed to write data to room.csv");
                    } else
                        roomData.storeData(time, (System.currentTimeMillis() - startTime.getTime()) / 1000d, room.getTemperature(), targetTemperature);
                }
                hla.nextMessageRequest(stepSize);
            });

            hla.addInteractionListener(new RoomThermostatInteractionListener() {
                @Override
                public void targetTemperatureUp() {

                }

                @Override
                public void targetTemperatureDown() {

                }

                @Override
                public void requestTemperature() {
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(RoomThermostatHLAImplementation.PARAM_TEMPERATURE, Double.toString(room.getTemperature()));
                    hla.sendEvent(new HLAEvent(RoomThermostatHLAImplementation.INTERACTION_PROVIDETEMPERATURE, parameters));
                }

                @Override
                public void provideTemperature(double temperature) {

                }

                @Override
                public void turnHeaterOn() {
                    sim.setHeaterState(true);
                    room.setHeaterState(true);
                    hla.updateRoom(room);
                }

                @Override
                public void turnHeaterOff() {
                    sim.setHeaterState(false);
                    room.setHeaterState(false);
                    hla.updateRoom(room);
                }
            });

            hla.registerFederationSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN, null);
            while(!hla.hasSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);
            Util.waitForUser();
            hla.synchronisationPointAchieved(RoomThermostatHLAImplementation.READY_TO_RUN);
            while(!hla.isSynchronised(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);

            startTime = new Date();

            hla.nextMessageRequest(stepSize);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) {
        System.setProperty("logFilename", "room");
        String fmuFile, logConfig;
        double stepSize, lookahead, measureEndTime;
        if (args.length < 3) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Please specify the following values before proceeding.");
            System.out.print(" FMU model file: ");
            fmuFile = scan.nextLine().trim();
            System.out.print("      Step size: ");
            stepSize = Double.parseDouble(scan.nextLine().trim());
            System.out.print("      Lookahead: ");
            lookahead = Double.parseDouble(scan.nextLine().trim());
            System.out.println("  MeasureTime: ");
            measureEndTime = Double.parseDouble(scan.nextLine().trim());
            System.out.print("     Log config: ");
            logConfig = scan.nextLine().trim();
            scan.reset();
        } else {
            fmuFile = args[0];
            stepSize = Double.parseDouble(args[1]);
            lookahead = Double.parseDouble(args[2]);
            measureEndTime = Double.parseDouble(args[3]);
            if (args.length >= 5)
                logConfig = args[4];
            else
                logConfig = "../../../../../log4j2.xml";
        }

        new RoomFMIWrapper(0d, stepSize, lookahead, fmuFile, logConfig, measureEndTime).start();
    }

}
