package pt.ricardofalcao.lsts.controller.wizard;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WizardDeviceController {

    private final WizardController wizardController;

    private final String name;

    private final String address;

    /*

     */

    @FXML
    private HBox root;

    @FXML
    private Label deviceName;

    @FXML
    private Label deviceAddress;

    @FXML
    private void initialize() {
        this.deviceName.setText(this.name);
        this.deviceAddress.setText(this.address);

        this.root.setOnMouseClicked(this::rootClick);
    }

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private void rootClick(MouseEvent event) {
        this.root.pseudoClassStateChanged(SELECTED, true);

        this.wizardController.setSelectedDevice(this);
    }

    protected void unselect() {
        this.root.pseudoClassStateChanged(SELECTED, false);
    }

}
