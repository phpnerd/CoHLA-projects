package nl.ru.sws.hla.examples.roomthermostat.thermostatgui;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by thomas on 14-4-16.
 */
public class FXMain extends Application {

    private ThermostatService thermostatService;

    @Override
    public void start(Stage stage) {
        try {
            thermostatService = Injector.instantiateModelOrService(ThermostatService.class);
            ThermostatView view = new ThermostatView();
            Scene scene = new Scene(view.getView());
            stage.setTitle("Thermostat");
            final String uri = getClass().getResource("thermostat.css").toExternalForm();
            scene.getStylesheets().add(uri);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        thermostatService.stop();
        Injector.forgetAll();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
