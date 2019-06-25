package nl.ru.sws.hla.poosl;

import com.google.gson.annotations.SerializedName;

/**
 * Created by thomas on 4-4-16.
 */
public enum HLAMessageType {

    @SerializedName("time")
    TIME("time"),
    @SerializedName("attribute")
    ATTRIBUTE("attribute"),
    @SerializedName("interaction")
    INTERACTION("interaction");

    private final String stringValue;

    HLAMessageType(String value) {
        this.stringValue = value;
    }

    private String stringValue() {
        return stringValue;
    }

    public String toString() {
        return stringValue;
    }

}
