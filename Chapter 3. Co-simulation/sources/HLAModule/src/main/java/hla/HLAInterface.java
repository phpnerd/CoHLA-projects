package hla;

import hla.rti1516e.FederateAmbassador;

import java.net.URL;

/**
 * Created by Thomas Nägele on 10-12-15.
 */
public interface HLAInterface<HIL, HI> extends FederateAmbassador {

    void start(String localSettingsDesignator, URL fddFile, String federationName, String federateName);

    void stop();

//    Time management

    void enableTimeConstrained();

    void enableTimeRegulation(double interval);

    void addTimeAdvanceGrantListener(HLATimeAdvanceGrantListener listener);

    void removeTimeAdvanceGrantListener(HLATimeAdvanceGrantListener listener);

    void timeAdvanceRequest(double timeStep);

    double getFederateTime();

    boolean isConstrained();

    boolean isRegulating();

    double getLookahead();

    void setLookahead(double lookahead);

    void registerFederationSynchronisationPoint(String label, byte[] bytes);

    void synchronisationPointAchieved(String label);

    boolean isSynchronised(String label);

    boolean hasSynchronisationPoint(String label);

//    Interactions

    void addInteractionListener(HIL listener);

    void removeInteractionListener(HIL listener);

    void sendEvent(HLAEvent event);

    void addInteractionSub(HI interaction);

    void addInteractionPub(HI interaction);

}
