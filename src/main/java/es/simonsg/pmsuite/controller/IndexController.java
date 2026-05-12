package es.simonsg.pmsuite.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IndexController {

    @FXML private Label fecha_label;
    @FXML private BorderPane rootBorderPane;

    private Node vistaInicio;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        actualizarFechaHora();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> actualizarFechaHora())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /** Abre el panel Central reemplazando el contenido central del BorderPane. */
    @FXML
    private void abrirCentral() {
        try {
            // Guardar la vista de inicio la primera vez
            if (vistaInicio == null) {
                vistaInicio = rootBorderPane.getCenter();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/es/simonsg/pmsuite/central.fxml")
            );
            Node centralView = loader.load();
            CentralController centralController = loader.getController();
            centralController.setIndexController(this);

            rootBorderPane.setCenter(centralView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Restaura la cuadrícula de módulos (vista principal). */
    public void mostrarInicio() {
        if (vistaInicio != null) {
            rootBorderPane.setCenter(vistaInicio);
        }
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        fecha_label.setText(ahora.format(formatter));
    }
}
