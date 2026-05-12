package es.simonsg.pmsuite.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class SceneBuilder {

    private static BorderPane root;

    public static void setRoot(BorderPane borderPane) {
        root = borderPane;
    }

    public static void load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneBuilder.class.getResource("/es/simonsg/pmsuite/" + fxml)
            );

            Node view = loader.load();
            root.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
