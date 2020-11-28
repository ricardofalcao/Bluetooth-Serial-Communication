package pt.ricardofalcao.lsts.controller.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
import pt.ricardofalcao.lsts.Main;

@RequiredArgsConstructor
public class MainDeviceController {

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
    private Circle deviceConnectedCircle;

    /*

     */

    @Getter
    private boolean favorite;

    private boolean connected;

    @Getter
    private List<String> messageHistory = new ArrayList<>();

    /*

     */

    @FXML
    private void initialize() {
        this.favorite = Main.config.main.deviceIsFavorite(this.address);
        favoriteUpdateUI();

        this.connected = Main.server.handle.getConnectedDevice(this.address) != null;
        connectedUpdateUI();

        this.deviceName.setText(this.name);
        this.deviceAddress.setText(this.address);

        this.root.prefWidthProperty().bind(this.mainController.sidebarScrollPane.widthProperty());
        this.root.setOnMouseClicked(this::rootClick);

        this.favoriteButton.setOnMouseClicked(this::favoriteClick);
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
        CONNECTED
     */

    protected void setConnected(boolean value) {
        this.connected = value;
        connectedUpdateUI();
    }

    private void connectedUpdateUI() {
        this.deviceConnectedCircle.setFill(this.connected ? Color.rgb(40, 180, 40) : Color.rgb(200, 200, 200));
    }

    /*
        MESSAGE HISTORY
     */

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void addMessage(String data) {
        this.messageHistory.add(String.format("[%s] %s", dateTimeFormatter.format(LocalDateTime.now()), data));

        if (this.equals(mainController.getSelectedDevice())) {
            mainController.selectedDeviceUpdateUI();
        }
    }
}
