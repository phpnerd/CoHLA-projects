package nl.ru.sws.hla.examples.roomthermostat.thermostatpooslwrapper;

import hla.HLAEvent;
import hla.tools.DataCollector;
import hla.tools.Util;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatHLAImplementation;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatInteraction;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatInteractionListener;
import nl.ru.sws.hla.examples.RoomThermostat.models.Room;
import nl.ru.sws.hla.examples.RoomThermostat.models.Thermostat;

import java.util.Date;
import java.util.Scanner;

/**
 * Created by thomas on 4-4-16.
 */
public class ThermostatPOOSLWrapper {

    private ThermostatSimulator sim;
    private RoomThermostatHLAImplementation hla;

    private DataCollector thermostatData;
    private double measureEndTime;
    private Date startTime;

    public ThermostatPOOSLWrapper(String hostname, int port, String logConfig, double measureEndTime) {
        sim = new ThermostatSimulator(hostname, port);
        hla = new RoomThermostatHLAImplementation(1d, logConfig);
        thermostatData = new DataCollector("Time (s)", "SimuTime (s)", "Temperature (C)", "TargetTemperature (C)", "HeaterState");
        this.measureEndTime = measureEndTime;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sim.stop();
            hla.stop();
        }));
    }

    public void start() {
        try {
            sim.addResponseListener(message -> {
                if (message.isTimeAdvanceRequest()) {
                    double step = message.getParameter(0, Double.class);
                    hla.timeAdvanceRequest(step);
                } else if (message.isInteraction()) {
                    hla.sendEvent(new HLAEvent(message.getName()));
                }
            });

            hla.start("Tutorial", hla.getClass().getClassLoader().getResource("RoomThermostat.xml"), "RoomThermostat",
                    RoomThermostatHLAImplementation.OBJECT_THERMOSTAT);
            hla.addInteractionSub(RoomThermostatInteraction.TARGET_TEMPERATURE_UP);
            hla.addInteractionSub(RoomThermostatInteraction.TARGET_TEMPERATURE_DOWN);
            hla.addInteractionSub(RoomThermostatInteraction.PROVIDE_TEMPERATURE);
            hla.addInteractionPub(RoomThermostatInteraction.REQUEST_TEMPERATURE);
            hla.addInteractionPub(RoomThermostatInteraction.TURN_HEATER_ON);
            hla.addInteractionPub(RoomThermostatInteraction.TURN_HEATER_OFF);

            hla.createThermostat(sim.getThermostat());

            hla.enableTimeConstrained();
            while(!hla.isConstrained())
                Thread.sleep(500);
            hla.enableTimeRegulation(0d);
            while(!hla.isRegulating())
                Thread.sleep(500);

            hla.addTimeAdvanceGrantListener(time -> {
                sim.advanceTime(time);
                Thermostat thermostat = sim.getThermostat();
                hla.updateThermostat(thermostat);
                System.out.print("\r[" + time + "]: " + thermostat);
                if (thermostatData.isEnabled()) {
                    if (time > measureEndTime) {
                        if (thermostatData.export("thermostat.csv"))
                            System.out.println("Written data to thermostat.csv");
                        else
                            System.err.println("Failed to write data to thermostat.csv");
                    } else
                        thermostatData.storeData(time, (System.currentTimeMillis() - startTime.getTime()) / 1000d, thermostat.getTemperature(), thermostat.getTargetTemperature(), thermostat.heaterState() ? 1d : 0d);
                }
            });

            hla.addInteractionListener(new RoomThermostatInteractionListener() {
                @Override
                public void targetTemperatureUp() {
                    sim.sendInteraction(RoomThermostatHLAImplementation.INTERACTION_TARGETTEMPERATUREUP);
                }

                @Override
                public void targetTemperatureDown() {
                    sim.sendInteraction(RoomThermostatHLAImplementation.INTERACTION_TARGETTEMPERATUREDOWN);
                }

                @Override
                public void requestTemperature() {

                }

                @Override
                public void provideTemperature(double temperature) {
                    sim.setTemperature(temperature);
                    sim.sendInteraction("ProvideTemperature", temperature);
                    sim.syncAttributes();
                }

                @Override
                public void turnHeaterOn() {

                }

                @Override
                public void turnHeaterOff() {

                }
            });

            hla.registerFederationSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN, null);
            while(!hla.hasSynchronisationPoint(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);
            Util.waitForUser();
            hla.synchronisationPointAchieved(RoomThermostatHLAImplementation.READY_TO_RUN);
            while(!hla.isSynchronised(RoomThermostatHLAImplementation.READY_TO_RUN))
                Thread.sleep(500);

            sim.init();
            sim.start();
            startTime = new Date();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) {
        System.setProperty("logFilename", "thermostat");
        String hostname, logConfig;
        int port;
        double measureEndTime;
        if (args.length < 3) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Please specify the following values before proceeding.");
            System.out.print("   Hostname: ");
            hostname = scan.nextLine().trim();
            System.out.print("       Port: ");
            port = Integer.parseInt(scan.nextLine().trim());
            System.out.print("MeasureTime: ");
            measureEndTime = Double.parseDouble(scan.nextLine().trim());
            System.out.print(" Log config: ");
            logConfig = scan.nextLine().trim();
            scan.reset();
        } else {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            measureEndTime = Double.parseDouble(args[2]);
            if (args.length >= 4)
                logConfig = args[3];
            else
                logConfig = "../../../../../log4j2.xml";
        }

        new ThermostatPOOSLWrapper(hostname, port, logConfig, measureEndTime).start();
    }

}
