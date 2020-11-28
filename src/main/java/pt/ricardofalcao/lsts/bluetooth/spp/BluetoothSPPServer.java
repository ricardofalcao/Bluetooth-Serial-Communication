package pt.ricardofalcao.lsts.bluetooth.spp;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pt.ricardofalcao.lsts.bluetooth.BluetoothDevice;
import pt.ricardofalcao.lsts.bluetooth.BluetoothServer;

@RequiredArgsConstructor
public class BluetoothSPPServer implements BluetoothServer {

    @Getter
    private final UUID serverId;

    @Getter
    private final String serverName;

    /*

     */

    private Thread acceptThread;

    private StreamConnectionNotifier connectionNotifier;

    private Map<Long, BluetoothSPPDevice> deviceList = new HashMap<>();

    @Getter
    private boolean running = false;

    /*

     */

    @Setter
    private Consumer<BluetoothDevice> connectCallback;

    @Setter
    private BiConsumer<BluetoothDevice, String> dataCallback;

    @Setter
    private Consumer<BluetoothDevice> disconnectCallback;

    /*

     */

    @Override
    public BluetoothDevice getConnectedDevice(String address) {
        return this.deviceList.get(RemoteDeviceHelper.getAddress(address));
    }

    public void start() throws IOException {
        if (this.running) {
            return;
        }

        LocalDevice local = LocalDevice.getLocalDevice();
        System.out.println(String.format("Device name: %s", local.getFriendlyName()));
        System.out.println(String.format("Bluetooth Address: %s", local.getBluetoothAddress()));

        boolean result = local.setDiscoverable(DiscoveryAgent.GIAC);
        System.out.println(String.format("Discoverability set: %s", result));

        String connectionString = String.format("btspp://localhost:%s;name=%s", serverId.toString(), serverName);
        this.connectionNotifier = (StreamConnectionNotifier) Connector.open( connectionString );
        this.running = true;

        this.acceptThread = new Thread(this::acceptClients);
        this.acceptThread.start();
    }

    private void acceptClients() {
        while(running) {
            try {
                StreamConnection connection = this.connectionNotifier.acceptAndOpen();

                RemoteDevice device = RemoteDevice.getRemoteDevice(connection);
                System.out.println(String.format("Device connected: %s [%s]", device.getFriendlyName(true), device.getBluetoothAddress()));

                BluetoothSPPDevice threadedDevice = new BluetoothSPPDevice(this, device, connection);
                deviceList.put(RemoteDeviceHelper.getAddress(device.getBluetoothAddress()), threadedDevice);

                if(connectCallback != null) {
                    connectCallback.accept(threadedDevice);
                }

                new Thread(threadedDevice::readData).start();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws IOException {
        if (!this.running) {
            return;
        }

        this.running = false;
        this.connectionNotifier.close();
    }

    protected void receivedData(BluetoothSPPDevice threadedDevice, String data) {
        if(dataCallback != null) {
            dataCallback.accept(threadedDevice, data);
        }
    }

    protected void removeDevice(BluetoothSPPDevice threadedDevice) {
        if(disconnectCallback != null) {
            disconnectCallback.accept(threadedDevice);
        }

        deviceList.remove(RemoteDeviceHelper.getAddress(threadedDevice.getDevice().getBluetoothAddress()));
    }

}
