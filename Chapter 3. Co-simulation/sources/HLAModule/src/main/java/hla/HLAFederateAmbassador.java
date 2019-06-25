package hla;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import hla.tools.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.net.URL;
import java.util.*;

/**
 * Created by Thomas Nägele on 11-12-15.
 */
public abstract class HLAFederateAmbassador<HIL, HI> implements HLAInterface<HIL, HI> {

    public static final String READY_TO_RUN = "ReadyToRun";

    protected RTIambassador rtiAmbassador;

    protected final List<HIL>                           interactionListeners    = new ArrayList<>();
    protected final List<HLATimeAdvanceGrantListener>   advanceGrantListeners   = new ArrayList<>();

    protected final Map<String, Boolean>    synchronisationPoints   = new HashMap<>();

    protected String                        localSettingsDesignator,
                                            federationName,
                                            federateName;
    protected URL                           fddFile;

    protected EncoderFactory encoderFactory;
    protected HLAunicodeString unicodeStringCoder;

    protected HLAfloat64TimeFactory timeFactory;
    protected double                        federateTime,
                                            lookahead;
    protected boolean                       isConstrained   = false,
                                            isRegulating    = false;

    private String logConfig;

    public HLAFederateAmbassador() {
        lookahead = 1d;
    }

    public HLAFederateAmbassador(double lookahead, String logConfig) {
        this.lookahead = lookahead;
        this.logConfig = logConfig;
    }

    @Override
    public void start(String localSettingsDesignator, URL fddFile, String federationName, String federateName) {
        System.out.println(String.format("Starting HLAFederate: %s, %s, %s, %s", localSettingsDesignator, fddFile.getFile(), federationName, federateName));
        this.localSettingsDesignator = localSettingsDesignator;
        this.fddFile = fddFile;
        this.federationName = federationName;
        this.federateName = federateName;

        try {
            RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
            rtiAmbassador = rtiFactory.getRtiAmbassador();

            encoderFactory = rtiFactory.getEncoderFactory();
            unicodeStringCoder = encoderFactory.createHLAunicodeString();
            timeFactory = (HLAfloat64TimeFactory) rtiAmbassador.getTimeFactory();
            System.out.println("[" + federateTime + "] Federate and factories created");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }

//      Connect to RTI
        try {
            rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE);
            System.out.println("[" + federateTime + "] Connected to RTIAmbassador");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }

//      Destroy old federation execution
        try {
            rtiAmbassador.destroyFederationExecution(federationName);
            System.out.println("[" + federateTime + "] Destroyed federation execution");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] Failed to destroy federation execution");
        }

//      Create federation execution
        try {
            rtiAmbassador.createFederationExecution(federationName, new URL[]{fddFile});
            System.out.println("[" + federateTime + "] Created federation execution: " + federationName);
        } catch (FederationExecutionAlreadyExists e) {
            System.out.println("[" + federateTime + "] Federation already existed.");
        } catch (ErrorReadingFDD | InconsistentFDD | CouldNotOpenFDD | RTIinternalError | NotConnected e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }

