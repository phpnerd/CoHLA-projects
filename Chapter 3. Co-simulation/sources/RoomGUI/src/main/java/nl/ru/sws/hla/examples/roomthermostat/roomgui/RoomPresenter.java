package nl.ru.sws.hla.examples.roomthermostat.roomgui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import nl.ru.sws.hla.examples.RoomThermostat.models.Room;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by thomas on 14-4-16.
 */
public class RoomPresenter implements Initializable {

    @Inject
    private RoomService roomService;

    @FXML
    TextField txtStepSize, txtCurrentTime;

    @FXML
    Button btnStart, btnStop, btnStep;

    @FXML
    private LineChart<Number, Number> tempChart;

    private final XYChart.Series<Number, Number> samples = new XYChart.Series<>();

    private Timer timer = new Timer("Simulation Timer");
    private TimerTask advanceSimulationTask;
    private boolean isRunning = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NumberAxis xAxis = (NumberAxis)tempChart.getXAxis(),
                yAxis = (NumberAxis)tempChart.getYAxis();
        yAxis.setAutoRanging(true);
//        yAxis.setLowerBound(16d);
//        yAxis.setUpperBound(22d);
        tempChart.getData().add(samples);

        roomService.addRoomListener(obj -> {
            Platform.runLater(() -> {
                if (obj != null && obj instanceof Room) {
                    Room room = (Room) obj;
                    double time = Double.parseDouble(txtCurrentTime.getText());
                    samples.getData().add(new XYChart.Data<>(time, room.getTemperature()));
                }
            });
        });

        roomService.addTimeAdvanceGrantListener(time -> {
            Platform.runLater(() -> {
                txtCurrentTime.setText(Double.toString(time));
            });
        });
    }

    public void startButtonPressed() {
        if (isRunning)
            return;
        isRunning = true;
        double stepSize = getStepSize();
        advanceSimulationTask = new TimerTask() {
            @Override
            public void run() {
                if (!roomService.isAdvancing())
                    roomService.timeAdvanceRequest(stepSize);
            }
        };
        timer.scheduleAtFixedRate(advanceSimulationTask, 0, 10);
    }

    public void stopButtonPressed() {
        if (isRunning) {
            advanceSimulationTask.cancel();
            isRunning = false;
        }
    }

    public void stepButtonPressed() {
        if (!roomService.isAdvancing() && !isRunning)
            roomService.timeAdvanceRequest(getStepSize());
    }

    private double getStepSize() {
        return Double.parseDouble(txtStepSize.getText());
    }

}
