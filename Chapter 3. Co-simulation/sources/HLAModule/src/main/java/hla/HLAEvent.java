package hla;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Nägele on 10-12-15.
 */
public class HLAEvent {

    protected String id;
    protected Map<String, String> parameters;

    public HLAEvent() {
        parameters = new HashMap<>();
    }

    public HLAEvent(String id) {
        this();
        this.id = id;
    }

    public HLAEvent(String id, Map<String, String> parameters) {
        this(id);
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HLAEvent event = (HLAEvent) obj;
        if (!id.equals(event.id))
            return false;
        if (!parameters.equals(event.parameters))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = id != null ? id.hashCode() : 0;
        hash = 67 * hash + (parameters != null ? parameters.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "HLAEvent{id=\"" + id + "\", parameters=" + parameters + "}";
    }

}
