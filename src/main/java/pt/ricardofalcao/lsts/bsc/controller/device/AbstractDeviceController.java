package pt.ricardofalcao.lsts.bsc.controller.device;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.scene.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.bluetooth.BluetoothClient;
import pt.ricardofalcao.lsts.bsc.controller.main.MainController;

@RequiredArgsConstructor
public abstract class AbstractDeviceController {

    protected final MainController mainController;

    @Getter
    protected final String name;

    @Getter
    protected final String address;

    /*

     */

    protected BluetoothClient bluetoothClient;

    /*

     */



    /*
        ROOT
     */

    public abstract Node getSidebarItem();

    public abstract Node getContentItem();

    /*
        SELECTION
     */

    public abstract void unselect();

    /*
        BLUETOOTH
     */

    public void attachBluetoothClient(BluetoothClient client, String connectionUrl) {
        this.bluetoothClient = client;

        CompletableFuture.runAsync(() ->  {
            try {
                bluetoothClient.connect(connectionUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /*

     */

    public void detach() {
        if (this.bluetoothClient == null) {
            return;
        }

        try {
            this.bluetoothClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