//      Join federation
        try {
            rtiAmbassador.joinFederationExecution(federateName, federateName, federationName, new URL[]{fddFile});
            System.out.println("[" + federateTime + "] Joined federation execution: " + federateName);
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
        getHandles();

        if (logConfig != null) {
            System.setProperty("log4j.configurationFile", logConfig);
            System.setOut(Util.createLoggingProxy(LogManager.getRootLogger(), System.out, Level.TRACE));
            System.setErr(Util.createLoggingProxy(LogManager.getRootLogger(), System.err, Level.ERROR));
        }
    }

    @Override
    public void stop() {
        try {
            rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
            System.out.println("[" + federateTime + "] Resigned federation execution");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }

        if (federationName != null) {
            try {
                rtiAmbassador.destroyFederationExecution(federationName);
                System.out.println("[" + federateTime + "] Destroyed federation execution");
            } catch (Exception e) {
                System.err.println("[" + federateTime + "] " + e.getMessage());
            }
        }

        try {
            rtiAmbassador.disconnect();
            System.out.println("[" + federateTime + "] Disconnected RTIAmbassador");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }

    }

    @Override
    public void enableTimeConstrained() {
        try {
            rtiAmbassador.enableTimeConstrained();
            System.out.println("[" + federateTime + "] Enabling time constrained mode");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
    }

    @Override
    public void enableTimeRegulation(double interval) {
        try {
            rtiAmbassador.enableTimeRegulation(timeFactory.makeInterval(interval));
            System.out.println("[" + federateTime + "] Enabling time regulating mode (" + interval + ")");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
    }

    @Override
    public void addTimeAdvanceGrantListener(HLATimeAdvanceGrantListener listener) {
        advanceGrantListeners.add(listener);
        System.out.println("[" + federateTime + "] Added timeAdvanceGrant listener");
    }

    @Override
    public void removeTimeAdvanceGrantListener(HLATimeAdvanceGrantListener listener) {
        advanceGrantListeners.remove(listener);
        System.out.println("[" + federateTime + "] Removed timeAdvanceGrant listener");
    }

    @Override
    public void timeAdvanceRequest(double timeStep) {
        try {
            HLAfloat64Time time = timeFactory.makeTime(this.federateTime + timeStep);
            rtiAmbassador.timeAdvanceRequest(time);
            System.out.println("[" + federateTime + "] Requested time advance (" + timeStep + ")");
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
    }

    public void nextMessageRequest(double maxStep) {
        try {
            rtiAmbassador.nextMessageRequest(timeFactory.makeTime(federateTime + maxStep));
            System.out.println("[" + federateTime + "] Requested next message");
        } catch (Exception ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public double getFederateTime() {
        return federateTime;
    }

    @Override
    public boolean isConstrained() {
        return isConstrained;
    }

    @Override
    public boolean isRegulating() {
        return isRegulating;
    }

    @Override
    public double getLookahead() {
        return lookahead;
    }

    @Override
    public void setLookahead(double lookahead) {
        this.lookahead = lookahead;
        System.out.println("[" + federateTime + "] Set lookahead to " + lookahead);
    }

    @Override
    public void registerFederationSynchronisationPoint(String label, byte[] bytes) {
        try {
            rtiAmbassador.registerFederationSynchronizationPoint(label, bytes);
            System.out.println("[" + federateTime + "] Registered federation synchronisation point " + label);
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
    }

    @Override
    public void synchronisationPointAchieved(String label) {
        try {
            rtiAmbassador.synchronizationPointAchieved(label);
            System.out.println("[" + federateTime + "] Achieved synchronisation point " + label);
        } catch (Exception e) {
            System.err.println("[" + federateTime + "] " + e.getMessage());
        }
    }

    @Override
    public boolean isSynchronised(String label) {
        if (synchronisationPoints.containsKey(label))
            return synchronisationPoints.get(label);
        return false;
    }

    @Override
    public boolean hasSynchronisationPoint(String label) {
        return synchronisationPoints.containsKey(label);
    }

    @Override
    public void addInteractionListener(HIL listener) {
        interactionListeners.add(listener);
        System.out.println("[" + federateTime + "] Added interaction listener");
    }

    @Override
    public void removeInteractionListener(HIL listener) {
        interactionListeners.remove(listener);
        System.out.println("[" + federateTime + "] Removed interaction listener");
    }

    @Override
    public void connectionLost(String s) throws FederateInternalError {

    }

    @Override
    public void reportFederationExecutions(FederationExecutionInformationSet federationExecutionInformationSet) throws FederateInternalError {

    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String s) throws FederateInternalError {
        if (!synchronisationPoints.containsKey(s)) {
            synchronisationPoints.put(s, false);
            System.out.println("[" + federateTime + "] Succeeded registration of synchronisation point");
        }
    }

    @Override
    public void synchronizationPointRegistrationFailed(String s, SynchronizationPointFailureReason synchronizationPointFailureReason) throws FederateInternalError {
        System.err.println("[" + federateTime + "] Failed to register sychronisation point (\"" + s + "\").");
    }

    @Override
    public void announceSynchronizationPoint(String s, byte[] bytes) throws FederateInternalError {
        if (!synchronisationPoints.containsKey(s)) {
            synchronisationPoints.put(s, false);
            System.out.println("[" + federateTime + "] Announced synchronisation point");
        }
    }

    @Override
    public void federationSynchronized(String s, FederateHandleSet federateHandleSet) throws FederateInternalError {
        if (synchronisationPoints.containsKey(s)) {
            synchronisationPoints.put(s, true);
            System.out.println("[" + federateTime + "] Synchronised federation");
        }
    }

    @Override
    public void initiateFederateSave(String s) throws FederateInternalError {

    }

    @Override
    public void initiateFederateSave(String s, LogicalTime logicalTime) throws FederateInternalError {

    }

    @Override
    public void federationSaved() throws FederateInternalError {

    }

    @Override
    public void federationNotSaved(SaveFailureReason saveFailureReason) throws FederateInternalError {

    }

    @Override
    public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] federateHandleSaveStatusPairs) throws FederateInternalError {

    }

    @Override
    public void requestFederationRestoreSucceeded(String s) throws FederateInternalError {

    }

    @Override
    public void requestFederationRestoreFailed(String s) throws FederateInternalError {

    }

    @Override
    public void federationRestoreBegun() throws FederateInternalError {

    }

    @Override
    public void initiateFederateRestore(String s, String s1, FederateHandle federateHandle) throws FederateInternalError {

    }

    @Override
    public void federationRestored() throws FederateInternalError {

    }

    @Override
    public void federationNotRestored(RestoreFailureReason restoreFailureReason) throws FederateInternalError {

    }

    @Override
    public void federationRestoreStatusResponse(FederateRestoreStatus[] federateRestoreStatuses) throws FederateInternalError {

    }

    @Override
    public void startRegistrationForObjectClass(ObjectClassHandle objectClassHandle) throws FederateInternalError {

    }

    @Override
    public void stopRegistrationForObjectClass(ObjectClassHandle objectClassHandle) throws FederateInternalError {

    }

    @Override
    public void turnInteractionsOn(InteractionClassHandle interactionClassHandle) throws FederateInternalError {

    }

    @Override
    public void turnInteractionsOff(InteractionClassHandle interactionClassHandle) throws FederateInternalError {

    }

    @Override
    public void objectInstanceNameReservationSucceeded(String s) throws FederateInternalError {

    }

    @Override
    public void objectInstanceNameReservationFailed(String s) throws FederateInternalError {

    }

    @Override
    public void multipleObjectInstanceNameReservationSucceeded(Set<String> set) throws FederateInternalError {

    }

    @Override
    public void multipleObjectInstanceNameReservationFailed(Set<String> set) throws FederateInternalError {

    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String s, FederateHandle federateHandle) throws FederateInternalError {
        discoverObjectInstance(objectInstanceHandle, objectClassHandle, s);
    }

    public abstract void reflectAttributeValues(ObjectInstanceHandle object, AttributeHandleValueMap attributes);

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, FederateAmbassador.SupplementalReflectInfo supplementalReflectInfo) throws FederateInternalError {
        reflectAttributeValues(objectInstanceHandle, attributeHandleValueMap);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, LogicalTime logicalTime, OrderType orderType1, FederateAmbassador.SupplementalReflectInfo supplementalReflectInfo) throws FederateInternalError {
        reflectAttributeValues(objectInstanceHandle, attributeHandleValueMap);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, LogicalTime logicalTime, OrderType orderType1, MessageRetractionHandle messageRetractionHandle, FederateAmbassador.SupplementalReflectInfo supplementalReflectInfo) throws FederateInternalError {
        reflectAttributeValues(objectInstanceHandle, attributeHandleValueMap);
    }

    public abstract void receiveInteraction(InteractionClassHandle interaction, ParameterHandleValueMap parameters);

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, FederateAmbassador.SupplementalReceiveInfo supplementalReceiveInfo) throws FederateInternalError {
        receiveInteraction(interactionClassHandle, parameterHandleValueMap);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, LogicalTime logicalTime, OrderType orderType1, FederateAmbassador.SupplementalReceiveInfo supplementalReceiveInfo) throws FederateInternalError {
        receiveInteraction(interactionClassHandle, parameterHandleValueMap);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterHandleValueMap, byte[] bytes, OrderType orderType, TransportationTypeHandle transportationTypeHandle, LogicalTime logicalTime, OrderType orderType1, MessageRetractionHandle messageRetractionHandle, FederateAmbassador.SupplementalReceiveInfo supplementalReceiveInfo) throws FederateInternalError {
        receiveInteraction(interactionClassHandle, parameterHandleValueMap);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] bytes, OrderType orderType, FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo) throws FederateInternalError {

    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] bytes, OrderType orderType, LogicalTime logicalTime, OrderType orderType1, FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo) throws FederateInternalError {

    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] bytes, OrderType orderType, LogicalTime logicalTime, OrderType orderType1, MessageRetractionHandle messageRetractionHandle, FederateAmbassador.SupplementalRemoveInfo supplementalRemoveInfo) throws FederateInternalError {

    }

    @Override
    public void attributesInScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void turnUpdatesOnForObjectInstance(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void turnUpdatesOnForObjectInstance(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, String s) throws FederateInternalError {

    }

    @Override
    public void turnUpdatesOffForObjectInstance(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void confirmAttributeTransportationTypeChange(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, TransportationTypeHandle transportationTypeHandle) throws FederateInternalError {

    }

    @Override
    public void reportAttributeTransportationType(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, TransportationTypeHandle transportationTypeHandle) throws FederateInternalError {

    }

    @Override
    public void confirmInteractionTransportationTypeChange(InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle) throws FederateInternalError {

    }

    @Override
    public void reportInteractionTransportationType(FederateHandle federateHandle, InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle) throws FederateInternalError {

    }

    @Override
    public void requestAttributeOwnershipAssumption(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, byte[] bytes) throws FederateInternalError {

    }

    @Override
    public void requestDivestitureConfirmation(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void attributeOwnershipAcquisitionNotification(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, byte[] bytes) throws FederateInternalError {

    }

    @Override
    public void attributeOwnershipUnavailable(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void requestAttributeOwnershipRelease(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, byte[] bytes) throws FederateInternalError {

    }

    @Override
    public void confirmAttributeOwnershipAcquisitionCancellation(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet) throws FederateInternalError {

    }

    @Override
    public void informAttributeOwnership(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, FederateHandle federateHandle) throws FederateInternalError {

    }

    @Override
    public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle) throws FederateInternalError {

    }

    @Override
    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle) throws FederateInternalError {

    }

    @Override
    public void timeRegulationEnabled(LogicalTime time) throws FederateInternalError {
        federateTime = ((HLAfloat64Time)time).getValue();
        isRegulating = true;
        System.out.println("[" + federateTime + "] Set time regulating (" + federateTime + ")");
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError {
        federateTime = ((HLAfloat64Time)time).getValue();
        isConstrained = true;
        System.out.println("[" + federateTime + "] Set time constrained (" + federateTime + ")");
    }

    @Override
    public void timeAdvanceGrant(LogicalTime logicalTime) throws FederateInternalError {
        HLAfloat64Time time = (HLAfloat64Time) logicalTime;
        federateTime = time.getValue();
        System.out.println("[" + federateTime + "] Grant time advancement: " + federateTime);
        advanceGrantListeners.forEach(l -> { l.timeAdvanceGrant(federateTime); });
    }

    @Override
    public void requestRetraction(MessageRetractionHandle messageRetractionHandle) throws FederateInternalError {

    }

    protected abstract void getHandles();

}
