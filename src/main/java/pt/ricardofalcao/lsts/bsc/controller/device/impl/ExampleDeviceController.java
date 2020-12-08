package pt.ricardofalcao.lsts.bsc.controller.device.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.Constants;
import pt.ricardofalcao.lsts.bsc.bluetooth.BluetoothClient;
import pt.ricardofalcao.lsts.bsc.controller.main.MainController;
import pt.ricardofalcao.lsts.bsc.controller.device.AbstractDeviceController;

public class ExampleDeviceController extends AbstractDeviceController {

    private static final ScheduledExecutorService TASK_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    /*

     */

    @RequiredArgsConstructor
    private static enum DeviceStatus {

        IDLE(Color.rgb(200, 200, 200)),
        CONNECTING(Color.rgb(219, 161, 129)),
        CONNECTED(Color.rgb(40, 180, 40));

        private final Color color;

    }

    public ExampleDeviceController(MainController mainController, String name,
        String address) {
        super(mainController, name, address);
    }

    /*
        SIDEBAR
     */

    private boolean sidebarInitialized;

    @FXML
    public HBox sidebarRoot;

    @FXML
    private Label sidebarDeviceName;

    @FXML
    private Label sidebarDeviceAddress;

    @FXML
    private Circle sidebarDeviceStatusCircle;

    /*
        CONTENT
     */

    private boolean contentInitialized;

    @FXML
    public VBox contentRoot;

    @FXML
    private Label contentTimeLabel;

    @FXML
    private Button contentSyncTimeButton;

    @FXML
    private Label contentBatteryLabel;

    @FXML
    private TextField contentWakeTextInput;

    @FXML
    private Button contentWakeButton;

    @FXML
    private TextArea contentTextArea;

    /*
        DEVICE DATA
     */

    private DeviceStatus status;

    private long lastDateTime;

    private long lastDateTimeReference;

    /*

     */

    @FXML
    private void initialize() {
        if (!sidebarInitialized && sidebarRoot != null) {
            sidebarInitialized = true;

            this.sidebarDeviceName.setText(this.name);
            this.sidebarDeviceAddress.setText(this.address);

            this.sidebarRoot.prefWidthProperty().bind(this.mainController.sidebarScrollPane.widthProperty());
            this.sidebarRoot.setOnMouseClicked(this::rootClick);
        }

        if(!contentInitialized && contentRoot != null) {
            contentInitialized = true;

            this.status = DeviceStatus.IDLE;
            statusUpdateUI();

            this.contentSyncTimeButton.setOnMouseClicked(this::contentSyncTimeClicked);
            this.contentWakeButton.setOnMouseClicked(this::contentWakeButtonClicked);

            TASK_EXECUTOR.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        }
    }

    /*
        ROOT
     */
    public Node getSidebarItem() {
        return this.sidebarRoot;
    }

    @Override
    public Node getContentItem() {
        return this.contentRoot;
    }

    /*
        TASK
     */

    private void tick() {
        System.out.println("tick");

        try {
            if (this.contentTimeLabel != null) {
                if(this.lastDateTime == 0L) {
                    Platform.runLater(() -> {
                        this.contentTimeLabel.setText("No time");
                    });

                } else {
                    long time = lastDateTime + (System.currentTimeMillis() - lastDateTimeReference);
                    LocalDateTime dateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();

                    Platform.runLater(() -> {
                        this.contentTimeLabel.setText(Constants.DATE_TIME_FORMATTER.format(dateTime));
                    });
                }
            }
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void detach() {
        super.detach();

        System.out.println("Detaching");
        TASK_EXECUTOR.shutdownNow();
    }

    /*
        BLUETOOTH
     */

    public void attachBluetoothClient(BluetoothClient client, String connectionUrl) {
        super.attachBluetoothClient(client, connectionUrl);

        this.status = DeviceStatus.CONNECTING;
        this.statusUpdateUI();

        this.bluetoothClient.setConnectCallback(this::bluetoothConnect);
        this.bluetoothClient.setDisconnectCallback(this::bluetoothDisconnect);
        this.bluetoothClient.setDataCallback(this::bluetoothData);
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
        String[] split = data.split(":");

        String command = split[0];

        switch(command.toLowerCase()) {
            case "consoleclear": {
                contentTextArea.setText("");
                break;
            }
            case "console": {
                System.out.println("Received console command");

                if (split.length == 0) {
                    break;
                }

                contentTextArea.appendText(split[1]);
                contentTextArea.appendText(System.lineSeparator());
                break;
            }
            case "time": {
                System.out.println("Received time command");

                if (split.length == 0) {
                    break;
                }

                try {
                    Long time = Long.parseLong(split[1]);
                    lastDateTime = time;
                    lastDateTimeReference = System.currentTimeMillis();
                } catch(NumberFormatException ex) {
                    ex.printStackTrace();
                }

                break;
            }
            case "battery": {
                System.out.println("Received battery command");

                if (split.length == 0) {
                    break;
                }

                try {
                    Float battery = Float.parseFloat(split[1]);

                    Platform.runLater(() -> {
                        contentBatteryLabel.setText(String.format("%d%%", (int) (battery * 100)));
                    });
                } catch(NumberFormatException ex) {
                    ex.printStackTrace();
                }

                break;
            }
        }
    }

    /*
        SELECTION
     */

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private void rootClick(MouseEvent event) {
        this.sidebarRoot.pseudoClassStateChanged(SELECTED, true);

        this.mainController.setSelectedDevice(this);
    }

    @Override
    public void unselect() {
        this.sidebarRoot.pseudoClassStateChanged(SELECTED, false);
    }

    /*
        STATUS
     */

    private void statusUpdateUI() {
        this.sidebarDeviceStatusCircle.setFill(this.status.color);
    }

    /*
        SYNC TIME
     */

    private void contentSyncTimeClicked(MouseEvent mouseEvent) {
        try {
            this.bluetoothClient.sendData(String.format("time:%d", System.currentTimeMillis()));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
        WAKE
     */

    private void contentWakeButtonClicked(MouseEvent mouseEvent) {
        try {
            String text = this.contentWakeTextInput.getText();

            TemporalAccessor accessor = Constants.TIME_FORMATTER.parse(text);
            LocalTime localDateTime = LocalTime.from(accessor);

            long millis = localDateTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.bluetoothClient.sendData(String.format("wake:%d", millis));
        } catch(IOException | DateTimeParseException ex) {
            ex.printStackTrace();
        }
    }
}
