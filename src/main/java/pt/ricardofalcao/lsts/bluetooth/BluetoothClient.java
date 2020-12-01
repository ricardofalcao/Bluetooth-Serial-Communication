package pt.ricardofalcao.lsts.bluetooth;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.bluetooth.UUID;

public interface BluetoothClient {

    boolean isRunning();

    /*

     */

    void setConnectCallback(Runnable consumer);

    void setDataCallback(Consumer<String> consumer);

    void setDisconnectCallback(Runnable consumer);

    /*

     */

    void connect(String connectionURL) throws IOException;

    void disconnect() throws IOException;
}
