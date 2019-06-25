package hla;

import org.javafmi.wrapper.Simulation;
import org.javafmi.wrapper.v2.Access;

/**
 * Created by Thomas Nägele on 14-1-16.
 */
public abstract class HLAFMUSimulator implements HLASimulator {

    protected static Simulation simulation;

    public HLAFMUSimulator() {}

    public HLAFMUSimulator(double startTime, String fmuFile) {
        simulation = new Simulation(fmuFile);
        simulation.init(startTime, Double.MAX_VALUE);
    }

    public void stop() {
        simulation.cancelStep();
        simulation.terminate();
    }

    public void advanceTime(double time) {
        double stepSize = time - simulation.getCurrentTime();
        simulation.doStep(stepSize);
    }

    public Simulation getSimulation() {
        return simulation;
    }

}
