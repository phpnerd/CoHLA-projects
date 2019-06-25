package nl.ru.sws.hla.examples.roomthermostat.thermostatgui;

import hla.HLAEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import nl.ru.sws.hla.examples.RoomThermostat.RoomThermostatHLAImplementation;
import nl.ru.sws.hla.examples.RoomThermostat.models.Thermostat;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by thomas on 14-4-16.
 */
public class ThermostatPresenter implements Initializable {

    @FXML
    Label lblTemperature, lblTarget;

    @FXML
    Circle crcHeaterState;

    @FXML
    Button increaseTarget, decreaseTarget;

    @Inject
    ThermostatService thermostatService;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        thermostatService.addThermostatListener(obj -> {
            Platform.runLater(() -> {
                if (obj != null && obj instanceof Thermostat) {
                    Thermostat thermostat = (Thermostat) obj;
                    lblTemperature.setText(Double.toString(thermostat.getTemperature()));
                    lblTarget.setText(Double.toString(thermostat.getTargetTemperature()));
                    crcHeaterState.setFill(Paint.valueOf(thermostat.heaterState() ? "red" : "blue"));
                }
            });
        });
    }

    public void increaseTargetButtonPressed() {
        thermostatService.sendEvent(new HLAEvent(RoomThermostatHLAImplementation.INTERACTION_TARGETTEMPERATUREUP));
    }

    public void decreaseTargetButtonPressed() {
        thermostatService.sendEvent(new HLAEvent(RoomThermostatHLAImplementation.INTERACTION_TARGETTEMPERATUREDOWN));
    }

}
