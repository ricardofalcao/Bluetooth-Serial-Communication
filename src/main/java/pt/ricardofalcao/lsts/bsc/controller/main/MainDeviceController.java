package pt.ricardofalcao.lsts.bsc.controller.main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.Main;
import pt.ricardofalcao.lsts.bsc.bluetooth.BluetoothClient;

@RequiredArgsConstructor
public class MainDeviceController {

    @RequiredArgsConstructor
    private static enum DeviceStatus {

        IDLE(Color.rgb(200, 200, 200)),
        CONNECTING(Color.rgb(219, 161, 129)),
        CONNECTED(Color.rgb(40, 180, 40));

        private final Color color;

    }

    private final MainController mainController;

    @Getter
    private final String name;

    @Getter
    private final String address;

    /*

     */

    @FXML
    public HBox root;

    @FXML
    private Label deviceName;

    @FXML
    private Label deviceAddress;

    @FXML
    private SVGPath favoriteButton;

    @FXML
    private Circle deviceStatusCircle;

    /*

     */

    private BluetoothClient bluetoothClient;

    @Getter
    private boolean favorite;

    private DeviceStatus status;

    @Getter
    private List<String> messageHistory = new ArrayList<>();

    /*

     */

    @FXML
    private void initialize() {
        this.favorite = Main.config.main.deviceIsFavorite(this.address);
        favoriteUpdateUI();

        this.status = DeviceStatus.IDLE;
        statusUpdateUI();

        this.deviceName.setText(this.name);
        this.deviceAddress.setText(this.address);

        this.root.prefWidthProperty().bind(this.mainController.sidebarScrollPane.widthProperty());
        this.root.setOnMouseClicked(this::rootClick);

        this.favoriteButton.setOnMouseClicked(this::favoriteClick);
    }

    /*
        BLUETOOTH
     */

    public void attachBluetoothClient(BluetoothClient client, String connectionUrl) {
        this.bluetoothClient = client;

        this.status = DeviceStatus.CONNECTING;
        this.statusUpdateUI();

        this.bluetoothClient.setConnectCallback(this::bluetoothConnect);
        this.bluetoothClient.setDisconnectCallback(this::bluetoothDisconnect);
        this.bluetoothClient.setDataCallback(this::bluetoothData);

        CompletableFuture.runAsync(() ->  {
            try {
                bluetoothClient.connect(connectionUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void detachBluetooth() {
        if (this.bluetoothClient == null) {
            return;
        }

        try {
            this.bluetoothClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bluetoothConnect() {
        Platform.runLater(() -> {
            this.status = DeviceStatus.CONNECTED;
            this.statusUpdateUI();
        });
    }

    private void bluetoothDisconnect() {
        Platform.runLater(() -> {
            this.status = DeviceStatus.IDLE;
            this.statusUpdateUI();
        });
    }

    private void bluetoothData(String data) {
        this.addMessage(data);
    }

    /*
        SELECTION
     */

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private void rootClick(MouseEvent event) {
        this.root.pseudoClassStateChanged(SELECTED, true);

        this.mainController.setSelectedDevice(this);
    }

    protected void unselect() {
        this.root.pseudoClassStateChanged(SELECTED, false);
    }

    /*
        FAVORITE
     */

    private void favoriteClick(MouseEvent mouseEvent) {
        this.favorite = !this.favorite;

        favoriteUpdateUI();
        Main.config.main.deviceSetFavorite(this.address, this.favorite);

        this.mainController.populateSidebar();
    }

    private void favoriteUpdateUI() {
        this.favoriteButton.setFill(this.favorite ? Color.rgb(238, 196, 82) : Color.rgb(200, 200, 200));
    }

    /*
        STATUS
     */

    private void statusUpdateUI() {
        this.deviceStatusCircle.setFill(this.status.color);
    }

    /*
        MESSAGE HISTORY
     */

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private void addMessage(String data) {
        this.messageHistory.add(String.format("[%s] %s", dateTimeFormatter.format(LocalDateTime.now()), data));

        if (this.equals(mainController.getSelectedDevice())) {
            mainController.selectedDeviceUpdateUI();
        }
    }
}
