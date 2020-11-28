package pt.ricardofalcao.lsts.bluetooth;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.bluetooth.UUID;

public interface BluetoothServer {

    UUID getServerId();

    String getServerName();

    /*

     */

    boolean isRunning();

    BluetoothDevice getConnectedDevice(String address);

    /*

     */

    void setConnectCallback(Consumer<BluetoothDevice> consumer);

    void setDataCallback(BiConsumer<BluetoothDevice, String> consumer);

    void setDisconnectCallback(Consumer<BluetoothDevice> consumer);

    /*

     */

    void start() throws IOException;

    void stop() throws IOException;
}
