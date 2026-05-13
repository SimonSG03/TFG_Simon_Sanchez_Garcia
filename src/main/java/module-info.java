module es.simonsg.pmsuite {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;
    requires java.desktop;   // java.awt.Color usado por InvoicePdfGenerator
    requires com.github.librepdf.openpdf;  // Generación de PDFs
    requires jakarta.mail;

    opens es.simonsg.pmsuite to javafx.fxml;
    exports es.simonsg.pmsuite;
    exports es.simonsg.pmsuite.controller;
    exports es.simonsg.pmsuite.model;
    exports es.simonsg.pmsuite.db;
    exports es.simonsg.pmsuite.util;
    opens es.simonsg.pmsuite.controller to javafx.fxml;
}