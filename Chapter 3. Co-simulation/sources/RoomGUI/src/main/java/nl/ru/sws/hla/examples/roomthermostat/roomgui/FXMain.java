package nl.ru.sws.hla.examples.roomthermostat.roomgui;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by thomas on 14-4-16.
 */
public class FXMain extends Application {

    private RoomService roomService;

    @Override
    public void start(Stage stage) {
        try {
            roomService = Injector.instantiateModelOrService(RoomService.class);
            RoomView view = new RoomView();
            Scene scene = new Scene(view.getView());
            stage.setTitle("Room");
            final String uri = getClass().getResource("room.css").toExternalForm();
            scene.getStylesheets().add(uri);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        roomService.stop();
        Injector.forgetAll();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}

