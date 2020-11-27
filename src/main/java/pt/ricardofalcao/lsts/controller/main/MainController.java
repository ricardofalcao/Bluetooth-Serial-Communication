package pt.ricardofalcao.lsts.controller.main;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import pt.ricardofalcao.lsts.Main;
import pt.ricardofalcao.lsts.ServerHandler;

public class MainController {

    private Stage wizardStage;

    //

    @FXML
    private ToggleButton powerButton;

    @FXML
    private Circle powerIndicator;

    //

    @FXML
    private ScrollPane sidebarScrollPane;

    @FXML
    private VBox sidebarPane;

    //

    @FXML
    private Button deviceAddButton;

    //

    private DeviceController selectedDevice;

    /*

     */

    @FXML
    private void initialize() {
        powerButton.setOnMouseClicked(this::powerButtonClicked);

        sidebarScrollPane.setOnMouseClicked(this::sidebarScrollPaneClicked);
        deviceAddButton.setOnMouseClicked(this::deviceAddClicked);

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
        Platform.runLater(() -> {
            powerButton.setDisable(false);
            powerButton.setText("OFF");
            powerIndicator.setFill(Color.rgb(185, 185, 185));

            sidebarPane.getChildren().clear();
        });
    }

    private void powerButtonStartCallback(boolean success) {
        Platform.runLater(() -> {
            powerButton.setDisable(false);
            powerButton.setText("ON");
            powerIndicator.setFill(Color.rgb(40, 180, 40));

            try {
                for(int i = 0; i < 5; i++) {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main/device.fxml"));
                    loader.setController(new DeviceController(MainController.this, "Device #" + (i + 1), UUID.randomUUID().toString()));

                    Parent deviceRoot = loader.load();
                    sidebarPane.getChildren().add(deviceRoot);
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    /*
        SIDEBAR
     */

    private void sidebarScrollPaneClicked(MouseEvent mouseEvent) {
        PickResult pick = mouseEvent.getPickResult();

        Node node = pick.getIntersectedNode();
        if(node != null && (node = node.getParent()) != null && sidebarScrollPane.equals(node.getParent())) {
            setSelectedDevice(null);
        }
    }

    /*
        DEVICE MANAGEMENT
     */

    private void deviceAddClicked(MouseEvent mouseEvent) {
        try {
            if (wizardStage != null) {
                wizardStage.requestFocus();
                return;
            }

            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/wizard/wizard.fxml"));

            wizardStage = new Stage();
            wizardStage.setTitle("Add new device");
            wizardStage.setScene(new Scene(root));

            wizardStage.setOnCloseRequest((event) -> {
                wizardStage = null;
            });

            wizardStage.show();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
        MISC FUNCTIONS
     */

    public void setSelectedDevice(DeviceController controller) {
        if(Objects.equals(controller, this.selectedDevice)) {
            return;
        }

        if (this.selectedDevice != null) {
            this.selectedDevice.unselect();
        }

        this.selectedDevice = controller;
    }


}
