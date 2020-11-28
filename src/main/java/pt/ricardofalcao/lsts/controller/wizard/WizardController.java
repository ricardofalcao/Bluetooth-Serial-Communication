package pt.ricardofalcao.lsts.controller.wizard;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import pt.ricardofalcao.lsts.controller.main.MainController;
import pt.ricardofalcao.lsts.controller.main.MainDeviceController;

public class WizardController {

    @FXML
    private ScrollPane deviceScrollPane;

    @FXML
    private VBox devicePane;

    //

    private WizardDeviceController selectedDevice;

    @FXML
    private void initialize() {
        try {
            for(int i = 0; i < 5; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main/device.fxml"));
                loader.setController(new WizardDeviceController(WizardController.this, "Device #" + (i + 1), UUID.randomUUID().toString()));

                Parent deviceRoot = loader.load();
                devicePane.getChildren().add(deviceRoot);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        deviceScrollPane.setOnMouseClicked(this::deviceScrollPaneClicked);
    }

    private void deviceScrollPaneClicked(MouseEvent mouseEvent) {
        PickResult pick = mouseEvent.getPickResult();

        Node node = pick.getIntersectedNode();
        if(node != null && (node = node.getParent()) != null && deviceScrollPane.equals(node.getParent())) {
            setSelectedDevice(null);
        }
    }

    /*
        MISC FUNCTIONS
     */

    public void setSelectedDevice(WizardDeviceController controller) {
        if(Objects.equals(controller, this.selectedDevice)) {
            return;
        }

        if (this.selectedDevice != null) {
            this.selectedDevice.unselect();
        }

        this.selectedDevice = controller;
    }

}
