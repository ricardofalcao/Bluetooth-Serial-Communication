package pt.ricardofalcao.lsts;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.bluetooth.UUID;
import pt.ricardofalcao.lsts.bluetooth.BluetoothServer;
import pt.ricardofalcao.lsts.bluetooth.spp.BluetoothSPPServer;

public class ServerHandler {

    private BluetoothServer server;

    /*

     */

    public ServerHandler() {
        this.server = new BluetoothSPPServer(new UUID(1234567890L), "BSComm");
    }

    public boolean isRunning() {
        return this.server.isRunning();
    }

    public CompletableFuture<Boolean> startServerAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.server.start();

                return true;
            } catch(IOException ex) {
                ex.printStackTrace();

                return false;
            }
        });
    }

    public CompletableFuture<Boolean> stopServerAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.server.stop();

                return true;
            } catch(IOException ex) {
                ex.printStackTrace();

                return false;
            }
        });
    }

}
