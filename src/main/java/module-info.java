module bluetooth_serial_comm {
    requires javafx.controls;
    requires javafx.fxml;

    requires static lombok;

    requires bluecove;
    requires com.google.gson;

    opens pt.ricardofalcao.lsts.bsc.controller.main to javafx.fxml;

    exports pt.ricardofalcao.lsts.bsc;
}