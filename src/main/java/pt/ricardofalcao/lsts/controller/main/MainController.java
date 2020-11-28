package pt.ricardofalcao.lsts.controller.main;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import lombok.Getter;
import pt.ricardofalcao.lsts.Main;
import pt.ricardofalcao.lsts.ServerHandler;
import pt.ricardofalcao.lsts.bluetooth.spp.BluetoothSPPDevice;

public class MainController {

    private Stage wizardStage;

    //

    @FXML
    private ToggleButton powerButton;

    @FXML
    private Circle powerIndicator;

    //

    @FXML
    protected ScrollPane sidebarScrollPane;

    @FXML
    private VBox sidebarPane;

    @FXML
    private Label deviceListLabel;

    @FXML
    private SVGPath favoriteFilterButton;

    @FXML
    private Button refreshDevicesButton;

    @FXML
    private Label selectDeviceWarnLabel;

    @FXML
    private TextArea deviceContent;

    //

    private boolean scanningDevices = false;

    private Map<Long, MainDeviceController> devices = Collections.synchronizedMap(new LinkedHashMap<>());

    @Getter
    private MainDeviceController selectedDevice;

    private boolean filterFavorite;

    //

    private final DiscoveryListener bluetoothDiscoveryListener = new DiscoveryListener() {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main/device.fxml"));
                String friendlyName = btDevice.getFriendlyName(true);
                MainDeviceController controller = new MainDeviceController(MainController.this, friendlyName,
                    btDevice.getBluetoothAddress());
                loader.setController(controller);

                loader.load();
                devices.put(RemoteDeviceHelper.getAddress(btDevice.getBluetoothAddress()), controller);

                Platform.runLater(() -> {
                    populateSidebar();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {

        }

        @Override
        public void inquiryCompleted(int discType) {
            scanningDevices = false;

            Platform.runLater(() -> {
                if (!devices.isEmpty()) {
                    sidebarPane.getChildren().remove(deviceListLabel);
                } else {
                    deviceListLabel.setText("Could not found any device nearby.");
                }

                refreshDevicesButton.setDisable(false);
            });

            System.out.println("Finished scanning devices.");
        }
    };

    /*

     */

    @FXML
    private void initialize() {
        this.filterFavorite = Main.config.main.filterFavorite;
        filterFavoriteUpdateUI();

        powerButton.setOnMouseClicked(this::powerButtonClicked);

        sidebarScrollPane.setOnMouseClicked(this::sidebarScrollPaneClicked);
        favoriteFilterButton.setOnMouseClicked(this::favoriteFilterClicked);

        sidebarPane.getChildren().remove(deviceListLabel);
        deviceListLabel.prefWidthProperty().bind(sidebarScrollPane.widthProperty());

        refreshDevicesButton.setDisable(true);
        refreshDevicesButton.setOnMouseClicked(this::refreshDevicesClicked);

        deviceContent.setManaged(false);
        deviceContent.setVisible(false);

        Main.server.handle.setDataCallback((device, data) -> {
            long address = RemoteDeviceHelper.getAddress(device.getDevice().getBluetoothAddress());
            MainDeviceController controller = devices.get(address);

            if (controller != null) {
                controller.addMessage(data);
            }
        });

        Main.server.handle.setConnectCallback((device) -> {
            long address = RemoteDeviceHelper.getAddress(device.getDevice().getBluetoothAddress());
            MainDeviceController controller = devices.get(address);

            if (controller != null) {
                controller.setConnected(true);
            }
        });

        Main.server.handle.setDisconnectCallback((device) -> {
            long address = RemoteDeviceHelper.getAddress(device.getDevice().getBluetoothAddress());
            MainDeviceController controller = devices.get(address);

            if (controller != null) {
                controller.setConnected(false);
            }
        });

        Main.gui.getPrimaryStage().setOnCloseRequest((event) -> {
            if (wizardStage != null) {
                wizardStage.close();
            }
        });
    }

    /*
        POWER BUTTON
     */

    private void powerButtonClicked(MouseEvent mouseEvent) {
        ServerHandler serverHandler = Main.server;

        powerButton.setDisable(true);

        if (serverHandler.isRunning()) {
            System.out.println("Stopping bluetooth server");

            serverHandler.stopServerAsync().thenAccept(this::powerButtonStopCallback);
            return;
        }

        System.out.println("Starting bluetooth server");

        serverHandler.startServerAsync().thenAccept(this::powerButtonStartCallback);
    }

    private void powerButtonStopCallback(boolean success) {
        this.scanningDevices = false;

        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            agent.cancelInquiry(this.bluetoothDiscoveryListener);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Platform.runLater(() -> {
            powerButton.setDisable(false);
            refreshDevicesButton.setDisable(true);
            sidebarPane.getChildren().remove(deviceListLabel);

            powerButton.setText("OFF");
            powerIndicator.setFill(Color.rgb(200, 200, 200));

            sidebarPane.getChildren().clear();

            selectedDevice = null;
            selectedDeviceUpdateUI();
        });
    }

    private void powerButtonStartCallback(boolean success) {
        Platform.runLater(() -> {
            powerButton.setDisable(false);
            refreshDevicesButton.setDisable(false);

            powerButton.setText("ON");
            powerIndicator.setFill(Color.rgb(40, 180, 40));
        });

        CompletableFuture.runAsync(this::scanDevices);
    }

    /*
        SIDEBAR
     */

    private void sidebarScrollPaneClicked(MouseEvent mouseEvent) {
        PickResult pick = mouseEvent.getPickResult();

        Node node = pick.getIntersectedNode();
        if (node != null && (node = node.getParent()) != null && sidebarScrollPane.equals(node.getParent())) {
            setSelectedDevice(null);
        }
    }

    private void scanDevices() {
        if (this.scanningDevices) {
            return;
        }

        this.scanningDevices = true;
        System.out.println("Scanning devices...");

        Platform.runLater(() -> {
            if(!sidebarPane.getChildren().contains(deviceListLabel)) {
                sidebarPane.getChildren().add(deviceListLabel);
            }

            deviceListLabel.setText("Scanning for nearby devices...");

            this.refreshDevicesButton.setDisable(true);
        });

        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();

            agent.startInquiry(DiscoveryAgent.GIAC, bluetoothDiscoveryListener);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void populateSidebar() {
        sidebarPane.getChildren().clear();

        List<MainDeviceController> _devices = new ArrayList<>();
        _devices.addAll(this.devices.values());

        Collections.sort(_devices, (o1, o2) -> {
            if (o1.isFavorite() == o2.isFavorite()) {
                return o1.getName().compareTo(o2.getName());
            }

            return o1.isFavorite() ? -1 : 1;
        });

        for (MainDeviceController device : _devices) {
            if (this.filterFavorite && !device.isFavorite()) {
                continue;
            }

            sidebarPane.getChildren().add(device.root);
        }
    }

    private void refreshDevicesClicked(MouseEvent mouseEvent) {
        this.devices.clear();
        sidebarPane.getChildren().clear();

        CompletableFuture.runAsync(this::scanDevices);
    }

    /*
        FAVORITE
     */


    private void favoriteFilterClicked(MouseEvent mouseEvent) {
        this.filterFavorite = !this.filterFavorite;
        filterFavoriteUpdateUI();
        this.populateSidebar();

        Main.config.main.filterFavorite = this.filterFavorite;
    }

    private void filterFavoriteUpdateUI() {
        this.favoriteFilterButton.setFill(this.filterFavorite ? Color.rgb(238, 196, 82) : Color.rgb(200, 200, 200));
    }

    /*
        MISC FUNCTIONS
     */

    public void setSelectedDevice(MainDeviceController controller) {
        if (Objects.equals(controller, this.selectedDevice)) {
            return;
        }

        if (this.selectedDevice != null) {
            this.selectedDevice.unselect();
        }

        this.selectedDevice = controller;

        selectedDeviceUpdateUI();
    }

    protected void selectedDeviceUpdateUI() {
        if(this.selectedDevice != null) {
            deviceContent.setManaged(true);
            deviceContent.setVisible(true);

            selectDeviceWarnLabel.setManaged(false);
            selectDeviceWarnLabel.setVisible(false);

            String content = String.join("\n", selectedDevice.getMessageHistory());
            deviceContent.setText(content);
            deviceContent.positionCaret(content.length());
        } else {
            deviceContent.setManaged(false);
            deviceContent.setVisible(false);

            selectDeviceWarnLabel.setManaged(true);
            selectDeviceWarnLabel.setVisible(true);
        }
    }


}
