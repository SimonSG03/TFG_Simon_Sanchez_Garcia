package es.simonsg.pmsuite;

import es.simonsg.pmsuite.db.GestorBD;
import es.simonsg.pmsuite.db.InicializadorEsquema;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    @Override
    public void init() {
        try {
            InicializadorEsquema.initialize();
            LOGGER.info("Base de datos inicializada correctamente.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar la base de datos. La aplicación continuará sin BD.", e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("index.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("PMSuite");
        stage.setScene(scene);

        // ICONO
        Image icon = new Image(getClass().getResourceAsStream("/images/logo_pmsuite.png"));
        stage.getIcons().add(icon);

        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        GestorBD.getInstance().close();
    }

    public static void main(String[] args) {
        launch();
    }
}