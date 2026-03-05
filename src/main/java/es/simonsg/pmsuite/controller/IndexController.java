package es.simonsg.pmsuite.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class IndexController {
    @FXML
    private Label fecha_label;

    @FXML
    private PieChart estadoHabitacionesChart;

    @FXML
    private PieChart situacionHabitacionesChart;

    @FXML private PieChart ocupacionDonut, checkinDonut, checkoutDonut;

    @FXML
    private Label updateLabel;

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
        // ESTADO HABITACIONES (Azul y Gris)
        // 1. Configurar gráficos de arriba (Círculos sólidos)
//        setupPieChart(estadoHabitacionesChart, new double[]{65, 35}, new String[]{"#007bff", "#c0c0c0"});
//        setupPieChart(situacionHabitacionesChart, new double[]{70, 15, 15}, new String[]{"#ff66ff", "#ffcc00", "#cc3300"});
//
//        // 2. Configurar Donuts de abajo (Progreso blanco vs transparente)
//        setupDonut(ocupacionDonut, 0.86);
//        setupDonut(checkinDonut, 0.54);
//        setupDonut(checkoutDonut, 0.80);
    }

    /**private void setupDonut(PieChart chart, double progress) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Progreso", progress),
                new PieChart.Data("Resto", 1 - progress)
        );
        chart.setData(data);
        data.get(0).getNode().setStyle("-fx-pie-color: white;");
        data.get(1).getNode().setStyle("-fx-pie-color: rgba(255,255,255,0.15);"); // El carril gris claro
    }

    private void setupPieChart(PieChart chart, double[] values, String[] colors) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for(int i=0; i<values.length; i++) data.add(new PieChart.Data("", values[i]));
        chart.setData(data);
        for(int i=0; i<colors.length; i++) data.get(i).getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
    } **/


    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        fecha_label.setText(ahora.format(formatter));
    }
}
