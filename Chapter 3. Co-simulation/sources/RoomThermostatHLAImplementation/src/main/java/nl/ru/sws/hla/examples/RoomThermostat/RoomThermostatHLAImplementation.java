package nl.ru.sws.hla.examples.RoomThermostat;

import hla.HLAEvent;
import hla.HLAFederateAmbassador;
import hla.HLAObjectListener;
import hla.rti1516e.*;
import hla.rti1516e.encoding.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;
import nl.ru.sws.hla.examples.RoomThermostat.models.Room;
import nl.ru.sws.hla.examples.RoomThermostat.models.Thermostat;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 30-3-16.
 */
public class RoomThermostatHLAImplementation
        extends HLAFederateAmbassador<RoomThermostatInteractionListener, RoomThermostatInteraction> {

    private List<HLAObjectListener> roomListeners = new ArrayList<>(), thermostatListeners = new ArrayList<>();
    private HashMap<RoomThermostatInteraction, InteractionClassHandle> interactionMap = new HashMap<>();

    private ObjectClassHandle objectClassRoomHandle, objectClassThermostatHandle;
    private ObjectInstanceHandle objectInstanceRoomHandle, objectInstanceThermostatHandle;
    private AttributeHandle roomTemperatureHandle, roomHeaterStateHandle,
            thermostatTemperatureHandle, thermostatHeaterStateHandle, thermostatTargetTemperatureHandle;
    private InteractionClassHandle targetTemperatureUpHandle, targetTemperatureDownHandle, requestTemperatureHandle,
            provideTemperatureHandle, turnHeaterOnHandle, turnHeaterOffHandle;
    private ParameterHandle temperatureParamHandle;

    private Room room;
    private Thermostat thermostat;

    public RoomThermostatHLAImplementation() {
        super();
    }

    public RoomThermostatHLAImplementation(double lookahead, String configFile) {
        super(lookahead, configFile);
    }

    @Override
    public void start(String localSettingsDesignator, URL fddFile, String federationName, String federateName) {
        super.start(localSettingsDesignator, fddFile, federationName, federateName);
        publishRoom();
        publishThermostat();
        System.out.println("[" +federateTime + "] RoomThermostatHLAImplementation started");
    }

    private void createRoom(Room room, ObjectInstanceHandle objectInstanceRoomHandle) {
        this.room = room;
        this.objectInstanceRoomHandle = objectInstanceRoomHandle;
    }

    public void createRoom(Room room) {
        try {
            createRoom(room, rtiAmbassador.registerObjectInstance(objectClassRoomHandle));
            AttributeHandleValueMap attributeMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(2);
            attributeMap.put(roomTemperatureHandle, encoderFactory.createHLAfloat64BE(room.getTemperature()).toByteArray());
            attributeMap.put(roomHeaterStateHandle, encoderFactory.createHLAboolean(room.heaterState()).toByteArray());
            rtiAmbassador.updateAttributeValues(objectInstanceRoomHandle, attributeMap, null);
            System.out.println("[" +federateTime + "] Created Room: " + room);
        } catch (ObjectInstanceNotKnown | ObjectClassNotPublished | ObjectClassNotDefined | SaveInProgress | RestoreInProgress |
                AttributeNotOwned | AttributeNotDefined | FederateNotExecutionMember | NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void removeRoom() {
        try {
            rtiAmbassador.deleteObjectInstance(objectInstanceRoomHandle, null);
            System.out.println("[" +federateTime + "] Removed Room");
        } catch (DeletePrivilegeNotHeld | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress |
                FederateNotExecutionMember | NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
        room = null;
        objectInstanceRoomHandle = null;
    }

    public void updateRoom(Room room) {
        try {
            this.room = room;
            AttributeHandleValueMap attributeMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(2);
            attributeMap.put(roomTemperatureHandle, encoderFactory.createHLAfloat64BE(room.getTemperature()).toByteArray());
            attributeMap.put(roomHeaterStateHandle, encoderFactory.createHLAboolean(room.heaterState()).toByteArray());
            HLAfloat64Time time = timeFactory.makeTime(federateTime + lookahead);
            rtiAmbassador.updateAttributeValues(objectInstanceRoomHandle, attributeMap, null, time);
            System.out.println("[" +federateTime + "] Updated room: " + room);
        } catch (SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError |
                AttributeNotOwned |AttributeNotDefined | ObjectInstanceNotKnown | InvalidLogicalTime ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    private void createThermostat(Thermostat thermostat, ObjectInstanceHandle objectInstanceThermostatHandle) {
        this.thermostat = thermostat;
        this.objectInstanceThermostatHandle = objectInstanceThermostatHandle;
    }

    public void createThermostat(Thermostat thermostat) {
        try {
            createThermostat(thermostat, rtiAmbassador.registerObjectInstance(objectClassThermostatHandle));
            AttributeHandleValueMap attributeMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(3);
            attributeMap.put(thermostatTemperatureHandle, encoderFactory.createHLAfloat64BE(thermostat.getTemperature()).toByteArray());
            attributeMap.put(thermostatHeaterStateHandle, encoderFactory.createHLAboolean(thermostat.heaterState()).toByteArray());
            attributeMap.put(thermostatTargetTemperatureHandle, encoderFactory.createHLAfloat64BE(thermostat.getTargetTemperature()).toByteArray());
            rtiAmbassador.updateAttributeValues(objectInstanceThermostatHandle, attributeMap, null);
            System.out.println("[" +federateTime + "] Created Thermostat: " + thermostat);
        } catch (ObjectInstanceNotKnown | ObjectClassNotPublished | ObjectClassNotDefined | SaveInProgress | RestoreInProgress |
                AttributeNotOwned | AttributeNotDefined | FederateNotExecutionMember | NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void removeThermostat() {
        try {
            rtiAmbassador.deleteObjectInstance(objectInstanceThermostatHandle, null);
            System.out.println("[" +federateTime + "] Removed Thermostat");
        } catch (DeletePrivilegeNotHeld | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress |
                FederateNotExecutionMember | NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
        thermostat = null;
        objectInstanceThermostatHandle = null;
    }

    public void updateThermostat(Thermostat thermostat) {
        try {
            this.thermostat = thermostat;
            AttributeHandleValueMap attributeMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(3);
            attributeMap.put(thermostatTemperatureHandle, encoderFactory.createHLAfloat64BE(thermostat.getTemperature()).toByteArray());
            attributeMap.put(thermostatHeaterStateHandle, encoderFactory.createHLAboolean(thermostat.heaterState()).toByteArray());
            attributeMap.put(thermostatTargetTemperatureHandle, encoderFactory.createHLAfloat64BE(thermostat.getTargetTemperature()).toByteArray());
            HLAfloat64Time time = timeFactory.makeTime(federateTime + lookahead);
            rtiAmbassador.updateAttributeValues(objectInstanceThermostatHandle, attributeMap, null, time);
            System.out.println("[" +federateTime + "] Updated Thermostat: " + thermostat);
        } catch (SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError |
                AttributeNotOwned |AttributeNotDefined | ObjectInstanceNotKnown | InvalidLogicalTime ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void publishRoom() {
        try {
            rtiAmbassador.publishObjectClassAttributes(objectClassRoomHandle,
                    createHandleSet(roomTemperatureHandle, roomHeaterStateHandle));
            System.out.println("[" +federateTime + "] Published Room");
        } catch (FederateNotExecutionMember | NotConnected | AttributeNotDefined | ObjectClassNotDefined |
                RestoreInProgress | SaveInProgress | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void subscribeRoom() {
        try {
            rtiAmbassador.subscribeObjectClassAttributes(objectClassRoomHandle,
                    createHandleSet(roomTemperatureHandle, roomHeaterStateHandle));
            System.out.println("[" +federateTime + "] Subscribed Room");
        } catch (FederateNotExecutionMember | NotConnected | AttributeNotDefined | ObjectClassNotDefined |
                RestoreInProgress | SaveInProgress | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void publishThermostat() {
        try {
            rtiAmbassador.publishObjectClassAttributes(objectClassThermostatHandle,
                    createHandleSet(thermostatTemperatureHandle, thermostatHeaterStateHandle, thermostatTargetTemperatureHandle));
            System.out.println("[" +federateTime + "] Published Thermostat");
        } catch (FederateNotExecutionMember | NotConnected | AttributeNotDefined | ObjectClassNotDefined |
                RestoreInProgress | SaveInProgress | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    public void subscribeThermostat() {
        try {
            rtiAmbassador.subscribeObjectClassAttributes(objectClassThermostatHandle,
                    createHandleSet(thermostatTemperatureHandle, thermostatHeaterStateHandle, thermostatTargetTemperatureHandle));
            System.out.println("[" +federateTime + "] Subscribed Thermostat");
        } catch (FederateNotExecutionMember | NotConnected | AttributeNotDefined | ObjectClassNotDefined |
                RestoreInProgress | SaveInProgress | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle object, AttributeHandleValueMap attributes) {
        if (object.equals(objectInstanceRoomHandle)) {
            if (attributes.containsKey(roomTemperatureHandle))
                room.setTemperature(attributeToValue(attributes.get(roomTemperatureHandle), Double.class));
            if (attributes.containsKey(roomHeaterStateHandle))
                room.setHeaterState(attributeToValue(attributes.get(roomHeaterStateHandle), Boolean.class));
            roomListeners.forEach(l -> l.updated(room));
            System.out.println("[" +federateTime + "] Reflected Room attributes");
        } else if (object.equals(objectInstanceThermostatHandle)) {
            if (attributes.containsKey(thermostatTemperatureHandle))
                thermostat.setTemperature(attributeToValue(attributes.get(thermostatTemperatureHandle), Double.class));
            if (attributes.containsKey(thermostatHeaterStateHandle))
                thermostat.setHeaterState(attributeToValue(attributes.get(thermostatHeaterStateHandle), Boolean.class));
            if (attributes.containsKey(thermostatTargetTemperatureHandle))
                thermostat.setTargetTemperature(attributeToValue(attributes.get(thermostatTargetTemperatureHandle), Double.class));
            thermostatListeners.forEach(l -> l.updated(thermostat));
            System.out.println("[" +federateTime + "] Reflected Thermostat attributes");
        }
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interaction, ParameterHandleValueMap parameters) {
        System.out.println("[" +federateTime + "] Received interaction: " + interaction + " (" + parameters.size() + ")");
        if (interaction.equals(targetTemperatureUpHandle))
            interactionListeners.forEach(RoomThermostatInteractionListener::targetTemperatureUp);
        else if (interaction.equals(targetTemperatureDownHandle))
            interactionListeners.forEach(RoomThermostatInteractionListener::targetTemperatureDown);
        else if (interaction.equals(requestTemperatureHandle))
            interactionListeners.forEach(RoomThermostatInteractionListener::requestTemperature);
        else if (interaction.equals(provideTemperatureHandle)) {
            try {
                HLAfloat64LE hlaTemp = encoderFactory.createHLAfloat64LE();
                hlaTemp.decode(parameters.getValueReference(temperatureParamHandle));
                final double temperature = hlaTemp.getValue();
                interactionListeners.forEach(l -> l.provideTemperature(temperature));
            } catch (DecoderException ex) {
                System.err.println("[" + federateTime + "] Decoding temperature failed.");
            }
        } else if (interaction.equals(turnHeaterOnHandle))
            interactionListeners.forEach(RoomThermostatInteractionListener::turnHeaterOn);
        else if (interaction.equals(turnHeaterOffHandle))
            interactionListeners.forEach(RoomThermostatInteractionListener::turnHeaterOff);
    }

    @Override
    protected void getHandles() {
        try {
            objectClassRoomHandle = rtiAmbassador.getObjectClassHandle(OBJECT_ROOM);
            objectClassThermostatHandle = rtiAmbassador.getObjectClassHandle(OBJECT_THERMOSTAT);

            roomTemperatureHandle = rtiAmbassador.getAttributeHandle(objectClassRoomHandle, ATTRIBUTE_ROOM_TEMPERATURE);
            roomHeaterStateHandle = rtiAmbassador.getAttributeHandle(objectClassRoomHandle, ATTRIBUTE_ROOM_HEATERSTATE);
            thermostatTemperatureHandle = rtiAmbassador.getAttributeHandle(objectClassThermostatHandle, ATTRIBUTE_THERMOSTAT_TEMPERATURE);
            thermostatHeaterStateHandle = rtiAmbassador.getAttributeHandle(objectClassThermostatHandle, ATTRIBUTE_THERMOSTAT_HEATERSTATE);
            thermostatTargetTemperatureHandle = rtiAmbassador.getAttributeHandle(objectClassThermostatHandle, ATTRIBUTE_THERMOSTAT_TARGETTEMPERATURE);

            targetTemperatureUpHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_TARGETTEMPERATUREUP);
            targetTemperatureDownHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_TARGETTEMPERATUREDOWN);
            requestTemperatureHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_REQUESTTEMPERATURE);
            provideTemperatureHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_PROVIDETEMPERATURE);
            turnHeaterOnHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_TURNHEATERON);
            turnHeaterOffHandle = rtiAmbassador.getInteractionClassHandle(INTERACTION_TURNHEATEROFF);

            temperatureParamHandle = rtiAmbassador.getParameterHandle(provideTemperatureHandle, PARAM_TEMPERATURE);

            interactionMap.put(RoomThermostatInteraction.TARGET_TEMPERATURE_UP, targetTemperatureUpHandle);
            interactionMap.put(RoomThermostatInteraction.TARGET_TEMPERATURE_DOWN, targetTemperatureDownHandle);
            interactionMap.put(RoomThermostatInteraction.REQUEST_TEMPERATURE, requestTemperatureHandle);
            interactionMap.put(RoomThermostatInteraction.PROVIDE_TEMPERATURE, provideTemperatureHandle);
            interactionMap.put(RoomThermostatInteraction.TURN_HEATER_ON, turnHeaterOnHandle);
            interactionMap.put(RoomThermostatInteraction.TURN_HEATER_OFF, turnHeaterOffHandle);
            System.out.println("[" +federateTime + "] Handles set");
        } catch (FederateNotExecutionMember | NotConnected | NameNotFound | RTIinternalError |
                InvalidObjectClassHandle | InvalidInteractionClassHandle ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public void sendEvent(HLAEvent event) {
        try {
            ParameterHandleValueMap parameters = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
            switch(event.getId()) {
                case INTERACTION_TARGETTEMPERATUREUP:
                    rtiAmbassador.sendInteraction(targetTemperatureUpHandle, parameters, null);
                    break;
                case INTERACTION_TARGETTEMPERATUREDOWN:
                    rtiAmbassador.sendInteraction(targetTemperatureDownHandle, parameters, null);
                    break;
                case INTERACTION_REQUESTTEMPERATURE:
                    rtiAmbassador.sendInteraction(requestTemperatureHandle, parameters, null);
                    break;
                case INTERACTION_PROVIDETEMPERATURE:
                    HLAfloat64LE temp = encoderFactory.createHLAfloat64LE(Double.parseDouble(event.getParameters().get("temperature")));
                    parameters.put(temperatureParamHandle, temp.toByteArray());
                    rtiAmbassador.sendInteraction(provideTemperatureHandle, parameters, null);
                    break;
                case INTERACTION_TURNHEATERON:
                    rtiAmbassador.sendInteraction(turnHeaterOnHandle, parameters, null);
                    break;
                case INTERACTION_TURNHEATEROFF:
                    rtiAmbassador.sendInteraction(turnHeaterOffHandle, parameters, null);
                    break;
            }
            System.out.println("[" +federateTime + "] Sent event: " + event +  "(" + event.getParameters().size() + ")");
        } catch (FederateNotExecutionMember | NotConnected | InteractionClassNotPublished | InteractionClassNotDefined |
                InteractionParameterNotDefined | SaveInProgress | RestoreInProgress | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public void addInteractionSub(RoomThermostatInteraction interaction) {
        try {
            if (interactionMap.containsKey(interaction))
                rtiAmbassador.subscribeInteractionClass(interactionMap.get(interaction));
            System.out.println("[" +federateTime + "] Added interaction subscription: " + interaction.name());
        } catch (FederateServiceInvocationsAreBeingReportedViaMOM | InteractionClassNotDefined | SaveInProgress | RestoreInProgress |
                FederateNotExecutionMember | NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public void addInteractionPub(RoomThermostatInteraction interaction) {
        try {
            if (interactionMap.containsKey(interaction))
                rtiAmbassador.publishInteractionClass(interactionMap.get(interaction));
            System.out.println("[" +federateTime + "] Added interaction publisher: " + interaction.name());
        } catch (InteractionClassNotDefined | SaveInProgress | RestoreInProgress | FederateNotExecutionMember |
                NotConnected | RTIinternalError ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String s)
            throws FederateInternalError {
        if (objectClassHandle.equals(objectClassRoomHandle) && room == null) {
            createRoom(new Room(), objectInstanceHandle);
            System.out.println("[" +federateTime + "] Discovered Room");
        } else if (objectClassHandle.equals(objectClassThermostatHandle) && thermostat == null) {
            createThermostat(new Thermostat(), objectInstanceHandle);
            System.out.println("[" +federateTime + "] Discovered Thermostat");
        }
    }

    @Override
    public void provideAttributeValueUpdate(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandleSet, byte[] bytes)
            throws FederateInternalError {
        if (objectInstanceHandle.equals(objectInstanceRoomHandle) && room != null)
            updateRoom(room);
        else if (objectInstanceHandle.equals(objectInstanceThermostatHandle) && thermostat != null)
            updateThermostat(thermostat);
        System.out.println("[" +federateTime + "] Attribute value update provided");
    }

    public void addRoomListener(HLAObjectListener listener) {
        roomListeners.add(listener);
        System.out.println("[" +federateTime + "] Added Room listener");
    }

    public boolean removeRoomListener(HLAObjectListener listener) {
        boolean b = roomListeners.remove(listener);
        System.out.println("[" +federateTime + "] Removed Room listener: " + (b ? "success" : "failed"));
        return b;
    }

    public void addThermostatListener(HLAObjectListener listener) {
        thermostatListeners.add(listener);
        System.out.println("[" +federateTime + "] Added Thermostat listener");
    }

    public boolean removeThermostatListener(HLAObjectListener listener) {
        boolean b = thermostatListeners.remove(listener);
        System.out.println("[" +federateTime + "] Removed Thermostat listener: " + (b ? "success" : "failed"));
        return b;
    }

    private AttributeHandleSet createHandleSet(AttributeHandle... attributeHandles) {
        try {
            AttributeHandleSet attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
            attributes.addAll(Arrays.asList(attributeHandles));
            return attributes;
        } catch (FederateNotExecutionMember | NotConnected ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
        return null;
    }

    private<T> T attributeToValue(byte[] attribute, Class<T> clazz) {
        try {
            if (clazz == Double.class) {
                HLAfloat64BE hlaFloat = encoderFactory.createHLAfloat64BE();
                hlaFloat.decode(attribute);
                return clazz.cast(hlaFloat.getValue());
            } else if (clazz == Boolean.class) {
                HLAboolean hlaBoolean = encoderFactory.createHLAboolean();
                hlaBoolean.decode(attribute);
                return clazz.cast(hlaBoolean.getValue());
            } else if (clazz == Integer.class) {
                HLAinteger64BE hlaInteger = encoderFactory.createHLAinteger64BE();
                hlaInteger.decode(attribute);
                return clazz.cast(hlaInteger.getValue());
            }
        } catch (DecoderException ex) {
            System.err.println("[" + federateTime + "] " + ex.getMessage());
        }
        return clazz.cast(null);
    }

    public static final String  OBJECT_ROOM                             = "Room",
                                OBJECT_THERMOSTAT                       = "Thermostat",

                                ATTRIBUTE_ROOM_TEMPERATURE              = "temperature",
                                ATTRIBUTE_ROOM_HEATERSTATE              = "heaterState",
                                ATTRIBUTE_THERMOSTAT_TEMPERATURE        = "temperature",
                                ATTRIBUTE_THERMOSTAT_TARGETTEMPERATURE  = "targetTemperature",
                                ATTRIBUTE_THERMOSTAT_HEATERSTATE        = "heaterState",

                                INTERACTION_TARGETTEMPERATUREUP         = "TargetTemperatureUp",
                                INTERACTION_TARGETTEMPERATUREDOWN       = "TargetTemperatureDown",
                                INTERACTION_REQUESTTEMPERATURE          = "RequestTemperature",
                                INTERACTION_PROVIDETEMPERATURE          = "ProvideTemperature",
                                INTERACTION_TURNHEATERON                = "TurnHeaterOn",
                                INTERACTION_TURNHEATEROFF               = "TurnHeaterOff",

                                PARAM_TEMPERATURE                       = "temperature";

}
