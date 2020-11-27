package pt.ricardofalcao.lsts.bluetooth;

import java.io.IOException;
import javax.bluetooth.UUID;

public interface BluetoothServer {

    UUID getServerId();

    String getServerName();

    boolean isRunning();

    void start() throws IOException;

    void stop() throws IOException;
}
