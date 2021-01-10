package pt.ricardofalcao.lsts.bsc.controller.main;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import lombok.Getter;
import pt.ricardofalcao.lsts.bsc.Constants;
import pt.ricardofalcao.lsts.bsc.Main;
import pt.ricardofalcao.lsts.bsc.bluetooth.spp.BluetoothSPPClient;
import pt.ricardofalcao.lsts.bsc.device.AbstractDevice;
import pt.ricardofalcao.lsts.bsc.controller.device.AbstractDeviceController;
import pt.ricardofalcao.lsts.bsc.device.EnumDevice;

public class MainController {

    private Stage wizardStage;

    //

    @FXML
    public ScrollPane sidebarScrollPane;

    @FXML
    private VBox sidebarPane;

    @FXML
    private Label deviceListLabel;

    @FXML
    private Button refreshDevicesButton;

    //

    @FXML
    private VBox contentBox;

    @FXML
    private Label selectDeviceWarnLabel;

    //

    private boolean scanningDevices = false;

    private List<Integer> pendingDevices = Collections.synchronizedList(new ArrayList<>());

    //

    private Map<Long, AbstractDeviceController> devices = Collections.synchronizedMap(new LinkedHashMap<>());

    @Getter
    private AbstractDeviceController selectedDevice;

    //

    private final DiscoveryListener bluetoothDiscoveryListener = new DiscoveryListener() {

        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            try {
                System.out.println("Found device " + btDevice.toString());
                LocalDevice localDevice = LocalDevice.getLocalDevice();

                int[] attrs = new int[] { 0X0100 };
                UUID[] uuidSet = new UUID[] {Constants.BLUETOOTH_SPP_SERVICE_UUID };

                int transID = localDevice.getDiscoveryAgent().searchServices(attrs, uuidSet, btDevice, bluetoothServiceListener);
                pendingDevices.add(transID);
            } catch (Exception ex) {
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
            if(pendingDevices.isEmpty()) {
                _endBluetoothDiscovery();
            }
        }
    };

    private final DiscoveryListener bluetoothServiceListener = new DiscoveryListener() {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            System.out.println(String.format("Discovered services for %d", transID));

            for(ServiceRecord record : servRecord) {
                try {
                    String url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

                    if (url == null) {
                        continue;
                    }

                    DataElement serviceName = record.getAttributeValue(0x0100);
                    if (serviceName == null) {
                        continue;
                    }

                    System.out.println(String.format("Service found %s. (%s)", url, serviceName.getValue()));

                    AbstractDevice device = EnumDevice.getDevice((String) serviceName.getValue());
                    if(device == null) {
                        System.out.println(String.format("Invalid service: %s", serviceName.getValue()));
                        return;
                    }

                    RemoteDevice host = record.getHostDevice();

                    String friendlyName = host.getFriendlyName(false);
                    AbstractDeviceController controller = device.buildController(MainController.this, friendlyName, host.getBluetoothAddress());

                    // Sidebar
                    FXMLLoader loader = new FXMLLoader(device.getSidebarUiPath());
                    loader.setController(controller);
                    loader.load();

                    // Content
                    loader = new FXMLLoader(device.getContentUiPath());
                    loader.setController(controller);
                    loader.load();

                    devices.put(RemoteDeviceHelper.getAddress(host.getBluetoothAddress()), controller);

                    Platform.runLater(() -> {
                        populateSidebar();
                    });

                    BluetoothSPPClient client = new BluetoothSPPClient();
                    controller.attachBluetoothClient(client, url);

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            pendingDevices.remove((Integer) transID);

            System.out.println("serviceSearchCompleted");

            if (pendingDevices.isEmpty()) {
                _endBluetoothDiscovery();
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
        }
    };

    private void _endBluetoothDiscovery() {
        scanningDevices = false;
        pendingDevices.clear();

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



    /*

     */

    @FXML
    private void initialize() {
        sidebarScrollPane.setOnMouseClicked(this::sidebarScrollPaneClicked);

        sidebarPane.getChildren().remove(deviceListLabel);
        deviceListLabel.prefWidthProperty().bind(sidebarScrollPane.widthProperty());

        refreshDevicesButton.setOnMouseClicked(this::refreshDevicesClicked);

        Main.gui.getPrimaryStage().setOnCloseRequest((event) -> {
            detachConnectedDevices();

            if (wizardStage != null) {
                wizardStage.close();
            }
        });
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
        this.pendingDevices.clear();

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
        boolean hasLabel = sidebarPane.getChildren().contains(deviceListLabel);

        sidebarPane.getChildren().clear();

        List<AbstractDeviceController> _devices = new ArrayList<>();
        _devices.addAll(this.devices.values());

        Collections.sort(_devices, Comparator.comparing(AbstractDeviceController::getName));

        for (AbstractDeviceController device : _devices) {
            sidebarPane.getChildren().add(device.getSidebarItem());
        }

        if (hasLabel) {
            sidebarPane.getChildren().add(deviceListLabel);
        }
    }

    private void detachConnectedDevices() {
        for (AbstractDeviceController value : this.devices.values()) {
            value.detach();
        }

        this.devices.clear();

        boolean hasLabel = sidebarPane.getChildren().contains(deviceListLabel);
        sidebarPane.getChildren().clear();

        if (hasLabel) {
            sidebarPane.getChildren().add(deviceListLabel);
        }
    }

    private void refreshDevicesClicked(MouseEvent mouseEvent) {
        detachConnectedDevices();

        CompletableFuture.runAsync(this::scanDevices);
    }

    /*
        MISC FUNCTIONS
     */

    public void setSelectedDevice(AbstractDeviceController controller) {
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
            contentBox.getChildren().clear();
            contentBox.getChildren().add(this.selectedDevice.getContentItem());
        } else {
            contentBox.getChildren().clear();
            contentBox.getChildren().add(selectDeviceWarnLabel);
        }
    }


}
