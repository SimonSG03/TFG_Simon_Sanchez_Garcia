module es.simonsg.pmsuite {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires jakarta.mail;


    opens es.simonsg.pmsuite to javafx.fxml;
    exports es.simonsg.pmsuite;
    exports es.simonsg.pmsuite.controller;
    opens es.simonsg.pmsuite.controller to javafx.fxml;
}