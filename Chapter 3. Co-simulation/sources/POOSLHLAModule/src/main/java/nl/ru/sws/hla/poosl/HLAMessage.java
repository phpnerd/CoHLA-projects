package nl.ru.sws.hla.poosl;

import com.google.gson.JsonElement;
import hla.tools.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 4-4-16.
 */
public class HLAMessage {

    private HLAMessageType type;
    private String name;
    private List<Object> parameters;

    public HLAMessage(HLAMessageType type, String name) {
        this.type = type;
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public HLAMessage(HLAMessageType type, String name, List<Object> parameters) {
        this(type, name);
        this.parameters = parameters;
    }

    public void addParameter(Object parameter) {
        parameters.add(parameter);
    }

    public<T> T getParameter(int i, Class<T> clazz) {
        return Util.convert(parameters.get(i), clazz);
    }

    public HLAMessageType getType() {
        return type;
    }

    public void setType(HLAMessageType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "{type: " + type + ", name: " + name + ", parameters: " + parameters + "};";
    }

    public boolean isAttributeUpdate() {
        return type == HLAMessageType.ATTRIBUTE && parameters.size() == 1;
    }

    public boolean isTimeAdvanceRequest() {
        return type == HLAMessageType.TIME && name.equals(STR_TIMEADVANCEREQUEST) && parameters.size() == 1;
    }

    public boolean isInteraction() {
        return type == HLAMessageType.INTERACTION;
    }

    public static final String  STR_TIMEADVANCEREQUEST      = "timeAdvanceRequest",
                                STR_TIMEADVANCEGRANT        = "timeAdvanceGrant";
}
