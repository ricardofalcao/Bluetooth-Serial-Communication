package pt.ricardofalcao.lsts.bluetooth;

import java.io.IOException;

public interface BluetoothDevice {

    void readData();

    void disconnect() throws IOException;

}
