package pt.ricardofalcao.lsts.bluetooth.spp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pt.ricardofalcao.lsts.bluetooth.BluetoothClient;

@RequiredArgsConstructor
public class BluetoothSPPClient implements BluetoothClient {

    /*

     */

    private Thread acceptThread;

    private StreamConnection connection;

    @Getter
    private boolean running = false;

    /*

     */

    private BufferedReader reader;

    private BufferedWriter writer;

    /*

     */

    @Setter
    private Runnable connectCallback;

    @Setter
    private Consumer<String> dataCallback;

    @Setter
    private Runnable disconnectCallback;

    /*

     */

    public void connect(String connectionURL) throws IOException {
        LocalDevice local = LocalDevice.getLocalDevice();
        System.out.println(String.format("Local Device name: %s", local.getFriendlyName()));
        System.out.println(String.format("Local Bluetooth Address: %s", local.getBluetoothAddress()));

        connection = (StreamConnection) Connector.open(connectionURL);

        this.reader = new BufferedReader(new InputStreamReader(connection.openInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(connection.openOutputStream()));

        this.running = true;

        if (this.connectCallback != null) {
            this.connectCallback.run();
        }

        this.acceptThread = new Thread(this::readData);
        this.acceptThread.start();
    }

    private void readData() {
        try {
            String data;
            while((data = reader.readLine()) != null) {
                if (this.dataCallback != null) {
                    this.dataCallback.accept(data);
                }

                System.out.println(String.format("Received data: %s", data));

                this.writer.write(data);
                this.writer.flush();
            }

            this.disconnect();
        } catch (InterruptedIOException ignored) {

        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void disconnect() throws IOException {
        LocalDevice device = LocalDevice.getLocalDevice();
        System.out.println(String.format("Device disconnected: %s [%s]", device.getFriendlyName(), device.getBluetoothAddress()));

        this.acceptThread.interrupt();

        connection.close();
        reader.close();
        writer.close();

        if (this.disconnectCallback != null) {
            this.disconnectCallback.run();
        }
    }

}
