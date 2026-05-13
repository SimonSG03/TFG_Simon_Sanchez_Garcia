module es.simonsg.pmsuite {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires com.zaxxer.hikari;
    requires java.desktop;
    requires com.github.librepdf.openpdf;  // Generación de PDFs



    opens es.simonsg.pmsuite to javafx.fxml;
    exports es.simonsg.pmsuite;
    exports es.simonsg.pmsuite.controller;
    opens es.simonsg.pmsuite.controller to javafx.fxml;
}