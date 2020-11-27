package pt.ricardofalcao.lsts.bluetooth.spp;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;
import lombok.Getter;
import pt.ricardofalcao.lsts.bluetooth.BluetoothDevice;

public class BluetoothSPPDevice implements BluetoothDevice {

    private final BluetoothSPPServer server;

    private final RemoteDevice device;

    @Getter
    private final StreamConnection connection;

    private final String friendlyName;

    /*

     */

    private BufferedReader reader;

    private BufferedWriter writer;

    /*

     */

    public BluetoothSPPDevice(BluetoothSPPServer server, RemoteDevice device, StreamConnection connection) throws IOException {
        this.server = server;
        this.device = device;
        this.connection = connection;
        this.friendlyName = device.getFriendlyName(false);

        this.reader = new BufferedReader(new InputStreamReader(connection.openInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(connection.openOutputStream()));
    }

    @Override
    public void readData() {
        try {
            String data;
            while((data = reader.readLine()) != null) {
                System.out.println(String.format("[%s] Received data: %s", this.friendlyName, data));

                this.writer.write(data);
                this.writer.flush();
            }

            this.disconnect();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void disconnect() throws IOException {
        System.out.println(String.format("Device disconnected: %s [%s]", device.getFriendlyName(false), device.getBluetoothAddress()));

        connection.close();
        reader.close();
        writer.close();

        server.removeDevice(RemoteDeviceHelper.getAddress(device.getBluetoothAddress()));
    }
}
