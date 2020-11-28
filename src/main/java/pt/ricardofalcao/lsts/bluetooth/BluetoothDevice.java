package pt.ricardofalcao.lsts.bluetooth;

import java.io.IOException;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

public interface BluetoothDevice {

    RemoteDevice getDevice();

    StreamConnection getConnection();

    void readData();

    void disconnect() throws IOException;

}
